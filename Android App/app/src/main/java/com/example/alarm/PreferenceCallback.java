package com.example.alarm;

import android.util.Pair;

public interface PreferenceCallback {
    void onResult(Pair<String, Integer> result);
}
