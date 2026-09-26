package com.example.telephony

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log

class MmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.WAP_PUSH_DELIVER_ACTION) {
            val contentType = intent.type
            if (contentType == "application/vnd.wap.mms-message") {
                Log.d("SalimMms", "Received MMS WAP Push notification")
                // On default SMS role, MMS PDU is delivered here
            }
        }
    }
}
