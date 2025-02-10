package com.mmt.app

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.database.CursorWindow
import android.os.Build
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
@HiltAndroidApp
class BaseApplication : Application() {


    override fun onCreate() {
        super.onCreate()

        // Open Channel with Notification Service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create Notification Channel
            val channel = NotificationChannel(
                "AutoBackupService.CHANNEL_ID",
                "App Update Watching Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Increase the CursorWindow size to 100 MB
        try {
            val field = CursorWindow::class.java.getDeclaredField("sCursorWindowSize")
            field.isAccessible = true
            field[null] = 100 * 1024 * 1024
        } catch (e: Exception) {
            Timber.e(e)
        }
    }
}