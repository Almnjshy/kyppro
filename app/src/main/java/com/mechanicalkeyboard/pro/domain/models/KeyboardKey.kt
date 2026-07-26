package com.mechanicalkeyboard.pro.domain.models

/**
 * What happens when a key is pressed.
 *
 * Stage 1: letters, digits, space, enter, backspace, shift, language switch.
 *
 * Stage 3 adds real computer keys. Note on F-keys: Android's KeyEvent API
 * only defines KEYCODE_F1..KEYCODE_F12 — there is no F13-F24 keycode in
 * the Android SDK at all, so those can't be sent as real key events on
 * Android regardless of implementation. F1-F12 (everything the platform
 * actually defines) is implemented.
 *
 * Stage 4 adds the toolbar: mode is handled as local UI state (see
 * KeyboardScreen), but committing emoji/clipboard text and opening the
 * settings screen need to reach the IME service, hence the actions below.
 */
sealed class KeyAction {
    data class Character(val lower: Char, val upper: Char) : KeyAction()
    object Space : KeyAction()
    object Enter : KeyAction()
    object Backspace : KeyAction()
    object Shift : KeyAction()
    object SwitchLanguage : KeyAction()

    // Stage 3 — computer keys
    object Escape : KeyAction()
    object Tab : KeyAction()
    object CtrlModifier : KeyAction()
    object AltModifier : KeyAction()
    object WinKey : KeyAction()
    object MenuKey : KeyAction()
    object ArrowUp : KeyAction()
    object ArrowDown : KeyAction()
    object ArrowLeft : KeyAction()
    object ArrowRight : KeyAction()
    object Home : KeyAction()
    object End : KeyAction()
    object Insert : KeyAction()
    object ForwardDelete : KeyAction()
    object PageUp : KeyAction()
    object PageDown : KeyAction()
    object PrintScreen : KeyAction()
    object PauseKey : KeyAction()
    object ScrollLock : KeyAction()
    /** number is 1..12 — see the class doc above for why it stops at 12. */
    data class FunctionKey(val number: Int) : KeyAction()

    // Stage 4 — toolbar
    /** Commits arbitrary text as-is: used by the emoji panel and clipboard history. */
    data class CommitText(val text: String) : KeyAction()
    /** Opens MainActivity (the real settings/enable screen that already exists). */
    object OpenAppSettings : KeyAction()
}

/**
 * Which modifier keys are currently held (one-shot: set by the UI when a
 * key is pressed, auto-released right after — see KeyboardScreen). Only
 * meaningful for keys the IME service sends as raw KeyEvents; plain text
 * commits (letters with no Ctrl/Alt held, space, etc.) ignore this.
 */
data class KeyModifiers(
    val ctrl: Boolean = false,
    val alt: Boolean = false,
    val shift: Boolean = false
)

/**
 * A single key as laid out on screen.
 * [flexWeight] controls relative width inside its row (1f = one normal key).
 */
data class KeyboardKey(
    val action: KeyAction,
    val flexWeight: Float = 1f
)
