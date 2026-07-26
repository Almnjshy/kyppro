package com.mechanicalkeyboard.pro

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.mechanicalkeyboard.pro.core.ime.CrashReporter
import com.mechanicalkeyboard.pro.ui.theme.MechanicalKeyboardTheme

/**
 * This screen is NOT the keyboard. It only helps the user do the two
 * manual steps Android requires before any third-party IME can be used:
 * 1) enable it in system settings, 2) pick it as the active keyboard.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CrashReporter.install(this)
        setContent {
            MechanicalKeyboardTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    EnableKeyboardScreen(
                        onOpenSettings = {
                            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                        },
                        onPickKeyboard = {
                            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.showInputMethodPicker()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EnableKeyboardScreen(
    onOpenSettings: () -> Unit,
    onPickKeyboard: () -> Unit
) {
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* Either way, the app still works — this only affects whether a crash log is visible. */ }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.main_activity_title),
            style = MaterialTheme.typography.headlineSmall
        )
        Text(stringResource(R.string.step_1))
        Text(stringResource(R.string.step_2))
        Text(stringResource(R.string.step_3))

        Button(onClick = onOpenSettings) {
            Text(stringResource(R.string.btn_open_ime_settings))
        }
        Button(onClick = onPickKeyboard) {
            Text(stringResource(R.string.btn_pick_ime))
        }
    }
}
