package com.example.alarm.room

import androidx.room.ColumnInfo

data class AlarmsGetTuple(
    val id: Long,
    @ColumnInfo(name = "time_hours", collate = ColumnInfo.NOCASE) var timeHours: Int,
    @ColumnInfo(name = "time_minutes", collate = ColumnInfo.NOCASE) var timeMinutes: Int,
    var name: String,
    var enabled: Int
)

data class AlarmUpdateEnabledTuple(
    val id: Long,
    var enabled: Int
)