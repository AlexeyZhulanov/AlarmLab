package com.example.alarm

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alarm.databinding.FragmentMainBinding
import com.example.alarm.model.Alarm
import com.example.alarm.model.AlarmService
import com.example.alarm.model.AlarmsListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var adapter: AlarmsAdapter

    private val alarmsService: AlarmService
        get() = (applicationContext as App).alarmsService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = AlarmsAdapter(object: AlarmActionListener {
            override fun onAlarmEnabled(alarm: Alarm) {
                alarm.isEnabled = !alarm.isEnabled
                Toast.makeText(this@MainActivity, "IsEnabled: ${alarm.isEnabled}", Toast.LENGTH_SHORT).show()
            }

            override fun onAlarmDelete(alarm: Alarm) {
                Toast.makeText(this@MainActivity, "Alarm minutes: ${alarm.timeMinutes}", Toast.LENGTH_SHORT).show()
            }

            override fun onAlarmChange(alarm: Alarm) {
                Toast.makeText(this@MainActivity, "Alarm hours: ${alarm.timeHours}", Toast.LENGTH_SHORT).show()
            }
        })

        val layoutManager = LinearLayoutManager(this)
        binding.recyclerview.layoutManager = layoutManager
        binding.recyclerview.adapter = adapter

        alarmsService.addListener(alarmsListener)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = "Будильник"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.alarm_menu, menu)
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        alarmsService.removeListener(alarmsListener)
    }

    private val alarmsListener: AlarmsListener = {
        adapter.alarms = it
    }
}
