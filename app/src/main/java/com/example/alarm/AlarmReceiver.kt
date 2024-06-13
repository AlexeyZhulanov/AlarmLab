package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra("alarmName")
        val id = intent.getLongExtra("alarmId", 0)
        Log.d("testWork", "Alarm name is $name")
        MainActivity().showSignalFragment(name!!, id)
    }
}