package com.example.data.repository

import com.example.data.local.dao.ConversationDao
import com.example.data.local.dao.MessageDao
import com.example.data.local.entity.AttachmentType
import com.example.data.local.entity.MessageDeliveryStatus
import com.example.data.local.entity.MessageEntity
import com.example.telephony.MessagingTransport
import com.example.telephony.SmsSendResult
import kotlinx.coroutines.flow.Flow

interface MessagingRepository {
    fun getMessagesForConversation(conversationId: Long): Flow<List<MessageEntity>>
    fun getStarredMessages(): Flow<List<MessageEntity>>
    suspend fun sendMessage(
        conversationId: Long,
        recipientAddress: String,
        body: String,
        attachmentUri: String? = null,
        attachmentType: AttachmentType = AttachmentType.NONE,
        attachmentName: String? = null
    ): Result<Long>
    suspend fun retryMessage(messageId: Long): Result<Unit>
    suspend fun markConversationAsRead(conversationId: Long)
    suspend fun setStarred(messageId: Long, isStarred: Boolean)
    suspend fun updateReaction(messageId: Long, reaction: String?)
    suspend fun deleteMessage(messageId: Long)
    fun searchMessages(query: String): Flow<List<MessageEntity>>
}

class MessagingRepositoryImpl(
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val smsTransport: MessagingTransport
) : MessagingRepository {

    override fun getMessagesForConversation(conversationId: Long): Flow<List<MessageEntity>> =
        messageDao.getMessagesForConversation(conversationId)

    override fun getStarredMessages(): Flow<List<MessageEntity>> =
        messageDao.getStarredMessages()

    override suspend fun sendMessage(
        conversationId: Long,
        recipientAddress: String,
        body: String,
        attachmentUri: String?,
        attachmentType: AttachmentType,
        attachmentName: String?
    ): Result<Long> {
        val now = System.currentTimeMillis()
        val message = MessageEntity(
            conversationId = conversationId,
            senderAddress = "Me",
            recipientAddress = recipientAddress,
            body = body,
            timestamp = now,
            isIncoming = false,
            status = MessageDeliveryStatus.PENDING,
            isRead = true,
            attachmentUri = attachmentUri,
            attachmentType = attachmentType,
            attachmentName = attachmentName
        )

        val localId = messageDao.insert(message)
        // Update conversation snippet and clear draft
        conversationDao.updateLastMessage(conversationId, body.ifBlank { "[Attachment]" }, now)
        conversationDao.updateDraft(conversationId, null)

        val result = smsTransport.sendTextMessage(recipientAddress, body, localId)
        return when (result) {
            is SmsSendResult.Success -> {
                // Keep PENDING or SENT until broadcast confirms
                messageDao.updateStatus(localId, MessageDeliveryStatus.SENT)
                Result.success(localId)
            }
            is SmsSendResult.Failure -> {
                messageDao.update(message.copy(id = localId, status = MessageDeliveryStatus.FAILED, failureReason = result.reason))
                Result.failure(Exception(result.reason))
            }
        }
    }

    override suspend fun retryMessage(messageId: Long): Result<Unit> {
        val message = messageDao.getMessageById(messageId) ?: return Result.failure(Exception("Message not found"))
        messageDao.updateStatus(messageId, MessageDeliveryStatus.PENDING)
        val result = smsTransport.sendTextMessage(message.recipientAddress, message.body, messageId)
        return when (result) {
            is SmsSendResult.Success -> {
                messageDao.updateStatus(messageId, MessageDeliveryStatus.SENT)
                Result.success(Unit)
            }
            is SmsSendResult.Failure -> {
                messageDao.update(message.copy(status = MessageDeliveryStatus.FAILED, failureReason = result.reason))
                Result.failure(Exception(result.reason))
            }
        }
    }

    override suspend fun markConversationAsRead(conversationId: Long) {
        messageDao.markAllAsReadForConversation(conversationId)
        conversationDao.updateUnreadCount(conversationId, 0)
    }

    override suspend fun setStarred(messageId: Long, isStarred: Boolean) {
        messageDao.setStarred(messageId, isStarred)
    }

    override suspend fun updateReaction(messageId: Long, reaction: String?) {
        messageDao.updateReaction(messageId, reaction)
    }

    override suspend fun deleteMessage(messageId: Long) {
        messageDao.deleteById(messageId)
    }

    override fun searchMessages(query: String): Flow<List<MessageEntity>> =
        messageDao.searchMessages(query)
}
