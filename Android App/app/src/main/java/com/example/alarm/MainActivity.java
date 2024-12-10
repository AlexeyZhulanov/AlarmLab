package com.example.alarm;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.example.alarm.databinding.ActivityMainBinding;
import com.example.alarm.model.Alarm;
import com.example.alarm.model.AlarmService;
import com.example.alarm.model.RetrofitService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    @Inject
    AlarmService alarmsService;
    @Inject
    RetrofitService retrofitService;

    private final ExecutorService uiExecutor = Executors.newSingleThreadExecutor();
    public static String APP_PREFERENCES = "APP_PREFERENCES";
    public static String PREF_THEME = "PREF_THEME";

    private final FragmentManager.FragmentLifecycleCallbacks fragmentListener = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, Bundle savedInstanceState) {
            super.onFragmentViewCreated(fm, f, v, savedInstanceState);
        }
    };

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1;

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
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
        checkOverlayPermission(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragmentContainer, new AlarmFragment(), "ALARM_FRAGMENT_TAG")
                    .commit();
        }
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(fragmentListener, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSupportFragmentManager().unregisterFragmentLifecycleCallbacks(fragmentListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.alarm_menu, menu);
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(androidx.appcompat.R.attr.colorAccent, typedValue, true);
        int color = typedValue.data;
        applyMenuTextColor(menu, color);
        return true;
    }

    private void applyMenuTextColor(Menu menu, int color) {
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            SpannableString spannableTitle = new SpannableString(menuItem.getTitle());
            spannableTitle.setSpan(new ForegroundColorSpan(color), 0, spannableTitle.length(), 0);
            menuItem.setTitle(spannableTitle);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return switch (item.getItemId()) {
            case 2131231129 -> {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new SettingsFragment())
                        .addToBackStack("settings")
                        .commit();
                yield true;
            }
            case 2131231112 -> {
                List<Alarm> list = new ArrayList<>();
                for(Alarm alarm : alarmsService.alarms) {
                    if(alarm.getEnabled()) {
                        Pair<Boolean, String> result = retrofitService.deleteAlarm((int)alarm.getId());
                        if(result.first) {
                            list.add(alarm);
                        }
                    }
                }
                alarmsService.offAlarms(list);
                ((AlarmFragment) getSupportFragmentManager().findFragmentByTag("ALARM_FRAGMENT_TAG")).fillAndUpdateBar();
                yield true;
            }
            default -> super.onOptionsItemSelected(item);
        };
    }

    private void checkOverlayPermission(Context context) {
        if (!Settings.canDrawOverlays(context)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Необходимы разрешения")
                    .setMessage("Пожалуйста, предоставьте разрешение на наложение поверх других приложений для правильной работы будильника.")
                    .setPositiveButton("Настройки", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
                        context.startActivity(intent);
                    })
                    .setNegativeButton("Отмена", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }
}
