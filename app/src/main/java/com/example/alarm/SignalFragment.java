package com.example.alarm;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.util.Calendar;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.example.alarm.databinding.FragmentSignalBinding;
import com.example.alarm.model.Alarm;
import com.example.alarm.model.MyAlarmManager;
import com.example.alarm.model.Settings;
import com.ncorti.slidetoact.SlideToActView;

import java.util.concurrent.Executors;

public class SignalFragment extends Fragment {

    private final Alarm alarmPlug;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private AudioManager audioManager;
    private AudioFocusRequest focusRequest;
    private int originalMusicVolume;
    private Settings settings;
    private static final String LOCAL_BROADCAST_KEY3 = "alarm_turnoff";
    private BroadcastReceiver turnOffReceiver;

    public SignalFragment(String name, long id, Settings settings) {
        Alarm alarm = new Alarm(id);
        alarm.setName(name);
        this.alarmPlug = alarm;
        this.settings = settings;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("testSignalFrag", "works");
        FragmentSignalBinding binding = FragmentSignalBinding.inflate(inflater, container, false);

        // WorkManager with Data.Builder (equivalent of WorkDataOf in Kotlin)
        Data inputData = new Data.Builder()
                .putLong("alarmId", alarmPlug.getId())
                .putInt("enabled", 0)
                .build();

        OneTimeWorkRequest updateWorkRequest = new OneTimeWorkRequest.Builder(AlarmWorker.class)
                .setInputData(inputData)
                .build();

        WorkManager.getInstance(requireContext()).enqueue(updateWorkRequest);
        selectMelody(settings);
        if (settings.getVibration() == 1) {
            startVibrator();
        }

        String currentTime = Calendar.getInstance().getTime().toString();
        String[] str = currentTime.split(" ");
        String date = str[0] + " " + str[1] + " " + str[2];
        String[] tmpTime = str[3].split(":");
        String time = tmpTime[0] + ":" + tmpTime[1];
        binding.currentDateTextView.setText(date);
        binding.currentTimeTextView.setText(time);
        binding.nameTextView.setText(alarmPlug.getName());

        if (settings.getRepetitions() <= 0) {
            binding.repeatButton.setVisibility(View.GONE);
        } else {
            binding.pulsator.start();
            binding.repeatButton.setOnClickListener(v -> dropAndRepeatFragment());
        }

        binding.slideButton.setOnSlideCompleteListener(view -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                MyAlarmManager alarmManager = new MyAlarmManager(requireContext(), alarmPlug, new Settings(0));
                alarmManager.endProcess();
            });
            requireActivity().onBackPressed();
        });

        return binding.getRoot();
    }

    public void dropAndRepeatFragment() {
        if (settings.getRepetitions() > 0) {
            settings.setRepetitions(settings.getRepetitions() - 1);
            Executors.newSingleThreadExecutor().execute(() -> {
                Context context = requireContextOrNull();
                if (context == null) {
                    Log.e("dropAndRepeatFragment", "Context is null");
                    return;
                }
                MyAlarmManager alarmManager = new MyAlarmManager(context, alarmPlug, new Settings(0));
                alarmManager.endProcess();
                alarmManager.repeatProcess();
                showTurnOffNotification();
                requireActivity().onBackPressed();
            });
        }
    }

    private Context requireContextOrNull() {
        return isAdded() ? requireContext() : null;
    }

    @SuppressLint("LaunchActivityFromNotification")
    public void showTurnOffNotification() {
        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "basic_channel_id";
        NotificationChannel channel = new NotificationChannel(channelId, "Basic Notifications", NotificationManager.IMPORTANCE_LOW);

        notificationManager.createNotificationChannel(channel);

        PendingIntent turnOffPendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                0,
                new Intent(LOCAL_BROADCAST_KEY3).putExtra("alarmId", settings.getId()).putExtra("notificationId", 3),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(requireContext(), channelId)
                .setSmallIcon(R.mipmap.ic_alarm_adaptive_fore)
                .setContentTitle("Будильник")
                .setContentText("Alarm notification")
                .addAction(R.drawable.ic_clear, "Turn Off", turnOffPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        notificationManager.notify(3, notificationBuilder.build());

        // Register the broadcast receiver
        turnOffReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long alarmId = intent.getLongExtra("alarmId", -1);
                if (alarmId == settings.getId()) {
                    // Handle the alarm turn off here
                    Log.d("SignalFragment", "Alarm turned off");
                    notificationManager.cancel(3); // Cancel notification
                    // Implement other logic to stop the alarm
                }
            }
        };
        requireContext().registerReceiver(turnOffReceiver, new IntentFilter(LOCAL_BROADCAST_KEY3));
    }

    private void selectMelody(Settings settings) {
        audioManager = (AudioManager) requireContext().getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.default_signal1);

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        mediaPlayer.setAudioAttributes(audioAttributes);
        mediaPlayer.setLooping(true);

        originalMusicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int currentAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentAlarmVolume, 0);

        focusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .build();

        audioManager.requestAudioFocus(focusRequest);

        mediaPlayer.start();
    }

    private void startVibrator() {
        vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        VibrationEffect vibrationEffect = VibrationEffect.createWaveform(new long[]{0, 500, 1000}, 0);
        vibrator.vibrate(vibrationEffect);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        if (vibrator != null) {
            vibrator.cancel();
        }
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalMusicVolume, 0);
        audioManager.abandonAudioFocusRequest(focusRequest);
        if (turnOffReceiver != null) {
            requireContext().unregisterReceiver(turnOffReceiver);
        }
    }
}
