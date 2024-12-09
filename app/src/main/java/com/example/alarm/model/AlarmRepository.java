package com.example.alarm.model;

import android.content.Context;
import java.util.List;

public interface AlarmRepository {

    List<Alarm> getAlarms() throws Exception;

    boolean addAlarm(Alarm alarm) throws Exception;

    boolean updateAlarm(Alarm alarm) throws Exception;

    void updateEnabled(long id, Boolean enabled) throws Exception;

    void deleteAlarms(List<Alarm> list, Context context) throws Exception;
}
