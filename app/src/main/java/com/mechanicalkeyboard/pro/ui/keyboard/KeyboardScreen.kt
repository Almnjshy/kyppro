package com.mechanicalkeyboard.pro.ui.keyboard

import android.media.AudioManager
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mechanicalkeyboard.pro.domain.models.KeyAction
import com.mechanicalkeyboard.pro.domain.models.KeyModifiers
import com.mechanicalkeyboard.pro.domain.models.KeyboardKey
import com.mechanicalkeyboard.pro.domain.models.KeyboardMode
import com.mechanicalkeyboard.pro.domain.models.KeyboardSettings
import com.mechanicalkeyboard.pro.ui.theme.MechanicalKeyboardTheme
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.Flow

/**
 * Root composable for the IME window.
 *
 * [onKeyAction] fires for every key that must actually reach the focused
 * app (text, raw key events, commits) together with which modifiers
 * (Ctrl/Alt/Shift) were held for that press — the IME service decides
 * how to translate that into InputConnection calls.
 *
 * Mode (Standard vs Function/computer-keys layer) and which toolbar
 * panel is open are pure UI state, same as Shift always was — nothing
 * about switching layers needs to reach the service.
 */
@Composable
fun KeyboardScreen(
    clipboardHistory: List<String>,
    settingsFlow: Flow<KeyboardSettings>,
    onKeyAction: (KeyAction, KeyModifiers) -> Unit
) {
    val settings by settingsFlow.collectAsState(initial = KeyboardSettings())
    var mode by remember { mutableStateOf(KeyboardMode.STANDARD) }
    var shiftEnabled by remember { mutableStateOf(false) }
    var ctrlEnabled by remember { mutableStateOf(false) }
    var altEnabled by remember { mutableStateOf(false) }

    fun dispatch(action: KeyAction) {
        val resolved = if (action is KeyAction.Character) {
            val ch = if (shiftEnabled) action.upper else action.lower
            KeyAction.Character(ch, ch)
        } else {
            action
        }
        onKeyAction(resolved, KeyModifiers(ctrl = ctrlEnabled, alt = altEnabled, shift = shiftEnabled))
        // One-shot modifiers: every real keypress releases them, matching
        // how Shift already behaved before Ctrl/Alt existed.
        shiftEnabled = false
        ctrlEnabled = false
        altEnabled = false
    }

    MechanicalKeyboardTheme(accentColor = Color(settings.accentColor.argb)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            KeyboardToolbar(
                mode = mode,
                onModeChange = { mode = it },
                clipboardHistory = clipboardHistory,
                onKeyAction = { action -> onKeyAction(action, KeyModifiers()) }
            )

            val rows = LayerRegistry.rowsFor(mode)
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    row.forEach { key ->
                        KeyButton(
                            key = key,
                            shiftEnabled = shiftEnabled,
                            ctrlEnabled = ctrlEnabled,
                            altEnabled = altEnabled,
                            weight = key.flexWeight,
                            heightDp = settings.keyHeightDp,
                            soundEnabled = settings.soundEnabled,
                            hapticEnabled = settings.hapticEnabled,
                            onClick = {
                                when (key.action) {
                                    is KeyAction.Shift -> shiftEnabled = !shiftEnabled
                                    is KeyAction.CtrlModifier -> ctrlEnabled = !ctrlEnabled
                                    is KeyAction.AltModifier -> altEnabled = !altEnabled
                                    else -> dispatch(key.action)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * A single mechanical keycap.
 *
 * The "mechanical" look/feel is built from three cheap, GPU-friendly
 * pieces rather than any bitmap asset:
 *  - a vertical gradient standing in for a beveled keycap face
 *  - an elevation shadow that shrinks and an offset that grows on press,
 *    simulating physical switch travel
 *  - a scale-down animation so a tapped key visibly compresses
 *
 * Sound and haptics both defer entirely to the system: haptics use
 * [HapticFeedbackConstants.VIRTUAL_KEY] (the same constant the stock
 * Android keyboard uses, so it silently respects the user's system
 * "touch vibration" setting), and sound uses
 * [AudioManager.playSoundEffect] with [AudioManager.FX_KEYPRESS_STANDARD]
 * (silently respects the user's system "touch sounds" setting). Neither
 * requires bundling an audio asset or tracking extra state.
 */
@Composable
private fun RowScope.KeyButton(
    key: KeyboardKey,
    shiftEnabled: Boolean,
    ctrlEnabled: Boolean,
    altEnabled: Boolean,
    weight: Float,
    heightDp: Int,
    soundEnabled: Boolean,
    hapticEnabled: Boolean,
    onClick: () -> Unit
) {
    val label = keyLabel(key.action, shiftEnabled)
    val isAccent = key.action is KeyAction.Enter
    val isActive = (key.action is KeyAction.Shift && shiftEnabled) ||
        (key.action is KeyAction.CtrlModifier && ctrlEnabled) ||
        (key.action is KeyAction.AltModifier && altEnabled)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val elevation: Dp by animateDpAsState(
        targetValue = if (isPressed) 0.dp else 3.dp,
        animationSpec = tween(durationMillis = 60),
        label = "keyElevation"
    )
    val travelOffset: Dp by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 0.dp,
        animationSpec = tween(durationMillis = 60),
        label = "keyTravel"
    )
    val scale: Float by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 60),
        label = "keyScale"
    )

    val view = LocalView.current
    val context = LocalContext.current
    val shape = RoundedCornerShape(8.dp)

    val topColor = when {
        isAccent -> MaterialTheme.colorScheme.primary
        isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.surface
    }
    val bottomColor = when {
        isAccent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.75f)
        isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
        else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
    }
    val bottomBevel = if (isAccent) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
    } else {
        Color.Black.copy(alpha = 0.35f)
    }

    Box(
        modifier = Modifier
            .weight(weight)
            .height(heightDp.dp)
            .scale(scale)
            .offset(y = travelOffset)
            .shadow(elevation = elevation, shape = shape, clip = false)
            .background(
                brush = Brush.verticalGradient(listOf(topColor, bottomColor)),
                shape = shape
            )
            .border(width = 0.5.dp, color = bottomBevel, shape = shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (hapticEnabled) {
                        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    }
                    if (soundEnabled) {
                        val audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as? AudioManager
                        audioManager?.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD)
                    }
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isAccent) Color.White else MaterialTheme.colorScheme.onSurface,
            fontSize = if (label.length > 3) 10.sp else 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

private fun keyLabel(action: KeyAction, shiftEnabled: Boolean): String = when (action) {
    is KeyAction.Character -> (if (shiftEnabled) action.upper else action.lower).toString()
    KeyAction.Space -> ""
    KeyAction.Enter -> "⏎"
    KeyAction.Backspace -> "⌫"
    KeyAction.Shift -> "⇧"
    KeyAction.SwitchLanguage -> "🌐"
    KeyAction.Escape -> "Esc"
    KeyAction.Tab -> "Tab"
    KeyAction.CtrlModifier -> "Ctrl"
    KeyAction.AltModifier -> "Alt"
    KeyAction.WinKey -> "⊞"
    KeyAction.MenuKey -> "☰"
    KeyAction.ArrowUp -> "↑"
    KeyAction.ArrowDown -> "↓"
    KeyAction.ArrowLeft -> "←"
    KeyAction.ArrowRight -> "→"
    KeyAction.Home -> "Home"
    KeyAction.End -> "End"
    KeyAction.Insert -> "Ins"
    KeyAction.ForwardDelete -> "Del"
    KeyAction.PageUp -> "PgUp"
    KeyAction.PageDown -> "PgDn"
    KeyAction.PrintScreen -> "PrtSc"
    KeyAction.PauseKey -> "Pause"
    KeyAction.ScrollLock -> "ScrLk"
    is KeyAction.FunctionKey -> "F${action.number}"
    is KeyAction.CommitText -> if (action.text.isBlank()) "⇥⇥" else action.text
    KeyAction.OpenAppSettings -> "⚙"
}
