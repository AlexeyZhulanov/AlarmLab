package com.example.alarm.room;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Update;

import kotlinx.coroutines.flow.Flow;

@Dao
public interface SettingsDao {
    @Query("SELECT * FROM settings")
    SettingsDbEntity getSettings();

    @Update
    void updateSettings(SettingsDbEntity settingsDbEntity);
}
