package com.example.alarm;

import com.example.alarm.model.Alarm;

public interface BottomSheetListener {
    void onAddAlarm(Alarm alarm);
    void onChangeAlarm(Alarm alarmOld, Alarm alarmNew);
}