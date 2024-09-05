package com.example.alarm;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.content.IntentCompat;
import androidx.work.OneTimeWorkRequestBuilder;
import androidx.work.WorkManager;
import androidx.work.WorkData;

import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.Job;
import kotlinx.coroutines.launch;

public class AlarmReceiver extends BroadcastReceiver {

    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private AudioFocusRequest focusRequest;
    private int originalMusicVolume = 0;
    private Job job = new Job();
    private CoroutineScope uiScope = new CoroutineScope(Dispatchers.Main.plus(job));

    @SuppressLint("Wakelock")
    @Override
    public void onReceive(Context context, Intent intent) {
        AppVisibilityTracker.initialize(context);
        String name = intent.getStringExtra("alarmName");
        long id = intent.getLongExtra("alarmId", 0);
        Settings settings = IntentCompat.getParcelableExtra(intent, "settings", Settings.class);

        if (!AppVisibilityTracker.isScreenOn()) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            @SuppressWarnings("deprecation")
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                    PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE,
                    "MyApp:AlarmWakeLockTag"
            );
            wakeLock.acquire(180000); // 3 minutes

            Intent signalIntent = new Intent(context, SignalActivity.class);
            signalIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            signalIntent.putExtra("alarmId", id);
            signalIntent.putExtra("alarmName", name);
            signalIntent.putExtra("settings", settings);
            context.getApplicationContext().startActivity(signalIntent);

            wakeLock.release();
        } else {
            showBasicTurnOffNotification(context, id, settings);
        }
    }

    @SuppressLint("LaunchActivityFromNotification")
    private void showBasicTurnOffNotification(Context context, long id, Settings settings) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "basic_channel_id";
        String channelName = "Basic Notifications";

        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.enableLights(true);
        channel.setLightColor(android.graphics.Color.RED);
        channel.enableVibration(true);
        channel.setDescription("Alarm notification");
        notificationManager.createNotificationChannel(channel);

        WorkData workData = new WorkData.Builder().putLong("alarmId", id).putInt("enabled", 0).build();
        OneTimeWorkRequestBuilder<AlarmWorker> updateWorkRequest = new OneTimeWorkRequestBuilder<AlarmWorker>().setInputData(workData);
        WorkManager.getInstance(context).enqueue(updateWorkRequest);

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        originalMusicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                .build();

        int audioFocusResult = audioManager.requestAudioFocus(focusRequest);

        if (audioFocusResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            OneTimeWorkRequestBuilder<VolumeAdjusterWorker> volumeAdjusterRequest = new OneTimeWorkRequestBuilder<VolumeAdjusterWorker>();
            WorkManager.getInstance(context).enqueue(volumeAdjusterRequest);
            selectMelody(settings, context);
            mediaPlayer.setLooping(true);
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build());
            mediaPlayer.start();
        } else {
            Log.d("AudioFocus", "Failed to gain audio focus");
        }

        IntentFilter filter = new IntentFilter(LOCAL_BROADCAST_KEY2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getApplicationContext().registerReceiver(turnOffReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        }
        Intent turnOffIntent = new Intent(LOCAL_BROADCAST_KEY2);
        turnOffIntent.putExtra("alarmId", id);
        turnOffIntent.putExtra("notificationId", 2);
        PendingIntent turnOffPendingIntent = PendingIntent.getBroadcast(context, 0, turnOffIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_alarm_adaptive_fore)
                .setContentTitle("Будильник")
                .setContentText("Нажмите, чтобы отключить будильник")
                .addAction(R.drawable.ic_clear, "Turn Off", turnOffPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setFullScreenIntent(turnOffPendingIntent, true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true);

        notificationManager.notify(2, notificationBuilder.build());
    }

    private void selectMelody(Settings settings, Context context) {
        int melodyResource = R.raw.default_signal1; // Default melody
        String melody = settings != null ? settings.melody : null;

        switch (melody) {
            case "melody1":
                melodyResource = R.raw.default_signal1;
                break;
            case "melody2":
                melodyResource = R.raw.default_signal2;
                break;
            case "melody3":
                melodyResource = R.raw.default_signal3;
                break;
            case "melody4":
                melodyResource = R.raw.default_signal4;
                break;
            case "melody5":
                melodyResource = R.raw.default_signal5;
                break;
            case "melody6":
                melodyResource = R.raw.signal;
                break;
            case "melody7":
                melodyResource = R.raw.banjo_signal;
                break;
            case "melody8":
                melodyResource = R.raw.morning_signal;
                break;
            case "melody9":
                melodyResource = R.raw.simple_signal;
                break;
            case "melody10":
                melodyResource = R.raw.fitness_signal;
                break;
            case "melody11":
                melodyResource = R.raw.medieval_signal;
                break;
            case "melody12":
                melodyResource = R.raw.introduction_signal;
                break;
            default:
                melodyResource = R.raw.default_signal1;
                break;
        }

        mediaPlayer = MediaPlayer.create(context, melodyResource);
    }

    private BroadcastReceiver turnOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mediaPlayer.stop();
            mediaPlayer.release();
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalMusicVolume, 0);
            audioManager.abandonAudioFocusRequest(focusRequest);

            uiScope.launch(() -> {
                long alarmId = intent.getLongExtra("alarmId", 0);
                int notificationId = intent.getIntExtra("notificationId", -1);
                Alarm alarmPlug = new Alarm(alarmId);
                new MyAlarmManager(context, alarmPlug, new Settings(0)).endProcess();
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(notificationId);
            });
        }
    };
}

const String LOCAL_BROADCAST_KEY2 = "alarm_update";
