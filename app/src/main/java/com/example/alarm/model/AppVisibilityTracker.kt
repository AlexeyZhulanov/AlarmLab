package com.example.alarm.model

object AppVisibilityTracker {
    private var isAppVisible = false

    fun isAppRunning(): Boolean {
        return isAppVisible
    }

    fun setAppVisible(isVisible: Boolean) {
        isAppVisible = isVisible
    }
}