package com.example.alarm.model

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.icu.util.ULocale
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.alarm.AlarmReceiver
import kotlin.math.abs

class MyAlarmManager(
    private val context: Context?,
    val alarm: Alarm
) {
    private var alarmManager: AlarmManager? = null
    private var alarmIntent: PendingIntent
    private val calendar = Calendar.getInstance()

    init {
        alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            intent.putExtra("alarmName", alarm.name)
            PendingIntent.getBroadcast(
                context,
                alarm.id.toInt(),
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        }
    }

    fun startProcess() {
        calendar.set(Calendar.HOUR_OF_DAY, alarm.timeHours)
        calendar.set(Calendar.MINUTE, alarm.timeMinutes)
        calendar.set(Calendar.SECOND, 0)
        val calendar2 = Calendar.getInstance(ULocale.ROOT)
        val longTime: Long = if(calendar2.timeInMillis > calendar.timeInMillis) {
            calendar.timeInMillis + 86400000
        } else calendar.timeInMillis

        alarmManager?.setAlarmClock(AlarmManager.AlarmClockInfo(longTime, alarmIntent), alarmIntent)
        Log.d("test", longTime.toString())

        var minutes: Int = 0
        minutes = if(calendar2.timeInMillis > calendar.timeInMillis) {
            ((longTime - calendar2.timeInMillis) / 60000).toInt()
        } else ((calendar.timeInMillis - calendar2.timeInMillis) / 60000).toInt()
        var str = ""
        when(minutes) {
            0 -> str += "Звонок менее чем через 1 мин."
            in 1..59 -> str += "Звонок через $minutes мин."
            else -> {
                val hours = minutes / 60
                str += "Звонок через $hours ч. ${minutes % 60} мин."
            }
        }
        Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
    }

    fun endProcess() {
        alarmManager?.cancel(alarmIntent)
    }

    fun restartProcess() {
        endProcess()
        startProcess()
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun endAllProcesses() {
        alarmManager?.cancelAll()
    }
}