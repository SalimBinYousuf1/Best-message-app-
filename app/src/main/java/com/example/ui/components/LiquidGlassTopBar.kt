package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.preferences.ThemeMode
import com.example.ui.theme.LocalThemeIsDark
import com.example.ui.theme.LocalThemeMode
import com.example.ui.theme.SalimCanvasDark
import com.example.ui.theme.SalimCanvasLight
import com.example.ui.theme.SalimCanvasOled
import com.example.ui.theme.liquidGlass

@Composable
fun LiquidGlassTopBar(
    title: String,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = LocalThemeIsDark.current
    val themeMode = LocalThemeMode.current

    val headerBaseColor = when (themeMode) {
        ThemeMode.SALIM -> Color.Transparent
        ThemeMode.LIGHT -> SalimCanvasLight
        ThemeMode.OLED -> SalimCanvasOled
        ThemeMode.DARK -> SalimCanvasDark
        ThemeMode.SYSTEM -> if (isDark) SalimCanvasDark else SalimCanvasLight
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(headerBaseColor)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .liquidGlass(shape = RoundedCornerShape(22.dp), elevation = 4.dp)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                actions()
            }
        }
    }
}
