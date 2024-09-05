package com.example.alarm;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import androidx.lifecycle.lifecycleScope;
import com.example.alarm.databinding.ActivitySignalBinding;
import dagger.hilt.android.AndroidEntryPoint;
import kotlinx.coroutines.Job;
import kotlinx.coroutines.delay;
import kotlinx.coroutines.launch;

@AndroidEntryPoint
public class SignalActivity extends AppCompatActivity {

    private ActivitySignalBinding binding;
    private boolean isHomePressed = false;
    private Job homePressResetJob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeNumber = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE).getInt(PREF_THEME, 0);
        switch (themeNumber) {
            case 0: setTheme(R.style.Theme_Alarm); break;
            case 1: setTheme(R.style.Theme1); break;
            case 2: setTheme(R.style.Theme2); break;
            case 3: setTheme(R.style.Theme3); break;
            case 4: setTheme(R.style.Theme4); break;
            case 5: setTheme(R.style.Theme5); break;
            case 6: setTheme(R.style.Theme6); break;
            case 7: setTheme(R.style.Theme7); break;
            case 8: setTheme(R.style.Theme8); break;
            default: setTheme(R.style.Theme_Alarm); break;
        }
        super.onCreate(savedInstanceState);
        binding = ActivitySignalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        @SuppressWarnings("deprecation")
                getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        );

        String alarmName = getIntent().getStringExtra("alarmName", "");
        long alarmId = getIntent().getLongExtra("alarmId", 0);
        Settings settings = IntentCompat.getParcelableExtra(getIntent(), "settings", Settings.class);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer2, new SignalFragment(alarmName, alarmId, settings))
                    .commit();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (getSupportFragmentManager().findFragmentById(R.id.fragmentContainer2) instanceof SignalFragment) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                ((SignalFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer2)).dropAndRepeatFragment();
            }
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (getSupportFragmentManager().findFragmentById(R.id.fragmentContainer2) instanceof SignalFragment) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                ((SignalFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer2)).dropAndRepeatFragment();
            }
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isHomePressed = true;
        if (homePressResetJob != null) {
            homePressResetJob.cancel();
        }
        homePressResetJob = lifecycleScope.launch(() -> {
            delay(1000);
            isHomePressed = false;
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isHomePressed) {
            ((SignalFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainer2)).dropAndRepeatFragment();
        }
    }
}
