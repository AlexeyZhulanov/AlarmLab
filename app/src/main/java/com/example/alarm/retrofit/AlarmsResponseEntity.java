package com.example.alarm.retrofit;

import com.example.alarm.model.AlarmShort;

import java.util.List;

public class AlarmsResponseEntity {
    private List<AlarmShort> alarms;

    public List<AlarmShort> getAlarms() {
        return alarms;
    }
}
