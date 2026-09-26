package com.example.data.repository

import android.content.Context
import com.example.data.local.dao.ScheduledMessageDao
import com.example.data.local.entity.ScheduledMessageEntity
import com.example.telephony.ScheduledSmsReceiver
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    fun getPendingScheduledMessages(): Flow<List<ScheduledMessageEntity>>
    suspend fun scheduleMessage(
        conversationId: Long,
        recipientAddress: String,
        recipientName: String?,
        body: String,
        scheduledTimestamp: Long
    ): Long
    suspend fun cancelScheduledMessage(id: Long)
    suspend fun sendScheduledMessageNow(id: Long)
}

class ScheduleRepositoryImpl(
    private val context: Context,
    private val scheduledMessageDao: ScheduledMessageDao,
    private val messagingRepository: MessagingRepository
) : ScheduleRepository {

    override fun getPendingScheduledMessages(): Flow<List<ScheduledMessageEntity>> =
        scheduledMessageDao.getPendingScheduledMessages()

    override suspend fun scheduleMessage(
        conversationId: Long,
        recipientAddress: String,
        recipientName: String?,
        body: String,
        scheduledTimestamp: Long
    ): Long {
        val entity = ScheduledMessageEntity(
            conversationId = conversationId,
            recipientAddress = recipientAddress,
            recipientName = recipientName,
            body = body,
            scheduledTimestamp = scheduledTimestamp
        )
        val id = scheduledMessageDao.insert(entity)
        val saved = entity.copy(id = id)
        ScheduledSmsReceiver.scheduleAlarm(context, saved)
        return id
    }

    override suspend fun cancelScheduledMessage(id: Long) {
        scheduledMessageDao.markCancelled(id)
        ScheduledSmsReceiver.cancelAlarm(context, id)
    }

    override suspend fun sendScheduledMessageNow(id: Long) {
        val item = scheduledMessageDao.getById(id) ?: return
        ScheduledSmsReceiver.cancelAlarm(context, id)
        scheduledMessageDao.markExecuted(id)
        messagingRepository.sendMessage(
            conversationId = item.conversationId,
            recipientAddress = item.recipientAddress,
            body = item.body
        )
    }
}
