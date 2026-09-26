package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_messages")
data class ScheduledMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: Long,
    val recipientAddress: String,
    val recipientName: String? = null,
    val body: String,
    val scheduledTimestamp: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val isExecuted: Boolean = false,
    val isCancelled: Boolean = false
)
