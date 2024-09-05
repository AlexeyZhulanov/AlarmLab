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
import android.icu.util.ULocale;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.work.OneTimeWorkRequestBuilder;
import androidx.work.WorkManager;
import androidx.work.workDataOf;
import com.example.alarm.databinding.FragmentSignalBinding;
import com.ncorti.slidetoact.SlideToActView;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.Job;
import kotlinx.coroutines.launch;

public class SignalFragment extends Fragment {

    private final Alarm alarmPlug;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private AudioManager audioManager;
    private AudioFocusRequest focusRequest;
    private int originalMusicVolume;

    private final Job job = new Job();
    private final CoroutineScope uiScope = CoroutineScope(Dispatchers.Main + job);

    public SignalFragment(String name, long id, Settings settings) {
        this.alarmPlug = new Alarm(id, name);
        // Initialize additional fields if needed here...
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d("testSignalFrag", "works");
        FragmentSignalBinding binding = FragmentSignalBinding.inflate(inflater, container, false);

        OneTimeWorkRequest updateWorkRequest = OneTimeWorkRequestBuilder<AlarmWorker>()
                .setInputData(workDataOf("alarmId", alarmPlug.id, "enabled", 0))
                .build();

        WorkManager.getInstance(requireContext()).enqueue(updateWorkRequest);
        selectMelody(settings);  // Ensure settings is passed or set a default
        if(settings.vibration == 1) {
            startVibrator();
        }

        // Handle date and time display
        String currentTime = Calendar.getInstance().getTime().toString();  // Adjust as needed
        binding.currentTimeTextView.setText(currentTime);
        binding.nameTextView.setText(alarmPlug.name);

        // Repeat button functionality
        if(settings.repetitions <= 0) {
            binding.repeatButton.setVisibility(View.GONE);
        } else {
            binding.pulsator.start();
            binding.repeatButton.setOnClickListener(v -> dropAndRepeatFragment());
        }

        binding.slideButton.onSlideCompleteListener = new SlideToActView.OnSlideCompleteListener() {
            @Override
            public void onSlideComplete(SlideToActView view) {
                uiScope.launch(() -> {
                    MyAlarmManager alarmManager = new MyAlarmManager(requireContext(), alarmPlug, new Settings(0));
                    alarmManager.endProcess();
                });
                requireActivity().onBackPressed();
            }
        };

        return binding.getRoot();
    }

    public void dropAndRepeatFragment() {
        if (settings.repetitions > 0) {
            settings.repetitions--;
            uiScope.launch(() -> {
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
        return isAdded ? requireContext() : null;
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
                new Intent(LOCAL_BROADCAST_KEY3).putExtra("alarmId", id).putExtra("notificationId", 3),
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
    }

    private void selectMelody(Settings settings) {
        audioManager = (AudioManager) requireContext().getSystemService(Context.AUDIO_SERVICE);
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.default_signal1); // Update as needed based on settings

        // Initialize audio attributes, focus requests, and configure the media player...
        mediaPlayer.setLooping(true);

        // Store original music volume
        originalMusicVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mediaPlayer.start();
    }

    private void startVibrator() {
        vibrator = (Vibrator) requireContext().getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 100, 300, 200, 250, 300, 200, 400, 150, 300, 150, 200};
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mediaPlayer.release();
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalMusicVolume, 0);
        if (settings.vibration == 1) {
            vibrator.cancel();
        }
    }

    private final BroadcastReceiver turnOffReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Handle turning off alarm
        }
    };
}
