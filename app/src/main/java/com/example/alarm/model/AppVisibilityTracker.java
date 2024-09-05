package com.example.alarm.model;

import android.content.Context;
import android.os.PowerManager;

public class AppVisibilityTracker {
    private static PowerManager powerManager;

    public static void initialize(Context context) {
        powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    }

    public static boolean isScreenOn() {
        return powerManager.isInteractive();
    }
}
