package com.example.alarm

import android.icu.util.Calendar
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.alarm.databinding.FragmentSignalBinding
import com.example.alarm.model.Alarm
import com.example.alarm.model.AlarmService
import com.ncorti.slidetoact.SlideToActView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job


class SignalFragment(
    val name: String,
    val id: Long
) : Fragment() {

    private val alarmPlug = Alarm(id = id, name = name)
    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)
    private lateinit var mediaPlayer: MediaPlayer
    private val alarmsService: AlarmService
        get() = Repositories.alarmRepository as AlarmService
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentSignalBinding.inflate(inflater, container, false)
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
        binding.repeatButton.setOnClickListener {
//            uiScope.launch {
//                MyAlarmManager(context, alarmPlug).endProcess()
//                val settings = alarmsService.getSettings()
//                MyAlarmManager(context, alarmPlug).repeatProcess(settings)
//                requireActivity().onBackPressedDispatcher.onBackPressed()
//            }
            dropFragment()
        }
        binding.slideButton.onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener {
            override fun onSlideComplete(view: SlideToActView) {
//                uiScope.launch { MyAlarmManager(context, alarmPlug).endProcess() }
//                dropFragment()

            }
        }

        return binding.root
    }

    fun dropFragment() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer.stop()
    }
}