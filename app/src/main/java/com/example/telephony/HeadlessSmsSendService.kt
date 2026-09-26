package com.example.telephony

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.telephony.TelephonyManager
import com.example.data.local.SalimDatabase
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MessageDeliveryStatus
import com.example.data.local.entity.MessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HeadlessSmsSendService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == TelephonyManager.ACTION_RESPOND_VIA_MESSAGE) {
            val uri = intent.data ?: return START_NOT_STICKY
            val rawRecipient = uri.schemeSpecificPart
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)

            if (!rawRecipient.isNullOrBlank() && !text.isNullOrBlank()) {
                val cleanRecipient = ContactResolver.normalizePhoneNumber(rawRecipient)
                val db = SalimDatabase.getInstance(applicationContext)

                CoroutineScope(Dispatchers.IO).launch {
                    val existing = db.conversationDao().getConversationByAddress(cleanRecipient)
                    val convId = existing?.id ?: db.conversationDao().insertOrUpdate(
                        ConversationEntity(
                            recipientAddress = cleanRecipient,
                            lastMessageText = text,
                            lastMessageTimestamp = System.currentTimeMillis()
                        )
                    )

                    val msgId = db.messageDao().insert(
                        MessageEntity(
                            conversationId = convId,
                            senderAddress = "Me",
                            recipientAddress = cleanRecipient,
                            body = text,
                            isIncoming = false,
                            status = MessageDeliveryStatus.PENDING
                        )
                    )
                    db.conversationDao().updateLastMessage(convId, text, System.currentTimeMillis())

                    val transport = SmsTransport(applicationContext)
                    val res = transport.sendTextMessage(cleanRecipient, text, msgId)
                    if (res is SmsSendResult.Failure) {
                        db.messageDao().updateStatus(msgId, MessageDeliveryStatus.FAILED)
                    }
                    stopSelf(startId)
                }
                return START_STICKY
            }
        }
        stopSelf(startId)
        return START_NOT_STICKY
    }
}
