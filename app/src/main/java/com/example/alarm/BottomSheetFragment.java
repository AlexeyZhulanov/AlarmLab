package com.example.alarm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.fragment.app.viewModels;
import androidx.lifecycle.lifecycleScope;
import com.example.alarm.databinding.FragmentBottomsheetBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import dagger.hilt.android.AndroidEntryPoint;
import kotlinx.coroutines.launch;

@AndroidEntryPoint
public class BottomSheetFragment extends BottomSheetDialogFragment {
    private final boolean isAdd;
    private final Alarm oldAlarm;
    private final BottomSheetListener bottomSheetListener;
    private FragmentBottomsheetBinding binding;
    private final AlarmViewModel alarmViewModel = new AlarmViewModel(); // You may need to use dependency injection

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
            binding.timePicker.setHour(oldAlarm.timeHours);
            binding.timePicker.setMinute(oldAlarm.timeMinutes);
            if (!oldAlarm.name.equals("default") && !oldAlarm.name.isEmpty()) {
                binding.signalName.setText(oldAlarm.name);
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
        Alarm alarm = new Alarm(
                0,
                binding.timePicker.getHour(),
                binding.timePicker.getMinute(),
                binding.signalName.getText().toString().isEmpty() ? "default" : binding.signalName.getText().toString(),
                1
        );
        lifecycleScope.launch(() -> {
            if (alarmViewModel.addAlarm(alarm, requireContext())) {
                bottomSheetListener.onAddAlarm(alarm);
            } else {
                Toast.makeText(getContext(), getString(R.string.error_is_exist), Toast.LENGTH_SHORT).show();
            }
            dismiss();
        });
    }

    private void changeAlarm(Alarm oldAlarm) {
        Alarm alarmNew = new Alarm(
                oldAlarm.id,
                binding.timePicker.getHour(),
                binding.timePicker.getMinute(),
                binding.signalName.getText().toString().isEmpty() ? "default" : binding.signalName.getText().toString(),
                oldAlarm.enabled
        );
        lifecycleScope.launch(() -> {
            if (alarmViewModel.updateAlarm(alarmNew, requireContext())) {
                bottomSheetListener.onChangeAlarm(oldAlarm, alarmNew);
            } else {
                Toast.makeText(getContext(), getString(R.string.error_is_exist), Toast.LENGTH_SHORT).show();
            }
            dismiss();
        });
    }
}
