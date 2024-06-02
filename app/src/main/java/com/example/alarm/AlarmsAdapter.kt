package com.example.alarm

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.alarm.databinding.ItemAlarmBinding
import com.example.alarm.model.Alarm

interface AlarmActionListener {
    fun onAlarmEnabled(alarm: Alarm)
    fun onAlarmDelete(alarm: Alarm)
    fun onAlarmChange(alarm: Alarm)
}

class AlarmsAdapter(
    private val actionListener: AlarmActionListener
) : RecyclerView.Adapter<AlarmsAdapter.AlarmsViewHolder>(), View.OnClickListener {

    var alarms: List<Alarm> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(newValue) {
            field = newValue
            notifyDataSetChanged()
        }

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
                //actionListener.onAlarmChange(alarm)
                showFullScreenDialog(v, alarm)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemAlarmBinding.inflate(inflater, parent, false)

        binding.root.setOnClickListener(this) //list<alarm> element
        binding.switch1.setOnClickListener(this)

        return AlarmsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmsViewHolder, position: Int) {
        val alarm = alarms[position]
        with(holder.binding) {
            holder.itemView.tag = alarm
            switch1.tag = alarm
            val txt: String = "${alarm.timeHours}:${alarm.timeMinutes}"
            timeTextView.text = txt
            switch1.isChecked = alarm.enabled
        }
    }

    override fun getItemCount(): Int = alarms.size

    private fun showFullScreenDialog(view: View, alarm: Alarm) {
        // todo
    }

    class AlarmsViewHolder(
        val binding: ItemAlarmBinding
    ) : RecyclerView.ViewHolder(binding.root)
}