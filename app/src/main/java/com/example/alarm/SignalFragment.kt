package com.example.alarm

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.icu.util.Calendar
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
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
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignalFragment(
    val name: String,
    val id: Long,
    val settings: Settings? = Settings(0)
) : Fragment() {

    private val alarmPlug = Alarm(id = id, name = name)
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var vibrator: Vibrator

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Repositories.init(requireActivity().applicationContext)
        val binding = FragmentSignalBinding.inflate(inflater, container, false)

        val updateWorkRequest = OneTimeWorkRequestBuilder<AlarmWorker>()
            .setInputData(workDataOf("alarmId" to alarmPlug.id, "enabled" to 0))
            .build()

        WorkManager.getInstance(requireContext()).enqueue(updateWorkRequest)
        selectMelody(settings!!)
        if(settings.vibration == 1) startVibrator()
        val tmp = Calendar.getInstance().time.toString()
        val str = tmp.split(" ")
        val date = "${str[0]} ${str[1]} ${str[2]}"
        val tmpTime = str[3].split(":")
        val time = "${tmpTime[0]}:${tmpTime[1]}"
        binding.currentTimeTextView.text = time
        binding.currentDateTextView.text = date
        binding.nameTextView.text = name
        val fragmentContext = requireContext()
        if(settings.repetitions <= 0) {
            binding.repeatButton.visibility = View.GONE
        }
        else {
            binding.pulsator.start()
            binding.repeatButton.setOnClickListener {
                settings.repetitions -= 1
                dropAndRepeatFragment()
            }
        }
        binding.slideButton.onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener {
            override fun onSlideComplete(view: SlideToActView) {
                lifecycleScope.launch {
                    MyAlarmManager(fragmentContext, alarmPlug, Settings(0)).endProcess()
                }
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        return binding.root
    }

    fun dropAndRepeatFragment() {
        lifecycleScope.launch {
                val ctx = requireContextOrNull()
                if (ctx == null) {
                    Log.e("dropAndRepeatFragment", "Context is null")
                    return@launch
                }
                MyAlarmManager(ctx, alarmPlug, Settings(0)).endProcess()
                MyAlarmManager(ctx, alarmPlug, settings!!).repeatProcess()
                requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    // Helper function to safely get context
    private fun Fragment.requireContextOrNull(): Context? {
        return if (isAdded) requireContext() else null
    }

    private fun selectMelody(settings: Settings) {
        mediaPlayer = when(settings.melody) {
            getString(R.string.melody1) -> MediaPlayer.create(context, R.raw.default_signal1)
            getString(R.string.melody2) -> MediaPlayer.create(context, R.raw.default_signal2)
            getString(R.string.melody3) -> MediaPlayer.create(context, R.raw.default_signal3)
            getString(R.string.melody4) -> MediaPlayer.create(context, R.raw.default_signal4)
            getString(R.string.melody5) -> MediaPlayer.create(context, R.raw.default_signal5)
            getString(R.string.melody6) -> MediaPlayer.create(context, R.raw.signal)
            getString(R.string.melody7) -> MediaPlayer.create(context, R.raw.banjo_signal)
            getString(R.string.melody8) -> MediaPlayer.create(context, R.raw.morning_signal)
            getString(R.string.melody9) -> MediaPlayer.create(context, R.raw.simple_signal)
            getString(R.string.melody10) -> MediaPlayer.create(context, R.raw.fitness_signal)
            getString(R.string.melody11) -> MediaPlayer.create(context, R.raw.medieval_signal)
            getString(R.string.melody12) -> MediaPlayer.create(context, R.raw.introduction_signal)
            else -> MediaPlayer.create(context, R.raw.default_signal1)
        }
        mediaPlayer.isLooping = true
        mediaPlayer.start()
    }

    private fun startVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = requireContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            requireContext().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        val pattern = longArrayOf(0, 100, 300, 200, 250, 300, 200, 400, 150, 300, 150, 200)
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))

    }
    override fun onDestroyView() {
        super.onDestroyView()
        mediaPlayer.stop()
        if(settings!!.vibration == 1) vibrator.cancel()
    }
}
const val LOCAL_BROADCAST_KEY2 = "alarm_update"