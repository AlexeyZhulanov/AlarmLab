package com.example.alarm

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.icu.util.Calendar
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.alarm.Repositories.alarmRepository
import com.example.alarm.databinding.FragmentSignalBinding
import com.example.alarm.model.Alarm
import com.example.alarm.model.AlarmService
import com.example.alarm.model.MyAlarmManager
import com.example.alarm.model.Settings
import com.ncorti.slidetoact.SlideToActView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SignalFragment(
    val name: String,
    val id: Long
) : Fragment() {

    private val alarmPlug = Alarm(id = id, name = name)
    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var preferences: SharedPreferences
    private lateinit var settings: Settings
    private val alarmsService: AlarmService
        get() = Repositories.alarmRepository as AlarmService

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Repositories.init(requireActivity().applicationContext)
        Log.d("testSignFragment", "Works!")
        val binding = FragmentSignalBinding.inflate(inflater, container, false)
        uiScope.launch {
            settings = alarmsService.getSettings()
        }

        val updateWorkRequest = OneTimeWorkRequestBuilder<AlarmWorker>()
            .setInputData(workDataOf("alarmId" to alarmPlug.id, "enabled" to 0))
            .build()

        WorkManager.getInstance(requireContext()).enqueue(updateWorkRequest)

        val tmp = Calendar.getInstance().time.toString()
        val str = tmp.split(" ")
        val date = "${str[0]} ${str[1]} ${str[2]}"
        val tmpTime = str[3].split(":")
        val time = "${tmpTime[0]}:${tmpTime[1]}"
        binding.currentTimeTextView.text = time
        binding.currentDateTextView.text = date
        binding.nameTextView.text = name
        mediaPlayer = MediaPlayer.create(context, R.raw.signal)
        mediaPlayer.isLooping = true
        mediaPlayer.start()
        binding.pulsator.start()
        val fragmentContext = requireContext()
        binding.repeatButton.setOnClickListener {
            dropAndRepeatFragment()
        }
        binding.slideButton.onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener {
            override fun onSlideComplete(view: SlideToActView) {
                uiScope.launch {
                    preferences = requireActivity().getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE)
                    preferences.edit()
                        .putLong(DISABLE_ID, alarmPlug.id)
                        .apply()
                    MyAlarmManager(fragmentContext, alarmPlug).endProcess()
                }
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        return binding.root
    }

    fun dropAndRepeatFragment() {
        uiScope.launch {
                preferences.edit()
                    .putLong(DISABLE_ID, alarmPlug.id)
                    .apply()
                MyAlarmManager(context, alarmPlug).endProcess()
                MyAlarmManager(context, alarmPlug).repeatProcess(settings)
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer.stop()
    }
}
const val LOCAL_BROADCAST_KEY2 = "alarm_update"