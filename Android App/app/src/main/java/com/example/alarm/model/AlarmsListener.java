package com.example.alarm.model;

import java.util.List;

@FunctionalInterface
public interface AlarmsListener {
    void invoke(List<Alarm> alarms);
}