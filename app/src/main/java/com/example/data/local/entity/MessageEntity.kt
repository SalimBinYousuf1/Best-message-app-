package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class MessageDeliveryStatus {
    PENDING,
    SENT,
    DELIVERED,
    FAILED,
    RECEIVED
}

enum class AttachmentType {
    NONE,
    IMAGE,
    AUDIO,
    DOCUMENT
}

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["timestamp"]),
        Index(value = ["telephonyMessageId"], unique = false),
        Index(value = ["isStarred"])
    ]
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: Long,
    val telephonyMessageId: Long? = null,
    val senderAddress: String,
    val recipientAddress: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isIncoming: Boolean,
    val status: MessageDeliveryStatus = if (isIncoming) MessageDeliveryStatus.RECEIVED else MessageDeliveryStatus.SENT,
    val isRead: Boolean = !isIncoming,
    val isStarred: Boolean = false,
    val attachmentUri: String? = null,
    val attachmentType: AttachmentType = AttachmentType.NONE,
    val attachmentName: String? = null,
    val isScheduled: Boolean = false,
    val scheduledTime: Long? = null,
    val failureReason: String? = null
)
