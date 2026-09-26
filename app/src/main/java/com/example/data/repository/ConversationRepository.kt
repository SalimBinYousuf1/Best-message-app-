package com.example.data.repository

import com.example.data.local.dao.ConversationDao
import com.example.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    fun getActiveConversations(): Flow<List<ConversationEntity>>
    fun getArchivedConversations(): Flow<List<ConversationEntity>>
    fun getUnreadConversations(): Flow<List<ConversationEntity>>
    fun getConversationFlow(id: Long): Flow<ConversationEntity?>
    suspend fun getConversationById(id: Long): ConversationEntity?
    suspend fun getOrCreateConversation(address: String, name: String?): Long
    suspend fun setPinned(id: Long, isPinned: Boolean)
    suspend fun setArchived(id: Long, isArchived: Boolean)
    suspend fun setMuted(id: Long, isMuted: Boolean)
    suspend fun setSpam(id: Long, isSpam: Boolean)
    suspend fun updateDraft(id: Long, draft: String?)
    suspend fun deleteConversation(id: Long)
    fun searchConversations(query: String): Flow<List<ConversationEntity>>
}

class ConversationRepositoryImpl(
    private val conversationDao: ConversationDao
) : ConversationRepository {

    override fun getActiveConversations(): Flow<List<ConversationEntity>> =
        conversationDao.getActiveConversations()

    override fun getArchivedConversations(): Flow<List<ConversationEntity>> =
        conversationDao.getArchivedConversations()

    override fun getUnreadConversations(): Flow<List<ConversationEntity>> =
        conversationDao.getUnreadConversations()

    override fun getConversationFlow(id: Long): Flow<ConversationEntity?> =
        conversationDao.getConversationByIdFlow(id)

    override suspend fun getConversationById(id: Long): ConversationEntity? =
        conversationDao.getConversationById(id)

    override suspend fun getOrCreateConversation(address: String, name: String?): Long {
        val existing = conversationDao.getConversationByAddress(address)
        if (existing != null) return existing.id
        val newConv = ConversationEntity(
            recipientAddress = address,
            recipientName = name,
            lastMessageTimestamp = System.currentTimeMillis()
        )
        return conversationDao.insertOrUpdate(newConv)
    }

    override suspend fun setPinned(id: Long, isPinned: Boolean) =
        conversationDao.setPinned(id, isPinned)

    override suspend fun setArchived(id: Long, isArchived: Boolean) =
        conversationDao.setArchived(id, isArchived)

    override suspend fun setMuted(id: Long, isMuted: Boolean) =
        conversationDao.setMuted(id, isMuted)

    override suspend fun setSpam(id: Long, isSpam: Boolean) =
        conversationDao.setSpam(id, isSpam)

    override suspend fun updateDraft(id: Long, draft: String?) =
        conversationDao.updateDraft(id, draft)

    override suspend fun deleteConversation(id: Long) =
        conversationDao.deleteById(id)

    override fun searchConversations(query: String): Flow<List<ConversationEntity>> =
        conversationDao.searchConversations(query)
}
