package com.example.alarm.retrofit.source;

import com.example.alarm.model.Alarm;
import com.example.alarm.model.exceptions.AppException;

import java.util.List;

public interface AlarmSource {
    String setAlarm(Alarm alarm) throws AppException;

    List<Integer> getAlarms() throws AppException;

    String deleteAlarm(int alarm_id) throws AppException;

    String updateAlarm(Alarm alarm) throws AppException;
}
