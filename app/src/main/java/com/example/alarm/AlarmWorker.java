package com.example.alarm;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Room;
import androidx.work.CoroutineWorker;
import androidx.work.WorkerParameters;

import com.example.alarm.model.AlarmService;
import com.example.alarm.room.AppDatabase;

import kotlin.coroutines.Continuation;

public class AlarmWorker extends CoroutineWorker {

    public AlarmWorker(Context appContext, WorkerParameters workerParams) {
        super(appContext, workerParams);
    }


    @Nullable
    @Override
    public Object doWork(@NonNull Continuation<? super Result> continuation) {
        long alarmId = getInputData().getLong("alarmId", 0L);
        int enabled = getInputData().getInt("enabled", 0);

        AppDatabase database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database.db").build();
        AlarmService alarmService = new AlarmService(database.getAlarmDao(), database.getSettingsDao());

        alarmService.updateEnabled(alarmId, enabled);

        return Result.success();
    }
}
