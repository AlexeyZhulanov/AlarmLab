package com.example.alarm.room;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.alarm.model.Settings;

@Entity(
        tableName = "settings",
        indices = {
                @Index(value = {"melody"}, unique = true)
        }
)
public class SettingsDbEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String melody;
    public int vibration;
    public int interval;
    public int repetitions;

    @ColumnInfo(name = "disable_type")
    public int disableType;

    public SettingsDbEntity(long id, String melody, int vibration, int interval, int repetitions, int disableType) {
        this.id = id;
        this.melody = melody;
        this.vibration = vibration;
        this.interval = interval;
        this.repetitions = repetitions;
        this.disableType = disableType;
    }

    public Settings toSettings() {
        return new Settings(id, melody, vibration, interval, repetitions, disableType);
    }

    public static SettingsDbEntity fromUserInput(Settings settings) {
        return new SettingsDbEntity(settings.getId(), settings.getMelody(), settings.getVibration(), settings.getInterval(), settings.getRepetitions(), settings.getDisableType());
    }
}
