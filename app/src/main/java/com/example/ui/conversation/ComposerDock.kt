package com.example.ui.conversation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.telephony.SmsTransport
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.liquidGlass

@Composable
fun ComposerDock(
    text: String,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    onOpenAttachments: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenSchedule: () -> Unit,
    showCharCounter: Boolean = true,
    sendOnEnter: Boolean = false,
    modifier: Modifier = Modifier
) {
    val (segments, remaining) = remember(text) {
        SmsTransport.calculateSmsSegments(text)
    }

    val canSend = text.isNotBlank()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .liquidGlass(shape = RoundedCornerShape(26.dp), elevation = 6.dp)
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attachments button
                IconButton(
                    onClick = onOpenAttachments,
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("composer_attachment_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Attach media",
                        tint = SalimBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Quick Templates button
                IconButton(
                    onClick = onOpenTemplates,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TextSnippet,
                        contentDescription = "Quick templates",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Schedule send shortcut button
                IconButton(
                    onClick = onOpenSchedule,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("composer_schedule_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Schedule SMS",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Text input area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = "Text message (SMS)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }

                    BasicTextField(
                        value = text,
                        onValueChange = onTextChanged,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(SalimBlue),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = if (sendOnEnter) ImeAction.Send else ImeAction.Default
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (canSend) onSend()
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("composer_input_field")
                    )
                }

                // Send Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (canSend) SalimBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable(enabled = canSend) { onSend() }
                        .testTag("composer_send_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (canSend) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Character and segment count helper (shown when typing)
            if (showCharCounter && text.length > 50) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 48.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "$remaining chars • $segments ${if (segments == 1) "SMS" else "parts"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
