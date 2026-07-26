package com.mechanicalkeyboard.pro.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mechanicalkeyboard.pro.domain.models.AccentColor
import com.mechanicalkeyboard.pro.domain.models.KeyboardSettings

/**
 * Every control here writes straight through to DataStore (via the
 * callbacks the caller wires to [com.mechanicalkeyboard.pro.data.repository.SettingsRepository])
 * and the keyboard applies the result on its very next composition —
 * there's no "Save" button because there's no unsaved, fake state.
 *
 * Deliberately NOT here yet: per-key position/size/icon/function
 * remapping, or a full theme/layer editor. Those need a dedicated
 * drag-and-drop key editor, which is real, substantial UI work beyond
 * what this pass could build and still keep every part of it genuinely
 * working end to end — see the README for the honest scope call.
 */
@Composable
fun SettingsScreen(
    settings: KeyboardSettings,
    onKeyHeightChange: (Int) -> Unit,
    onSoundEnabledChange: (Boolean) -> Unit,
    onHapticEnabledChange: (Boolean) -> Unit,
    onAccentColorChange: (AccentColor) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        Text("تخصيص لوحة المفاتيح", style = MaterialTheme.typography.headlineSmall)

        Column {
            Text("حجم المفتاح: ${settings.keyHeightDp}dp")
            Slider(
                value = settings.keyHeightDp.toFloat(),
                onValueChange = { onKeyHeightChange(it.toInt()) },
                valueRange = 34f..56f,
                steps = 21
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("صوت الضغط على المفاتيح")
            Switch(checked = settings.soundEnabled, onCheckedChange = onSoundEnabledChange)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("الاهتزاز عند الضغط")
            Switch(checked = settings.hapticEnabled, onCheckedChange = onHapticEnabledChange)
        }

        Column {
            Text("لون الثيم")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AccentColor.entries.forEach { color ->
                    val isSelected = settings.accentColor == color
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(color.argb), shape = CircleShape)
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = MaterialTheme.colorScheme.onBackground,
                                shape = CircleShape
                            )
                            .clickable { onAccentColorChange(color) }
                    )
                }
            }
        }
    }
}
