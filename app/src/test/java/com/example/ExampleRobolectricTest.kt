package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SalimDatabase
import com.example.data.local.entity.ConversationEntity
import com.example.data.local.entity.MessageDeliveryStatus
import com.example.data.local.entity.MessageEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Salim", appName)
    }

    @Test
    fun `test database conversation and message insertion`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = SalimDatabase.getInstance(context)

        val convId = db.conversationDao().insertOrUpdate(
            ConversationEntity(
                recipientAddress = "+15550001111",
                recipientName = "Alice",
                lastMessageText = "Hello Salim!",
                lastMessageTimestamp = System.currentTimeMillis()
            )
        )

        val messageId = db.messageDao().insert(
            MessageEntity(
                conversationId = convId,
                senderAddress = "Me",
                recipientAddress = "+15550001111",
                body = "Hello Salim!",
                status = MessageDeliveryStatus.SENT,
                isIncoming = false
            )
        )

        val savedMsg = db.messageDao().getMessageById(messageId)
        assertNotNull(savedMsg)
        assertEquals("Hello Salim!", savedMsg?.body)
        assertEquals(MessageDeliveryStatus.SENT, savedMsg?.status)
    }
}
