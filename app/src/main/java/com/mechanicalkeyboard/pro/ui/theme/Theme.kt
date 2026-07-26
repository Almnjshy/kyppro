package com.mechanicalkeyboard.pro.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * [accentColor] defaults to the original purple but is driven by the
 * user's Stage 6 setting (see KeyboardScreen / SettingsScreen) — this is
 * a real, applied customization, not a cosmetic default that ignores
 * what the user picked.
 */
@Composable
fun MechanicalKeyboardTheme(
    accentColor: Color = Color(0xFF7C4DFF),
    content: @Composable () -> Unit
) {
    val colors = darkColorScheme(
        primary = accentColor,
        background = Color(0xFF14141F),
        surface = Color(0xFF1E1E2E),
        onBackground = Color(0xFFEDEDF2),
        onSurface = Color(0xFFEDEDF2)
    )
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
