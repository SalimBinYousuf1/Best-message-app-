package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ScheduledMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledMessageDao {

    @Query("SELECT * FROM scheduled_messages WHERE isExecuted = 0 AND isCancelled = 0 ORDER BY scheduledTimestamp ASC")
    fun getPendingScheduledMessages(): Flow<List<ScheduledMessageEntity>>

    @Query("SELECT * FROM scheduled_messages WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ScheduledMessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(scheduledMessage: ScheduledMessageEntity): Long

    @Update
    suspend fun update(scheduledMessage: ScheduledMessageEntity)

    @Query("UPDATE scheduled_messages SET isExecuted = 1 WHERE id = :id")
    suspend fun markExecuted(id: Long)

    @Query("UPDATE scheduled_messages SET isCancelled = 1 WHERE id = :id")
    suspend fun markCancelled(id: Long)

    @Query("DELETE FROM scheduled_messages WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM scheduled_messages WHERE isExecuted = 0 AND isCancelled = 0 AND scheduledTimestamp <= :now")
    suspend fun getDueMessages(now: Long): List<ScheduledMessageEntity>
}
