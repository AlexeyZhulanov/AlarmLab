package com.example.alarm;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alarm.databinding.ItemAlarmBinding;
import com.example.alarm.model.Alarm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlarmsAdapter extends RecyclerView.Adapter<AlarmsAdapter.AlarmsViewHolder>
        implements View.OnClickListener, View.OnLongClickListener {

    private final int interval;
    private final AlarmActionListener actionListener;

    List<Alarm> alarms = new ArrayList<>();
    boolean canLongClick = true;
    private final Set<Integer> checkedPositions = new HashSet<>();
    private int index = 0;

    public AlarmsAdapter(int interval, AlarmActionListener actionListener) {
        this.interval = interval;
        this.actionListener = actionListener;
    }

    public void setAlarms(List<Alarm> alarms) {
        this.alarms = alarms;
        notifyDataSetChanged();
    }

    public List<Alarm> getDeleteList() {
        List<Alarm> list = new ArrayList<>();
        for (int i : checkedPositions) {
            list.add(alarms.get(i));
        }
        return list;
    }

    public void clearPositions() {
        canLongClick = true;
        checkedPositions.clear();
    }

    private void savePosition(Alarm alarm) {
        for (int i = 0; i < alarms.size(); i++) {
            if (alarms.get(i).equals(alarm)) {
                if (checkedPositions.contains(i)) {
                    checkedPositions.remove(i);
                } else {
                    checkedPositions.add(i);
                }
                break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        Alarm alarm = (Alarm) v.getTag();
        if(v.getId() == R.id.switch1) {
            if (canLongClick) {
                for (int i = 0; i < alarms.size(); i++) {
                    if (alarms.get(i).equals(alarm)) {
                        index = i;
                        break;
                    }
                }
                actionListener.onAlarmEnabled(alarm, index);
            }
        } else if (v.getId() == R.id.checkBox) {
            if (!canLongClick) {
                savePosition(alarm);
            }
        } else {
            if (canLongClick) {
                actionListener.onAlarmChange(alarm);
            } else {
                savePosition(alarm);
                notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (canLongClick) {
            Alarm alarm = (Alarm) v.getTag();
            savePosition(alarm);
            actionListener.onAlarmLongClicked();
            notifyDataSetChanged();
            canLongClick = false;
        }
        return true;
    }

    @NonNull
    @Override
    public AlarmsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemAlarmBinding binding = ItemAlarmBinding.inflate(inflater, parent, false);
        binding.getRoot().setOnClickListener(this);
        binding.getRoot().setOnLongClickListener(this);
        binding.switch1.setOnClickListener(this);
        binding.checkBox.setOnClickListener(this);
        return new AlarmsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmsViewHolder holder, int position) {
        Alarm alarm = alarms.get(position);
        holder.itemView.setTag(alarm);
        holder.binding.switch1.setTag(alarm);

        String tm = (alarm.getTimeMinutes() == 0) ? "0" : "";
        String tm2 = (alarm.getTimeMinutes() >= 1 && alarm.getTimeMinutes() <= 9) ? "0" : "";

        String timeText = alarm.getTimeHours() + ":" + tm2 + alarm.getTimeMinutes() + tm;
        holder.binding.timeTextView.setText(timeText);

        String intervalText = (alarm.getName().equals("default"))
                ? "раз в " + interval + " минут"
                : "<font color='#FF00FF'>" + alarm.getName() + "</font>";

        if (interval == 3 && alarm.getName().equals("default")) {
            intervalText += "ы";
        }

        holder.binding.intervalTextView.setText(Html.fromHtml(intervalText, Html.FROM_HTML_MODE_LEGACY));
        holder.binding.switch1.setChecked(alarm.getEnabled());

        if (!canLongClick) {
            holder.binding.checkBox.setTag(alarm);
            holder.binding.switch1.setVisibility(View.INVISIBLE);
            holder.binding.checkBox.setVisibility(View.VISIBLE);
            holder.binding.checkBox.setChecked(checkedPositions.contains(position));
        } else {
            holder.binding.switch1.setVisibility(View.VISIBLE);
            holder.binding.checkBox.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return alarms.size();
    }

    public static class AlarmsViewHolder extends RecyclerView.ViewHolder {
        final ItemAlarmBinding binding;

        public AlarmsViewHolder(ItemAlarmBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
