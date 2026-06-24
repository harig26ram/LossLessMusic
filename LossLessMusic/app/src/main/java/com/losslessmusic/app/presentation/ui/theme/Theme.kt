package com.losslessmusic.app.presentation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val DarkBackground = Color(0xFF0D0D0D)
val DarkSurface = Color(0xFF1A1A1A)
val DarkSurfaceVariant = Color(0xFF242424)
val AccentGreen = Color(0xFF1DB954)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB3B3B3)
val TextTertiary = Color(0xFF727272)
val ErrorColor = Color(0xFFCF6679)
val LightBackground = Color(0xFFFFFFFF)
val LightSurface = Color(0xFFF5F5F5)
val LightSurfaceVariant = Color(0xFFE0E0E0)
val LightTextPrimary = Color(0xFF0D0D0D)
val LightTextSecondary = Color(0xFF5F5F5F)

enum class ThemeMode { DARK, LIGHT, DYNAMIC }

private val DarkScheme = darkColorScheme(
    primary = AccentGreen,
    onPrimary = DarkBackground,
    primaryContainer = AccentGreen.copy(alpha = 0.2f),
    secondary = AccentGreen,
    tertiary = AccentGreen,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = ErrorColor,
)

private val LightScheme = lightColorScheme(
    primary = AccentGreen,
    onPrimary = LightBackground,
    primaryContainer = AccentGreen.copy(alpha = 0.15f),
    secondary = AccentGreen,
    tertiary = AccentGreen,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary,
    error = ErrorColor,
)

@Composable
fun LossLessMusicTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        themeMode == ThemeMode.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (isSystemInDarkTheme()) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        themeMode == ThemeMode.LIGHT -> LightScheme
        else -> DarkScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}