package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "conversations",
    indices = [
        Index(value = ["recipientAddress"], unique = true),
        Index(value = ["lastMessageTimestamp"]),
        Index(value = ["isPinned", "lastMessageTimestamp"])
    ]
)
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipientAddress: String, // e.g. "+15551234567"
    val recipientName: String? = null,
    val lastMessageText: String? = null,
    val lastMessageTimestamp: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isMuted: Boolean = false,
    val isSpam: Boolean = false,
    val customColor: String? = null,
    val draftText: String? = null
)
