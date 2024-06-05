package com.example.alarm.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings")
    suspend fun getSettings(): SettingsDbEntity

    @Update(entity = SettingsDbEntity::class)
    suspend fun updateSettings(settingsDbEntity: SettingsDbEntity)
}