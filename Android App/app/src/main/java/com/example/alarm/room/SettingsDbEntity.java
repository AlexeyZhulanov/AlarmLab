package com.example.alarm.room;

import androidx.annotation.NonNull;
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

    @NonNull public String melody;
    @NonNull public Boolean vibration;
    public int interval;
    public int repetitions;

    public SettingsDbEntity(long id, @NonNull String melody, @NonNull Boolean vibration, int interval, int repetitions) {
        this.id = id;
        this.melody = melody;
        this.vibration = vibration;
        this.interval = interval;
        this.repetitions = repetitions;
    }

    public Settings toSettings() {
        Settings settings = new Settings(id);
        settings.setMelody(melody);
        settings.setVibration(vibration);
        settings.setInterval(interval);
        settings.setRepetitions(repetitions);
        return settings;
    }

    public static SettingsDbEntity fromUserInput(Settings settings) {
        return new SettingsDbEntity(settings.getId(), settings.getMelody(), settings.getVibration(), settings.getInterval(), settings.getRepetitions());
    }
}
