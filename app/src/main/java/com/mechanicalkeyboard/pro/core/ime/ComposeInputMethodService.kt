package com.mechanicalkeyboard.pro.core.ime

import android.inputmethodservice.InputMethodService
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

/**
 * An InputMethodService is NOT an Activity/ComponentActivity, so Compose
 * gets none of the lifecycle/saved-state plumbing it normally relies on.
 * This class provides exactly what a real on-device crash proved
 * Compose actually requires here — no more, no less:
 *
 *  1. ViewTreeLifecycleOwner — required, confirmed by:
 *     "ViewTreeLifecycleOwner not found from ... android:id/parentPanel"
 *  2. ViewTreeSavedStateRegistryOwner — also required, confirmed by:
 *     "Composed into the View which doesn't propagate
 *      ViewTreeSavedStateRegistryOwner!"
 *     AndroidComposeView checks for this on every attach regardless of
 *     whether the composition actually calls rememberSaveable().
 *
 * Still deliberately NOT implemented: ViewModelStoreOwner. Nothing has
 * thrown asking for it, and nothing in the app calls viewModel() — add
 * it (same decorView pattern used below) only if a real stack trace
 * ever asks for it.
 *
 * IMPORTANT lesson baked into both owners below: Compose's internal
 * lookups do not start from the ComposeView we return in
 * onCreateInputView() — InputMethodService's window is internally a
 * Dialog, and the lookup starts from that Dialog's decorView, several
 * framework panel layouts above our content. Setting an owner only on
 * our ComposeView (a leaf, deep in that tree) is not enough; it has to
 * be set on the decorView so the whole hierarchy inherits it.
 */
abstract class ComposeInputMethodService :
    InputMethodService(),
    LifecycleOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        // performRestore() must run while the lifecycle is still
        // INITIALIZED (i.e. before the ON_CREATE event below) — this
        // matches how ComponentActivity itself orders these two calls.
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)

        super.onCreate()
        CrashReporter.install(this)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        val decorView = window?.window?.decorView
        decorView?.setViewTreeLifecycleOwner(this)
        decorView?.setViewTreeSavedStateRegistryOwner(this)
    }

    /** Wraps [content] in a ComposeView with valid owners attached. */
    protected fun composeView(content: @Composable () -> Unit): View {
        return ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            // Redundant with the decorView owners set in onCreate() above,
            // but harmless to also set directly here — nearest ancestor
            // wins, and it keeps this view correct even if it's ever
            // reused outside this service's own window.
            setViewTreeLifecycleOwner(this@ComposeInputMethodService)
            setViewTreeSavedStateRegistryOwner(this@ComposeInputMethodService)
            setContent(content)
        }
    }

    override fun onStartInputView(info: android.view.inputmethod.EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        if (lifecycleRegistry.currentState != Lifecycle.State.RESUMED) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        if (lifecycleRegistry.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        }
        super.onFinishInputView(finishingInput)
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }
}
