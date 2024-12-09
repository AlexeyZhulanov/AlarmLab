package com.example.alarm.room;

import androidx.annotation.NonNull;
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
    @PrimaryKey(autoGenerate = true) public long id;
    @ColumnInfo(name = "time_hours") public int timeHours;
    @ColumnInfo(name = "time_minutes") public int timeMinutes;
    @NonNull public String name;
    @ColumnInfo(name = "enabled", defaultValue = "0") public boolean enabled;

    public AlarmDbEntity(long id, int timeHours, int timeMinutes, @NonNull String name, boolean enabled) {
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
