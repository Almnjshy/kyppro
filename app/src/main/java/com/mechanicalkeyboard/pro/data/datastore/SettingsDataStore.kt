package com.mechanicalkeyboard.pro.data.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

/** Single DataStore instance for the whole app, keyed by this file name. */
val Context.keyboardSettingsDataStore by preferencesDataStore(name = "keyboard_settings")
