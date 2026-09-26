package com.example.telephony

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import androidx.core.app.RemoteInput
import com.example.data.local.SalimDatabase
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MessageDeliveryStatus
import com.example.data.local.entity.MessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MARK_READ = "com.example.ACTION_MARK_READ"
        const val ACTION_INLINE_REPLY = "com.example.ACTION_INLINE_REPLY"
        const val ACTION_SMS_SENT = "com.example.ACTION_SMS_SENT"
        const val ACTION_SMS_DELIVERED = "com.example.ACTION_SMS_DELIVERED"

        const val EXTRA_CONVERSATION_ID = "extra_conversation_id"
        const val EXTRA_MESSAGE_ID = "extra_message_id"
        const val EXTRA_RECIPIENT_ADDRESS = "extra_recipient_address"
        const val EXTRA_PART_INDEX = "extra_part_index"
        const val EXTRA_TOTAL_PARTS = "extra_total_parts"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val db = SalimDatabase.getInstance(context)

        when (action) {
            ACTION_MARK_READ -> {
                val conversationId = intent.getLongExtra(EXTRA_CONVERSATION_ID, -1L)
                if (conversationId != -1L) {
                    NotificationCoordinator.dismissNotification(context, conversationId)
                    CoroutineScope(Dispatchers.IO).launch {
                        db.messageDao().markAllAsReadForConversation(conversationId)
                        db.conversationDao().updateUnreadCount(conversationId, 0)
                    }
                }
            }

            ACTION_INLINE_REPLY -> {
                val conversationId = intent.getLongExtra(EXTRA_CONVERSATION_ID, -1L)
                val recipient = intent.getStringExtra(EXTRA_RECIPIENT_ADDRESS) ?: return
                val bundle = RemoteInput.getResultsFromIntent(intent)
                val replyText = bundle?.getCharSequence(NotificationCoordinator.KEY_TEXT_REPLY)?.toString()

                if (!replyText.isNullOrBlank()) {
                    NotificationCoordinator.dismissNotification(context, conversationId)
                    CoroutineScope(Dispatchers.IO).launch {
                        var targetConvId = conversationId
                        if (targetConvId == -1L) {
                            val existing = db.conversationDao().getConversationByAddress(recipient)
                            targetConvId = existing?.id ?: db.conversationDao().insertOrUpdate(
                                ConversationEntity(
                                    recipientAddress = recipient,
                                    lastMessageText = replyText,
                                    lastMessageTimestamp = System.currentTimeMillis()
                                )
                            )
                        }

                        val localMsgId = db.messageDao().insert(
                            MessageEntity(
                                conversationId = targetConvId,
                                senderAddress = "Me",
                                recipientAddress = recipient,
                                body = replyText,
                                isIncoming = false,
                                status = MessageDeliveryStatus.PENDING
                            )
                        )
                        db.conversationDao().updateLastMessage(targetConvId, replyText, System.currentTimeMillis())

                        val transport = SmsTransport(context)
                        val sendResult = transport.sendTextMessage(recipient, replyText, localMsgId)
                        if (sendResult is SmsSendResult.Failure) {
                            db.messageDao().updateStatus(localMsgId, MessageDeliveryStatus.FAILED)
                        }
                    }
                }
            }

            ACTION_SMS_SENT -> {
                val messageId = intent.getLongExtra(EXTRA_MESSAGE_ID, -1L)
                if (messageId != -1L) {
                    val isSuccess = resultCode == Activity.RESULT_OK
                    CoroutineScope(Dispatchers.IO).launch {
                        if (isSuccess) {
                            db.messageDao().updateStatus(messageId, MessageDeliveryStatus.SENT)
                        } else {
                            db.messageDao().updateStatus(messageId, MessageDeliveryStatus.FAILED)
                        }
                    }
                }
            }

            ACTION_SMS_DELIVERED -> {
                val messageId = intent.getLongExtra(EXTRA_MESSAGE_ID, -1L)
                if (messageId != -1L) {
                    val isSuccess = resultCode == Activity.RESULT_OK
                    if (isSuccess) {
                        CoroutineScope(Dispatchers.IO).launch {
                            db.messageDao().updateStatus(messageId, MessageDeliveryStatus.DELIVERED)
                        }
                    }
                }
            }
        }
    }
}
