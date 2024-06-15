package com.example.alarm.model

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.icu.util.ULocale
import android.widget.Toast
import com.example.alarm.AlarmReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class MyAlarmManager(
    private val context: Context?,
    val alarm: Alarm
) {
    private var alarmManager: AlarmManager? = null
    private var alarmIntent: PendingIntent
    private val calendar = Calendar.getInstance()
    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Default + job)

    init {
            alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
                intent.putExtra("alarmName", alarm.name)
                intent.putExtra("alarmId", alarm.id)
                PendingIntent.getBroadcast(
                    context,
                    alarm.id.toInt(),
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            }
    }

    suspend fun startProcess() {
        val part = uiScope.async {
            calendar.set(Calendar.HOUR_OF_DAY, alarm.timeHours)
            calendar.set(Calendar.MINUTE, alarm.timeMinutes)
            calendar.set(Calendar.SECOND, 0)
            val calendar2 = Calendar.getInstance(ULocale.ROOT)
            val longTime: Long = if (calendar2.timeInMillis > calendar.timeInMillis) {
                calendar.timeInMillis + 86400000
            } else calendar.timeInMillis

            alarmManager?.setAlarmClock(
                AlarmManager.AlarmClockInfo(longTime, alarmIntent),
                alarmIntent
            )

            val minutes: Int = if (calendar2.timeInMillis > calendar.timeInMillis) {
                ((longTime - calendar2.timeInMillis) / 60000).toInt()
            } else ((calendar.timeInMillis - calendar2.timeInMillis) / 60000).toInt()
            var str = ""
            when (minutes) {
                0 -> str += "Звонок менее чем через 1 мин."
                in 1..59 -> str += "Звонок через $minutes мин."
                else -> {
                    val hours = minutes / 60
                    str += "Звонок через $hours ч. ${minutes % 60} мин."
                }
            }
            return@async str
        }
        val res = part.await()
        Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
    }

    suspend fun endProcess() = withContext(Dispatchers.Default) {
        alarmManager?.cancel(alarmIntent)
    }

    suspend fun restartProcess() = withContext(Dispatchers.Main + job) {
        endProcess()
        startProcess()
    }
    suspend fun repeatProcess(settings: Settings) = withContext(Dispatchers.Default) {
        val calendar = Calendar.getInstance(ULocale.ROOT)
        val time = calendar.timeInMillis + settings.interval.toLong()
        alarmManager?.setAlarmClock(AlarmManager.AlarmClockInfo(time, alarmIntent), alarmIntent)
    }
}