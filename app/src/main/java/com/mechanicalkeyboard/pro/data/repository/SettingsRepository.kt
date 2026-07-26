package com.mechanicalkeyboard.pro.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mechanicalkeyboard.pro.data.datastore.keyboardSettingsDataStore
import com.mechanicalkeyboard.pro.domain.models.AccentColor
import com.mechanicalkeyboard.pro.domain.models.KeyboardSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Reads/writes [KeyboardSettings] through DataStore. This is the only
 * place that knows about DataStore's Preferences API — everything else
 * in the app (UI, IME service) only ever sees [KeyboardSettings].
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val KEY_HEIGHT = intPreferencesKey("key_height_dp")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
    }

    val settingsFlow: Flow<KeyboardSettings> = context.keyboardSettingsDataStore.data.map { prefs ->
        KeyboardSettings(
            keyHeightDp = prefs[Keys.KEY_HEIGHT] ?: 42,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            hapticEnabled = prefs[Keys.HAPTIC_ENABLED] ?: true,
            accentColor = prefs[Keys.ACCENT_COLOR]
                ?.let { name -> AccentColor.entries.find { it.name == name } }
                ?: AccentColor.PURPLE
        )
    }

    suspend fun setKeyHeight(dp: Int) {
        context.keyboardSettingsDataStore.edit { it[Keys.KEY_HEIGHT] = dp }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.keyboardSettingsDataStore.edit { it[Keys.SOUND_ENABLED] = enabled }
    }

    suspend fun setHapticEnabled(enabled: Boolean) {
        context.keyboardSettingsDataStore.edit { it[Keys.HAPTIC_ENABLED] = enabled }
    }

    suspend fun setAccentColor(color: AccentColor) {
        context.keyboardSettingsDataStore.edit { it[Keys.ACCENT_COLOR] = color.name }
    }
}
