package com.example.alarm.room

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM settings")
    fun getSettings(): Flow<SettingsDbEntity>

    @Update(entity = SettingsDbEntity::class)
    suspend fun updateSettings(settingsDbEntity: SettingsDbEntity)
}