package com.example.alarm;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.alarm.model.AlarmService;

import javax.inject.Inject;

public class ViewModelFactory implements ViewModelProvider.Factory {
    private final AlarmService alarmService;

    @Inject
    public ViewModelFactory(AlarmService alarmService) {
        this.alarmService = alarmService;
    }

    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AlarmViewModel.class)) {
            return (T) new AlarmViewModel(alarmService);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
