package com.example.alarm.model

import android.content.Context
import android.os.PowerManager

object AppVisibilityTracker {
    private lateinit var powerManager: PowerManager

    fun initialize(context: Context) {
        powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    }

    fun isScreenOn(): Boolean {
        return powerManager.isInteractive
    }
}