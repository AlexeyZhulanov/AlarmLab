package com.example.alarm;

import android.content.Context;
import android.media.AudioManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.CoroutineWorker;
import androidx.work.WorkerParameters;

import kotlin.coroutines.Continuation;

public class VolumeAdjusterWorker extends CoroutineWorker {

    public VolumeAdjusterWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Nullable
    @Override
    public Object doWork(@NonNull Continuation<? super Result> continuation) {
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

        // Get current alarm volume
        int currentAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);

        // Set music volume to match alarm volume
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentAlarmVolume, 0);

        return Result.success();
    }
}
