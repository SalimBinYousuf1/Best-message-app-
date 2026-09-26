package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.data.local.preferences.ThemeMode

private val LightColorScheme = lightColorScheme(
    primary = SalimBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE5F1FF),
    onPrimaryContainer = Color(0xFF003875),
    secondary = Color(0xFF5E6E82),
    onSecondary = Color.White,
    background = SalimCanvasLight,
    onBackground = TextPrimaryLight,
    surface = Color.White,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFEBF0F5),
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFCBD5E1),
    error = StatusError,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = SalimBlueLight,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF004999),
    onPrimaryContainer = Color(0xFFD6E7FF),
    secondary = Color(0xFF94A3B8),
    onSecondary = Color.Black,
    background = SalimCanvasDark,
    onBackground = TextPrimaryDark,
    surface = Color(0xFF1E2127),
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF282C34),
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0xFF3E4451),
    error = StatusError,
    onError = Color.White
)

private val OledColorScheme = darkColorScheme(
    primary = SalimBlueLight,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF003D80),
    onPrimaryContainer = Color(0xFFCCE4FF),
    secondary = Color(0xFF8899A6),
    onSecondary = Color.Black,
    background = SalimCanvasOled,
    onBackground = Color.White,
    surface = Color(0xFF0E0F12),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF16181D),
    onSurfaceVariant = Color(0xFFA0AAB5),
    outline = Color(0xFF2A2D35),
    error = StatusError,
    onError = Color.White
)

@Composable
fun SalimTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.OLED -> true
    }

    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        themeMode == ThemeMode.OLED -> OledColorScheme
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
