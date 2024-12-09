package com.example.alarm.model;

import com.example.alarm.model.exceptions.AppException;

import java.util.List;

public interface RetrofitRepository {
    boolean setAlarm(int hours, int minutes) throws AppException;

    List<AlarmShort> getAlarms() throws AppException;
}
