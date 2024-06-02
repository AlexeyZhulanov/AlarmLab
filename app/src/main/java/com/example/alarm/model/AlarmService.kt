package com.example.alarm.model


typealias AlarmsListener = (alarms: List<Alarm>) -> Unit
class AlarmService {
    private var alarms = mutableListOf<Alarm>()
    private val listeners = mutableSetOf<AlarmsListener>()

    //Заменить на вытягивание данных из базы
    init {
        alarms = (1..20).map { Alarm(
            id = (0..100).random().toLong(),
            timeHours = (10..23).random(),
            timeMinutes = (10..59).random(),
            enabled = false
        )}.toMutableList()
    }

    fun getAlarms(): List<Alarm> {
        return alarms
    }

    fun changeAlarm(alarm: Alarm, newAlarm: Alarm) {
        val indexToChange = alarms.indexOfFirst { it.id == alarm.id }
        alarms[indexToChange] = newAlarm
        notifyChanges()
    }

    fun isEnabledAlarm(alarm: Alarm) {
        val indexAlarm = alarms.indexOfFirst { it.id == alarm.id }
        alarms[indexAlarm].enabled = !alarms[indexAlarm].enabled
        notifyChanges()
    }

    fun deleteAlarm(alarm: Alarm) {
        val indexToDelete = alarms.indexOfFirst { it.id == alarm.id }
        if(indexToDelete != -1) alarms.removeAt(indexToDelete)
        notifyChanges()
    }

    fun addListener(listener: AlarmsListener) {
        listeners.add(listener)
        listener.invoke(alarms)
    }
    fun removeListener(listener: AlarmsListener) = listeners.remove(listener)
    private fun notifyChanges() {
        listeners.forEach { it.invoke(alarms) }
    }
}