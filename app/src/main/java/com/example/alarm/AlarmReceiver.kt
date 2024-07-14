package com.example.alarm

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.IntentCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.alarm.model.Alarm
import com.example.alarm.model.AppVisibilityTracker
import com.example.alarm.model.MyAlarmManager
import com.example.alarm.model.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class AlarmReceiver : BroadcastReceiver() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioManager: AudioManager
    private lateinit var focusRequest: AudioFocusRequest
    private var originalMusicVolume: Int = 0
    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    override fun onReceive(context: Context, intent: Intent) {
        AppVisibilityTracker.initialize(context)
        val name = intent.getStringExtra("alarmName")
        val id = intent.getLongExtra("alarmId", 0)
        val settings = IntentCompat.getParcelableExtra(intent, "settings", Settings::class.java)
        if (!AppVisibilityTracker.isScreenOn()) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            @Suppress("DEPRECATION") val wakeLock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "MyApp:AlarmWakeLockTag"
            )
            wakeLock.acquire(10 * 60 * 1000L /* 10 minutes */)

            val signalIntent = Intent(context, SignalActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("alarmId", id)
                putExtra("alarmName", name)
                putExtra("settings", settings)
            }
            context.applicationContext.startActivity(signalIntent)

            // Release wakeLock after starting the activity
            wakeLock.release()
        } else {
            showBasicTurnOffNotification(context, id, settings)
        }
    }

    @SuppressLint("LaunchActivityFromNotification")
    private fun showBasicTurnOffNotification(context: Context, id: Long, settings: Settings?) {
        val notificationManager =
            context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "basic_channel_id"
        val channelName = "Basic Notifications"

        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                enableLights(true)
                lightColor = android.graphics.Color.RED
                enableVibration(true)
                description = "Alarm notification"
            }
        notificationManager.createNotificationChannel(channel)

        val updateWorkRequest = OneTimeWorkRequestBuilder<AlarmWorker>()
            .setInputData(workDataOf("alarmId" to id, "enabled" to 0))
            .build()

        WorkManager.getInstance(context).enqueue(updateWorkRequest)


        selectMelody(settings, context)
        mediaPlayer.isLooping = true
        mediaPlayer.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        audioManager = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // Store original music volume
        originalMusicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        // Get current alarm volume
        val currentAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM)

        // Set music volume to match alarm volume
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentAlarmVolume, 0)

        // Build AudioFocusRequest
        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            ).build()

        val filter = IntentFilter(LOCAL_BROADCAST_KEY2)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.applicationContext.registerReceiver(turnOffReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        }
        val turnOffIntent = Intent(LOCAL_BROADCAST_KEY2).apply {
            putExtra("alarmId", id)
            putExtra("notificationId",2)
        }
        val turnOffPendingIntent = PendingIntent.getBroadcast(
            context.applicationContext,
            0,
            turnOffIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notificationBuilder = NotificationCompat.Builder(context.applicationContext, channelId)
            .setSmallIcon(R.mipmap.ic_alarm_adaptive_fore)
            .setContentTitle("Будильник")
            .setContentText("Нажмите, чтобы отключить будильник")
            .addAction(R.drawable.ic_clear, "Turn Off", turnOffPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setFullScreenIntent(turnOffPendingIntent, true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
        audioManager.requestAudioFocus(focusRequest)
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener {
            mediaPlayer.release() // Clear resources mediaPlayer
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalMusicVolume, 0) // Restore original music volume
            audioManager.abandonAudioFocusRequest(focusRequest) // Abandon audio focus request
        }
        notificationManager.notify(2, notificationBuilder.build())
    }

    private fun selectMelody(settings: Settings?, context: Context) {
        mediaPlayer = when(settings?.melody ?: -1) {
            context.getString(R.string.melody1) -> MediaPlayer.create(context, R.raw.default_signal1)
            context.getString(R.string.melody2) -> MediaPlayer.create(context, R.raw.default_signal2)
            context.getString(R.string.melody3) -> MediaPlayer.create(context, R.raw.default_signal3)
            context.getString(R.string.melody4) -> MediaPlayer.create(context, R.raw.default_signal4)
            context.getString(R.string.melody5) -> MediaPlayer.create(context, R.raw.default_signal5)
            context.getString(R.string.melody6) -> MediaPlayer.create(context, R.raw.signal)
            context.getString(R.string.melody7) -> MediaPlayer.create(context, R.raw.banjo_signal)
            context.getString(R.string.melody8) -> MediaPlayer.create(context, R.raw.morning_signal)
            context.getString(R.string.melody9) -> MediaPlayer.create(context, R.raw.simple_signal)
            context.getString(R.string.melody10) -> MediaPlayer.create(context, R.raw.fitness_signal)
            context.getString(R.string.melody11) -> MediaPlayer.create(context, R.raw.medieval_signal)
            context.getString(R.string.melody12) -> MediaPlayer.create(context, R.raw.introduction_signal)
            else -> MediaPlayer.create(context, R.raw.default_signal1)
        }
    }

    private val turnOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val cont = context.applicationContext
            Log.d("testOff", "Yes")
            mediaPlayer.stop()
            mediaPlayer.release() // Clear resources mediaPlayer
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalMusicVolume, 0) // Restore original music volume
            audioManager.abandonAudioFocusRequest(focusRequest) // Abandon audio focus request
            uiScope.launch {
                val alarmId = intent.getLongExtra("alarmId", 0)
                val notificationId = intent.getIntExtra("notificationId", -1)
                val alarmPlug = Alarm(alarmId)
                MyAlarmManager(cont, alarmPlug, Settings(0)).endProcess()
                val notificationManager =
                    cont.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(notificationId)
            }
        }
    }

}