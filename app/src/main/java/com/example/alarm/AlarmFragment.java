package com.example.alarm;

import android.annotation.SuppressLint;
import android.icu.util.Calendar;
import android.icu.util.ULocale;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.alarm.databinding.FragmentAlarmBinding;
import com.example.alarm.model.Alarm;
import dagger.hilt.android.AndroidEntryPoint;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@AndroidEntryPoint
public class AlarmFragment extends Fragment {

    private AlarmsAdapter adapter;
    private FragmentAlarmBinding binding;
    private Future<?> updateJob;
    private int interval = 0;
    private String wallpaper = "";
    private Map<Long, Long> millisToAlarm = new HashMap<>();
    private AlarmViewModel alarmViewModel;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @SuppressLint("DiscouragedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        alarmViewModel = new ViewModelProvider(this).get(AlarmViewModel.class);
        binding = FragmentAlarmBinding.inflate(inflater, container, false);
        alarmViewModel.getPreferencesWallpaperAndInterval(requireContext(), new PreferenceCallback() {
            @Override
            public void onResult(Pair<String, Integer> result) {
                wallpaper = result.first;
                interval = result.second;
            }
        });

        if (!Objects.equals(wallpaper, "")) {
            int resId = requireContext().getResources().getIdentifier(wallpaper, "drawable", requireContext().getPackageName());
            if (resId != 0) {
                binding.alarmLayout.setBackground(ContextCompat.getDrawable(requireContext(), resId));
            }
        }

        adapter = new AlarmsAdapter(interval, new AlarmActionListener() {
            @Override
            public void onAlarmEnabled(Alarm alarm, int index) {
                changeAlarmTime(alarm);
                binding.barTextView.setText(updateBar());

                alarmViewModel.updateEnabledAlarm(alarm, !alarm.getEnabled(), result -> {
                    if(result) {
                        adapter.notifyItemChanged(index);
                    }
                });
            }

            @Override
            public void onAlarmChange(Alarm alarm) {
                BottomSheetFragment fragment = new BottomSheetFragment(alarmViewModel,false, alarm, new BottomSheetListener() {
                    @Override
                    public void onAddAlarm(Alarm alarm) {
                    }

                    @Override
                    public void onChangeAlarm(Alarm alarmOld, Alarm alarmNew) {
                        if (alarmNew.getEnabled()) {
                            changeAlarmTime(alarmOld);
                            changeAlarmTime(alarmNew);
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
                            if (a.getEnabled()) changeAlarmTime(a);
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

        alarmViewModel.alarms.observe(getViewLifecycleOwner(), alarms -> adapter.setAlarms(alarms));

        ((AppCompatActivity) requireActivity()).setSupportActionBar(binding.toolbar);

        binding.floatingActionButtonAdd.setOnClickListener(v -> {
            BottomSheetFragment fragment = new BottomSheetFragment(alarmViewModel,true, new Alarm(0), new BottomSheetListener() {
                @Override
                public void onAddAlarm(Alarm alarm) {
                    long id = 0;
                    for (Alarm a : adapter.alarms) {
                        if (a.getTimeHours() == alarm.getTimeHours() && a.getTimeMinutes() == alarm.getTimeMinutes()) {
                            id = a.getId();
                            break;
                        }
                    }
                    Alarm alr = new Alarm(id);
                    alr.setTimeHours(alarm.getTimeHours());
                    alr.setTimeMinutes(alarm.getTimeMinutes());
                    alr.setName(alarm.getName());
                    alr.setEnabled(alarm.getEnabled());
                    changeAlarmTime(alr);
                    binding.barTextView.setText(updateBar());
                }

                @Override
                public void onChangeAlarm(Alarm alarmOld, Alarm alarmNew) {
                }
            });
            fragment.show(getChildFragmentManager(), "AddTag");
        });

        alarmViewModel.getInitCompleted().observe(getViewLifecycleOwner(), it -> {
            if (it) {
                updateJob = executorService.submit(() -> {
                    try {
                        millisToAlarm = fillAlarmsTime();
                        while (!Thread.currentThread().isInterrupted()) {
                            binding.barTextView.post(() -> binding.barTextView.setText(updateBar()));
                            TimeUnit.SECONDS.sleep(30);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
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
            updateJob.cancel(true);
        }
    }

    private Map<Long, Long> fillAlarmsTime() {
        Map<Long, Long> map = new HashMap<>();
        Calendar calendar = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance(ULocale.ROOT);

        for (Alarm alr : adapter.alarms) {
            if (alr.getEnabled()) {
                calendar.set(Calendar.HOUR_OF_DAY, alr.getTimeHours());
                calendar.set(Calendar.MINUTE, alr.getTimeMinutes());
                calendar.set(Calendar.SECOND, 0);
                long longTime = (calendar2.getTimeInMillis() > calendar.getTimeInMillis())
                        ? calendar.getTimeInMillis() + 86400000
                        : calendar.getTimeInMillis();
                map.put(alr.getId(), longTime);
            }
        }

        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    private void changeAlarmTime(Alarm alarm) {
        Log.d("testChangeTimeBefore", millisToAlarm.toString());
        if (millisToAlarm.containsKey(alarm.getId())) {
            Log.d("testRemove", String.valueOf(alarm.getId()));
            millisToAlarm.remove(alarm.getId());
        } else {
            Calendar calendar = Calendar.getInstance();
            Calendar calendar2 = Calendar.getInstance(ULocale.ROOT);
            calendar.set(Calendar.HOUR_OF_DAY, alarm.getTimeHours());
            calendar.set(Calendar.MINUTE, alarm.getTimeMinutes());
            calendar.set(Calendar.SECOND, 0);
            long longTime = (calendar2.getTimeInMillis() > calendar.getTimeInMillis())
                    ? calendar.getTimeInMillis() + 86400000
                    : calendar.getTimeInMillis();
            millisToAlarm.put(alarm.getId(), longTime);
        }
        Log.d("testChangeTimeMid", millisToAlarm.toString());
        millisToAlarm = millisToAlarm.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        Log.d("testChangeTimeAfter", millisToAlarm.toString());
    }

    private String updateBar() {
        StringBuilder txt = new StringBuilder();
        if (millisToAlarm.isEmpty()) {
            txt.append("Все сигналы\nвыключены");
        } else {
            Calendar calendar = Calendar.getInstance(ULocale.ROOT);
            long longTime = millisToAlarm.entrySet().iterator().next().getValue();
            int minutes = (int) ((longTime - calendar.getTimeInMillis()) / 60000);

            if (minutes == 0) {
                txt.append("Звонок менее чем через 1 мин.");
            } else if (minutes < 60) {
                txt.append("Звонок через\n").append(minutes).append(" мин.");
            } else {
                int hours = minutes / 60;
                txt.append("Звонок через\n").append(hours).append(" ч. ").append(minutes % 60).append(" мин.");
            }
        }
        Log.d("testUpdateBar", txt.toString());
        return txt.toString();
    }

    public void fillAndUpdateBar() {
        millisToAlarm = fillAlarmsTime();
        binding.barTextView.setText(updateBar());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (updateJob != null) {
            updateJob.cancel(true); // Принудительно завершаем задачу
        }
    }
}

