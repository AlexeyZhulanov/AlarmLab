package com.example.alarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alarm.databinding.FragmentAlarmBinding
import com.example.alarm.model.Alarm
import com.example.alarm.model.AlarmService
import com.example.alarm.model.AlarmsListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class AlarmFragment : Fragment() {

    private lateinit var adapter: AlarmsAdapter

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)
    private val alarmsService: AlarmService
        get() = Repositories.alarmRepository as AlarmService


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Repositories.init(requireActivity().applicationContext)
        val binding = FragmentAlarmBinding.inflate(inflater, container, false)
        adapter = AlarmsAdapter(object: AlarmActionListener {
            override fun onAlarmEnabled(alarm: Alarm) {
                uiScope.launch {
                    var bool = 0
                    if(alarm.enabled == 0) bool = 1
                    alarmsService.updateEnabled(alarm.id, bool)
                    Toast.makeText(
                        requireContext(),
                        "IsEnabled: ${alarm.enabled}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onAlarmDelete(alarm: Alarm) {
                Toast.makeText(requireContext(), "Alarm minutes: ${alarm.timeMinutes}", Toast.LENGTH_SHORT).show()
            }

            override fun onAlarmChange(alarm: Alarm) {
                Toast.makeText(requireContext(), "Alarm hours: ${alarm.timeHours}", Toast.LENGTH_SHORT).show()
            }
        })
        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerview.layoutManager = layoutManager
        binding.recyclerview.adapter = adapter

        alarmsService.addListener(alarmsListener)
        (activity as AppCompatActivity?)!!.setSupportActionBar(binding.toolbar) //adds a button

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        alarmsService.removeListener(alarmsListener)
    }


    private val alarmsListener: AlarmsListener = {
        adapter.alarms = it
    }
}