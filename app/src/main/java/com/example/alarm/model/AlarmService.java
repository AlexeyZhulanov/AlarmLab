package com.example.alarm.model;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.alarm.room.AlarmDao;
import com.example.alarm.room.AlarmDbEntity;
import com.example.alarm.room.AlarmUpdateEnabledTuple;
import com.example.alarm.room.AlarmsGetTuple;
import com.example.alarm.room.SettingsDao;
import com.example.alarm.room.SettingsDbEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AlarmService implements AlarmRepository {
    private final AlarmDao alarmDao;
    private final SettingsDao settingsDao;
    private List<Alarm> alarms = new ArrayList<>();
    private final Set<AlarmsListener> listeners = new HashSet<>();
    private Settings settings = new Settings(0);

    private final MutableLiveData<Boolean> _initCompleted = new MutableLiveData<>();
    public LiveData<Boolean> initCompleted = _initCompleted;

    private final ExecutorService executorService = Executors.newFixedThreadPool(2); // Adjust thread pool size as needed

    public AlarmService(AlarmDao alarmDao, SettingsDao settingsDao) {
        this.alarmDao = alarmDao;
        this.settingsDao = settingsDao;

        executorService.execute(() -> {
            try {
                alarms = getAlarms();
                _initCompleted.postValue(true);
            } catch (Exception e) {
                Log.e("AlarmService", "Error initializing alarms", e);
            }
        });
    }

    @Override
    public List<Alarm> getAlarms() {
        return runTask(() -> {
            alarms.clear();
            List<AlarmsGetTuple> tuple = alarmDao.selectAlarms();
            for (AlarmsGetTuple t : tuple) {
                Alarm alarm = new Alarm(t.id);
                alarm.setTimeHours(t.timeHours);
                alarm.setTimeMinutes(t.timeMinutes);
                alarm.setName(t.name);
                alarm.setEnabled(t.enabled);
                alarms.add(alarm);
            }
            return alarms;
        });
    }

    @Override
    public boolean addAlarm(Alarm alarm) {
        return Boolean.TRUE.equals(runTask(() -> {
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
        }));
    }

    @Override
    public boolean updateAlarm(Alarm alarm) {
        return Boolean.TRUE.equals(runTask(() -> {
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
        }));
    }

    @Override
    public void updateEnabled(long id, int enabled) {
        runTask(() -> {
            alarmDao.updateEnabled(new AlarmUpdateEnabledTuple(id, enabled));
            alarms = getAlarms(); // Update alarms
            return true;
        });
    }

    @Override
    public void deleteAlarms(List<Alarm> list, Context context) {
        runTask(() -> {
            for (Alarm l : list) {
                if (l.getEnabled() == 1) {
                    new MyAlarmManager(context, l, new Settings(0)).endProcess();
                }
                alarmDao.deleteAlarm(AlarmDbEntity.fromUserInput(l));
            }
            alarms = getAlarms();
            notifyChanges();
            return true;
        });
    }

    public void offAlarms(Context context) {
        runTask(() -> {
            for (Alarm alarm : alarms) {
                if (alarm.getEnabled() == 1) {
                    alarmDao.updateEnabled(new AlarmUpdateEnabledTuple(alarm.getId(), 0));
                    new MyAlarmManager(context, alarm, new Settings(0)).endProcess();
                }
            }
            alarms = getAlarms();
            notifyChanges();
            return true;
        });
    }

    public Settings getSettings() {
        return runTask(() -> {
            settings = settingsDao.getSettings().toSettings();
            return settings;
        });
    }

    public void updateSettings(Settings settings) {
        runTask(() -> {
            settingsDao.updateSettings(SettingsDbEntity.fromUserInput(settings));
            return true;
        });
    }

    public void addListener(AlarmsListener listener) {
        listeners.add(listener);
        listener.invoke(alarms);
    }

    public void removeListener(AlarmsListener listener) {
        listeners.remove(listener);
    }

    public void notifyChanges() {
        for (AlarmsListener listener : listeners) {
            listener.invoke(alarms);
        }
    }

    // Utility method to handle background tasks
    private <T> T runTask(Callable<T> task) {
        Future<T> future = executorService.submit(task);
        try {
            return future.get();
        } catch (Exception e) {
            Log.e("AlarmService", "Error running task", e);
            return null;
        }
    }
}
