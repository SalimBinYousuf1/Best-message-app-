package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(20.dp),
    elevation: Dp = 4.dp,
    borderWidth: Dp = 1.dp,
    customAlpha: Float? = null
): Modifier {
    val isDark = isSystemInDarkTheme()

    val surfaceColor = if (isDark) {
        val alpha = customAlpha ?: 0.65f
        Color(0xFF1E2229).copy(alpha = alpha)
    } else {
        val alpha = customAlpha ?: 0.78f
        Color.White.copy(alpha = alpha)
    }

    val borderBrush = Brush.verticalGradient(
        colors = if (isDark) {
            listOf(
                Color.White.copy(alpha = 0.22f),
                Color.White.copy(alpha = 0.05f)
            )
        } else {
            listOf(
                Color.White.copy(alpha = 0.85f),
                Color(0xFFE2E8F0).copy(alpha = 0.40f)
            )
        }
    )

    return this
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = if (isDark) Color.Black.copy(alpha = 0.5f) else Color(0x1A0F172A),
            spotColor = if (isDark) Color.Black.copy(alpha = 0.6f) else Color(0x140F172A)
        )
        .clip(shape)
        .background(surfaceColor, shape)
        .border(width = borderWidth, brush = borderBrush, shape = shape)
}

@Composable
fun Modifier.liquidGlassBubble(
    isOutgoing: Boolean,
    shape: Shape
): Modifier {
    val isDark = isSystemInDarkTheme()

    if (isOutgoing) {
        val gradient = Brush.linearGradient(
            colors = listOf(
                SalimBlueLight,
                SalimBlue
            )
        )
        val borderBrush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.35f),
                Color.White.copy(alpha = 0.10f)
            )
        )
        return this
            .shadow(elevation = 2.dp, shape = shape, spotColor = SalimBlue.copy(alpha = 0.3f))
            .clip(shape)
            .background(gradient, shape)
            .border(width = 0.75.dp, brush = borderBrush, shape = shape)
    } else {
        val bubbleColor = if (isDark) {
            Color(0xFF23272F).copy(alpha = 0.90f)
        } else {
            Color(0xFFEFF2F6).copy(alpha = 0.95f)
        }
        val borderBrush = Brush.verticalGradient(
            colors = if (isDark) {
                listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.03f))
            } else {
                listOf(Color.White.copy(alpha = 0.9f), Color(0xFFDDE3EA).copy(alpha = 0.5f))
            }
        )
        return this
            .shadow(elevation = 1.dp, shape = shape)
            .clip(shape)
            .background(bubbleColor, shape)
            .border(width = 0.75.dp, brush = borderBrush, shape = shape)
    }
}
