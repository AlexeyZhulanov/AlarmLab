package com.example.alarm.di;

import android.app.AlarmManager;
import android.content.Context;
import androidx.room.Room;
import com.example.alarm.model.AlarmService;
import com.example.alarm.room.AlarmDao;
import com.example.alarm.room.AppDatabase;
import com.example.alarm.room.SettingsDao;
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AlarmModule {

    @Provides
    @Singleton
    public AppDatabase provideDatabase(@ApplicationContext Context appContext) {
        return Room.databaseBuilder(appContext, AppDatabase.class, "database.db")
                .createFromAsset("init_db.db")
                .build();
    }

    @Provides
    @Singleton
    public AlarmDao provideAlarmDao(AppDatabase appDatabase) {
        return appDatabase.getAlarmDao();
    }

    @Provides
    @Singleton
    public SettingsDao provideSettingsDao(AppDatabase appDatabase) {
        return appDatabase.getSettingsDao();
    }

    @Provides
    @Singleton
    public AlarmService provideAlarmService(AlarmDao alarmDao, SettingsDao settingsDao) {
        return new AlarmService(alarmDao, settingsDao);
    }

    @Provides
    public AlarmManager provideAlarmManager(@ApplicationContext Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }
}
