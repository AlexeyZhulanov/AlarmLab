package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

interface SignalStart {
    fun onAlarmTriggered(name: String?, id: Long)
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra("alarmName")
        val id = intent.getLongExtra("alarmId", 0)
        Log.d("testWork", "It works!")
        val mainActivity = context as MainActivity
        mainActivity.onAlarmTriggered(name, id)
    }
}