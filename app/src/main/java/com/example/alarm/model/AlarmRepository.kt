package com.example.alarm.model

import kotlinx.coroutines.flow.Flow

// Придется что-то явно фиксить

interface AlarmRepository {

    suspend fun getAlarms(): MutableList<Alarm>

    suspend fun addAlarm(alarm: Alarm)

    suspend fun updateAlarm(alarm: Alarm)

    suspend fun updateEnabled(enabled: Boolean)

    suspend fun getAlarmById(id: Long): Flow<Alarm>
}