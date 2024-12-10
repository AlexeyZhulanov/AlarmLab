package com.example.alarm;

import android.util.Pair;

import com.example.alarm.model.Alarm;

import java.util.List;

public interface AlarmCallback {
    void onResult(Pair<Boolean, List<Alarm>> result);
}
