package com.example

import com.example.telephony.ContactResolver
import com.example.telephony.OtpDetector
import com.example.telephony.SmsTransport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `test phone number normalization`() {
        assertEquals("+15551234567", ContactResolver.normalizePhoneNumber("+1 (555) 123-4567"))
        assertEquals("5551234567", ContactResolver.normalizePhoneNumber("(555) 123-4567"))
        assertEquals("+447911123456", ContactResolver.normalizePhoneNumber(" +44 7911 123456 "))
        assertEquals("911", ContactResolver.normalizePhoneNumber("911"))
        assertEquals("GOOGLE", ContactResolver.normalizePhoneNumber("GOOGLE"))
        assertEquals("VM-HDFCBK", ContactResolver.normalizePhoneNumber("VM-HDFCBK"))
        assertEquals("AMAZON", ContactResolver.normalizePhoneNumber("AMAZON"))
    }

    @Test
    fun `test OTP detection in incoming messages`() {
        val otp1 = OtpDetector.detectOtp("Your Salim verification code is 482910. Do not share.")
        assertNotNull(otp1)
        assertEquals("482910", otp1?.code)

        val otp2 = OtpDetector.detectOtp("Use 123456 to verify your login.")
        assertNotNull(otp2)
        assertEquals("123456", otp2?.code)

        val otp3 = OtpDetector.detectOtp("Your one-time password OTP is: 9518")
        assertNotNull(otp3)
        assertEquals("9518", otp3?.code)

        val noOtp = OtpDetector.detectOtp("Hey Salim, let's meet tomorrow at 5pm!")
        assertNull(noOtp)
    }

    @Test
    fun `test SMS segments calculation for 7-bit GSM text`() {
        val shortText = "Hello from Salim messaging"
        val (segmentsShort, remainingShort) = SmsTransport.calculateSmsSegments(shortText)
        assertEquals(1, segmentsShort)
        assertEquals(160 - shortText.length, remainingShort)

        val longText = "A".repeat(170)
        val (segmentsLong, _) = SmsTransport.calculateSmsSegments(longText)
        assertEquals(2, segmentsLong)
    }
}
