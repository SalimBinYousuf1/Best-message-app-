package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {

    @Query("SELECT * FROM conversations WHERE isArchived = 0 AND isSpam = 0 ORDER BY isPinned DESC, lastMessageTimestamp DESC")
    fun getActiveConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isArchived = 1 ORDER BY lastMessageTimestamp DESC")
    fun getArchivedConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE unreadCount > 0 AND isArchived = 0 ORDER BY lastMessageTimestamp DESC")
    fun getUnreadConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id")
    fun getConversationByIdFlow(id: Long): Flow<ConversationEntity?>

    @Query("SELECT * FROM conversations WHERE id = :id")
    suspend fun getConversationById(id: Long): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE recipientAddress = :address LIMIT 1")
    suspend fun getConversationByAddress(address: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(conversation: ConversationEntity): Long

    @Update
    suspend fun update(conversation: ConversationEntity)

    @Query("UPDATE conversations SET lastMessageText = :snippet, lastMessageTimestamp = :timestamp WHERE id = :id")
    suspend fun updateLastMessage(id: Long, snippet: String, timestamp: Long)

    @Query("UPDATE conversations SET unreadCount = :unreadCount WHERE id = :id")
    suspend fun updateUnreadCount(id: Long, unreadCount: Int)

    @Query("UPDATE conversations SET draftText = :draft WHERE id = :id")
    suspend fun updateDraft(id: Long, draft: String?)

    @Query("UPDATE conversations SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPinned(id: Long, isPinned: Boolean)

    @Query("UPDATE conversations SET isArchived = :isArchived WHERE id = :id")
    suspend fun setArchived(id: Long, isArchived: Boolean)

    @Query("UPDATE conversations SET isMuted = :isMuted WHERE id = :id")
    suspend fun setMuted(id: Long, isMuted: Boolean)

    @Query("UPDATE conversations SET isSpam = :isSpam WHERE id = :id")
    suspend fun setSpam(id: Long, isSpam: Boolean)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM conversations WHERE recipientName LIKE '%' || :query || '%' OR recipientAddress LIKE '%' || :query || '%' OR lastMessageText LIKE '%' || :query || '%'")
    fun searchConversations(query: String): Flow<List<ConversationEntity>>
}
