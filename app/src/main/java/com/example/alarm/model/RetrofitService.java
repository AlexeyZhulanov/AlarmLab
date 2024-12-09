package com.example.alarm.model;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.example.alarm.model.exceptions.AlarmAlreadyExistsException;
import com.example.alarm.model.exceptions.AlarmNotFoundException;
import com.example.alarm.model.exceptions.AppException;
import com.example.alarm.model.exceptions.BackendException;
import com.example.alarm.retrofit.source.AlarmSource;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RetrofitService implements RetrofitRepository {
    private final AlarmSource alarmSource;

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final MutableLiveData<Boolean> initCompleted = new MutableLiveData<>();

    public RetrofitService(AlarmSource alarmSource) {
        this.alarmSource = alarmSource;

        executorService.submit(() -> initCompleted.postValue(true));
    }

    public MutableLiveData<Boolean> getInitCompleted() {
        return initCompleted;
    }

    @Override
    public List<AlarmShort> getAlarms() throws AppException {
        try {
            List<AlarmShort> alarms = executorService.submit(() -> {
                try {
                    return alarmSource.getAlarms();
                } catch (BackendException e) {
                    if(e.getCode() == 404) {
                        throw new AlarmNotFoundException(e);
                    } else {
                        throw e;
                    }
                }
            }).get();
            Log.d("testGetAlarms", alarms.toString());
            return alarms;
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean setAlarm(int hours, int minutes) throws AppException {
        try {
            String message = executorService.submit(() -> {
                try {
                    return alarmSource.setAlarm(hours, minutes);
                } catch (BackendException e) {
                    if(e.getCode() == 404) {
                        throw new AlarmNotFoundException(e);
                    } else if(e.getCode() == 409) {
                        throw new AlarmAlreadyExistsException(e);
                    } else {
                        throw e;
                    }
                }
            }).get();
            Log.d("testSetAlarm", message);
            return true;
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
