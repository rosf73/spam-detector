package com.spamdetector.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.spamdetector.R
import com.spamdetector.utils.Preferences

class ObserverService : Service() {
    companion object{
        private var isRunning = false
        fun isServiceRunning(): Boolean {
            return isRunning
        }
    }

    override fun onBind(intent: Intent?): IBinder? { return null }

    override fun onCreate() {
        super.onCreate()
        isRunning = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val strId = getString(R.string.observer_channel_id)
            val strTitle = getString(R.string.app_name)
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            var channel = notificationManager.getNotificationChannel(strId)
            if (channel == null) {
                channel = NotificationChannel(strId, strTitle, NotificationManager.IMPORTANCE_HIGH)
                notificationManager.createNotificationChannel(channel)
            }
            val notification: Notification = NotificationCompat.Builder(this, strId).build()
            startForeground(2, notification)
        }

        Preferences.init(applicationContext)
    }
}