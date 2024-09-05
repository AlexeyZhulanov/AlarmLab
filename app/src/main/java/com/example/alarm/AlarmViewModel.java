package com.example.alarm;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.viewModelScope;
import com.example.alarm.model.AlarmsListener;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.async;
import kotlinx.coroutines.launch;
import kotlinx.coroutines.withContext;

import javax.inject.Inject;

const String APP_PREFERENCES = "APP_PREFERENCES";
const String PREF_INTERVAL = "PREF_INTERVAL";
const String PREF_WALLPAPER = "PREF_WALLPAPER";
const String PREF_THEME = "PREF_THEME";

@HiltViewModel
public class AlarmViewModel extends ViewModel {

    private final AlarmService alarmsService;
    private final MutableLiveData<List<Alarm>> _alarms = new MutableLiveData<>();
    private final LiveData<List<Alarm>> alarms = _alarms;

    private final MutableLiveData<Boolean> _initCompleted = new MutableLiveData<>();
    public LiveData<Boolean> getInitCompleted() { return _initCompleted; }

    private final MutableLiveData<String> _wallpaper = new MutableLiveData<>();
    public LiveData<String> getWallpaper() { return _wallpaper; }

    private final AlarmsListener alarmsListener = new AlarmsListener() {
        @Override
        public void onAlarmsChanged(List<Alarm> alarms) {
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
    }

    public void updateEnabledAlarm(Alarm alarm, int enabled, Context context, int idx) {
        viewModelScope.launch(() -> {
            if (alarm.enabled == 0) { // turn on
                Settings settings = await withContext(Dispatchers.IO, () -> alarmsService.getSettings());
                new MyAlarmManager(context, alarm, settings).startProcess();
            } else {
                new MyAlarmManager(context, alarm, new Settings(0)).endProcess();
            }
            await withContext(Dispatchers.IO, () -> alarmsService.updateEnabled(alarm.id, enabled));
        });
    }

    public boolean addAlarm(Alarm alarm, Context context) {
        return viewModelScope.launch(() -> {
            if (await withContext(Dispatchers.IO, () -> alarmsService.addAlarm(alarm))) {
                Settings settings = await withContext(Dispatchers.IO, () -> alarmsService.getSettings());
                new MyAlarmManager(context, alarm, settings).startProcess();
                return true;
            } else {
                return false;
            }
        });
    }

    public boolean updateAlarm(Alarm alarmNew, Context context) {
        return viewModelScope.launch(() -> {
            if (await withContext(Dispatchers.IO, () -> alarmsService.updateAlarm(alarmNew))) {
                if (alarmNew.enabled == 1) {
                    Settings settings = await withContext(Dispatchers.IO, () -> alarmsService.getSettings());
                    new MyAlarmManager(context, alarmNew, settings).restartProcess();
                }
                return true;
            } else {
                return false;
            }
        });
    }

    public void deleteAlarms(List<Alarm> alarmsToDelete, Context context) {
        viewModelScope.launch(() -> alarmsService.deleteAlarms(alarmsToDelete, context));
    }

    public void getAndNotify() {
        viewModelScope.launch(() -> {
            alarmsService.getAlarms();
            alarmsService.notifyChanges();
        });
    }

    public Pair<String, Integer> getPreferencesWallpaperAndInterval(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        String wallpaper = preferences.getString(PREF_WALLPAPER, "");
        int interval = preferences.getInt(PREF_INTERVAL, 5);
        return new Pair<>(wallpaper, interval);
    }

    public int getPreferencesTheme(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        return preferences.getInt(PREF_THEME, 0);
    }

    public void editPreferencesWallpaper(Context context, String wallpaper) {
        viewModelScope.launch(() -> {
            await withContext(Dispatchers.IO, () -> {
                SharedPreferences preferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
                preferences.edit().putString(PREF_WALLPAPER, wallpaper).apply();
            });
        });
    }

    public void editPreferencesInterval(Context context, int interval) {
        viewModelScope.launch(() -> {
            await withContext(Dispatchers.IO, () -> {
                SharedPreferences preferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
                preferences.edit().putInt(PREF_INTERVAL, interval).apply();
            });
        });
    }

    public void editPreferencesTheme(Context context, int theme) {
        viewModelScope.launch(() -> {
            await withContext(Dispatchers.IO, () -> {
                SharedPreferences preferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
                preferences.edit().putInt(PREF_THEME, theme).apply();
            });
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

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = (sharedPreferences, key) -> {
        if (key.equals(PREF_WALLPAPER)) {
            String tmp = sharedPreferences.getString(PREF_WALLPAPER, "");
            _wallpaper.postValue(tmp);
        }
    };

    public Settings getSettings() {
        return viewModelScope.launch(() -> {
            return await withContext(Dispatchers.IO, () -> alarmsService.getSettings());
        });
    }

    public void updateSettings(Settings settings) {
        viewModelScope.launch(() -> {
            await withContext(Dispatchers.IO, () -> alarmsService.updateSettings(settings));
        });
    }
}
