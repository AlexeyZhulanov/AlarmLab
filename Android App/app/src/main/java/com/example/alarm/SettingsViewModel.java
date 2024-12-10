package com.example.alarm;

import android.content.SharedPreferences;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.alarm.model.AlarmService;
import com.example.alarm.model.Settings;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SettingsViewModel extends ViewModel {
    private static final String PREF_INTERVAL = "PREF_INTERVAL";
    private static final String PREF_WALLPAPER = "PREF_WALLPAPER";
    private static final String PREF_THEME = "PREF_THEME";
    private final AlarmService alarmsService;
    private final SharedPreferences preferences;

    private final MutableLiveData<String> _wallpaper = new MutableLiveData<>();
    public LiveData<String> wallpaper = _wallpaper;

    @Inject
    public SettingsViewModel(AlarmService alarmsService, SharedPreferences preferences) {
        this.alarmsService = alarmsService;
        this.preferences = preferences;
    }

    public int getPreferencesTheme() {
        return preferences.getInt(PREF_THEME, 0);
    }

    public void editPreferencesWallpaper(String wallpaper) {
        preferences.edit().putString(PREF_WALLPAPER, wallpaper).apply();
    }

    public void editPreferencesInterval(int interval) {
        preferences.edit().putInt(PREF_INTERVAL, interval).apply();
    }

    public void editPreferencesTheme(int theme) {
        preferences.edit().putInt(PREF_THEME, theme).apply();
    }

    public void registerPreferences() {
        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    public void unregisterPreferences() {
        preferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = (sharedPreferences, key) -> {
        if (key != null && key.equals(PREF_WALLPAPER)) {
            String tmp = sharedPreferences.getString(PREF_WALLPAPER, "");
            _wallpaper.postValue(tmp);
        }
    };

    public Settings getSettings() {
        return alarmsService.getSettings();
    }

    public void updateSettings(Settings settings) {
        alarmsService.updateSettings(settings);
    }

    public void getPreferencesWallpaper(PreferenceCallback callback) {
        String wallpaper = preferences.getString(PREF_WALLPAPER, "");
        callback.onResult(new Pair<>(wallpaper, 0));
    }
}
