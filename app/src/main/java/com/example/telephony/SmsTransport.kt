package com.example.telephony

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class SmsSendResult {
    data class Success(val partsCount: Int) : SmsSendResult()
    data class Failure(val reason: String) : SmsSendResult()
}

interface MessagingTransport {
    suspend fun sendTextMessage(
        recipient: String,
        text: String,
        localMessageId: Long
    ): SmsSendResult
}

class SmsTransport(private val context: Context) : MessagingTransport {

    @Suppress("DEPRECATION")
    private val smsManager: SmsManager?
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            SmsManager.getDefault()
        }

    override suspend fun sendTextMessage(
        recipient: String,
        text: String,
        localMessageId: Long
    ): SmsSendResult = withContext(Dispatchers.IO) {
        val manager = smsManager ?: return@withContext SmsSendResult.Failure("SmsManager unavailable on this device")
        val cleanRecipient = ContactResolver.normalizePhoneNumber(recipient)
        if (cleanRecipient.isEmpty()) {
            return@withContext SmsSendResult.Failure("Invalid recipient phone number")
        }
        if (text.isBlank()) {
            return@withContext SmsSendResult.Failure("Message cannot be empty")
        }

        try {
            val parts = manager.divideMessage(text)
            val sentIntents = ArrayList<PendingIntent>()
            val deliveryIntents = ArrayList<PendingIntent>()

            for (i in parts.indices) {
                val sentIntent = Intent(context, SmsActionReceiver::class.java).apply {
                    action = SmsActionReceiver.ACTION_SMS_SENT
                    putExtra(SmsActionReceiver.EXTRA_MESSAGE_ID, localMessageId)
                    putExtra(SmsActionReceiver.EXTRA_PART_INDEX, i)
                    putExtra(SmsActionReceiver.EXTRA_TOTAL_PARTS, parts.size)
                }
                val sentPI = PendingIntent.getBroadcast(
                    context,
                    (localMessageId * 100 + i).toInt(),
                    sentIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                sentIntents.add(sentPI)

                val deliveryIntent = Intent(context, SmsActionReceiver::class.java).apply {
                    action = SmsActionReceiver.ACTION_SMS_DELIVERED
                    putExtra(SmsActionReceiver.EXTRA_MESSAGE_ID, localMessageId)
                }
                val deliveryPI = PendingIntent.getBroadcast(
                    context,
                    (localMessageId * 100 + i + 50).toInt(),
                    deliveryIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                deliveryIntents.add(deliveryPI)
            }

            if (parts.size > 1) {
                manager.sendMultipartTextMessage(
                    cleanRecipient,
                    null,
                    parts,
                    sentIntents,
                    deliveryIntents
                )
            } else {
                manager.sendTextMessage(
                    cleanRecipient,
                    null,
                    text,
                    sentIntents.firstOrNull(),
                    deliveryIntents.firstOrNull()
                )
            }

            return@withContext SmsSendResult.Success(parts.size)
        } catch (e: SecurityException) {
            return@withContext SmsSendResult.Failure("SMS permission not granted. Please grant SMS permission in settings.")
        } catch (e: Exception) {
            return@withContext SmsSendResult.Failure(e.localizedMessage ?: "Failed to dispatch SMS")
        }
    }

    /**
     * Calculates segments count and remaining characters for standard GSM / 7-bit vs Unicode encoding.
     */
    companion object {
        fun calculateSmsSegments(text: String): Pair<Int, Int> {
            val is7Bit = text.all { it.code < 128 }
            val singleLimit = if (is7Bit) 160 else 70
            val multiLimit = if (is7Bit) 153 else 67

            val length = text.length
            return if (length <= singleLimit) {
                Pair(1, singleLimit - length)
            } else {
                val segments = (length + multiLimit - 1) / multiLimit
                val remainingInLast = (segments * multiLimit) - length
                Pair(segments, remainingInLast)
            }
        }
    }
}
