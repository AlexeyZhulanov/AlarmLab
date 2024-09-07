package com.example.alarm.room;

import androidx.room.ColumnInfo;

public class AlarmsGetTuple {
    public long id;

    @ColumnInfo(name = "time_hours", collate = ColumnInfo.NOCASE)
    public int timeHours;

    @ColumnInfo(name = "time_minutes", collate = ColumnInfo.NOCASE)
    public int timeMinutes;

    public String name;
    public int enabled;

    public AlarmsGetTuple(long id, int timeHours, int timeMinutes, String name, int enabled) {
        this.id = id;
        this.timeHours = timeHours;
        this.timeMinutes = timeMinutes;
        this.name = name;
        this.enabled = enabled;
    }
}