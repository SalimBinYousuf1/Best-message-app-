package com.example.telephony

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.local.SalimDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = SalimDatabase.getInstance(context)
                    val pendingList = db.scheduledMessageDao().getPendingScheduledMessages().first()
                    val now = System.currentTimeMillis()
                    for (item in pendingList) {
                        if (item.scheduledTimestamp > now) {
                            ScheduledSmsReceiver.scheduleAlarm(context, item)
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
