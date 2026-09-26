package com.example.ui.inbox

import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ConversationEntity
import com.example.ui.components.ContactAvatar
import com.example.ui.components.UnreadBadge
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.liquidGlass

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConversationItem(
    conversation: ConversationEntity,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleArchive: () -> Unit,
    onToggleMute: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    val formattedTime = remember(conversation.lastMessageTimestamp) {
        val now = System.currentTimeMillis()
        val time = conversation.lastMessageTimestamp
        if (now - time < DateUtils.MINUTE_IN_MILLIS) {
            "Just now"
        } else {
            DateUtils.getRelativeTimeSpanString(
                time,
                now,
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
            ).toString()
        }
    }

    val displayName = conversation.recipientName ?: conversation.recipientAddress
    val hasUnread = conversation.unreadCount > 0

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .liquidGlass(
                    shape = RoundedCornerShape(18.dp),
                    elevation = if (hasUnread) 3.dp else 1.dp,
                    customAlpha = if (hasUnread) 0.88f else 0.65f
                )
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showMenu = true }
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ContactAvatar(
                name = conversation.recipientName,
                address = conversation.recipientAddress,
                size = 50.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (hasUnread) SalimBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val previewText = when {
                        !conversation.draftText.isNullOrBlank() -> "Draft: ${conversation.draftText}"
                        !conversation.lastMessageText.isNullOrBlank() -> conversation.lastMessageText
                        else -> "No messages yet"
                    }

                    Text(
                        text = previewText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (hasUnread) FontWeight.Medium else FontWeight.Normal
                        ),
                        color = if (!conversation.draftText.isNullOrBlank()) {
                            SalimBlue
                        } else if (hasUnread) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (conversation.isMuted) {
                            Icon(
                                imageVector = Icons.Default.NotificationsOff,
                                contentDescription = "Muted",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        if (conversation.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = SalimBlue,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        UnreadBadge(count = conversation.unreadCount)
                    }
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(if (conversation.isPinned) "Unpin conversation" else "Pin to top") },
                onClick = {
                    showMenu = false
                    onTogglePin()
                },
                leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text(if (conversation.isArchived) "Unarchive" else "Archive") },
                onClick = {
                    showMenu = false
                    onToggleArchive()
                },
                leadingIcon = { Icon(Icons.Default.Archive, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text(if (conversation.isMuted) "Unmute notifications" else "Mute notifications") },
                onClick = {
                    showMenu = false
                    onToggleMute()
                },
                leadingIcon = { Icon(Icons.Default.NotificationsOff, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("Delete conversation") },
                onClick = {
                    showMenu = false
                    onDelete()
                },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
            )
        }
    }
}
