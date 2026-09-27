package com.example.ui.conversation

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import com.example.ui.theme.LocalThemeIsDark
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.preferences.ThemeMode
import com.example.telephony.SmsTransport
import com.example.ui.theme.LocalThemeMode
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
    val view = LocalView.current
    val isDark = LocalThemeIsDark.current
    val themeMode = LocalThemeMode.current

    val (segments, remaining) = remember(text) {
        SmsTransport.calculateSmsSegments(text)
    }

    val canSend = text.isNotBlank()

    val sendInteractionSource = remember { MutableInteractionSource() }
    val isSendPressed by sendInteractionSource.collectIsPressedAsState()

    val sendScale by animateFloatAsState(
        targetValue = if (isSendPressed) 0.88f else (if (canSend) 1.0f else 0.92f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "sendBtnScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
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
                // Attachments '+' button with liquid glass capsule feel
                IconButton(
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        onOpenAttachments()
                    },
                    modifier = Modifier
                        .size(38.dp)
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
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        onOpenTemplates()
                    },
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
                    onClick = {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        onOpenSchedule()
                    },
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f)
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
                                if (canSend) {
                                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                                    onSend()
                                }
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("composer_input_field")
                    )
                }

                // Apple-style circular send button
                Box(
                    modifier = Modifier
                        .scale(sendScale)
                        .size(40.dp)
                        .shadow(
                            elevation = if (canSend) 3.dp else 0.dp,
                            shape = CircleShape,
                            spotColor = if (canSend) SalimBlue.copy(alpha = 0.45f) else Color.Transparent
                        )
                        .clip(CircleShape)
                        .background(
                            if (canSend) {
                                when (themeMode) {
                                    ThemeMode.LIGHT -> Brush.verticalGradient(listOf(Color(0xFF2C2C2E), Color(0xFF1C1C1E)))
                                    ThemeMode.DARK -> Brush.verticalGradient(listOf(Color(0xFFF2F4F7), Color(0xFFE2E6EB)))
                                    ThemeMode.SALIM -> Brush.linearGradient(listOf(Color(0xFF3395FF), SalimBlue))
                                    ThemeMode.SYSTEM -> if (isDark) {
                                        Brush.verticalGradient(listOf(Color(0xFFF2F4F7), Color(0xFFE2E6EB)))
                                    } else {
                                        Brush.verticalGradient(listOf(Color(0xFF2C2C2E), Color(0xFF1C1C1E)))
                                    }
                                }
                            } else {
                                Brush.verticalGradient(
                                    if (isDark) listOf(Color(0xFF2C3038), Color(0xFF23272F))
                                    else listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                                )
                            }
                        )
                        .border(
                            width = 0.8.dp,
                            color = if (canSend) Color.White.copy(alpha = 0.40f) else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable(
                            enabled = canSend,
                            interactionSource = sendInteractionSource,
                            indication = null
                        ) {
                            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                            onSend()
                        }
                        .testTag("composer_send_button"),
                    contentAlignment = Alignment.Center
                ) {
                    val iconTint = if (canSend) {
                        if (isDark && themeMode != ThemeMode.SALIM) Color(0xFF1C1C1E) else Color.White
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = iconTint,
                        modifier = Modifier.size(17.dp)
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
