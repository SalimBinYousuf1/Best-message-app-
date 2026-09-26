package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MessageDeliveryStatus
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess

@Composable
fun MessageStatusIndicator(
    status: MessageDeliveryStatus,
    isIncoming: Boolean,
    modifier: Modifier = Modifier
) {
    if (isIncoming) return

    when (status) {
        MessageDeliveryStatus.PENDING -> {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Sending",
                modifier = modifier.size(13.dp),
                tint = Color.White.copy(alpha = 0.75f)
            )
        }
        MessageDeliveryStatus.SENT -> {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Sent",
                modifier = modifier.size(13.dp),
                tint = Color.White.copy(alpha = 0.85f)
            )
        }
        MessageDeliveryStatus.DELIVERED -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Delivered",
                modifier = modifier.size(14.dp),
                tint = Color.White
            )
        }
        MessageDeliveryStatus.FAILED -> {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = "Failed to send",
                modifier = modifier.size(14.dp),
                tint = StatusError
            )
        }
        MessageDeliveryStatus.RECEIVED -> Unit
    }
}

@Composable
fun UnreadBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    if (count <= 0) return

    val displayText = if (count > 99) "99+" else count.toString()

    Box(
        modifier = modifier
            .background(SalimBlue, CircleShape)
            .padding(horizontal = 7.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DateSeparator(
    dateText: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = dateText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
