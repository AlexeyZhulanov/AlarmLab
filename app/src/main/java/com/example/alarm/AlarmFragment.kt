package com.example.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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
import kotlinx.coroutines.launch


class AlarmFragment : Fragment() {

    private lateinit var adapter: AlarmsAdapter

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)
    private val alarmsService: AlarmService
        get() = Repositories.alarmRepository as AlarmService



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Repositories.init(requireActivity().applicationContext)
        var settings: Settings = Settings(0)
        uiScope.launch { settings = alarmsService.getSettings() }
        val binding = FragmentAlarmBinding.inflate(inflater, container, false)
        adapter = AlarmsAdapter(settings , object: AlarmActionListener {
            @SuppressLint("ScheduleExactAlarm", "NotifyDataSetChanged")
            override fun onAlarmEnabled(alarm: Alarm, index: Int) {
                uiScope.launch {
                    var bool = 0
                    if(alarm.enabled == 0) { //turn on
                        bool = 1
                        MyAlarmManager(context, alarm).startProcess()
                    }
                    else {
                        MyAlarmManager(context, alarm).endProcess()
                    }
                    alarmsService.updateEnabled(alarm.id, bool)
                    adapter.notifyItemChanged(index)
                }
            }
            override fun onAlarmChange(alarm: Alarm) {
                BottomSheetFragment(false, alarm).show(childFragmentManager, "ChangeTag")
            }

            override fun onAlarmLongClicked() {
                binding.floatingActionButtonAdd.visibility = View.GONE
                binding.floatingActionButtonDelete.visibility = View.VISIBLE
                requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
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
                    if(alarmsToDelete.isNotEmpty()) {
                        uiScope.launch { alarmsService.deleteAlarms(alarmsToDelete) }
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

        alarmsService.addListener(alarmsListener)
        (activity as AppCompatActivity?)!!.setSupportActionBar(binding.toolbar) //adds a button

        binding.floatingActionButtonAdd.setOnClickListener {
            BottomSheetFragment(true, Alarm(0)).show(childFragmentManager, "AddTag")
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
}