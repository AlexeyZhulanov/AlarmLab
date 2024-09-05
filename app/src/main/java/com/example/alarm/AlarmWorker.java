package com.example.alarm;

import android.content.Context;
import androidx.room.Room;
import androidx.work.CoroutineWorker;
import androidx.work.WorkerParameters;

public class AlarmWorker extends CoroutineWorker {

    public AlarmWorker(Context appContext, WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @Override
    public Result doWork() {
        long alarmId = getInputData().getLong("alarmId", 0L);
        int enabled = getInputData().getInt("enabled", 0);

        AppDatabase database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database.db").build();
        AlarmService alarmService = new AlarmService(database.getAlarmDao(), database.getSettingsDao());

        alarmService.updateEnabled(alarmId, enabled);

        return Result.success();
    }
}
