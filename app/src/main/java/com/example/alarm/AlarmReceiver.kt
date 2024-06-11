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
        Log.d("testWork", "Alarm name is $name")
        Toast.makeText(context, "YES SIR", Toast.LENGTH_SHORT).show()
    }
}