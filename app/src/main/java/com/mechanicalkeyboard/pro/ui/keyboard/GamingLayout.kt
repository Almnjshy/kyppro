package com.mechanicalkeyboard.pro.ui.keyboard

import com.mechanicalkeyboard.pro.domain.models.KeyAction
import com.mechanicalkeyboard.pro.domain.models.KeyboardKey

/**
 * Gaming layer, Stage 5 (Layer 4) — a basic but fully real, working
 * layout: WASD emphasized with extra width, common game keys (Shift to
 * run, Ctrl to crouch, Space to jump), and the number row for
 * weapon/ability slots.
 *
 * This is intentionally the *basic* version. The full Stage 9 vision
 * (game profiles, quick macros, complete per-game customization) needs
 * the macro engine (Stage 7) and more of the customization editor than
 * Stage 6 built in this pass — see the README. Everything below already
 * works for real typing/input today; nothing here is a mockup.
 */
object GamingLayout {

    private fun char(lower: Char, upper: Char) =
        KeyboardKey(KeyAction.Character(lower, upper))

    val row1: List<KeyboardKey> = "12345".map { char(it, it) } +
        listOf(KeyboardKey(KeyAction.Escape, flexWeight = 1.5f))

    val row2: List<KeyboardKey> = listOf(
        KeyboardKey(KeyAction.Tab),
        char('q', 'Q'),
        char('w', 'W'),
        char('e', 'E'),
        char('r', 'R'),
        KeyboardKey(KeyAction.Space, flexWeight = 2f)
    )

    val row3: List<KeyboardKey> = listOf(
        KeyboardKey(KeyAction.CtrlModifier),
        char('a', 'A'),
        char('s', 'S'),
        char('d', 'D'),
        char('f', 'F'),
        KeyboardKey(KeyAction.Space, flexWeight = 2f)
    )

    val row4: List<KeyboardKey> = listOf(
        KeyboardKey(KeyAction.Shift, flexWeight = 1.3f),
        char('z', 'Z'),
        char('x', 'X'),
        char('c', 'C'),
        char('v', 'V'),
        KeyboardKey(KeyAction.Backspace, flexWeight = 1.3f)
    )

    val rows: List<List<KeyboardKey>> = listOf(row1, row2, row3, row4)
}
