package com.mechanicalkeyboard.pro.ui.keyboard

import com.mechanicalkeyboard.pro.domain.models.KeyAction
import com.mechanicalkeyboard.pro.domain.models.KeyboardKey

/**
 * Programming layer, Stage 5 (Layer 3). Every key commits a real
 * character via [KeyAction.CommitText] — same mechanism the emoji panel
 * uses — so nothing here is a placeholder.
 */
object ProgrammingLayout {

    private fun sym(text: String) = KeyboardKey(KeyAction.CommitText(text))

    val row1: List<KeyboardKey> = listOf(
        sym("("), sym(")"), sym("["), sym("]"), sym("{"), sym("}"),
        sym("<"), sym(">"), sym(";"), sym(":")
    )

    val row2: List<KeyboardKey> = listOf(
        sym("="), sym("+"), sym("-"), sym("*"), sym("/"), sym("%"),
        sym("&"), sym("|"), sym("^"), sym("~")
    )

    val row3: List<KeyboardKey> = listOf(
        sym("\""), sym("'"), sym("`"), sym("_"), sym("\\"),
        sym("#"), sym("@"), sym("$"), sym("!"), sym("?")
    )

    val row4: List<KeyboardKey> = listOf(
        KeyboardKey(KeyAction.Tab, flexWeight = 1.3f),
        sym("  "),
        KeyboardKey(KeyAction.Space, flexWeight = 3f),
        KeyboardKey(KeyAction.Backspace, flexWeight = 1.3f),
        KeyboardKey(KeyAction.Enter, flexWeight = 1.3f)
    )

    val rows: List<List<KeyboardKey>> = listOf(row1, row2, row3, row4)
}
