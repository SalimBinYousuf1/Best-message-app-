package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import com.example.ui.theme.LocalThemeIsDark
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SalimBlue
import com.example.ui.theme.liquidGlass
import kotlin.math.roundToInt

/**
 * Apple iOS style segmented control with an animated liquid-glass sliding thumb.
 */
@Composable
fun <T> LiquidGlassSegmentedControl(
    items: List<Pair<T, String>>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val isDark = LocalThemeIsDark.current
    val selectedIndex = items.indexOfFirst { it.first == selectedItem }.coerceAtLeast(0)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .liquidGlass(
                shape = RoundedCornerShape(14.dp),
                elevation = 1.dp,
                customAlpha = if (isDark) 0.55f else 0.65f
            )
            .padding(3.dp)
    ) {
        val totalWidth = maxWidth
        val segmentWidth = if (items.isNotEmpty()) totalWidth / items.size else 0.dp

        // Animated thumb offset with Apple spring dynamics
        val animatedOffset by animateDpAsState(
            targetValue = segmentWidth * selectedIndex,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "SegmentedThumb"
        )

        // Sliding glass thumb
        Box(
            modifier = Modifier
                .offset(x = animatedOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(11.dp),
                    ambientColor = if (isDark) Color.Black.copy(alpha = 0.4f) else Color(0x140F172A),
                    spotColor = if (isDark) Color.Black.copy(alpha = 0.5f) else Color(0x100F172A)
                )
                .clip(RoundedCornerShape(11.dp))
                .background(
                    if (isDark) {
                        Brush.verticalGradient(
                            listOf(Color(0xFF383D47), Color(0xFF2B2F38))
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(Color.White, Color(0xFFF7FAFD))
                        )
                    }
                )
                .border(
                    width = 0.8.dp,
                    brush = Brush.verticalGradient(
                        if (isDark) listOf(Color.White.copy(alpha = 0.22f), Color.Transparent)
                        else listOf(Color.White, Color(0xFFE2E8F0).copy(alpha = 0.65f))
                    ),
                    shape = RoundedCornerShape(11.dp)
                )
        )

        // Tab labels
        Row(modifier = Modifier.fillMaxSize()) {
            items.forEachIndexed { index, (item, label) ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!isSelected) {
                                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                onItemSelected(item)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = if (items.size > 4) 11.5.sp else 13.sp
                        ),
                        color = if (isSelected) {
                            if (isDark) Color.White else SalimBlue
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

/**
 * Apple iOS style smooth liquid glass switch toggle.
 */
@Composable
fun LiquidGlassSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    val isDark = LocalThemeIsDark.current

    // Apple switch standard dimensions
    val width = 51.dp
    val height = 31.dp
    val thumbSize = 27.dp
    val padding = 2.dp

    val maxTravel = width - thumbSize - (padding * 2)

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) maxTravel else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "switchThumbOffset"
    )

    val trackColor by animateColorAsState(
        targetValue = if (checked) {
            SalimBlue
        } else {
            if (isDark) Color(0xFF393E46) else Color(0xFFE2E8F0)
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "switchTrackColor"
    )

    Box(
        modifier = modifier
            .size(width = width, height = height)
            .shadow(
                elevation = if (checked) 2.dp else 1.dp,
                shape = CircleShape,
                spotColor = if (checked) SalimBlue.copy(alpha = 0.35f) else Color.Transparent
            )
            .clip(CircleShape)
            .background(trackColor)
            .border(
                width = 0.8.dp,
                color = if (checked) Color.White.copy(alpha = 0.30f)
                else (if (isDark) Color.White.copy(alpha = 0.12f) else Color(0xFFCBD5E1)),
                shape = CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                onCheckedChange(!checked)
            }
            .padding(padding),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .shadow(
                    elevation = 3.dp,
                    shape = CircleShape,
                    ambientColor = Color.Black.copy(alpha = 0.25f),
                    spotColor = Color.Black.copy(alpha = 0.35f)
                )
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

/**
 * Apple-style liquid glass button with spring press bounce and subtle specular sheen.
 */
enum class LiquidGlassButtonStyle {
    PRIMARY,
    SECONDARY,
    DESTRUCTIVE
}

@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: LiquidGlassButtonStyle = LiquidGlassButtonStyle.PRIMARY,
    enabled: Boolean = true,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(14.dp),
    content: @Composable RowScope.() -> Unit
) {
    val view = LocalView.current
    val isDark = LocalThemeIsDark.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "btnScale"
    )

    val backgroundBrush = when (style) {
        LiquidGlassButtonStyle.PRIMARY -> {
            Brush.linearGradient(
                colors = if (enabled) {
                    listOf(Color(0xFF3395FF), Color(0xFF007AFF), Color(0xFF0062D2))
                } else {
                    listOf(Color(0xFF8E99A8), Color(0xFF758190))
                }
            )
        }
        LiquidGlassButtonStyle.SECONDARY -> {
            if (isDark) {
                Brush.linearGradient(
                    listOf(Color(0xFF333842).copy(alpha = 0.90f), Color(0xFF262A32).copy(alpha = 0.90f))
                )
            } else {
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.95f), Color(0xFFF1F5F9).copy(alpha = 0.90f))
                )
            }
        }
        LiquidGlassButtonStyle.DESTRUCTIVE -> {
            Brush.linearGradient(
                listOf(Color(0xFFFF453A), Color(0xFFD70015))
            )
        }
    }

    val borderBrush = when (style) {
        LiquidGlassButtonStyle.PRIMARY -> Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.45f), Color.White.copy(alpha = 0.15f))
        )
        LiquidGlassButtonStyle.SECONDARY -> Brush.verticalGradient(
            if (isDark) listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.05f))
            else listOf(Color.White, Color(0xFFCBD5E1).copy(alpha = 0.6f))
        )
        LiquidGlassButtonStyle.DESTRUCTIVE -> Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.40f), Color.White.copy(alpha = 0.10f))
        )
    }

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 1.dp else 3.dp,
                shape = shape,
                spotColor = if (style == LiquidGlassButtonStyle.PRIMARY) SalimBlue.copy(alpha = 0.35f) else Color.Transparent
            )
            .clip(shape)
            .background(backgroundBrush)
            .border(width = 0.8.dp, brush = borderBrush, shape = shape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}

/**
 * Pixel-perfect Apple-style liquid glass slider.
 */
@Composable
fun LiquidGlassSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f
) {
    val view = LocalView.current
    val density = LocalDensity.current
    val isDark = LocalThemeIsDark.current

    var isDragging by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val totalWidthPx = constraints.maxWidth.toFloat()
        val thumbRadiusDp = 14.dp
        val thumbRadiusPx = with(density) { thumbRadiusDp.toPx() }
        val usableWidthPx = (totalWidthPx - thumbRadiusPx * 2).coerceAtLeast(1f)

        val normalized = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
        val thumbOffsetPx = normalized * usableWidthPx
        val thumbOffsetDp = with(density) { thumbOffsetPx.toDp() }

        val thumbScale by animateFloatAsState(
            targetValue = if (isDragging) 1.18f else 1.0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "sliderThumbScale"
        )

        // Capsule Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (isDark) Color(0xFF2C303A) else Color(0xFFE2E8F0))
                .border(
                    width = 0.5.dp,
                    color = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            // Active portion
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(thumbOffsetDp + thumbRadiusDp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF3395FF), SalimBlue)
                        )
                    )
            )
        }

        // Grabbing Thumb
        Box(
            modifier = Modifier
                .offset { IntOffset(thumbOffsetPx.roundToInt(), 0) }
                .size(thumbRadiusDp * 2)
                .scale(thumbScale)
                .shadow(
                    elevation = if (isDragging) 6.dp else 3.dp,
                    shape = CircleShape,
                    spotColor = Color.Black.copy(alpha = 0.35f)
                )
                .clip(CircleShape)
                .background(Color.White)
                .border(width = 0.8.dp, color = Color(0xFFCBD5E1), shape = CircleShape)
                .pointerInput(valueRange, usableWidthPx) {
                    detectDragGestures(
                        onDragStart = {
                            isDragging = true
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        },
                        onDragEnd = {
                            isDragging = false
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val currentPx = normalized * usableWidthPx
                            val newPx = (currentPx + dragAmount.x).coerceIn(0f, usableWidthPx)
                            val fraction = newPx / usableWidthPx
                            val newValue = valueRange.start + fraction * (valueRange.endInclusive - valueRange.start)
                            onValueChange(newValue)
                            view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        }
                    )
                }
        )
    }
}

/**
 * Apple-style liquid glass search bar with frosted capsule and 1-tap clear button.
 */
@Composable
fun LiquidGlassSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    placeholder: String = "Search",
    modifier: Modifier = Modifier,
    onSearchAction: (() -> Unit)? = null
) {
    val isDark = LocalThemeIsDark.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(shape = RoundedCornerShape(16.dp), elevation = 2.dp)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = SalimBlue,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                }

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChanged,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(SalimBlue),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearchAction?.invoke() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF3F444E) else Color(0xFFCBD5E1))
                        .clickable { onQueryChanged("") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = if (isDark) Color.White else Color(0xFF475569),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * Apple iMessage-style quick reaction floating bar (Tapback).
 */
@Composable
fun LiquidGlassReactionPicker(
    visible: Boolean,
    onReactionSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reactions = listOf("❤️", "👍", "👎", "😂", "‼️", "❓")
    val view = LocalView.current

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0.80f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        ) + fadeIn(),
        exit = scaleOut(targetScale = 0.80f) + fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .liquidGlass(shape = RoundedCornerShape(26.dp), elevation = 6.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            reactions.forEach { emoji ->
                var isPressed by remember { mutableStateOf(false) }
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 1.35f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "reactionScale"
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onReactionSelected(emoji)
                            onDismiss()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 20.sp)
                }
            }
        }
    }
}

/**
 * Reaction badge pinned to message bubble.
 */
@Composable
fun LiquidGlassReactionBadge(
    reaction: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    Box(
        modifier = modifier
            .shadow(elevation = 3.dp, shape = CircleShape)
            .clip(CircleShape)
            .liquidGlass(shape = CircleShape, elevation = 2.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable {
                        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                        onClick()
                    }
                } else Modifier
            )
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = reaction, fontSize = 13.sp)
    }
}
