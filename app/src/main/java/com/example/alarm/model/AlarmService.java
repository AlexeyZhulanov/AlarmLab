package com.example.alarm.model;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.alarm.room.AlarmDao;
import com.example.alarm.room.AlarmDbEntity;
import com.example.alarm.room.AlarmUpdateEnabledTuple;
import com.example.alarm.room.SettingsDao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AlarmService implements AlarmRepository {

    private final AlarmDao alarmDao;
    private final SettingsDao settingsDao;
    private List<Alarm> alarms = new ArrayList<>();
    private final MutableLiveData<Boolean> _initCompleted = new MutableLiveData<>();
    private Settings settings = new Settings(0);

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public LiveData<Boolean> getInitCompleted() {
        return _initCompleted;
    }

    public AlarmService(AlarmDao alarmDao, SettingsDao settingsDao) {
        this.alarmDao = alarmDao;
        this.settingsDao = settingsDao;
        executorService.submit(() -> {
            try {
                alarms = getAlarms();
                _initCompleted.postValue(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public List<Alarm> getAlarms() throws Exception {
        alarms.clear();
        List<AlarmDbEntity> tuple = alarmDao.selectAlarms();
        for (AlarmDbEntity t : tuple) {
            alarms.add(new Alarm(t.getId(), t.getTimeHours(), t.getTimeMinutes(), t.getName(), t.getEnabled()));
        }
        return alarms;
    }

    @Override
    public boolean addAlarm(Alarm alarm) throws Exception {
        int existingAlarmsCount = alarmDao.countAlarmsWithTime(alarm.getTimeHours(), alarm.getTimeMinutes());
        if (existingAlarmsCount == 0) {
            try {
                alarmDao.addAlarm(AlarmDbEntity.fromUserInput(alarm));
            } catch (SQLiteConstraintException e) {
                Log.e("AlarmFragment", "Attempt to insert duplicate alarm", e);
            }
            alarms = getAlarms();
            notifyChanges();
            return true;
        }
        return false;
    }

    @Override
    public boolean updateAlarm(Alarm alarm) throws Exception {
        int existingAlarmsCount = alarmDao.countAlarmsWithTimeAndName(alarm.getTimeHours(), alarm.getTimeMinutes(), alarm.getName());
        if (existingAlarmsCount == 0) {
            try {
                alarmDao.updateAlarm(AlarmDbEntity.fromUserInput(alarm));
            } catch (SQLiteConstraintException e) {
                Log.e("AlarmFragment", "Attempt to insert duplicate alarm", e);
            }
            alarms = getAlarms();
            notifyChanges();
            return true;
        }
        return false;
    }

    @Override
    public void updateEnabled(long id, int enabled) throws Exception {
        alarmDao.updateEnabled(new AlarmUpdateEnabledTuple(id, enabled));
        alarms = getAlarms();
    }

    @Override
    public void deleteAlarms(List<Alarm> list, Context context) throws Exception {
        for (Alarm l : list) {
            if (l.getEnabled() == 1) new MyAlarmManager(context, l, new Settings(0)).endProcess();
            alarmDao.deleteAlarm(AlarmDbEntity.fromUserInput(l));
        }
        alarms = getAlarms();
        notifyChanges();
    }

    private void notifyChanges() {
        // Notify listeners about the changes (omitted for simplicity)
    }
}
