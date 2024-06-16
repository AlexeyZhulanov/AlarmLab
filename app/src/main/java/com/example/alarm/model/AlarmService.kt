package com.example.alarm.model

import android.content.Context
import android.util.Log
import com.example.alarm.room.AlarmDao
import com.example.alarm.room.AlarmDbEntity
import com.example.alarm.room.AlarmUpdateEnabledTuple
import com.example.alarm.room.SettingsDao
import com.example.alarm.room.SettingsDbEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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
        uiScope.launch {
            alarms = getAlarms()
        }
    }

    override suspend fun getAlarms(): MutableList<Alarm> = withContext(Dispatchers.IO) {
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
        return@withContext alarms
    }

    override suspend fun addAlarm(alarm: Alarm) = withContext(Dispatchers.IO) {
        alarmDao.addAlarm(AlarmDbEntity.fromUserInput(alarm))
        alarms = getAlarms()
        notifyChanges()
    }

    override suspend fun updateAlarm(alarm: Alarm) = withContext(Dispatchers.IO) {
        alarmDao.updateAlarm(AlarmDbEntity.fromUserInput(alarm))
        alarms = getAlarms()
        notifyChanges()
    }

    override suspend fun updateEnabled(id: Long, enabled: Int) = withContext(Dispatchers.IO) {
        alarmDao.updateEnabled(AlarmUpdateEnabledTuple(id, enabled))
        alarms = getAlarms()
    }

    override fun getAlarmById(id: Long): Flow<Alarm?> {
        return alarmDao.getById(id).map { it?.toAlarm() }
    }

    override suspend fun deleteAlarms(list: List<Alarm>, context: Context?) = withContext(Dispatchers.IO) {
        for(l in list) {
            if(l.enabled == 1) MyAlarmManager(context, l).endProcess()
            alarmDao.deleteAlarm(AlarmDbEntity.fromUserInput(l))
        }
        alarms = getAlarms()
        notifyChanges()
    }

    suspend fun offAlarms(context: Context) = withContext(Dispatchers.IO) {
        for(alarm in alarms) {
            if (alarm.enabled == 1) {
                alarmDao.updateEnabled(AlarmUpdateEnabledTuple(alarm.id, 0))
                MyAlarmManager(context, alarm).endProcess()
            }
        }
        alarms = getAlarms()
        notifyChanges()
    }

    suspend fun getSettings(): Settings = withContext(Dispatchers.IO) {
        settings = settingsDao.getSettings().toSettings()
        return@withContext settings
    }

    suspend fun updateSettings(settings: Settings) = withContext(Dispatchers.IO) {
        settingsDao.updateSettings(SettingsDbEntity.fromUserInput(settings))
    }

    fun addListener(listener: AlarmsListener) {
        listeners.add(listener)
        listener.invoke(alarms)
    }
    fun removeListener(listener: AlarmsListener) = listeners.remove(listener)
    suspend fun notifyChanges() = withContext(Dispatchers.Main + job) {
        listeners.forEach {
            it.invoke(alarms)
        }
    }
}