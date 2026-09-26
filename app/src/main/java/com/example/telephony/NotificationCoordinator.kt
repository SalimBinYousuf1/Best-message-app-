package com.example.telephony

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import com.example.MainActivity
import com.example.R
import com.example.data.local.preferences.NotificationPrivacy

object NotificationCoordinator {

    const val CHANNEL_MESSAGES = "salim_messages_channel"
    const val CHANNEL_SCHEDULED = "salim_scheduled_channel"
    const val KEY_TEXT_REPLY = "key_text_reply"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val messagesChannel = NotificationChannel(
                CHANNEL_MESSAGES,
                "Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming SMS and MMS messages"
                enableVibration(true)
                setShowBadge(true)
            }

            val scheduledChannel = NotificationChannel(
                CHANNEL_SCHEDULED,
                "Scheduled Messages",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for scheduled SMS dispatches"
                setShowBadge(false)
            }

            notificationManager.createNotificationChannel(messagesChannel)
            notificationManager.createNotificationChannel(scheduledChannel)
        }
    }

    fun showIncomingMessageNotification(
        context: Context,
        conversationId: Long,
        senderAddress: String,
        senderName: String?,
        messageText: String,
        timestamp: Long,
        privacy: NotificationPrivacy = NotificationPrivacy.SHOW_ALL
    ) {
        val displayName = senderName ?: senderAddress
        val senderPerson = Person.Builder()
            .setName(displayName)
            .setKey(senderAddress)
            .build()

        // Content intent: open conversation in MainActivity
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("conversation_id", conversationId)
            putExtra("recipient_address", senderAddress)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            conversationId.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Mark as Read action
        val markReadIntent = Intent(context, SmsActionReceiver::class.java).apply {
            action = SmsActionReceiver.ACTION_MARK_READ
            putExtra(SmsActionReceiver.EXTRA_CONVERSATION_ID, conversationId)
        }
        val markReadPendingIntent = PendingIntent.getBroadcast(
            context,
            (conversationId * 10 + 1).toInt(),
            markReadIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Inline Reply action with RemoteInput
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel("Reply to $displayName...")
            .build()

        val replyIntent = Intent(context, SmsActionReceiver::class.java).apply {
            action = SmsActionReceiver.ACTION_INLINE_REPLY
            putExtra(SmsActionReceiver.EXTRA_CONVERSATION_ID, conversationId)
            putExtra(SmsActionReceiver.EXTRA_RECIPIENT_ADDRESS, senderAddress)
        }
        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            (conversationId * 10 + 2).toInt(),
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val replyAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_send,
            "Reply",
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()

        val markReadAction = NotificationCompat.Action.Builder(
            android.R.drawable.checkbox_on_background,
            "Mark as read",
            markReadPendingIntent
        ).build()

        val builder = NotificationCompat.Builder(context, CHANNEL_MESSAGES)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setColor(0xFF007AFF.toInt())
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)

        when (privacy) {
            NotificationPrivacy.HIDE_ALL -> {
                builder.setContentTitle("New message")
                builder.setContentText("You have a new message")
            }
            NotificationPrivacy.SENDER_ONLY -> {
                builder.setContentTitle(displayName)
                builder.setContentText("New message")
                builder.addAction(markReadAction)
            }
            NotificationPrivacy.SHOW_ALL -> {
                val messagingStyle = NotificationCompat.MessagingStyle(
                    Person.Builder().setName("Me").build()
                )
                    .addMessage(messageText, timestamp, senderPerson)
                builder.setStyle(messagingStyle)
                builder.addAction(replyAction)
                builder.addAction(markReadAction)
            }
        }

        try {
            NotificationManagerCompat.from(context).notify(conversationId.toInt(), builder.build())
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS not granted
        }
    }

    fun dismissNotification(context: Context, conversationId: Long) {
        NotificationManagerCompat.from(context).cancel(conversationId.toInt())
    }
}
