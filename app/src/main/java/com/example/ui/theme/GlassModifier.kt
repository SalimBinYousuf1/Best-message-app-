package com.example.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Apple-inspired Liquid Glass surface modifier.
 * Applies multi-stop frosted glass refraction, crisp specular illumination border,
 * and realistic physical depth.
 */
@Composable
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(20.dp),
    elevation: Dp = 3.dp,
    borderWidth: Dp = 1.dp,
    customAlpha: Float? = null
): Modifier {
    val isDark = isSystemInDarkTheme()

    // Multi-stop liquid glass refraction gradient
    val surfaceBrush = if (isDark) {
        val baseAlpha = customAlpha ?: 0.72f
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF282C34).copy(alpha = baseAlpha),
                Color(0xFF1E2127).copy(alpha = (baseAlpha + 0.08f).coerceAtMost(0.96f)),
                Color(0xFF16181D).copy(alpha = (baseAlpha + 0.14f).coerceAtMost(0.98f))
            )
        )
    } else {
        val baseAlpha = customAlpha ?: 0.82f
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = (baseAlpha + 0.10f).coerceAtMost(0.98f)),
                Color(0xFFFBFDFF).copy(alpha = baseAlpha),
                Color(0xFFF1F5F9).copy(alpha = (baseAlpha - 0.08f).coerceAtLeast(0.55f))
            )
        )
    }

    // Specular highlight: top-edge illumination mimicking directional light
    val borderBrush = Brush.verticalGradient(
        colors = if (isDark) {
            listOf(
                Color.White.copy(alpha = 0.32f),
                Color.White.copy(alpha = 0.10f),
                Color.White.copy(alpha = 0.03f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.98f),
                Color(0xFFE2E8F0).copy(alpha = 0.65f),
                Color(0xFFCBD5E1).copy(alpha = 0.40f)
            )
        }
    )

    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = if (isDark) Color.Black.copy(alpha = 0.55f) else Color(0x180F172A),
            spotColor = if (isDark) Color.Black.copy(alpha = 0.65f) else Color(0x140F172A)
        )
        .clip(shape)
        .background(surfaceBrush, shape)
        .border(width = borderWidth, brush = borderBrush, shape = shape)
}

/**
 * Interactive liquid glass modifier with Apple-style spring press scale feedback.
 */
@Composable
fun Modifier.liquidGlassInteractive(
    shape: Shape = RoundedCornerShape(20.dp),
    elevation: Dp = 3.dp,
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.97f,
    customAlpha: Float? = null
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "glassPressScale"
    )

    return this
        .scale(animatedScale)
        .liquidGlass(
            shape = shape,
            elevation = if (isPressed) 1.dp else elevation,
            customAlpha = customAlpha
        )
}

/**
 * Apple iMessage style liquid glass message bubble with directional curvature and glow.
 */
@Composable
fun Modifier.liquidGlassBubble(
    isOutgoing: Boolean,
    shape: Shape
): Modifier {
    val isDark = isSystemInDarkTheme()

    if (isOutgoing) {
        val gradient = Brush.linearGradient(
            colors = listOf(
                Color(0xFF3395FF),
                Color(0xFF007AFF),
                Color(0xFF0066D6)
            )
        )
        val borderBrush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.50f),
                Color.White.copy(alpha = 0.18f)
            )
        )
        return this
            .shadow(
                elevation = 2.dp,
                shape = shape,
                spotColor = SalimBlue.copy(alpha = 0.35f)
            )
            .clip(shape)
            .background(gradient, shape)
            .border(width = 0.8.dp, brush = borderBrush, shape = shape)
    } else {
        val bubbleBrush = if (isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF2C313B).copy(alpha = 0.95f),
                    Color(0xFF22262E).copy(alpha = 0.95f)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.98f),
                    Color(0xFFECEFF3).copy(alpha = 0.98f)
                )
            )
        }

        val borderBrush = Brush.verticalGradient(
            colors = if (isDark) {
                listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.06f))
            } else {
                listOf(Color.White.copy(alpha = 0.98f), Color(0xFFDDE3EA).copy(alpha = 0.70f))
            }
        )
        return this
            .shadow(elevation = 1.dp, shape = shape)
            .clip(shape)
            .background(bubbleBrush, shape)
            .border(width = 0.8.dp, brush = borderBrush, shape = shape)
    }
}

