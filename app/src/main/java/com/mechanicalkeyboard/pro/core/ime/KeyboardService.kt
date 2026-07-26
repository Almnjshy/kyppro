package com.mechanicalkeyboard.pro.core.ime

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.TextView
import androidx.compose.runtime.mutableStateListOf
import com.mechanicalkeyboard.pro.data.repository.SettingsRepository
import com.mechanicalkeyboard.pro.domain.models.KeyAction
import com.mechanicalkeyboard.pro.domain.models.KeyModifiers
import com.mechanicalkeyboard.pro.ui.keyboard.KeyboardScreen
import com.mechanicalkeyboard.pro.ui.settings.SettingsActivity

/**
 * IME service. Stage 1: full QWERTY, digits, space, enter, backspace,
 * shift, language switch. Stage 3: real computer keys, sent as actual
 * Android KeyEvents (not text) so shortcuts like Ctrl+C genuinely work.
 * Stage 4: toolbar actions (settings, emoji/clipboard commits).
 * Stage 5: nothing new here — layer switching lives entirely in
 * KeyboardScreen/LayerRegistry. Stage 6: owns the [SettingsRepository]
 * and feeds its Flow straight into the Compose tree so every setting
 * change is applied on the very next composition.
 */
class KeyboardService : ComposeInputMethodService() {

    /** Backs the toolbar's clipboard panel — see onCreate()/onDestroy(). */
    private val clipboardHistory = mutableStateListOf<String>()
    private var clipboardManager: ClipboardManager? = null
    private val clipboardListener = ClipboardManager.OnPrimaryClipChangedListener {
        val manager = clipboardManager ?: return@OnPrimaryClipChangedListener
        try {
            val clip = manager.primaryClip ?: return@OnPrimaryClipChangedListener
            if (clip.itemCount == 0) return@OnPrimaryClipChangedListener
            val text = clip.getItemAt(0).coerceToText(this)?.toString()
            if (!text.isNullOrBlank() && clipboardHistory.firstOrNull() != text) {
                clipboardHistory.add(0, text)
                while (clipboardHistory.size > 20) {
                    clipboardHistory.removeAt(clipboardHistory.lastIndex)
                }
            }
        } catch (securityException: SecurityException) {
            // Some OEMs/Android versions restrict background clipboard
            // reads even for the active IME in edge cases. Not fatal —
            // the clipboard panel just stays empty until a read succeeds.
            Log.e(TAG, "Clipboard read denied", securityException)
        }
    }

    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository(applicationContext)
        clipboardManager = (getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.also {
            it.addPrimaryClipChangedListener(clipboardListener)
        }
    }

    override fun onDestroy() {
        clipboardManager?.removePrimaryClipChangedListener(clipboardListener)
        clipboardManager = null
        super.onDestroy()
    }

    override fun onCreateInputView(): View {
        return try {
            composeView {
                KeyboardScreen(
                    clipboardHistory = clipboardHistory,
                    settingsFlow = settingsRepository.settingsFlow,
                    onKeyAction = ::handleKeyAction
                )
            }
        } catch (t: Throwable) {
            // Never let building the input view crash the host app the
            // user is typing into. Log the real exception so it's
            // retrievable from logcat / any crash-report tool (even if
            // the on-screen keyboard itself can't render this run), and
            // show a plain, non-Compose fallback so at least something
            // visible (and not a crash) appears.
            Log.e(TAG, "Failed to build the Compose keyboard view", t)
            TextView(this).apply {
                text = "Mechanical Keyboard Pro failed to load: ${t.message}"
                setPadding(24, 24, 24, 24)
                gravity = Gravity.CENTER
            }
        }
    }

    private fun handleKeyAction(action: KeyAction, modifiers: KeyModifiers) {
        val ic: InputConnection = currentInputConnection ?: return

        when (action) {
            is KeyAction.Character -> {
                if (modifiers.ctrl || modifiers.alt) {
                    val keyCode = charToKeyCode(action.lower.lowercaseChar())
                    if (keyCode != null) {
                        sendRawKey(ic, keyCode, modifiers)
                    } else {
                        // No KeyEvent code for this character (e.g. most
                        // symbols) — falling back to committing it as
                        // text is the only meaningful thing left to do.
                        ic.commitText(action.lower.toString(), 1)
                    }
                } else {
                    ic.commitText(action.lower.toString(), 1)
                }
            }

            KeyAction.Space -> ic.commitText(" ", 1)

            KeyAction.Enter -> {
                val editorInfo: EditorInfo? = currentInputEditorInfo
                val imeAction = editorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
                val hasCustomAction = imeAction != null &&
                    imeAction != EditorInfo.IME_ACTION_NONE &&
                    imeAction != EditorInfo.IME_ACTION_UNSPECIFIED &&
                    editorInfo.imeOptions.and(EditorInfo.IME_FLAG_NO_ENTER_ACTION) == 0

                if (hasCustomAction) {
                    ic.performEditorAction(imeAction!!)
                } else {
                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                }
            }

            KeyAction.Backspace -> ic.deleteSurroundingText(1, 0)

            KeyAction.Shift, KeyAction.CtrlModifier, KeyAction.AltModifier -> {
                // Handled entirely as local UI state in KeyboardScreen —
                // it resolves the right character/metaState before ever
                // calling this function. Kept as explicit branches so
                // this `when` stays exhaustive.
            }

            KeyAction.SwitchLanguage -> switchToNextInputMethod(false)

            KeyAction.Escape -> sendRawKey(ic, KeyEvent.KEYCODE_ESCAPE, modifiers)
            KeyAction.Tab -> sendRawKey(ic, KeyEvent.KEYCODE_TAB, modifiers)
            KeyAction.WinKey -> sendRawKey(ic, KeyEvent.KEYCODE_META_LEFT, modifiers)
            KeyAction.MenuKey -> sendRawKey(ic, KeyEvent.KEYCODE_MENU, modifiers)
            KeyAction.ArrowUp -> sendRawKey(ic, KeyEvent.KEYCODE_DPAD_UP, modifiers)
            KeyAction.ArrowDown -> sendRawKey(ic, KeyEvent.KEYCODE_DPAD_DOWN, modifiers)
            KeyAction.ArrowLeft -> sendRawKey(ic, KeyEvent.KEYCODE_DPAD_LEFT, modifiers)
            KeyAction.ArrowRight -> sendRawKey(ic, KeyEvent.KEYCODE_DPAD_RIGHT, modifiers)
            KeyAction.Home -> sendRawKey(ic, KeyEvent.KEYCODE_MOVE_HOME, modifiers)
            KeyAction.End -> sendRawKey(ic, KeyEvent.KEYCODE_MOVE_END, modifiers)
            KeyAction.Insert -> sendRawKey(ic, KeyEvent.KEYCODE_INSERT, modifiers)
            KeyAction.ForwardDelete -> sendRawKey(ic, KeyEvent.KEYCODE_FORWARD_DEL, modifiers)
            KeyAction.PageUp -> sendRawKey(ic, KeyEvent.KEYCODE_PAGE_UP, modifiers)
            KeyAction.PageDown -> sendRawKey(ic, KeyEvent.KEYCODE_PAGE_DOWN, modifiers)
            KeyAction.PrintScreen -> sendRawKey(ic, KeyEvent.KEYCODE_SYSRQ, modifiers)
            KeyAction.PauseKey -> sendRawKey(ic, KeyEvent.KEYCODE_BREAK, modifiers)
            KeyAction.ScrollLock -> sendRawKey(ic, KeyEvent.KEYCODE_SCROLL_LOCK, modifiers)
            is KeyAction.FunctionKey -> sendRawKey(ic, KeyEvent.KEYCODE_F1 + (action.number - 1), modifiers)

            is KeyAction.CommitText -> ic.commitText(action.text, 1)

            KeyAction.OpenAppSettings -> {
                val intent = Intent(this, SettingsActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivity(intent)
            }
        }
    }

    /** Maps a-z / 0-9 to their Android KeyEvent code; everything else has no code. */
    private fun charToKeyCode(c: Char): Int? = when {
        c in 'a'..'z' -> KeyEvent.KEYCODE_A + (c - 'a')
        c in '0'..'9' -> KeyEvent.KEYCODE_0 + (c - '0')
        else -> null
    }

    private fun metaStateOf(modifiers: KeyModifiers): Int {
        var meta = 0
        if (modifiers.ctrl) meta = meta or KeyEvent.META_CTRL_ON or KeyEvent.META_CTRL_LEFT_ON
        if (modifiers.alt) meta = meta or KeyEvent.META_ALT_ON or KeyEvent.META_ALT_LEFT_ON
        if (modifiers.shift) meta = meta or KeyEvent.META_SHIFT_ON or KeyEvent.META_SHIFT_LEFT_ON
        return meta
    }

    private fun sendRawKey(ic: InputConnection, keyCode: Int, modifiers: KeyModifiers) {
        val metaState = metaStateOf(modifiers)
        val now = SystemClock.uptimeMillis()
        ic.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, metaState))
        ic.sendKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0, metaState))
    }

    private companion object {
        const val TAG = "MechanicalKeyboardPro"
    }
}
