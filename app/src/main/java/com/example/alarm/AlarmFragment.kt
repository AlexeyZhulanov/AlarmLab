package com.example.alarm

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.icu.util.Calendar
import android.icu.util.ULocale
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.toMutableStateMap
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alarm.databinding.FragmentAlarmBinding
import com.example.alarm.model.Alarm
import com.example.alarm.model.AlarmService
import com.example.alarm.model.AlarmsListener
import com.example.alarm.model.MyAlarmManager
import com.example.alarm.model.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlarmFragment : Fragment() {

    private lateinit var adapter: AlarmsAdapter

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)
    private val alarmsService: AlarmService
        get() = Repositories.alarmRepository as AlarmService
    private lateinit var settings: Settings

    private var millisToAlarm = mutableMapOf<Long, Long>()
    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val name = it.getStringExtra("alarmName")
                val id = it.getLongExtra("alarmId", 0)
                Log.d("testWork2", "YESYES")
                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, SignalFragment(name!!, id))
                    .addToBackStack("signal")
                    .commit()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
            Repositories.init(requireActivity().applicationContext)
            val binding = FragmentAlarmBinding.inflate(inflater, container, false)
//            uiScope.launch {
//                settings = alarmsService.getSettings()
//                Log.d("testSettings", settings.toString())
//            }
        settings = Settings(0)
            adapter = AlarmsAdapter(settings, object : AlarmActionListener {
                override fun onAlarmEnabled(alarm: Alarm, index: Int) {
                    uiScope.launch {
                        var bool = 0
                        if (alarm.enabled == 0) { //turn on
                            bool = 1
                            MyAlarmManager(context, alarm).startProcess()
                            changeAlarmTime(alarm, false)
                            binding.barTextView.text = updateBar()
                        } else {
                            MyAlarmManager(context, alarm).endProcess()
                            changeAlarmTime(alarm, true)
                            binding.barTextView.text = updateBar()
                        }
                        alarmsService.updateEnabled(alarm.id, bool)
                        adapter.notifyItemChanged(index)
                    }
                }

                override fun onAlarmChange(alarm: Alarm) {
                    BottomSheetFragment(false, alarm, object : BottomSheetListener {
                        override fun onAddAlarm(alarm: Alarm) { return }
                        override fun onChangeAlarm(alarmOld: Alarm, alarmNew: Alarm) {
                            if(alarmNew.enabled == 1) {
                                changeAlarmTime(alarmOld, true)
                                changeAlarmTime(alarmNew, false)
                                binding.barTextView.text = updateBar()
                            }
                        }
                    }).show(childFragmentManager, "ChangeTag")
                }

                override fun onAlarmLongClicked() {
                    binding.floatingActionButtonAdd.visibility = View.GONE
                    binding.floatingActionButtonDelete.visibility = View.VISIBLE
                    requireActivity().onBackPressedDispatcher.addCallback(
                        viewLifecycleOwner,
                        object : OnBackPressedCallback(true) {
                            @SuppressLint("NotifyDataSetChanged")
                            override fun handleOnBackPressed() {
                                if (!adapter.canLongClick) {
                                    adapter.clearPositions()
                                    adapter.notifyDataSetChanged()
                                    binding.floatingActionButtonDelete.visibility = View.GONE
                                    binding.floatingActionButtonAdd.visibility = View.VISIBLE
                                    uiScope.launch {
                                        alarmsService.getAlarms()
                                        alarmsService.notifyChanges()
                                    }
                                } else {
                                    //Removing this callback
                                    remove()
                                    requireActivity().onBackPressedDispatcher.onBackPressed()
                                }
                            }
                        })
                    binding.floatingActionButtonDelete.setOnClickListener {
                        val alarmsToDelete = adapter.getDeleteList()
                        if (alarmsToDelete.isNotEmpty()) {
                            uiScope.launch {
                                alarmsService.deleteAlarms(alarmsToDelete, context)
                                for(a in alarmsToDelete) {
                                    if(a.enabled == 1) changeAlarmTime(a, true)
                                }
                                binding.barTextView.text = updateBar()
                            }
                            binding.floatingActionButtonDelete.visibility = View.GONE
                            binding.floatingActionButtonAdd.visibility = View.VISIBLE
                            adapter.clearPositions()

                        }
                    }
                }
            })
        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerview.layoutManager = layoutManager
        binding.recyclerview.adapter = adapter
        uiScope.launch {
            millisToAlarm = fillAlarmsTime()
            while (true) {
                binding.barTextView.text = updateBar()
                delay(30000)
            }
        }

        alarmsService.addListener(alarmsListener)
        (activity as AppCompatActivity?)!!.setSupportActionBar(binding.toolbar) //adds a button

        binding.floatingActionButtonAdd.setOnClickListener {
            BottomSheetFragment(true, Alarm(0), object : BottomSheetListener {
                override fun onAddAlarm(alarm: Alarm) {
                    uiScope.launch {
                        var id: Long = 0
                        for (a in adapter.alarms) {
                            if (a.timeHours == alarm.timeHours && a.timeMinutes == alarm.timeMinutes) {
                                id = a.id
                                break
                            }
                        }
                        val alr = Alarm(
                            id = id,
                            timeHours = alarm.timeHours,
                            timeMinutes = alarm.timeMinutes,
                            name = alarm.name,
                            enabled = alarm.enabled
                        )
                        changeAlarmTime(alr, false)
                        binding.barTextView.text = updateBar()
                    }
                }

                override fun onChangeAlarm(alarmOld: Alarm, alarmNew: Alarm) { return }
            }).show(childFragmentManager, "AddTag")
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        alarmsService.removeListener(alarmsListener)
    }
    private val alarmsListener: AlarmsListener = {
        adapter.alarms = it
    }

    private suspend fun fillAlarmsTime() : MutableMap<Long, Long> = withContext(Dispatchers.Default){
        val map = mutableMapOf<Long, Long>()
        val calendar = Calendar.getInstance()
        val calendar2 = Calendar.getInstance(ULocale.ROOT)
        for(alr in adapter.alarms) {
            if(alr.enabled == 1) {
                calendar.set(Calendar.HOUR_OF_DAY, alr.timeHours)
                calendar.set(Calendar.MINUTE, alr.timeMinutes)
                calendar.set(Calendar.SECOND, 0)
                val longTime: Long = if (calendar2.timeInMillis > calendar.timeInMillis) {
                    calendar.timeInMillis + 86400000
                } else calendar.timeInMillis
                map[alr.id] = longTime
            }
        }
        val sortedMap = map.toList().sortedBy { it.second }.toMap().toMutableMap()
        return@withContext sortedMap
    }

    private fun changeAlarmTime(alarm: Alarm, isDisable: Boolean) {
        if(isDisable) {
            millisToAlarm.remove(alarm.id)
        }
        else {
            val calendar = Calendar.getInstance()
            val calendar2 = Calendar.getInstance(ULocale.ROOT)
            calendar.set(Calendar.HOUR_OF_DAY, alarm.timeHours)
            calendar.set(Calendar.MINUTE, alarm.timeMinutes)
            calendar.set(Calendar.SECOND, 0)
            val longTime: Long = if (calendar2.timeInMillis > calendar.timeInMillis) {
                calendar.timeInMillis + 86400000
            } else calendar.timeInMillis
            millisToAlarm[alarm.id] = longTime
            millisToAlarm = millisToAlarm.toList().sortedBy { it.second }.toMap().toMutableMap()
        }
    }
    private fun updateBar(): String {
        var txt: String = ""
        if(millisToAlarm.isEmpty()) txt += "Все сигналы\nвыключены"
        else {
            val calendar = Calendar.getInstance(ULocale.ROOT)
            val longTime: Long = millisToAlarm.entries.first().value
            val minutes: Int = if (calendar.timeInMillis > longTime) {
                ((longTime - calendar.timeInMillis) / 60000).toInt()
            } else ((longTime - calendar.timeInMillis) / 60000).toInt()
            when(minutes) {
                0 -> txt += "Сработает менее чем через 1 мин."
                in 1..59 -> txt += "Сработает через\n$minutes мин."
                else -> {
                    val hours = minutes / 60
                    txt += "Сработает через\n$hours ч. ${minutes % 60} мин."
                }
            }
        }
        return txt
    }

    private fun registerBroadCastReceiver() {
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            receiver,
            IntentFilter(LOCAL_BROADCAST_KEY)
        )
    }

    override fun onResume() {
        super.onResume()
        registerBroadCastReceiver()
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
    }
}