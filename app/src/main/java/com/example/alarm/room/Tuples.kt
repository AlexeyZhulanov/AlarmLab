package com.example.alarm.room

import androidx.room.ColumnInfo

data class AlarmsGet(
    @ColumnInfo(name = "time_hours", collate = ColumnInfo.NOCASE) var timeHours: Int,
    @ColumnInfo(name = "time_minutes", collate = ColumnInfo.NOCASE) var timeMinutes: Int,
    var enabled: Boolean
)