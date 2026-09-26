package com.example.telephony

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.R
import com.example.data.local.SalimDatabase
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MessageDeliveryStatus
import com.example.data.local.entity.MessageEntity
import com.example.data.local.entity.ScheduledMessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScheduledSmsReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TRIGGER_SCHEDULED = "com.example.ACTION_TRIGGER_SCHEDULED"
        const val EXTRA_SCHEDULED_ID = "extra_scheduled_id"

        fun scheduleAlarm(context: Context, item: ScheduledMessageEntity) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, ScheduledSmsReceiver::class.java).apply {
                action = ACTION_TRIGGER_SCHEDULED
                putExtra(EXTRA_SCHEDULED_ID, item.id)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                item.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        item.scheduledTimestamp,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        item.scheduledTimestamp,
                        pendingIntent
                    )
                }
            } catch (_: SecurityException) {
                // If SCHEDULE_EXACT_ALARM is not granted, fallback to inexact
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    item.scheduledTimestamp,
                    pendingIntent
                )
            }
        }

        fun cancelAlarm(context: Context, scheduledId: Long) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, ScheduledSmsReceiver::class.java).apply {
                action = ACTION_TRIGGER_SCHEDULED
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                scheduledId.toInt(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val scheduledId = intent.getLongExtra(EXTRA_SCHEDULED_ID, -1L)
        if (scheduledId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = SalimDatabase.getInstance(context)
                val scheduledItem = db.scheduledMessageDao().getById(scheduledId)
                if (scheduledItem != null && !scheduledItem.isExecuted && !scheduledItem.isCancelled) {
                    // Send message
                    val transport = SmsTransport(context)

                    var conversation = db.conversationDao().getConversationById(scheduledItem.conversationId)
                    val convId = if (conversation == null) {
                        val newConv = ConversationEntity(
                            recipientAddress = scheduledItem.recipientAddress,
                            recipientName = scheduledItem.recipientName,
                            lastMessageText = scheduledItem.body,
                            lastMessageTimestamp = System.currentTimeMillis()
                        )
                        db.conversationDao().insertOrUpdate(newConv)
                    } else {
                        conversation.id
                    }

                    val messageId = db.messageDao().insert(
                        MessageEntity(
                            conversationId = convId,
                            senderAddress = "Me",
                            recipientAddress = scheduledItem.recipientAddress,
                            body = scheduledItem.body,
                            isIncoming = false,
                            status = MessageDeliveryStatus.PENDING,
                            isScheduled = true,
                            scheduledTime = scheduledItem.scheduledTimestamp
                        )
                    )
                    db.conversationDao().updateLastMessage(convId, scheduledItem.body, System.currentTimeMillis())

                    val result = transport.sendTextMessage(scheduledItem.recipientAddress, scheduledItem.body, messageId)
                    if (result is SmsSendResult.Success) {
                        db.scheduledMessageDao().markExecuted(scheduledId)
                        // Post confirmation notification
                        postScheduledSentNotification(context, scheduledItem)
                    } else if (result is SmsSendResult.Failure) {
                        db.messageDao().updateStatus(messageId, MessageDeliveryStatus.FAILED)
                        postScheduledFailedNotification(context, scheduledItem, result.reason)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun postScheduledSentNotification(context: Context, item: ScheduledMessageEntity) {
        val target = item.recipientName ?: item.recipientAddress
        val notif = NotificationCompat.Builder(context, NotificationCoordinator.CHANNEL_SCHEDULED)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Scheduled message sent")
            .setContentText("Dispatched to $target: \"${item.body.take(30)}\"")
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify((item.id + 10000).toInt(), notif)
        } catch (_: SecurityException) {}
    }

    private fun postScheduledFailedNotification(context: Context, item: ScheduledMessageEntity, reason: String) {
        val target = item.recipientName ?: item.recipientAddress
        val notif = NotificationCompat.Builder(context, NotificationCoordinator.CHANNEL_SCHEDULED)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Scheduled message failed")
            .setContentText("Failed to send to $target: $reason")
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(context).notify((item.id + 10000).toInt(), notif)
        } catch (_: SecurityException) {}
    }
}
