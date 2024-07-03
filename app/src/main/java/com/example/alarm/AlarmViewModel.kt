package com.example.alarm

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.model.Alarm
import com.example.alarm.model.AlarmService
import com.example.alarm.model.AlarmsListener
import com.example.alarm.model.MyAlarmManager
import com.example.alarm.model.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val APP_PREFERENCES = "APP_PREFERENCES"
const val PREF_INTERVAL = "PREF_INTERVAL"

class AlarmViewModel(
    private val alarmsService: AlarmService
) : ViewModel() {

    private val _alarms = MutableLiveData<List<Alarm>>()
    val alarms: LiveData<List<Alarm>> = _alarms


    private val alarmsListener: AlarmsListener = {
        _alarms.value = it
    }
    init {
        alarmsService.addListener(alarmsListener)
    }


    override fun onCleared() {
        super.onCleared()
        alarmsService.removeListener(alarmsListener)
    }

    suspend fun updateEnabledAlarm(alarm: Alarm, enabled: Int, context: Context, idx: Int) = withContext(Dispatchers.Main) {
            if (alarm.enabled == 0) { //turn on
                val s = async(Dispatchers.IO) { alarmsService.getSettings() }
                MyAlarmManager(context, alarm, s.await()).startProcess()
            }
            else {
                MyAlarmManager(context, alarm, Settings(0)).endProcess()
            }
        withContext(Dispatchers.IO) {
            alarmsService.updateEnabled(alarm.id, enabled)
        }
        return@withContext idx
    }

    suspend fun addAlarm(alarm: Alarm, context: Context): Boolean = withContext(Dispatchers.Main) {
            if(withContext(Dispatchers.IO) { alarmsService.addAlarm(alarm)}) {
                val settings = async(Dispatchers.IO) { alarmsService.getSettings() }
                MyAlarmManager(context, alarm, settings.await()).startProcess()
                return@withContext true
            }
            else {
                return@withContext false
            }
    }

    suspend fun updateAlarm(alarmNew: Alarm, context: Context): Boolean = withContext(Dispatchers.Main) {
        if(withContext(Dispatchers.IO) { alarmsService.updateAlarm(alarmNew)}) {
            if (alarmNew.enabled == 1) {
                val settings = async(Dispatchers.IO) { alarmsService.getSettings() }
                MyAlarmManager(context, alarmNew, settings.await()).restartProcess()
            }
            return@withContext true
        }
        else {
            return@withContext false
        }
    }

    fun deleteAlarms(alarmsToDelete: List<Alarm>, context: Context?) {
        viewModelScope.launch {
            alarmsService.deleteAlarms(alarmsToDelete, context)
        }
    }
    fun getAndNotify() {
        viewModelScope.launch {
            alarmsService.getAlarms()
            alarmsService.notifyChanges()
        }
    }
    fun getPreferences(context: Context): Pair<String, Int>{
        val preferences: SharedPreferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        val wallpaper = preferences.getString(PREF_WALLPAPER, "")
        val interval: Int = preferences.getInt(PREF_INTERVAL, 5)
        return Pair(wallpaper!!, interval)
    }
}