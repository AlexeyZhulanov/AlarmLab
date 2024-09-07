package com.example.alarm.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.alarm.model.Alarm;

@Entity(
        tableName = "alarms",
        indices = {
                @Index(value = {"time_hours", "time_minutes"}, unique = true)
        }
)
public class AlarmDbEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "time_hours", collate = ColumnInfo.NOCASE)
    public int timeHours;

    @ColumnInfo(name = "time_minutes", collate = ColumnInfo.NOCASE)
    public int timeMinutes;

    public String name;
    public int enabled;

    public AlarmDbEntity(long id, int timeHours, int timeMinutes, String name, int enabled) {
        this.id = id;
        this.timeHours = timeHours;
        this.timeMinutes = timeMinutes;
        this.name = name;
        this.enabled = enabled;
    }

    public Alarm toAlarm() {
        Alarm alarm = new Alarm(id);
        alarm.setTimeHours(timeHours);
        alarm.setTimeMinutes(timeMinutes);
        alarm.setName(name);
        alarm.setEnabled(enabled);
        return alarm;
    }

    public static AlarmDbEntity fromUserInput(Alarm alarm) {
        return new AlarmDbEntity(alarm.getId(), alarm.getTimeHours(), alarm.getTimeMinutes(), alarm.getName(), alarm.getEnabled());
    }
}
