package com.example.telephony

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import com.example.data.local.SalimDatabase
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MessageDeliveryStatus
import com.example.data.local.entity.MessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SmsSyncManager {

    /**
     * Imports messages from Android's Telephony.Sms provider into Salim's Room database.
     * Prevents duplicates by mapping telephony message IDs or message timestamps and addresses.
     */
    suspend fun syncDeviceMessages(context: Context, maxLimit: Int = 300): Int = withContext(Dispatchers.IO) {
        val db = SalimDatabase.getInstance(context)
        var importedCount = 0

        try {
            val contentUri: Uri = Telephony.Sms.CONTENT_URI
            val projection = arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.TYPE,
                Telephony.Sms.READ
            )

            context.contentResolver.query(
                contentUri,
                projection,
                null,
                null,
                "${Telephony.Sms.DATE} DESC LIMIT $maxLimit"
            )?.use { cursor ->
                val idIdx = cursor.getColumnIndex(Telephony.Sms._ID)
                val addressIdx = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = cursor.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx = cursor.getColumnIndex(Telephony.Sms.DATE)
                val typeIdx = cursor.getColumnIndex(Telephony.Sms.TYPE)
                val readIdx = cursor.getColumnIndex(Telephony.Sms.READ)

                while (cursor.moveToNext()) {
                    val telephonyId = if (idIdx >= 0) cursor.getLong(idIdx) else -1L
                    val rawAddress = if (addressIdx >= 0) cursor.getString(addressIdx) else null ?: continue
                    val body = if (bodyIdx >= 0) cursor.getString(bodyIdx) else null ?: ""
                    val date = if (dateIdx >= 0) cursor.getLong(dateIdx) else System.currentTimeMillis()
                    val type = if (typeIdx >= 0) cursor.getInt(typeIdx) else Telephony.Sms.MESSAGE_TYPE_INBOX
                    val isRead = if (readIdx >= 0) cursor.getInt(readIdx) == 1 else true

                    // Check if already in DB
                    val existing = db.messageDao().getMessageByTelephonyId(telephonyId)
                    if (existing != null) continue

                    val isIncoming = type == Telephony.Sms.MESSAGE_TYPE_INBOX
                    val cleanAddress = ContactResolver.normalizePhoneNumber(rawAddress)
                    if (cleanAddress.isBlank()) continue

                    // Resolve or get conversation
                    var conv = db.conversationDao().getConversationByAddress(cleanAddress)
                    val convId: Long
                    if (conv == null) {
                        val contact = ContactResolver.resolveContact(context, cleanAddress)
                        val newConv = ConversationEntity(
                            recipientAddress = cleanAddress,
                            recipientName = contact.name,
                            lastMessageText = body,
                            lastMessageTimestamp = date,
                            unreadCount = if (!isRead && isIncoming) 1 else 0
                        )
                        convId = db.conversationDao().insertOrUpdate(newConv)
                    } else {
                        convId = conv.id
                        if (date > conv.lastMessageTimestamp) {
                            db.conversationDao().updateLastMessage(convId, body, date)
                        }
                    }

                    val msg = MessageEntity(
                        conversationId = convId,
                        telephonyMessageId = telephonyId,
                        senderAddress = if (isIncoming) rawAddress else "Me",
                        recipientAddress = if (isIncoming) "Me" else rawAddress,
                        body = body,
                        timestamp = date,
                        isIncoming = isIncoming,
                        status = if (isIncoming) MessageDeliveryStatus.RECEIVED else MessageDeliveryStatus.SENT,
                        isRead = isRead
                    )
                    db.messageDao().insert(msg)
                    importedCount++
                }
            }
        } catch (_: SecurityException) {
            // READ_SMS permission not granted
        } catch (_: Exception) {
            // Ignore other unexpected query exceptions
        }

        return@withContext importedCount
    }
}
