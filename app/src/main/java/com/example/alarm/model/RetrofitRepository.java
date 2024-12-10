package com.example.alarm.model;

import android.util.Pair;

import com.example.alarm.model.exceptions.AppException;

import java.util.List;

public interface RetrofitRepository {
    Pair<Boolean, String> setAlarm(Alarm alarm) throws AppException;

    List<Integer> getAlarms() throws AppException;

    Pair<Boolean, String> deleteAlarm(int alarm_id) throws AppException;

    Pair<Boolean, String> updateAlarm(Alarm alarm) throws AppException;
}
