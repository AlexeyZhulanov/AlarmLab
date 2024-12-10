package com.example.alarm.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(
        version = 1,
        entities = {
                AlarmDbEntity.class,
                SettingsDbEntity.class
        }
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract AlarmDao getAlarmDao();

    public abstract SettingsDao getSettingsDao();
}
