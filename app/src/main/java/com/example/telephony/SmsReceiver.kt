package com.example.telephony

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.data.local.SalimDatabase
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MessageDeliveryStatus
import com.example.data.local.entity.MessageEntity
import com.example.data.local.preferences.SalimPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Telephony.Sms.Intents.SMS_DELIVER_ACTION && action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            return
        }

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val senderAddress = messages[0].originatingAddress ?: "Unknown"
        val timestamp = messages[0].timestampMillis.takeIf { it > 0 } ?: System.currentTimeMillis()
        val fullBody = messages.joinToString("") { it.messageBody ?: "" }

        if (fullBody.isBlank()) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                handleIncomingMessage(context, senderAddress, fullBody, timestamp)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        suspend fun handleIncomingMessage(
            context: Context,
            senderAddress: String,
            body: String,
            timestamp: Long
        ) {
            val db = SalimDatabase.getInstance(context)
            val prefs = SalimPreferences(context)
            val userPrefs = prefs.preferencesFlow.first()

            val contactInfo = ContactResolver.resolveContact(context, senderAddress)
            val normalizedAddress = contactInfo.normalizedNumber

            // Find or create conversation
            var conversation = db.conversationDao().getConversationByAddress(normalizedAddress)
            val conversationId: Long

            if (conversation == null) {
                val newConv = ConversationEntity(
                    recipientAddress = normalizedAddress,
                    recipientName = contactInfo.name,
                    lastMessageText = body,
                    lastMessageTimestamp = timestamp,
                    unreadCount = 1,
                    isArchived = false,
                    isSpam = false
                )
                conversationId = db.conversationDao().insertOrUpdate(newConv)
                conversation = db.conversationDao().getConversationById(conversationId)
            } else {
                conversationId = conversation.id
                val updatedName = contactInfo.name ?: conversation.recipientName
                val updated = conversation.copy(
                    recipientName = updatedName,
                    lastMessageText = body,
                    lastMessageTimestamp = timestamp,
                    unreadCount = conversation.unreadCount + 1,
                    isArchived = false // unarchive on new incoming message
                )
                db.conversationDao().update(updated)
            }

            // Insert message
            db.messageDao().insert(
                MessageEntity(
                    conversationId = conversationId,
                    senderAddress = senderAddress,
                    recipientAddress = "Me",
                    body = body,
                    timestamp = timestamp,
                    isIncoming = true,
                    status = MessageDeliveryStatus.RECEIVED,
                    isRead = false
                )
            )

            // Post notification if not muted
            if (conversation?.isMuted != true) {
                NotificationCoordinator.showIncomingMessageNotification(
                    context = context,
                    conversationId = conversationId,
                    senderAddress = senderAddress,
                    senderName = contactInfo.name ?: conversation?.recipientName,
                    messageText = body,
                    timestamp = timestamp,
                    privacy = userPrefs.notificationPrivacy
                )
            }
        }
    }
}

class SmsFallbackReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Only process if Salim is NOT the default SMS app to avoid duplicate processing with SmsReceiver
        if (!DefaultSmsRoleManager.isDefaultSmsApp(context)) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
            if (messages.isEmpty()) return

            val senderAddress = messages[0].originatingAddress ?: "Unknown"
            val timestamp = messages[0].timestampMillis.takeIf { it > 0 } ?: System.currentTimeMillis()
            val fullBody = messages.joinToString("") { it.messageBody ?: "" }

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    SmsReceiver.handleIncomingMessage(context, senderAddress, fullBody, timestamp)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
