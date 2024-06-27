package com.example.alarm

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.IntentCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.alarm.databinding.ActivitySignalBinding
import com.example.alarm.model.AlarmService
import com.example.alarm.model.Settings

class SignalActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val preferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
        val themeNumber = preferences.getInt(PREF_THEME, 0)
        when(themeNumber) {
            0 -> setTheme(R.style.Theme_Alarm)
            1 -> setTheme(R.style.Theme1)
            2 -> setTheme(R.style.Theme2)
            3 -> setTheme(R.style.Theme3)
            4 -> setTheme(R.style.Theme4)
            5 -> setTheme(R.style.Theme5)
            6 -> setTheme(R.style.Theme6)
            7 -> setTheme(R.style.Theme7)
            8 -> setTheme(R.style.Theme8)
            else -> setTheme(R.style.Theme_Alarm)
        }
        super.onCreate(savedInstanceState)
        binding = ActivitySignalBinding.inflate(layoutInflater).also { setContentView(it.root) }
        setContentView(binding.root)
        @Suppress("DEPRECATION")
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        val alarmName = intent.getStringExtra("alarmName") ?: ""
        val alarmId = intent.getLongExtra("alarmId", 0)
        val settings = IntentCompat.getParcelableExtra(intent, "settings", Settings::class.java)
        Log.d("testSignActivity", settings.toString())
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer2, SignalFragment(alarmName, alarmId, settings))
                .commit()
        }
    }
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainer2) is SignalFragment) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                (supportFragmentManager.findFragmentById(R.id.fragmentContainer2) as SignalFragment).dropAndRepeatFragment()
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainer2) is SignalFragment) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                (supportFragmentManager.findFragmentById(R.id.fragmentContainer2) as SignalFragment).dropAndRepeatFragment()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}