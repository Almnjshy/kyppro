package com.mechanicalkeyboard.pro.domain.models

/**
 * Which key layer is currently visible.
 *
 * Only modes with a genuinely working, fully-typed layout behind them
 * are listed here — Stage 5 asked for a Macro layer and a "User Custom"
 * layer too, but neither has anything real behind it yet (the macro
 * engine is Stage 7; a real per-key custom editor is future work beyond
 * this pass's Stage 6 scope — see the README). A tab for either would be
 * exactly the fake UI this project disallows. The enum — and
 * LayerRegistry that maps each mode to its key rows — is intentionally
 * built so adding MACRO/CUSTOM later is a one-line change, satisfying
 * "قابل للتوسعة" without faking anything today.
 */
enum class KeyboardMode {
    STANDARD,
    FUNCTION,
    PROGRAMMING,
    GAMING
}
