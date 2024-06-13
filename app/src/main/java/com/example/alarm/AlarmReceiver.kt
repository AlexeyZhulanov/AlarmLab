package com.example.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra("alarmName")
        val id = intent.getIntExtra("alarmId", 0)
        Log.d("testWork", "Alarm name is $name")
        // in this should be signal fragment replace
    }
}