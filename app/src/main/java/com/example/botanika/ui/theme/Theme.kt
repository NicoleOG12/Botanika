package com.example.botanika.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BotanikaColorScheme = lightColorScheme(
    primary = BotanikaGreen,
    onPrimary = Color.White,
    primaryContainer = BotanikaPink,
    onPrimaryContainer = BotanikaGreen,
    secondary = BotanikaRose,
    onSecondary = Color.White,
    secondaryContainer = BotanikaSage,
    onSecondaryContainer = BotanikaGreen,
    tertiary = BotanikaRose,
    background = BotanikaBackground,
    onBackground = BotanikaGreen,
    surface = Color.White,
    onSurface = BotanikaGreen,
    surfaceVariant = BotanikaSage,
    onSurfaceVariant = BotanikaMuted,
    outline = BotanikaOutline,
    error = Color(0xFFB3261E)
)

@Composable
fun BotanikaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BotanikaColorScheme,
        typography = Typography,
        content = content
    )
}
