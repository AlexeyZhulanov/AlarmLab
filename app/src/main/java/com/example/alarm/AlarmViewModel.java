package com.example.alarm;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.alarm.model.Alarm;
import com.example.alarm.model.AlarmService;
import com.example.alarm.model.AlarmsListener;

import java.util.List;

import dagger.hilt.android.lifecycle.HiltViewModel;

import javax.inject.Inject;

@HiltViewModel
public class AlarmViewModel extends ViewModel {
    private static final String PREF_INTERVAL = "PREF_INTERVAL";
    private static final String PREF_WALLPAPER = "PREF_WALLPAPER";

    private final AlarmService alarmsService;
    private final SharedPreferences preferences;
    private final MutableLiveData<List<Alarm>> _alarms = new MutableLiveData<>();
    public LiveData<List<Alarm>> alarms = _alarms;

    private final MutableLiveData<Boolean> _initCompleted = new MutableLiveData<>();
    public LiveData<Boolean> getInitCompleted() {
        return _initCompleted;
    }

    private final AlarmsListener alarmsListener = _alarms::postValue;

    @Inject
    public AlarmViewModel(AlarmService alarmsService, SharedPreferences preferences) {
        this.alarmsService = alarmsService;
        this.preferences = preferences;

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

    public void updateEnabledAlarm(Alarm alarm, Boolean enabled, AlarmCallback callback) {
        alarmsService.updateEnabled(alarm.getId(), enabled);
        callback.onResult(true);
    }

    public void addAlarm(Alarm alarm, AlarmCallback callback) {
        boolean result = alarmsService.addAlarm(alarm);
        callback.onResult(result);
    }

    public void updateAlarm(Alarm alarmNew, AlarmCallback callback) {
        boolean result = alarmsService.updateAlarm(alarmNew);
        // Возвращаем результат через обратный вызов
        callback.onResult(result);
    }

    public void deleteAlarms(List<Alarm> alarmsToDelete, Context context) {
        alarmsService.deleteAlarms(alarmsToDelete, context);
    }

    public void getAndNotify() {
        alarmsService.getAlarms();
        alarmsService.notifyChanges();
    }

    public void getPreferencesWallpaperAndInterval(PreferenceCallback callback) {
        String wallpaper = preferences.getString(PREF_WALLPAPER, "");
        int interval = preferences.getInt(PREF_INTERVAL, 5);
        callback.onResult(new Pair<>(wallpaper, interval));
    }
}
