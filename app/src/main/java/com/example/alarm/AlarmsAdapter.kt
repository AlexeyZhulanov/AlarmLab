package com.example.alarm

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Switch
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.alarm.databinding.ItemAlarmBinding
import com.example.alarm.model.Alarm
import com.google.android.material.switchmaterial.SwitchMaterial

interface AlarmActionListener {
    fun onAlarmEnabled(alarm: Alarm)
    fun onAlarmDelete(alarm: Alarm)
    fun onAlarmChange(alarm: Alarm)
}

class AlarmsAdapter(
    private val actionListener: AlarmActionListener
) : RecyclerView.Adapter<AlarmsAdapter.AlarmsViewHolder>(), View.OnClickListener, View.OnLongClickListener {

    var alarms: List<Alarm> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

    var isLongClicked: Boolean = false

    override fun onClick(v: View) {
        val alarm = v.tag as Alarm
        when(v.id) {
            R.id.switch1 -> {
                actionListener.onAlarmEnabled(alarm)
            }
            R.id.checkBox -> {
                actionListener.onAlarmDelete(alarm)
            }
            else -> {
                actionListener.onAlarmChange(alarm)
            }
        }
    }

    override fun onLongClick(v: View?): Boolean {
        isLongClicked = true
        //need to call function invisible visible all elements
        return true
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAlarmBinding.inflate(inflater, parent, false)

        binding.root.setOnClickListener(this) //list<alarm> element
        binding.root.setOnLongClickListener(this)
        binding.switch1.setOnClickListener(this)
        binding.root.isHapticFeedbackEnabled = true //for test
        return AlarmsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmsViewHolder, position: Int) {
        val alarm = alarms[position]
        with(holder.binding) {
            holder.itemView.tag = alarm
            switch1.tag = alarm
            checkBox.tag = alarm
            var tm = ""
            if(alarm.timeMinutes == 0) tm = "0"
            val txt: String = "${alarm.timeHours}:${alarm.timeMinutes}${tm}"
            timeTextView.text = txt
            switch1.isChecked = alarm.enabled == 1
        }
    }

    override fun getItemCount(): Int = alarms.size

    class AlarmsViewHolder(
        val binding: ItemAlarmBinding
    ) : RecyclerView.ViewHolder(binding.root)
}