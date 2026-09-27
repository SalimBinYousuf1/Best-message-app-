package com.example.ui.theme

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.data.local.preferences.ThemeMode
import com.example.ui.components.SalimShaderBackground

val LocalThemeIsDark = compositionLocalOf { false }
val LocalThemeMode = compositionLocalOf { ThemeMode.LIGHT }

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

/**
 * 4th Theme: Salim ASGL translucent liquid glass color scheme
 */
private val SalimColorScheme = lightColorScheme(
    primary = SalimBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xCCDCEDFF),
    onPrimaryContainer = Color(0xFF003875),
    secondary = Color(0xFF4A5568),
    onSecondary = Color.White,
    background = Color.Transparent,
    onBackground = TextPrimaryLight,
    surface = Color(0xD9FFFFFF),
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xB8F1F5F9),
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0x66CBD5E1),
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
        ThemeMode.DARK -> true
        ThemeMode.SALIM -> false
    }

    val context = LocalContext.current
    val colorScheme = when {
        themeMode == ThemeMode.SALIM -> SalimColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (context as? Activity)?.window ?: (view.context as? Activity)?.window
            window?.let { win ->
                val insetsController = WindowCompat.getInsetsController(win, view)
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
                val windowBgColor = when (themeMode) {
                    ThemeMode.LIGHT -> android.graphics.Color.parseColor("#F6F8FA")
                    ThemeMode.SALIM -> android.graphics.Color.parseColor("#FAFBFC")
                    ThemeMode.DARK -> android.graphics.Color.parseColor("#111317")
                    ThemeMode.SYSTEM -> if (isDark) android.graphics.Color.parseColor("#111317") else android.graphics.Color.parseColor("#F6F8FA")
                }
                win.setBackgroundDrawable(ColorDrawable(windowBgColor))
            }
        }
    }

    CompositionLocalProvider(
        LocalThemeIsDark provides isDark,
        LocalThemeMode provides themeMode
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography
        ) {
            if (themeMode == ThemeMode.SALIM) {
                SalimShaderBackground(modifier = Modifier.fillMaxSize()) {
                    content()
                }
            } else {
                content()
            }
        }
    }
}

