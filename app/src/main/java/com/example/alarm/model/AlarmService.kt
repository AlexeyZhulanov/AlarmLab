package com.example.alarm.model

import com.example.alarm.room.AlarmDao
import com.example.alarm.room.AlarmDbEntity
import com.example.alarm.room.AlarmUpdateEnabledTuple
import com.example.alarm.room.SettingsDao
import com.example.alarm.room.SettingsDbEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


typealias AlarmsListener = (alarms: List<Alarm>) -> Unit
@OptIn(DelicateCoroutinesApi::class)
class AlarmService(
    private val alarmDao: AlarmDao,
    private val settingsDao: SettingsDao
): AlarmRepository {
    private var alarms = mutableListOf<Alarm>()
    private val listeners = mutableSetOf<AlarmsListener>()

    init {
        GlobalScope.launch { alarms = getAlarms() }
    }

    override suspend fun getAlarms(): MutableList<Alarm> {
        val tuple = alarmDao.selectAlarms()
        if (tuple.isNotEmpty()) {
            for (t in tuple) {
                alarms.add(
                    Alarm(
                        id = t?.id ?: throw Exception(),
                        timeHours = t.timeHours,
                        timeMinutes = t.timeMinutes,
                        enabled = t.enabled
                    )
                )
            }
        }
        else { alarms.add(Alarm(id = 0)) }

        return alarms
    }

    override suspend fun addAlarm(alarm: Alarm) {
        alarmDao.addAlarm(AlarmDbEntity.fromUserInput(alarm))
        notifyChanges()
    }

    override suspend fun updateAlarm(alarm: Alarm) {
        alarmDao.updateAlarm(AlarmDbEntity.fromUserInput(alarm))
        notifyChanges()
    }

    override suspend fun updateEnabled(id: Long, enabled: Int) {
        alarmDao.updateEnabled(AlarmUpdateEnabledTuple(id, enabled))
        notifyChanges()
    }

    override fun getAlarmById(id: Long): Flow<Alarm?> {
        return alarmDao.getById(id).map { it?.toAlarm() }
    }

    override suspend fun deleteAlarms(list: List<Alarm>) {
        for(l in list) {
            alarmDao.deleteAlarm(AlarmDbEntity.fromUserInput(l))
        }
        notifyChanges()
    }

    suspend fun getSettings(): Settings {
        return settingsDao.getSettings().toSettings()
    }

    suspend fun updateSettings(settings: Settings) {
        settingsDao.updateSettings(SettingsDbEntity.fromUserInput(settings))
    }

//    fun changeAlarm(alarm: Alarm, newAlarm: Alarm) {
//        val indexToChange = alarms.indexOfFirst { it.id == alarm.id }
//        alarms[indexToChange] = newAlarm
//        notifyChanges()
//    }
//
//    fun isEnabledAlarm(alarm: Alarm) {
//        val indexAlarm = alarms.indexOfFirst { it.id == alarm.id }
//        alarms[indexAlarm].enabled = !alarms[indexAlarm].enabled
//        notifyChanges()
//    }
//
//    fun deleteAlarm(alarm: Alarm) {
//        val indexToDelete = alarms.indexOfFirst { it.id == alarm.id }
//        if(indexToDelete != -1) alarms.removeAt(indexToDelete)
//        notifyChanges()
//    }

    fun addListener(listener: AlarmsListener) {
        listeners.add(listener)
        listener.invoke(alarms)
    }
    fun removeListener(listener: AlarmsListener) = listeners.remove(listener)
    private fun notifyChanges() {
        listeners.forEach { it.invoke(alarms) }
    }
}