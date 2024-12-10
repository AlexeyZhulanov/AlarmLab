package com.example.alarm;

import android.util.Pair;

public interface AlarmPairCallback {
    void onResult(Pair<Boolean, String> result);
}