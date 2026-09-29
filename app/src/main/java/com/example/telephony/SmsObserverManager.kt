package com.example.telephony

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Telephony
import android.util.Log
import com.example.data.local.SalimDatabase
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MessageDeliveryStatus
import com.example.data.local.entity.MessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Real-Time Incremental Sync Engine.
 * Registers a system ContentObserver on Telephony.Sms.CONTENT_URI.
 * When any message arrives or is updated in Android's telephony provider,
 * captures it immediately in real-time, performing a targeted single-row Room upsert.
 */
object SmsObserverManager {
    private const val TAG = "SmsObserverManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var observer: ContentObserver? = null
    private var isRegistered = false

    fun startObserving(context: Context) {
        if (isRegistered) return
        val appContext = context.applicationContext

        observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                Log.d(TAG, "ContentObserver onChange fired for URI: $uri")
                syncLatestMessage(appContext)
            }
        }

        try {
            appContext.contentResolver.registerContentObserver(
                Telephony.Sms.CONTENT_URI,
                true,
                observer!!
            )
            isRegistered = true
            Log.d(TAG, "SmsObserverManager successfully registered on Telephony.Sms.CONTENT_URI")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register ContentObserver on Telephony.Sms.CONTENT_URI", e)
        }
    }

    fun stopObserving(context: Context) {
        if (!isRegistered || observer == null) return
        try {
            context.applicationContext.contentResolver.unregisterContentObserver(observer!!)
            isRegistered = false
            observer = null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unregister ContentObserver", e)
        }
    }

    /**
     * Query recent messages (LIMIT 20 ORDER BY DATE DESC) and upsert any new ones into Room.
     */
    fun syncLatestMessage(context: Context) {
        scope.launch {
            try {
                val projection = arrayOf(
                    Telephony.Sms._ID,
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE,
                    Telephony.Sms.TYPE,
                    Telephony.Sms.READ
                )

                context.contentResolver.query(
                    Telephony.Sms.CONTENT_URI,
                    projection,
                    null,
                    null,
                    "${Telephony.Sms.DATE} DESC LIMIT 20"
                )?.use { cursor ->
                    val idIdx = cursor.getColumnIndex(Telephony.Sms._ID)
                    val addressIdx = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
                    val bodyIdx = cursor.getColumnIndex(Telephony.Sms.BODY)
                    val dateIdx = cursor.getColumnIndex(Telephony.Sms.DATE)
                    val typeIdx = cursor.getColumnIndex(Telephony.Sms.TYPE)
                    val readIdx = cursor.getColumnIndex(Telephony.Sms.READ)

                    val db = SalimDatabase.getInstance(context)

                    while (cursor.moveToNext()) {
                        val telephonyId = if (idIdx >= 0) cursor.getLong(idIdx) else -1L
                        val rawAddress = if (addressIdx >= 0) cursor.getString(addressIdx) else null
                        val body = if (bodyIdx >= 0) cursor.getString(bodyIdx) ?: "" else ""
                        val timestamp = if (dateIdx >= 0) cursor.getLong(dateIdx) else System.currentTimeMillis()
                        val type = if (typeIdx >= 0) cursor.getInt(typeIdx) else Telephony.Sms.MESSAGE_TYPE_INBOX
                        val isRead = if (readIdx >= 0) cursor.getInt(readIdx) == 1 else true

                        if (rawAddress.isNullOrBlank() || telephonyId < 0) continue

                        // Deduplicate against existing telephonyMessageId
                        val existing = db.messageDao().getMessageByTelephonyId(telephonyId)
                        if (existing != null) {
                            continue
                        }

                        val isIncoming = type == Telephony.Sms.MESSAGE_TYPE_INBOX
                        val cleanAddress = ContactResolver.normalizePhoneNumber(rawAddress)

                        var conversation = db.conversationDao().getConversationByAddress(cleanAddress)
                        val conversationId: Long

                        if (conversation == null) {
                            val contact = ContactResolver.resolveContact(context, cleanAddress)
                            val newConv = ConversationEntity(
                                recipientAddress = cleanAddress,
                                recipientName = contact.name,
                                lastMessageText = body,
                                lastMessageTimestamp = timestamp,
                                unreadCount = if (!isRead && isIncoming) 1 else 0,
                                isArchived = false,
                                isSpam = false
                            )
                            conversationId = db.conversationDao().insertOrUpdate(newConv)
                        } else {
                            conversationId = conversation.id
                            val unreadDiff = if (!isRead && isIncoming) 1 else 0
                            val updated = conversation.copy(
                                lastMessageText = if (timestamp >= conversation.lastMessageTimestamp) body else conversation.lastMessageText,
                                lastMessageTimestamp = maxOf(timestamp, conversation.lastMessageTimestamp),
                                unreadCount = (conversation.unreadCount + unreadDiff).coerceAtLeast(0),
                                isArchived = false
                            )
                            db.conversationDao().update(updated)
                        }

                        val msg = MessageEntity(
                            conversationId = conversationId,
                            telephonyMessageId = telephonyId,
                            senderAddress = if (isIncoming) rawAddress else "Me",
                            recipientAddress = if (isIncoming) "Me" else rawAddress,
                            body = body,
                            timestamp = timestamp,
                            isIncoming = isIncoming,
                            status = if (isIncoming) MessageDeliveryStatus.RECEIVED else MessageDeliveryStatus.SENT,
                            isRead = isRead
                        )
                        db.messageDao().insert(msg)
                        Log.d(TAG, "SmsObserverManager successfully upserted message id=$telephonyId for $cleanAddress")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in SmsObserverManager syncLatestMessage", e)
            }
        }
    }
}
