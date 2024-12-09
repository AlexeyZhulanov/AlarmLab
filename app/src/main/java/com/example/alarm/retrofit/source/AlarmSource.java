package com.example.alarm.retrofit.source;

import com.example.alarm.model.AlarmShort;
import com.example.alarm.model.exceptions.AppException;

import java.util.List;

public interface AlarmSource {
    String setAlarm(int hours, int minutes) throws AppException;

    List<AlarmShort> getAlarms() throws AppException;
}
