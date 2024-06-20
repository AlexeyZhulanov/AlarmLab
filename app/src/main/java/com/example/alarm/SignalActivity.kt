package com.example.alarm

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.alarm.databinding.ActivitySignalBinding

class SignalActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignalBinding.inflate(layoutInflater).also { setContentView(it.root) }
        setContentView(binding.root)
        val alarmName = intent.getStringExtra("alarmName") ?: ""
        val alarmId = intent.getLongExtra("alarmId", 0)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, SignalFragment(alarmName, alarmId))
                .commit()
        }
    }
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) is SignalFragment) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                (supportFragmentManager.findFragmentById(R.id.fragmentContainer) as SignalFragment).dropAndRepeatFragment()
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (supportFragmentManager.findFragmentById(R.id.fragmentContainer) is SignalFragment) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                (supportFragmentManager.findFragmentById(R.id.fragmentContainer) as SignalFragment).dropAndRepeatFragment()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
}