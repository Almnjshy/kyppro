package com.mechanicalkeyboard.pro.ui.keyboard

import com.mechanicalkeyboard.pro.domain.models.KeyboardKey
import com.mechanicalkeyboard.pro.domain.models.KeyboardMode

/**
 * Single lookup from [KeyboardMode] to the rows that mode shows. This is
 * the "قابل للتوسعة" (extensible) part of the Stage 5 layer system:
 * adding a real new layer later (once its stage exists) is one new
 * `object` layout file plus one new `when` branch here — nothing else
 * in the app needs to change.
 */
object LayerRegistry {
    fun rowsFor(mode: KeyboardMode): List<List<KeyboardKey>> = when (mode) {
        KeyboardMode.STANDARD -> KeyboardLayout.rows
        KeyboardMode.FUNCTION -> FunctionLayout.rows
        KeyboardMode.PROGRAMMING -> ProgrammingLayout.rows
        KeyboardMode.GAMING -> GamingLayout.rows
    }

    fun shortLabelFor(mode: KeyboardMode): String = when (mode) {
        KeyboardMode.STANDARD -> "PC"
        KeyboardMode.FUNCTION -> "FN"
        KeyboardMode.PROGRAMMING -> "{ }"
        KeyboardMode.GAMING -> "WASD"
    }
}
