package com.example.alarm

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.alarm.databinding.FragmentSettingsBinding
import com.example.alarm.model.AlarmService
import com.example.alarm.model.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)
    private val alarmsService: AlarmService
        get() = Repositories.alarmRepository as AlarmService
    private var globalId: Long = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentSettingsBinding.inflate(inflater, container, false)
        uiScope.launch {
            val settings = alarmsService.getSettings()
            binding.melodyName.text = settings.melody
            binding.repeatRadioGroup.isEnabled = settings.repetitions == 1
            binding.switchVibration.isChecked = settings.vibration == 1
            when(settings.repetitions) {
                3 -> binding.repeats3.isChecked = true
                5 -> binding.repeats5.isChecked = true
                else -> binding.repeatsInfinite.isChecked = true
            }
            when(settings.interval) {
                3 -> binding.interval3.isChecked = true
                5 -> binding.interval5.isChecked = true
                else -> binding.interval10.isChecked = true
            }
            globalId = settings.id
        }
        fun readSettings(id: Long): Settings {
            val settings = Settings(
                id = id,
                melody = binding.melodyName.text.toString(),
                vibration = if(binding.switchVibration.isChecked) 1 else 0,
                interval = if(binding.interval3.isChecked) 3
                    else if(binding.interval5.isChecked) 5
                    else 10,
                repetitions = if(binding.repeats3.isChecked) 3
                        else if(binding.repeats5.isChecked) 5
                        else 100,
                disableType = 0 //todo
            )
            return settings
        }
        binding.changeMelody.setOnClickListener {
            Toast.makeText(requireContext(), "Not implemented yet", Toast.LENGTH_SHORT).show()
        }
        binding.switchVibration.setOnClickListener {
            val s = readSettings(globalId)
            uiScope.launch { alarmsService.updateSettings(s) }
        }
        binding.repeats3.setOnClickListener {
            val s = readSettings(globalId)
            uiScope.launch { alarmsService.updateSettings(s) }
        }
        binding.repeats5.setOnClickListener {
            val s = readSettings(globalId)
            uiScope.launch { alarmsService.updateSettings(s) }
        }
        binding.repeatsInfinite.setOnClickListener {
            val s = readSettings(globalId)
            uiScope.launch { alarmsService.updateSettings(s) }
        }
        binding.interval3.setOnClickListener {
            val s = readSettings(globalId)
            uiScope.launch { alarmsService.updateSettings(s) }
        }
        binding.interval5.setOnClickListener {
            val s = readSettings(globalId)
            uiScope.launch { alarmsService.updateSettings(s) }
        }
        binding.interval10.setOnClickListener {
            val s = readSettings(globalId)
            uiScope.launch { alarmsService.updateSettings(s) }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}