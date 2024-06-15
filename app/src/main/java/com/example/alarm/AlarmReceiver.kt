package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra("alarmName")
        val id = intent.getLongExtra("alarmId", 0)
        var localBroadCastIntent = Intent(LOCAL_BROADCAST_KEY)
        localBroadCastIntent.putExtra("alarmName",name?:"")
        localBroadCastIntent.putExtra("alarmId",id)
        Handler(Looper.getMainLooper()).post {
            LocalBroadcastManager.getInstance(context).sendBroadcast(localBroadCastIntent)
        }
    }
}
const val LOCAL_BROADCAST_KEY = "alarm_start"