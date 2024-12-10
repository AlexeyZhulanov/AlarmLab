package com.example.alarm;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.alarm.databinding.FragmentBottomsheetBinding;
import com.example.alarm.model.Alarm;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BottomSheetFragment extends BottomSheetDialogFragment {
    private final boolean isAdd;
    private final Alarm oldAlarm;
    private final AlarmViewModel alarmViewModel;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final BottomSheetListener bottomSheetListener;
    private FragmentBottomsheetBinding binding;

    public BottomSheetFragment(AlarmViewModel alarmViewModel, boolean isAdd, Alarm oldAlarm, BottomSheetListener bottomSheetListener) {
        this.alarmViewModel = alarmViewModel;
        this.isAdd = isAdd;
        this.oldAlarm = oldAlarm;
        this.bottomSheetListener = bottomSheetListener;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.timePicker.setIs24HourView(true);
        if (isAdd) {
            binding.timePicker.setHour(7);
            binding.timePicker.setMinute(0);
        } else {
            binding.heading.setText("Изменить будильник");
            binding.timePicker.setHour(oldAlarm.getTimeHours());
            binding.timePicker.setMinute(oldAlarm.getTimeMinutes());
            if (!oldAlarm.getName().equals("default") && !oldAlarm.getName().isEmpty()) {
                binding.signalName.setText(oldAlarm.getName());
            }
        }

        binding.confirmButton.setOnClickListener(v -> {
            if (isAdd) {
                addNewAlarm();
            } else {
                changeAlarm(oldAlarm);
            }
        });

        binding.cancelButton.setOnClickListener(v -> dismiss());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBottomsheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private void addNewAlarm() {
        Alarm alarm = new Alarm(0);
        alarm.setTimeHours(binding.timePicker.getHour());
        alarm.setTimeMinutes(binding.timePicker.getMinute());
        alarm.setName(binding.signalName.getText().toString().isEmpty() ? "default" : binding.signalName.getText().toString());
        alarm.setEnabled(true);
        executorService.execute(() -> {
            alarmViewModel.addAlarm(alarm, result -> {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(requireContext(), result.second, Toast.LENGTH_SHORT).show();
                    if (result.first) {
                        bottomSheetListener.onAddAlarm(alarm);
                    }
                    dismiss();
                });
            });
        });
    }

    private void changeAlarm(Alarm oldAlarm) {
        Alarm alarmNew = new Alarm(oldAlarm.getId());
        alarmNew.setTimeHours(binding.timePicker.getHour());
        alarmNew.setTimeMinutes(binding.timePicker.getMinute());
        alarmNew.setName(binding.signalName.getText().toString().isEmpty() ? "default" : binding.signalName.getText().toString());
        alarmNew.setEnabled(oldAlarm.getEnabled());

        executorService.execute(() -> {
            alarmViewModel.updateAlarm(alarmNew, result -> {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(requireContext(), result.second, Toast.LENGTH_SHORT).show();
                if(result.first) {
                    bottomSheetListener.onChangeAlarm(oldAlarm, alarmNew);
                }
                dismiss();
                });
            });
        });
    }
}
