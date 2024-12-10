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
import com.example.alarm.model.RetrofitService;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.lifecycle.HiltViewModel;

import javax.inject.Inject;

@HiltViewModel
public class AlarmViewModel extends ViewModel {
    private static final String PREF_INTERVAL = "PREF_INTERVAL";
    private static final String PREF_WALLPAPER = "PREF_WALLPAPER";

    private final AlarmService alarmsService;
    private final RetrofitService retrofitService;
    private final SharedPreferences preferences;
    private final MutableLiveData<List<Alarm>> _alarms = new MutableLiveData<>();
    public LiveData<List<Alarm>> alarms = _alarms;

    private final MutableLiveData<Boolean> _initCompleted = new MutableLiveData<>();
    public LiveData<Boolean> initCompleted = _initCompleted;

    private final AlarmsListener alarmsListener = _alarms::postValue;

    @Inject
    public AlarmViewModel(AlarmService alarmsService, RetrofitService retrofitService, SharedPreferences preferences) {
        this.alarmsService = alarmsService;
        this.retrofitService = retrofitService;
        this.preferences = preferences;

        alarmsService.initCompleted.observeForever(initCompleted -> {
            if (initCompleted) {
                _initCompleted.postValue(true);
            }
        });
        alarmsService.addListener(alarmsListener);
        retrofitService.initCompleted.observeForever(list -> {
            if(list != null) {
                alarmsService.syncAlarms(list);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        alarmsService.removeListener(alarmsListener);
    }

    public void updateEnabledAlarm(Alarm alarm, Boolean enabled, AlarmPairCallback callback) {
        if(enabled) {
            Pair<Boolean, String> result = retrofitService.setAlarm(alarm);
            if(result.first) {
                alarmsService.updateEnabled(alarm.getId(), true);
                callback.onResult(new Pair<>(true, result.second));
            } else {
                callback.onResult(new Pair<>(false, result.second));
            }
        } else {
            Pair<Boolean, String> result = retrofitService.deleteAlarm((int)alarm.getId());
            if(result.first) {
                alarmsService.updateEnabled(alarm.getId(), false);
                callback.onResult(new Pair<>(true, result.second));
            } else {
                callback.onResult(new Pair<>(false, result.second));
            }
        }
    }

    public void addAlarm(Alarm alarm, AlarmPairCallback callback) {
        boolean res = alarmsService.addAlarm(alarm);
        if(res) {
            callback.onResult(new Pair<>(true, ""));
        } else {
            callback.onResult(new Pair<>(false, "Будильник уже есть в БД"));
        }
    }

    public void setAlarm(Alarm alarm, AlarmPairCallback callback) {
        Pair<Boolean, String> result = retrofitService.setAlarm(alarm);
        if(result.first) {
            callback.onResult(new Pair<>(true, result.second));
        } else {
            alarmsService.updateEnabled(alarm.getId(), false);
            alarmsService.notifyChanges();
            callback.onResult(new Pair<>(false, result.second));
        }
    }

    public void updateAlarm(Alarm alarmNew, AlarmPairCallback callback) {
        Pair<Boolean, String> result = retrofitService.updateAlarm(alarmNew);
        if(result.first) {
            alarmsService.updateAlarm(alarmNew);
            callback.onResult(new Pair<>(true, result.second));
        } else {
            callback.onResult(new Pair<>(false, result.second));
        }
    }

    public void deleteAlarms(List<Alarm> alarmsToDelete, Context context, AlarmCallback callback) {
        List<Alarm> list = new ArrayList<>();
        boolean res = true;
        for(Alarm alarm : alarmsToDelete) {
            if(alarm.getEnabled()) {
                Pair<Boolean, String> result = retrofitService.deleteAlarm((int)alarm.getId());
                if(result.first) {
                    list.add(alarm);
                } else {
                    res = false;
                }
            } else {
                list.add(alarm);
            }
        }
        alarmsService.deleteAlarms(list, context);
        callback.onResult(new Pair<>(res, list));
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
