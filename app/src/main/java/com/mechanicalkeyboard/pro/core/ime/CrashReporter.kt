package com.mechanicalkeyboard.pro.core.ime

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Installs a process-wide uncaught-exception handler that surfaces the
 * real crash to the *user's device* — as a notification with the full
 * stack trace — instead of only to a logcat neither of us can currently
 * read. Purely diagnostic: it does not attempt to prevent the crash or
 * keep the process alive, it just makes sure the actual exception text
 * is visible somewhere the person testing on the device can copy or
 * screenshot, before the system's normal crash handling takes over.
 *
 * Safe to call more than once — only installs itself the first time.
 */
object CrashReporter {
    private const val CHANNEL_ID = "crash_diagnostics"
    private const val NOTIFICATION_ID = 9001
    private const val TAG = "MechanicalKeyboardPro"

    private var installed = false

    @Synchronized
    fun install(context: Context) {
        if (installed) return
        installed = true

        val appContext = context.applicationContext
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e(TAG, "Uncaught exception on thread ${thread.name}", throwable)
                showCrashNotification(appContext, throwable)
            } catch (reportingFailure: Throwable) {
                // The reporter itself must never be the reason the crash
                // handler throws again — if notification delivery fails,
                // just fall through to the previous/system handler below.
                Log.e(TAG, "Failed to report crash", reportingFailure)
            }
            previousHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun showCrashNotification(context: Context, throwable: Throwable) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Crash diagnostics",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows the real error text when the keyboard crashes, for debugging."
            }
            manager.createNotificationChannel(channel)
        }

        val stringWriter = StringWriter()
        throwable.printStackTrace(PrintWriter(stringWriter))
        val fullTrace = stringWriter.toString()
        // Notifications can't show unlimited text; keep the most useful
        // part (exception type/message + top of the stack).
        val trimmedTrace = fullTrace.take(3500)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("Mechanical Keyboard Pro crashed")
            .setContentText(throwable.message ?: throwable.toString())
            .setStyle(NotificationCompat.BigTextStyle().bigText(trimmedTrace))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .build()

        manager.notify(NOTIFICATION_ID, notification)
    }
}
