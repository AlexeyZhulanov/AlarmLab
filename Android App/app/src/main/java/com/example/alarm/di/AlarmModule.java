package com.example.alarm.di;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.room.Room;
import com.example.alarm.model.AlarmService;
import com.example.alarm.model.RetrofitService;
import com.example.alarm.retrofit.source.AlarmSource;
import com.example.alarm.retrofit.source.SourceProviderHolder;
import com.example.alarm.retrofit.source.base.SourcesProvider;
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
    private static final String APP_PREFERENCES = "APP_PREFERENCES";

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
    @Singleton
    public SharedPreferences provideSharedPreferences(@ApplicationContext Context context) {
        return context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    public SourceProviderHolder provideSourceProviderHolder() {
        return new SourceProviderHolder();
    }

    @Provides
    @Singleton
    public SourcesProvider provideSourcesProvider(SourceProviderHolder holder) {
        return holder.getSourcesProvider();
    }

    @Provides
    @Singleton
    public RetrofitService provideRetrofitService(SourcesProvider sourcesProvider) {
        AlarmSource source = sourcesProvider.getAlarmSource();
        return new RetrofitService(source);
    }
}
