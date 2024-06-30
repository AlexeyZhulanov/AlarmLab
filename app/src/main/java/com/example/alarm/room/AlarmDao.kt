package com.example.alarm.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY time_hours ASC, time_minutes ASC")
    suspend fun selectAlarms(): List<AlarmsGetTuple?>

    @Query("SELECT COUNT(*) FROM alarms WHERE time_hours = :hours AND time_minutes = :minutes")
    suspend fun countAlarmsWithTime(hours: Int, minutes: Int): Int

    @Insert
    suspend fun addAlarm(alarmDbEntity: AlarmDbEntity)

    @Update(entity = AlarmDbEntity::class)
    suspend fun updateEnabled(updateEnabledTuple: AlarmUpdateEnabledTuple)

    @Update
    suspend fun updateAlarm(alarmDbEntity: AlarmDbEntity)

    @Query("SELECT * FROM alarms WHERE id = :alarmId")
    fun getById(alarmId: Long): Flow<AlarmDbEntity?>

    @Delete(entity = AlarmDbEntity::class)
    suspend fun deleteAlarm(alarmDbEntity: AlarmDbEntity)
}