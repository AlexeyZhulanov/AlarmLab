package com.example.alarm;

import com.example.alarm.model.Alarm;

public interface AlarmActionListener {
    void onAlarmEnabled(Alarm alarm, int index);
    void onAlarmChange(Alarm alarm);
    void onAlarmLongClicked();
}