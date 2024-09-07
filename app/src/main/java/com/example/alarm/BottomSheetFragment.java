package com.example.alarm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

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
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final BottomSheetListener bottomSheetListener;
    private FragmentBottomsheetBinding binding;
    private final AlarmViewModel alarmViewModel = new ViewModelProvider(this).get(AlarmViewModel.class);

    public BottomSheetFragment(boolean isAdd, Alarm oldAlarm, BottomSheetListener bottomSheetListener) {
        this.isAdd = isAdd;
        this.oldAlarm = oldAlarm;
        this.bottomSheetListener = bottomSheetListener;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBottomsheetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private void addNewAlarm() {
        Alarm alarm = new Alarm(0);
        alarm.setTimeHours(binding.timePicker.getHour());
        alarm.setTimeMinutes(binding.timePicker.getMinute());
        alarm.setName(binding.signalName.getText().toString().isEmpty() ? "default" : binding.signalName.getText().toString());
        alarm.setEnabled(1);

        executorService.execute(() -> {
            alarmViewModel.addAlarm(alarm, requireContext(), new AlarmCallback() {
                @Override
                public void onResult(boolean result) {
                    if(result) {
                        bottomSheetListener.onAddAlarm(alarm);
                    } else {
                        Toast.makeText(getContext(), getString(R.string.error_is_exist), Toast.LENGTH_SHORT).show();
                    }
                    dismiss();
                }
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
            alarmViewModel.updateAlarm(alarmNew, requireContext(), new AlarmCallback() {
                @Override
                public void onResult(boolean result) {
                    if(result) {
                        bottomSheetListener.onChangeAlarm(oldAlarm, alarmNew);
                    } else {
                        Toast.makeText(getContext(), getString(R.string.error_is_exist), Toast.LENGTH_SHORT).show();
                    }
                    dismiss();
                }
            });
        });
    }
}
