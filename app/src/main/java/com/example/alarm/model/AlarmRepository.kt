package com.example.alarm.model

import kotlinx.coroutines.flow.Flow

interface AlarmRepository {

    suspend fun getAlarms(): MutableList<Alarm>

    suspend fun addAlarm(alarm: Alarm)

    suspend fun updateAlarm(alarm: Alarm)

    suspend fun updateEnabled(id: Long, enabled: Int)

    fun getAlarmById(id: Long): Flow<Alarm?>

    suspend fun deleteAlarms(list: List<Alarm>)
}