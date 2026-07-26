package com.mechanicalkeyboard.pro.ui.keyboard

import com.mechanicalkeyboard.pro.domain.models.KeyAction
import com.mechanicalkeyboard.pro.domain.models.KeyboardKey

/**
 * Full QWERTY layout, standard mode, stage 1.
 * Only what the spec requires: A-Z, 0-9, space, enter, backspace,
 * shift, language switch. No FN row, no computer keys yet.
 */
object KeyboardLayout {

    private fun char(lower: Char, upper: Char) =
        KeyboardKey(KeyAction.Character(lower, upper))

    val numberRow: List<KeyboardKey> = "1234567890".map { char(it, it) }

    val row1: List<KeyboardKey> = listOf(
        char('q', 'Q'), char('w', 'W'), char('e', 'E'), char('r', 'R'), char('t', 'T'),
        char('y', 'Y'), char('u', 'U'), char('i', 'I'), char('o', 'O'), char('p', 'P')
    )

    val row2: List<KeyboardKey> = listOf(
        char('a', 'A'), char('s', 'S'), char('d', 'D'), char('f', 'F'), char('g', 'G'),
        char('h', 'H'), char('j', 'J'), char('k', 'K'), char('l', 'L')
    )

    val row3: List<KeyboardKey> = listOf(
        KeyboardKey(KeyAction.Shift, flexWeight = 1.5f),
        char('z', 'Z'), char('x', 'X'), char('c', 'C'), char('v', 'V'),
        char('b', 'B'), char('n', 'N'), char('m', 'M'),
        KeyboardKey(KeyAction.Backspace, flexWeight = 1.5f)
    )

    val row4: List<KeyboardKey> = listOf(
        KeyboardKey(KeyAction.SwitchLanguage, flexWeight = 1.5f),
        KeyboardKey(KeyAction.Space, flexWeight = 5f),
        KeyboardKey(KeyAction.Enter, flexWeight = 1.5f)
    )

    val rows: List<List<KeyboardKey>> = listOf(numberRow, row1, row2, row3, row4)
}
