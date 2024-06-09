package com.example.alarm.model

import android.util.Log
import com.example.alarm.room.AlarmDao
import com.example.alarm.room.AlarmDbEntity
import com.example.alarm.room.AlarmUpdateEnabledTuple
import com.example.alarm.room.SettingsDao
import com.example.alarm.room.SettingsDbEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


typealias AlarmsListener = (alarms: List<Alarm>) -> Unit
class AlarmService(
    private val alarmDao: AlarmDao,
    private val settingsDao: SettingsDao
): AlarmRepository {
    private var alarms = mutableListOf<Alarm>()
    private val listeners = mutableSetOf<AlarmsListener>()
    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.IO + job)
    private var settings = Settings(0)

    init {
        uiScope.launch { alarms = getAlarms() }
    }

    override suspend fun getAlarms(): MutableList<Alarm> {
        alarms.clear()
        val tuple = alarmDao.selectAlarms()
        if (tuple.isNotEmpty()) {
            for (t in tuple) {
                alarms.add(
                    Alarm(
                        id = t?.id ?: throw Exception(),
                        timeHours = t.timeHours,
                        timeMinutes = t.timeMinutes,
                        name = t.name,
                        enabled = t.enabled
                    )
                )
            }
        }
        return alarms
    }

    override suspend fun addAlarm(alarm: Alarm) {
        alarmDao.addAlarm(AlarmDbEntity.fromUserInput(alarm))
        alarms = getAlarms()
        notifyChanges()
    }

    override suspend fun updateAlarm(alarm: Alarm) {
        alarmDao.updateAlarm(AlarmDbEntity.fromUserInput(alarm))
        alarms = getAlarms()
        notifyChanges()
    }

    override suspend fun updateEnabled(id: Long, enabled: Int) {
        alarmDao.updateEnabled(AlarmUpdateEnabledTuple(id, enabled))
        alarms = getAlarms()
    }

    override fun getAlarmById(id: Long): Flow<Alarm?> {
        return alarmDao.getById(id).map { it?.toAlarm() }
    }

    override suspend fun deleteAlarms(list: List<Alarm>) {
        for(l in list) {
            alarmDao.deleteAlarm(AlarmDbEntity.fromUserInput(l))
        }
        alarms = getAlarms()
        notifyChanges()
    }

    suspend fun offAlarms() {
        for(alarm in alarms) {
            if (alarm.enabled == 1) {
                alarmDao.updateEnabled(AlarmUpdateEnabledTuple(alarm.id, 0))
            }
        }
        alarms = getAlarms()
        notifyChanges()
    }

    suspend fun getSettings(): Settings {
        settings = settingsDao.getSettings().toSettings()
        return settings
    }

    suspend fun updateSettings(settings: Settings) {
        settingsDao.updateSettings(SettingsDbEntity.fromUserInput(settings))
    }

    fun addListener(listener: AlarmsListener) {
        listeners.add(listener)
        listener.invoke(alarms)
    }
    fun removeListener(listener: AlarmsListener) = listeners.remove(listener)
    fun notifyChanges() {
        listeners.forEach { it.invoke(alarms) }
    }
}