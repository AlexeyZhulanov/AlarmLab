package com.example.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class AlarmForegroundService : Service() {
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val alarmName = intent.getStringExtra("alarmName")
        val alarmId = intent.getLongExtra("alarmId", 0)

        createNotificationChannel()
        val notification = buildNotification(alarmName, alarmId)
        startForeground(1, notification)

        val signalIntent = Intent(this, SignalActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra("alarmName", alarmName)
            putExtra("alarmId", alarmId)
        }
        startActivity(signalIntent)

        stopSelf() // Останавливаем сервис, так как он больше не нужен
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            "alarm_channel",
            "Alarm Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(alarmName: String?, alarmId: Long): Notification {
        return NotificationCompat.Builder(this, "alarm_channel")
            .setContentTitle("Alarm")
            .setContentText(alarmName)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }
}
