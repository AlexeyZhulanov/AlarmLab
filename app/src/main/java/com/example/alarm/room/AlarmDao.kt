package com.example.alarm.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT id, time_hours, time_minutes, enabled FROM alarms ORDER BY time_hours ASC, time_minutes ASC")
    suspend fun selectAlarms(): AlarmsGetTuple?

    @Insert
    suspend fun addAlarm(alarmDbEntity: AlarmDbEntity)

    @Update(entity = AlarmDbEntity::class)
    suspend fun updateEnabled(updateEnabledTuple: AlarmUpdateEnabledTuple)

    @Update(entity = AlarmDbEntity::class)
    suspend fun updateAlarm(alarmDbEntity: AlarmDbEntity)

    @Query("SELECT * FROM alarms WHERE id = :alarmId")
    fun getById(alarmId: Long): Flow<AlarmDbEntity?>
}