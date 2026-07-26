package com.mechanicalkeyboard.pro.domain.models

/**
 * Persisted, user-adjustable settings — Stage 6. Everything here is
 * actually applied by the keyboard and actually saved via DataStore
 * (see data/repository/SettingsRepository.kt); nothing here is a
 * placeholder that doesn't do anything yet.
 */
data class KeyboardSettings(
    val keyHeightDp: Int = 42,
    val soundEnabled: Boolean = true,
    val hapticEnabled: Boolean = true,
    val accentColor: AccentColor = AccentColor.PURPLE
)

/** A small, real set of theme accent colors the user can pick from. */
enum class AccentColor(val argb: Long) {
    PURPLE(0xFF7C4DFFL),
    BLUE(0xFF2979FFL),
    GREEN(0xFF00C853L),
    RED(0xFFFF5252L),
    ORANGE(0xFFFF9100L)
}
