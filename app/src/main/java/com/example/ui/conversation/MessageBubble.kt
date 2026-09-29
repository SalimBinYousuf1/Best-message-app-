package com.example.ui.conversation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.entity.AttachmentType
import com.example.data.local.entity.MessageDeliveryStatus
import com.example.data.local.entity.MessageEntity
import com.example.ui.components.LiquidGlassReactionBadge
import com.example.ui.components.LiquidGlassReactionPicker
import com.example.ui.components.MessageStatusIndicator
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.StatusError
import com.example.ui.theme.liquidGlassBubble
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: MessageEntity,
    onRetry: () -> Unit,
    onToggleStar: () -> Unit,
    onSelectReaction: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    textScale: Float = 1.0f
) {
    val context = LocalContext.current
    val view = LocalView.current
    var showMenu by remember { mutableStateOf(false) }
    var showReactionPicker by remember { mutableStateOf(false) }

    val isOutgoing = !message.isIncoming

    val timeString = remember(message.timestamp) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    val bubbleShape = if (isOutgoing) {
        RoundedCornerShape(
            topStart = 18.dp,
            topEnd = 18.dp,
            bottomStart = 18.dp,
            bottomEnd = 4.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 18.dp,
            topEnd = 18.dp,
            bottomStart = 4.dp,
            bottomEnd = 18.dp
        )
    }

    val isLocation = message.attachmentType == AttachmentType.LOCATION ||
        message.body.contains("maps.google.com") ||
        message.body.startsWith("geo:")

    val isContact = message.attachmentType == AttachmentType.CONTACT ||
        message.body.startsWith("Contact:")

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        contentAlignment = if (isOutgoing) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 310.dp)
        ) {
            // Floating Tapback Reaction Picker (above bubble)
            if (showReactionPicker) {
                LiquidGlassReactionPicker(
                    visible = showReactionPicker,
                    onReactionSelected = { emoji ->
                        onSelectReaction(emoji)
                        showReactionPicker = false
                    },
                    onDismiss = { showReactionPicker = false },
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Box(contentAlignment = if (isOutgoing) Alignment.BottomStart else Alignment.BottomEnd) {
                // Bubble Body
                Box(
                    modifier = Modifier
                        .liquidGlassBubble(isOutgoing = isOutgoing, shape = bubbleShape)
                        .combinedClickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                if (message.status == MessageDeliveryStatus.FAILED) {
                                    onRetry()
                                }
                            },
                            onLongClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                showReactionPicker = true
                                showMenu = true
                            }
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column {
                        // Location Card with interactive map link
                        if (isLocation) {
                            val mapUrl = if (message.body.contains("maps.google.com")) {
                                message.body
                            } else {
                                message.attachmentUri ?: "https://maps.google.com"
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isOutgoing) Color(0x33FFFFFF) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable {
                                        try {
                                            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl)).apply {
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(mapIntent)
                                        } catch (_: Exception) {}
                                    }
                                    .padding(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEF4444)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Shared Location",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = (14f * textScale).sp
                                            ),
                                            color = if (isOutgoing) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Tap to open in Google Maps",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = (11f * textScale).sp
                                            ),
                                            color = if (isOutgoing) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.OpenInNew,
                                        contentDescription = "Open Map",
                                        tint = if (isOutgoing) Color.White else SalimBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // Contact Card
                        if (isContact) {
                            val contactPayload = message.body
                            val phoneOnly = contactPayload.substringAfter("Phone: ").substringBefore("\n").trim()
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isOutgoing) Color(0x33FFFFFF) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable {
                                        if (phoneOnly.isNotBlank()) {
                                            try {
                                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneOnly")).apply {
                                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                }
                                                context.startActivity(dialIntent)
                                            } catch (_: Exception) {}
                                        }
                                    }
                                    .padding(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF59E0B)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = message.attachmentName ?: "Shared Contact",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = (14f * textScale).sp
                                            ),
                                            color = if (isOutgoing) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = contactPayload,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontSize = (11f * textScale).sp
                                            ),
                                            color = if (isOutgoing) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // Attachment if present (Image or Document)
                        if (!message.attachmentUri.isNullOrBlank() && !isLocation && !isContact) {
                            when (message.attachmentType) {
                                AttachmentType.IMAGE -> {
                                    AsyncImage(
                                        model = message.attachmentUri,
                                        contentDescription = "Image attachment",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(180.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .padding(bottom = 6.dp)
                                    )
                                }
                                else -> {
                                    Text(
                                        text = "📎 ${message.attachmentName ?: "Attachment"}",
                                        color = if (isOutgoing) Color.White else MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = (12f * textScale).sp
                                        ),
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                            }
                        }

                        // Message Body (only if not already displayed as custom location/contact card)
                        if (message.body.isNotBlank() && !isLocation && !isContact) {
                            Text(
                                text = message.body,
                                style = com.example.ui.theme.MessageBodyStyle.copy(
                                    fontSize = (15.5f * textScale).sp,
                                    lineHeight = (22f * textScale).sp
                                ),
                                color = if (isOutgoing) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Time and delivery status
                        Row(
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (message.isStarred) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Starred",
                                    tint = if (isOutgoing) Color.Yellow.copy(alpha = 0.9f) else Color(0xFFFFB300),
                                    modifier = Modifier.size(11.dp)
                                )
                            }

                            Text(
                                text = timeString,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isOutgoing) Color.White.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )

                            if (isOutgoing) {
                                MessageStatusIndicator(status = message.status, isIncoming = false)
                            }
                        }
                    }
                }

                // Reaction Badge attached to corner
                if (!message.reaction.isNullOrBlank()) {
                    LiquidGlassReactionBadge(
                        reaction = message.reaction,
                        onClick = { onSelectReaction(message.reaction) },
                        modifier = Modifier.offset(
                            x = if (isOutgoing) (-6).dp else 6.dp,
                            y = 10.dp
                        )
                    )
                }
            }

            // Error notice under failed message
            if (message.status == MessageDeliveryStatus.FAILED) {
                Row(
                    modifier = Modifier
                        .padding(top = 2.dp, end = 4.dp)
                        .clickable { onRetry() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Not sent. Tap to retry",
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusError,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry send",
                        tint = StatusError,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // Long press context menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Copy text") },
                onClick = {
                    showMenu = false
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    clipboard?.setPrimaryClip(ClipData.newPlainText("SMS Message", message.body))
                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                },
                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("Share") },
                onClick = {
                    showMenu = false
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, message.body)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share message"))
                },
                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text(if (message.isStarred) "Unstar" else "Star") },
                onClick = {
                    showMenu = false
                    onToggleStar()
                },
                leadingIcon = {
                    Icon(
                        if (message.isStarred) Icons.Default.StarOutline else Icons.Default.Star,
                        contentDescription = null
                    )
                }
            )
            if (message.status == MessageDeliveryStatus.FAILED) {
                DropdownMenuItem(
                    text = { Text("Retry send") },
                    onClick = {
                        showMenu = false
                        onRetry()
                    },
                    leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) }
                )
            }
            DropdownMenuItem(
                text = { Text("Delete message") },
                onClick = {
                    showMenu = false
                    onDelete()
                },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
            )
        }
    }
}
