package com.example.alarm;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.room.Update;

import com.example.alarm.model.Alarm;
import com.example.alarm.model.AlarmService;
import com.example.alarm.model.AlarmsListener;
import com.example.alarm.model.MyAlarmManager;
import com.example.alarm.model.Settings;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import dagger.hilt.android.lifecycle.HiltViewModel;

import javax.inject.Inject;

@HiltViewModel
public class AlarmViewModel extends ViewModel {

    private static final String APP_PREFERENCES = "APP_PREFERENCES";
    private static final String PREF_INTERVAL = "PREF_INTERVAL";
    private static final String PREF_WALLPAPER = "PREF_WALLPAPER";
    private static final String PREF_THEME = "PREF_THEME";

    private final AlarmService alarmsService;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<Alarm>> _alarms = new MutableLiveData<>();
    public LiveData<List<Alarm>> alarms = _alarms;

    private final MutableLiveData<Boolean> _initCompleted = new MutableLiveData<>();
    public LiveData<Boolean> getInitCompleted() {
        return _initCompleted;
    }

    private final MutableLiveData<String> _wallpaper = new MutableLiveData<>();
    public LiveData<String> getWallpaper() {
        return _wallpaper;
    }

    private final AlarmsListener alarmsListener = new AlarmsListener() {
        @Override
        public void invoke(List<Alarm> alarms) {
            _alarms.setValue(alarms);
        }
    };

    @Inject
    public AlarmViewModel(AlarmService alarmsService) {
        this.alarmsService = alarmsService;

        alarmsService.initCompleted.observeForever(initCompleted -> {
            if (initCompleted) {
                _initCompleted.postValue(true);
            }
        });
        alarmsService.addListener(alarmsListener);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        alarmsService.removeListener(alarmsListener);
        executorService.shutdown();
    }

    public Future<Integer> updateEnabledAlarm(Alarm alarm, int enabled, Context context, int index) {
        return executorService.submit(() -> {
            if (alarm.getEnabled() == 0) { // turn on
                Settings settings = alarmsService.getSettings(); // Call this synchronously
                new MyAlarmManager(context, alarm, settings).startProcess();
            } else {
                new MyAlarmManager(context, alarm, new Settings(0)).endProcess();
            }

            alarmsService.updateEnabled(alarm.getId(), enabled);
            return index;
        });
    }

    public void addAlarm(Alarm alarm, Context context, AlarmCallback callback) {
        executorService.execute(() -> {
            boolean result = alarmsService.addAlarm(alarm);
            if(result) {
                Settings settings = alarmsService.getSettings(); // Call this synchronously
                new MyAlarmManager(context, alarm, settings).startProcess();
            }
            callback.onResult(result);
        });
    }

    public void updateAlarm(Alarm alarmNew, Context context, AlarmCallback callback) {
        executorService.execute(() -> {
            boolean result = alarmsService.updateAlarm(alarmNew);
            if (result && alarmNew.getEnabled() == 1) {
                Settings settings = alarmsService.getSettings(); // Call this synchronously
                new MyAlarmManager(context, alarmNew, settings).restartProcess();
            }

            // Возвращаем результат через обратный вызов
            callback.onResult(result);
        });
    }

    public void deleteAlarms(List<Alarm> alarmsToDelete, Context context) {
        executorService.execute(() -> alarmsService.deleteAlarms(alarmsToDelete, context));
    }

    public void getAndNotify() {
        executorService.execute(() -> {
            alarmsService.getAlarms();
            alarmsService.notifyChanges();
        });
    }

    public void getPreferencesWallpaperAndInterval(Context context, PreferenceCallback callback) {
        executorService.execute(() -> {
            SharedPreferences preferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
            String wallpaper = preferences.getString(PREF_WALLPAPER, "");
            int interval = preferences.getInt(PREF_INTERVAL, 5);
            callback.onResult(new Pair<>(wallpaper, interval));
        });
    }

    public int getPreferencesTheme(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        return preferences.getInt(PREF_THEME, 0);
    }

    public void editPreferencesWallpaper(Context context, String wallpaper) {
        executorService.execute(() -> {
            SharedPreferences preferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
            preferences.edit().putString(PREF_WALLPAPER, wallpaper).apply();
        });
    }

    public void editPreferencesInterval(Context context, int interval) {
        executorService.execute(() -> {
            SharedPreferences preferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
            preferences.edit().putInt(PREF_INTERVAL, interval).apply();
        });
    }

    public void editPreferencesTheme(Context context, int theme) {
        executorService.execute(() -> {
            SharedPreferences preferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
            preferences.edit().putInt(PREF_THEME, theme).apply();
        });
    }

    public void registerPreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    public void unregisterPreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        prefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = (sharedPreferences, key) -> {
        if (key.equals(PREF_WALLPAPER)) {
            String tmp = sharedPreferences.getString(PREF_WALLPAPER, "");
            _wallpaper.postValue(tmp);
        }
    };

    public Settings getSettings() {
        return alarmsService.getSettings(); // Call this synchronously
    }

    public void updateSettings(Settings settings) {
        executorService.execute(() -> alarmsService.updateSettings(settings));
    }
}
