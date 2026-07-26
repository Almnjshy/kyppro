package com.mechanicalkeyboard.pro.ui.keyboard

import com.mechanicalkeyboard.pro.domain.models.KeyAction
import com.mechanicalkeyboard.pro.domain.models.KeyboardKey

/**
 * Computer-keys layout, Stage 3 (FN mode). Every key the spec lists is
 * present: ESC, TAB, CTRL, ALT, SHIFT, WIN, MENU, arrows, HOME, END,
 * INSERT, DELETE, PAGE UP/DOWN, PRINT SCREEN, PAUSE, SCROLL LOCK, and
 * F1-F12 (see KeyAction's doc comment for why F13-F24 can't exist on
 * Android). No numpad — not in the spec's key list, only in the
 * illustrative mockup image, and a numpad wired to nothing real would
 * just be decoration.
 */
object FunctionLayout {

    private fun function(n: Int) = KeyboardKey(KeyAction.FunctionKey(n))

    val row1: List<KeyboardKey> = listOf(
        KeyboardKey(KeyAction.Escape, flexWeight = 1.3f)
    ) + (1..12).map { function(it) }

    val row2: List<KeyboardKey> = listOf(
        KeyboardKey(KeyAction.Tab, flexWeight = 1.3f),
        KeyboardKey(KeyAction.Home),
        KeyboardKey(KeyAction.End),
        KeyboardKey(KeyAction.Insert),
        KeyboardKey(KeyAction.ForwardDelete),
        KeyboardKey(KeyAction.PageUp),
        KeyboardKey(KeyAction.PageDown)
    )

    val row3: List<KeyboardKey> = listOf(
        KeyboardKey(KeyAction.CtrlModifier, flexWeight = 1.2f),
        KeyboardKey(KeyAction.AltModifier, flexWeight = 1.2f),
        KeyboardKey(KeyAction.Shift, flexWeight = 1.2f),
        KeyboardKey(KeyAction.WinKey),
        KeyboardKey(KeyAction.MenuKey),
        KeyboardKey(KeyAction.PrintScreen, flexWeight = 1.3f),
        KeyboardKey(KeyAction.ScrollLock, flexWeight = 1.3f),
        KeyboardKey(KeyAction.PauseKey, flexWeight = 1.3f)
    )

    val row4: List<KeyboardKey> = listOf(
        KeyboardKey(KeyAction.Space, flexWeight = 3f),
        KeyboardKey(KeyAction.ArrowLeft),
        KeyboardKey(KeyAction.ArrowUp),
        KeyboardKey(KeyAction.ArrowDown),
        KeyboardKey(KeyAction.ArrowRight)
    )

    val rows: List<List<KeyboardKey>> = listOf(row1, row2, row3, row4)
}
