package com.example.telephony

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

data class OtpDetectionResult(
    val code: String,
    val serviceName: String? = null,
    val rawText: String
)

object OtpDetector {

    // Regex matching 4 to 8 digit numbers in text with security keywords
    private val OTP_PATTERNS = listOf(
        Regex("""(?i)(?:code|otp|verification|password|pin|security code|passcode|token)[^0-9\r\n]{0,20}?([0-9]{4,8})\b"""),
        Regex("""(?i)\b([0-9]{4,8})\b[^0-9\r\n]{0,20}?(?:is your|is the|to verify|verification)"""),
        Regex("""(?i)(?:use|enter)\s+([0-9]{4,8})\b[^0-9\r\n]{0,20}?(?:to verify|as your)"""),
        Regex("""\b[A-Za-z]-([0-9]{5,8})\b""")
    )

    fun detectOtp(text: String): OtpDetectionResult? {
        if (text.isBlank()) return null
        
        for (pattern in OTP_PATTERNS) {
            val match = pattern.find(text)
            if (match != null && match.groupValues.size > 1) {
                val code = match.groupValues[1]
                if (code.length in 4..8 && code.all { it.isDigit() }) {
                    return OtpDetectionResult(
                        code = code,
                        rawText = text
                    )
                }
            }
        }
        return null
    }

    fun copyToClipboard(context: Context, code: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText("Security Code", code)
        clipboard?.setPrimaryClip(clip)
        Toast.makeText(context, "Code $code copied", Toast.LENGTH_SHORT).show()
    }
}
