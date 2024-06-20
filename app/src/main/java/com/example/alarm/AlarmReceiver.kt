package com.example.alarm

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.alarm.model.AppVisibilityTracker


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("testWork", "It works!")
        val name = intent.getStringExtra("alarmName")
        val id = intent.getLongExtra("alarmId", 0)
        if (!AppVisibilityTracker.isAppRunning()) {
            val signalIntent = Intent(context, SignalActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("alarmId", id)
                putExtra("alarmName", name)
            }
            context.applicationContext.startActivity(signalIntent)

        }
        else {
//            val localBroadCastIntent = Intent(LOCAL_BROADCAST_KEY)
//            localBroadCastIntent.putExtra("alarmName",name?:"")
//            localBroadCastIntent.putExtra("alarmId",id)
//            Handler(Looper.getMainLooper()).post {
//                LocalBroadcastManager.getInstance(context).sendBroadcast(localBroadCastIntent)
//            }
            showBasicTurnOffNotification(context, name, id)
        }
    }

    private fun showBasicTurnOffNotification(context: Context, name: String?, id: Long) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "basic_channel_id"
        val channelName = "Basic Notifications"

        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)

        // Intent to handle the "turn off" action
        val turnOffIntent = Intent(LOCAL_BROADCAST_KEY3) // Replace with your actual receiver
        turnOffIntent.putExtra("alarmIdOff",id)
        val turnOffPendingIntent = PendingIntent.getBroadcast(context, 0, turnOffIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        Log.d("testWorkBasic", "It works!")
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("App is Running")
            .setContentText("Tap to turn off")
            .addAction(R.drawable.ic_clear, "Turn Off", turnOffPendingIntent) // Replace icons as needed
            .setAutoCancel(true)

        notificationManager.notify(2, notificationBuilder.build())
    }

}

private fun showFullScreenSignalNotification(context: Context, originalIntent: Intent) {
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val channelId = "signal_channel_id"
    val channelName = "Signal Notifications"

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)
    }

    // Intent to launch SignalActivity and display SignalFragment
    val signalIntent = Intent(context, SignalActivity::class.java).apply {
        // Pass data from the original Intent to SignalActivity
        putExtras(originalIntent)
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        signalIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notificationBuilder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your icon
        .setContentTitle("Incoming Signal") // Customize as needed
        .setContentText("Tap to view") // Customize as needed
        .setPriority(NotificationCompat.PRIORITY_HIGH) // Set high priority
        .setCategory(NotificationCompat.CATEGORY_CALL) // Use a relevant category
        .setFullScreenIntent(pendingIntent, true) // Use full-screen intent
        .setAutoCancel(true)

    notificationManager.notify(1, notificationBuilder.build())
}

const val LOCAL_BROADCAST_KEY = "alarm_start"
const val LOCAL_BROADCAST_KEY3 = "alarm_off"