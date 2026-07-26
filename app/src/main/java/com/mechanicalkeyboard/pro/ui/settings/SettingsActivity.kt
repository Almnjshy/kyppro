package com.mechanicalkeyboard.pro.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import com.mechanicalkeyboard.pro.core.ime.CrashReporter
import com.mechanicalkeyboard.pro.data.repository.SettingsRepository
import com.mechanicalkeyboard.pro.domain.models.KeyboardSettings
import com.mechanicalkeyboard.pro.ui.theme.MechanicalKeyboardTheme
import kotlinx.coroutines.launch

class SettingsActivity : ComponentActivity() {
    private lateinit var repository: SettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CrashReporter.install(this)
        repository = SettingsRepository(applicationContext)

        setContent {
            val settings by repository.settingsFlow.collectAsState(initial = KeyboardSettings())

            MechanicalKeyboardTheme(accentColor = Color(settings.accentColor.argb)) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SettingsScreen(
                        settings = settings,
                        onKeyHeightChange = { dp -> lifecycleScope.launch { repository.setKeyHeight(dp) } },
                        onSoundEnabledChange = { enabled -> lifecycleScope.launch { repository.setSoundEnabled(enabled) } },
                        onHapticEnabledChange = { enabled -> lifecycleScope.launch { repository.setHapticEnabled(enabled) } },
                        onAccentColorChange = { color -> lifecycleScope.launch { repository.setAccentColor(color) } }
                    )
                }
            }
        }
    }
}
