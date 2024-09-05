package com.example.alarm;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.icu.util.Calendar;
import android.icu.util.ULocale;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.viewModels;
import androidx.lifecycle.lifecycleScope;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.alarm.databinding.FragmentAlarmBinding;
import com.example.alarm.model.Alarm;
import dagger.hilt.android.AndroidEntryPoint;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.Job;
import kotlinx.coroutines.async;
import kotlinx.coroutines.delay;
import kotlinx.coroutines.isActive;
import kotlinx.coroutines.launch;
import kotlinx.coroutines.withContext;

import java.util.HashMap;
import java.util.Map;

@AndroidEntryPoint
public class AlarmFragment extends Fragment {

    private AlarmsAdapter adapter;
    private FragmentAlarmBinding binding;
    private Job updateJob;
    private Map<Long, Long> millisToAlarm = new HashMap<>();
    private final AlarmViewModel alarmViewModel = new AlarmViewModel(); // You may need to use dependency injection

    @SuppressLint("DiscouragedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAlarmBinding.inflate(inflater, container, false);
        String[] prefs = alarmViewModel.getPreferencesWallpaperAndInterval(requireContext());
        String wallpaper = prefs[0];
        int interval = Integer.parseInt(prefs[1]);

        if (!wallpaper.isEmpty()) {
            int resId = requireContext().getResources().getIdentifier(wallpaper, "drawable", requireContext().getPackageName());
            if (resId != 0)
                binding.alarmLayout.setBackground(ContextCompat.getDrawable(requireContext(), resId));
        }

        adapter = new AlarmsAdapter(interval, new AlarmActionListener() {
            @Override
            public void onAlarmEnabled(Alarm alarm, int index) {
                lifecycleScope.launch(() -> {
                    int bool = 0;
                    if (alarm.enabled == 0) {
                        bool = 1;
                        changeAlarmTime(alarm, false);
                        binding.barTextView.setText(updateBar());
                    } else {
                        changeAlarmTime(alarm, true);
                        binding.barTextView.setText(updateBar());
                    }
                    int idx = async(Dispatchers.IO, () -> alarmViewModel.updateEnabledAlarm(alarm, bool, requireContext(), index));
                    adapter.notifyItemChanged(idx);
                });
            }

            @Override
            public void onAlarmChange(Alarm alarm) {
                BottomSheetFragment fragment = new BottomSheetFragment(false, alarm, new BottomSheetListener() {
                    @Override
                    public void onAddAlarm(Alarm alarm) {}

                    @Override
                    public void onChangeAlarm(Alarm alarmOld, Alarm alarmNew) {
                        if (alarmNew.enabled == 1) {
                            changeAlarmTime(alarmOld, true);
                            changeAlarmTime(alarmNew, false);
                            binding.barTextView.setText(updateBar());
                        }
                    }
                });
                fragment.show(getChildFragmentManager(), "ChangeTag");
            }

            @Override
            public void onAlarmLongClicked() {
                binding.floatingActionButtonAdd.setVisibility(View.GONE);
                binding.floatingActionButtonDelete.setVisibility(View.VISIBLE);
                requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (!adapter.canLongClick) {
                            adapter.clearPositions();
                            adapter.notifyDataSetChanged();
                            binding.floatingActionButtonDelete.setVisibility(View.GONE);
                            binding.floatingActionButtonAdd.setVisibility(View.VISIBLE);
                            alarmViewModel.getAndNotify();
                        } else {
                            // Removing this callback
                            remove();
                            requireActivity().getOnBackPressedDispatcher().onBackPressed();
                        }
                    }
                });

                binding.floatingActionButtonDelete.setOnClickListener(v -> {
                    List<Alarm> alarmsToDelete = adapter.getDeleteList();
                    if (!alarmsToDelete.isEmpty()) {
                        alarmViewModel.deleteAlarms(alarmsToDelete, getContext());
                        for (Alarm a : alarmsToDelete) {
                            if (a.enabled == 1) changeAlarmTime(a, true);
                        }
                        binding.barTextView.setText(updateBar());
                        binding.floatingActionButtonDelete.setVisibility(View.GONE);
                        binding.floatingActionButtonAdd.setVisibility(View.VISIBLE);
                        adapter.clearPositions();
                    }
                });
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        binding.recyclerview.setLayoutManager(layoutManager);
        binding.recyclerview.setAdapter(adapter);
        binding.recyclerview.addItemDecoration(new VerticalSpaceItemDecoration(40));

        alarmViewModel.alarms.observe(getViewLifecycleOwner(), alarms -> {
            adapter.alarms = alarms;
        });

        ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.toolbar); // Adds a button

        binding.floatingActionButtonAdd.setOnClickListener(v -> {
            BottomSheetFragment fragment = new BottomSheetFragment(true, new Alarm(0), new BottomSheetListener() {
                @Override
                public void onAddAlarm(Alarm alarm) {
                    long id = 0;
                    for (Alarm a : adapter.alarms) {
                        if (a.timeHours == alarm.timeHours && a.timeMinutes == alarm.timeMinutes) {
                            id = a.id;
                            break;
                        }
                    }
                    Alarm alr = new Alarm(id, alarm.timeHours, alarm.timeMinutes, alarm.name, alarm.enabled);
                    changeAlarmTime(alr, false);
                    binding.barTextView.setText(updateBar());
                }

                @Override
                public void onChangeAlarm(Alarm alarmOld, Alarm alarmNew) {}
            });
            fragment.show(getChildFragmentManager(), "AddTag");
        });

        alarmViewModel.initCompleted.observe(getViewLifecycleOwner(), it -> {
            if (it) {
                updateJob = lifecycleScope.launch(() -> {
                    millisToAlarm = fillAlarmsTime();
                    while (isActive) {
                        binding.barTextView.setText(updateBar());
                        delay(30000);
                    }
                });
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (updateJob != null) {
            updateJob.cancel();
        }
    }

    private suspend Map<Long, Long> fillAlarmsTime() {
        return withContext(Dispatchers.Default, () -> {
            Map<Long, Long> map = new HashMap<>();
            Calendar calendar = Calendar.getInstance();
            Calendar calendar2 = Calendar.getInstance(ULocale.ROOT);
            for (Alarm alr : adapter.alarms) {
                if (alr.enabled == 1) {
                    calendar.set(Calendar.HOUR_OF_DAY, alr.timeHours);
                    calendar.set(Calendar.MINUTE, alr.timeMinutes);
                    calendar.set(Calendar.SECOND, 0);
                    long longTime = (calendar2.getTimeInMillis() > calendar.getTimeInMillis())
                            ? calendar.getTimeInMillis() + 86400000
                            : calendar.getTimeInMillis();
                    map.put(alr.id, longTime);
                }
            }
            return map.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, HashMap::new));
        });
    }

    private void changeAlarmTime(Alarm alarm, boolean isDisable) {
        if (isDisable) {
            millisToAlarm.remove(alarm.id);
        } else {
            Calendar calendar = Calendar.getInstance();
            Calendar calendar2 = Calendar.getInstance(ULocale.ROOT);
            calendar.set(Calendar.HOUR_OF_DAY, alarm.timeHours);
            calendar.set(Calendar.MINUTE, alarm.timeMinutes);
            calendar.set(Calendar.SECOND, 0);
            long longTime = (calendar2.getTimeInMillis() > calendar.getTimeInMillis())
                    ? calendar.getTimeInMillis() + 86400000
                    : calendar.getTimeInMillis();
            millisToAlarm.put(alarm.id, longTime);
        }
        millisToAlarm = millisToAlarm.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    private String updateBar() {
        StringBuilder txt = new StringBuilder();
        if (millisToAlarm.isEmpty()) {
            txt.append("Все сигналы\nвыключены");
        } else {
            Calendar calendar = Calendar.getInstance(ULocale.ROOT);
            long longTime = millisToAlarm.entrySet().iterator().next().getValue();
            int minutes = (int) ((longTime - calendar.getTimeInMillis()) / 60000);
            switch (minutes) {
                case 0:
                    txt.append("Звонок менее чем через 1 мин.");
                    break;
                case 1, 59 -> txt.append("Звонок через\n").append(minutes).append(" мин.");
                default -> {
                    int hours = minutes / 60;
                    txt.append("Звонок через\n").append(hours).append(" ч. ").append(minutes % 60).append(" мин.");
                }
            }
        }
        return txt.toString();
    }

    @SuppressWarnings("Unused")
    public void fillAndUpdateBar() {
        withContext(Dispatchers.Default, () -> {
            millisToAlarm = fillAlarmsTime();
            binding.barTextView.setText(updateBar());
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (updateJob != null) {
            updateJob.cancel();
        }
    }
}

class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {
    private final int verticalSpaceHeight;

    public VerticalSpaceItemDecoration(int verticalSpaceHeight) {
        this.verticalSpaceHeight = verticalSpaceHeight;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.bottom = verticalSpaceHeight;
    }
}
