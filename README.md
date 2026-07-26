# Mechanical Keyboard Pro — Stage 1 through 4

## What this is
A real Android IME (Input Method Editor). No mocks, no TODOs, no
placeholder screens — confirmed working on a real device as of Stage 2
(see "Crash history" below for exactly what was fixed and why).

**Stage 1** — functional keyboard: A–Z, a–z, 0–9, Space, Enter, Backspace,
Shift, language switch (globe key → system IME picker/subtype switch).

**Stage 2** — mechanical visual/tactile design: dark gradient keycaps,
elevation shadow that shrinks on press, scale-down press animation,
system-respecting haptics (`HapticFeedbackConstants.VIRTUAL_KEY`) and
key-press sound (`AudioManager.FX_KEYPRESS_STANDARD`) — both silently
honor the device's own touch-vibration/touch-sound settings, no fake
in-app toggle.

**Stage 3** — real computer keys, added as a second layer ("FN" mode):
ESC, TAB, CTRL, ALT, SHIFT, WIN, MENU, arrow keys, HOME, END, INSERT,
DELETE, PAGE UP/DOWN, PRINT SCREEN, PAUSE, SCROLL LOCK, F1–F12. Every one
of these sends a genuine Android `KeyEvent` via `InputConnection`, not
committed text. Ctrl and Alt work as real one-shot modifiers — press
Ctrl, then C, and the app receives an actual Ctrl+C key combo (checked
via `KeyEvent` meta-state), the same mechanism a physical keyboard uses.

**One honest platform limitation:** the spec asked for F1–F24, but
Android's `KeyEvent` API only defines `KEYCODE_F1`..`KEYCODE_F12` — there
is no F13–F24 keycode anywhere in the Android SDK, so those literally
cannot be sent as real key events on Android. F1–F12 (everything the
platform defines) is implemented.

**Stage 4** — toolbar, built directly into `KeyboardScreen`:
- **PC / FN tabs** — real mode switch between Stage 1's layout and
  Stage 3's computer-keys layout (this is "Current Layer").
- **Collapse button** — smoothly animates the toolbar open/closed
  (`AnimatedVisibility` with expand/shrink + fade).
- **Language button** — same real `switchToNextInputMethod()` as the
  globe key.
- **Settings button** — opens the real `MainActivity` screen (not a
  placeholder — it's the same enable/pick-keyboard screen from Stage 1).
- **Connection status indicator** — honestly always shows "غير متصل"
  (not connected), because Computer Mode (Bluetooth/WiFi) is the
  project's *final* stage and doesn't exist yet. Showing "connected"
  here would be fake; showing the true current state isn't.
- **Emoji panel** — a real, working grid of common emoji; tapping one
  commits the actual character.
- **Clipboard panel** — a real clipboard history, populated by a live
  `ClipboardManager` listener registered in `KeyboardService`; tapping
  an entry pastes it. Not persisted across app restarts yet (that's
  Stage 6's DataStore work) — it's in-memory only for now.

I deliberately did **not** add Media or Macro tabs — those aren't real
yet (their stages haven't been built), and a tab that does nothing on
tap is exactly the "fake interface" this project's own rules forbid.

## What I could NOT do myself
I do not have an Android SDK, an emulator, or a physical device, and I
have no network access. I cannot build, install, or test this on a
device myself — **you are the one who needs to do that**, per the
project's own rule that no stage advances without a real on-device test.

## Option A — build via GitHub Actions (`.github/workflows/build.yml`)
Push this project to a GitHub repo and the workflow runs automatically
on every push/PR to `main`/`master` (or trigger it manually from the
Actions tab — `workflow_dispatch` is enabled). It sets up JDK 17 +
Android SDK 35, provisions Gradle 8.7 directly (no committed
`gradlew`/wrapper jar — see the workflow file for why), builds both
debug and release-unsigned APKs, and uploads them as artifacts.
**Use the `debug` artifact to install on your device** — the release
one is unsigned and Android will refuse to install it until a real
signing config exists (a later-stage concern, not now).

## Option B — build locally in Android Studio
Open this folder as an existing project; it generates the Gradle
wrapper automatically on sync. Run the `app` module directly, or
Build → Generate Signed Bundle/APK → APK → debug.

## How to test on device
1. Install the debug APK.
2. Open the app once — it walks you to Settings → enable the keyboard,
   and lets you pick it directly.
3. Open any app with a text field.
4. **Stage 1+2 checks** (should already pass): typing, shift, backspace,
   enter, globe/language switch, mechanical key visuals/press animation,
   haptics/sound respecting system settings, no crashes.
5. **Stage 3 checks:**
   - Tap the "FN" tab in the toolbar — the keyboard switches to the
     computer-keys layout.
   - Arrow keys move the cursor in a text field.
   - Home/End jump to line start/end; Page Up/Down scroll where the app
     supports it.
   - Tab inserts a tab / moves focus, depending on the app.
   - Tap Ctrl, then tap a letter (e.g. C) in a field with selected text
     in an app that supports keyboard shortcuts (e.g. a browser address
     bar, a code editor app) — verify the shortcut actually fires
     (copy/paste/etc.), not just a "c" being typed.
   - F1–F12 send without crashing (actual effect depends entirely on
     whether the app you're in listens for function keys — most won't
     visibly react, and that's expected, not a bug).
   - Esc/Win/Menu/PrintScreen/Pause/ScrollLock/Insert/Delete send
     without crashing (visible effect, if any, is app-dependent).
6. **Stage 4 checks:**
   - PC/FN tabs switch layers and the active tab is visually highlighted.
   - Tapping the collapse chevron smoothly hides/shows the toolbar.
   - Globe icon in the toolbar switches input method, same as the key.
   - Gear icon opens the app's settings/enable screen.
   - The status dot always reads "غير متصل" — expected, not a bug.
   - 😊 icon opens the emoji grid; tapping an emoji types it.
   - 📋 icon opens clipboard history; copy some text in any app first,
     then check it appears in the list; tapping it pastes into the
     keyboard's current field.
   - No crashes switching tabs, opening/closing panels, or combining
     Stage 3 keys with the toolbar.

## Crash history (Stage 1/2 — resolved)
Two real, confirmed bugs were found and fixed via on-device stack
traces surfaced by the `CrashReporter` notification
(`core/ime/CrashReporter.kt`), which is still installed and will keep
catching anything new the same way:

1. **`ViewTreeLifecycleOwner not found`** — `InputMethodService`'s
   window is internally a `Dialog`. Compose's Recomposer lookup starts
   from that Dialog's `decorView`, not from the `ComposeView` we return
   — so the owner has to be attached to the `decorView`, not just the
   nested `ComposeView`. Fixed in `ComposeInputMethodService.onCreate()`.
2. **`ViewTreeSavedStateRegistryOwner` missing** — `AndroidComposeView`
   checks for this on every attach regardless of whether the app uses
   `rememberSaveable()`. Fixed the same way, attached to the `decorView`.

## Known, intentional limitations at this point
- No layers beyond Standard/FN (Media, Programming, Gaming, Macro,
  User Custom) — Stage 5.
- No persisted customization (key size/position/color/icon, themes,
  sound/haptic toggles) — Stage 6; everything today resets on restart.
- No Macro engine — Stage 7.
- No real RGB simulation / switch-sound themes — Stage 8.
- No dedicated Gaming layer (WASD emphasis, profiles) — Stage 9.
- No Computer Mode (Bluetooth/WiFi/WebSocket to a PC) — final stage.
- Clipboard history is in-memory only; clears on keyboard restart.

## Next step
Rebuild via GitHub Actions, install the debug APK, and run through the
full checklist above (Stages 1–4). Report back what works and what
doesn't — specific and exact, same as before. If anything crashes, the
`CrashReporter` notification is still active; send the expanded
screenshot the same way it worked last time.

## Stage 5 + Stage 6 addition

**Stage 5 — layer system.** A real, extensible registry
(`ui/keyboard/LayerRegistry.kt`) maps each `KeyboardMode` to its rows.
Four modes are live now: Standard (PC), Function (FN), Programming
(`{ }` — real symbols: brackets, operators, quotes, an indent key),
Gaming (`WASD` — a basic but real WASD-emphasized layout). Macro and
"User Custom" layers from the spec are deliberately **not** added as
tabs yet — the macro engine (Stage 7) and a real per-key editor don't
exist, and a tab with nothing behind it would be fake UI. Adding either
later is a one-line change to `LayerRegistry` plus one new layout file —
that's what "قابل للتوسعة" means here in practice.

**Stage 6 — customization, persisted via DataStore.** Real, saved,
applied-on-next-frame settings, reachable from the toolbar's gear icon
(`ui/settings/SettingsScreen.kt` + `SettingsActivity`):
- Key height (34–56dp slider) — changes the actual rendered key size.
- Key-press sound toggle — gates the same `AudioManager` call from
  Stage 2 instead of always firing it.
- Haptic toggle — same idea, gates the `performHapticFeedback` call.
- Accent color (5 swatches) — recolors the whole theme, including the
  Enter key and active-modifier highlighting.

All four persist through `SettingsRepository`
(`data/repository/SettingsRepository.kt`) into `DataStore` and survive
an app/keyboard restart.

**Honest scope note:** the spec's full Stage 6 vision includes
per-key position, size, icon, and function remapping via what would
need to be a real drag-and-drop editor — building that and cutting
every corner to fit it into this pass would have meant fake or
half-working UI, which this project's own rules forbid. What's here is
smaller than that vision but everything in it is completely real end
to end. The per-key editor is the natural next increment on top of this
foundation, not a redesign of it.

## Updated test checklist — Stage 5 + 6
- Toolbar now shows 4 tabs (PC / FN / `{ }` / WASD) — tap each, confirm
  the key rows actually change and the highlighted tab matches.
- Programming layer: tap a few symbols, confirm they type correctly;
  tap the indent key (⇥⇥), confirm it inserts two spaces.
- Gaming layer: WASD keys work as regular letters; number row 1-5 types
  digits; Esc/Tab/Ctrl/Shift/Backspace all still work as expected.
- Open Settings (gear icon in toolbar):
  - Drag the key-height slider — go back to the keyboard, keys should
    visibly be a different size.
  - Toggle sound off — type a few keys, confirm silence regardless of
    the system touch-sound setting.
  - Toggle haptic off — same check for vibration.
  - Tap a different accent color — go back to the keyboard, the Enter
    key and any active Shift/Ctrl/Alt highlight should use the new color.
  - **Force-stop the app (or reboot), reopen the keyboard** — all four
    settings should still be exactly as you left them (this is the
    actual DataStore persistence test, not just live state).
