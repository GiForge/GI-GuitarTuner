package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = EmeraldTune,
    onSecondary = Color(0xFF00391A),
    secondaryContainer = Color(0xFF005227),
    onSecondaryContainer = Color(0xFF70FCA4),
    tertiary = GoldAccent,
    background = StudioDarkBg,
    onBackground = TextPrimary,
    surface = StudioCardSurface,
    onSurface = TextPrimary,
    surfaceVariant = StudioCardElevated,
    onSurfaceVariant = TextSecondary,
    outline = StudioBorder,
    error = CoralFlat
)

private val LightColorScheme = DarkColorScheme // Precision tuner UX is optimized for high-contrast dark studio view

@Composable
fun AccordatoreTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
