package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.QuickReplyTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {

    @Query("SELECT * FROM quick_reply_templates ORDER BY orderIndex ASC")
    fun getAllTemplates(): Flow<List<QuickReplyTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: QuickReplyTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(templates: List<QuickReplyTemplateEntity>)

    @Update
    suspend fun update(template: QuickReplyTemplateEntity)

    @Query("DELETE FROM quick_reply_templates WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM quick_reply_templates")
    suspend fun count(): Int
}
