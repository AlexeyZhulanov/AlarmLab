package com.example.alarm.model;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.icu.util.Calendar;
import android.icu.util.ULocale;
import android.util.Log;
import android.widget.Toast;

import com.example.alarm.AlarmReceiver;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyAlarmManager {
    private final Context context;
    private final Alarm alarm;
    private final Settings settings;
    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;
    private final Calendar calendar = Calendar.getInstance();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public MyAlarmManager(Context context, Alarm alarm, Settings settings) {
        this.context = context;
        this.alarm = alarm;
        this.settings = settings;
    }

    private void initialFunc(boolean isEnd) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        if (!isEnd) {
            intent.putExtra("alarmName", alarm.getName());
            intent.putExtra("alarmId", alarm.getId());
            intent.putExtra("settings", settings);
        }
        intent.setAction("com.example.alarm.ALARM_TRIGGERED");
        alarmIntent = PendingIntent.getBroadcast(
                context,
                (int) alarm.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    public void startProcess() {
        executor.execute(() -> {
            initialFunc(false);
            calendar.set(Calendar.HOUR_OF_DAY, alarm.getTimeHours());
            calendar.set(Calendar.MINUTE, alarm.getTimeMinutes());
            calendar.set(Calendar.SECOND, 0);
            Calendar calendar2 = Calendar.getInstance(ULocale.ROOT);
            long longTime = (calendar2.getTimeInMillis() > calendar.getTimeInMillis()) ?
                    calendar.getTimeInMillis() + 86400000 :
                    calendar.getTimeInMillis();

            alarmManager.setAlarmClock(
                    new AlarmManager.AlarmClockInfo(longTime, alarmIntent),
                    alarmIntent
            );

            int minutes = (calendar2.getTimeInMillis() > calendar.getTimeInMillis()) ?
                    (int) ((longTime - calendar2.getTimeInMillis()) / 60000) :
                    (int) ((calendar.getTimeInMillis() - calendar2.getTimeInMillis()) / 60000);

            String str;
            if (minutes == 0) {
                str = "Звонок менее чем через 1 мин.";
            } else if (minutes <= 59) {
                str = "Звонок через " + minutes + " мин.";
            } else {
                int hours = minutes / 60;
                str = "Звонок через " + hours + " ч. " + (minutes % 60) + " мин.";
            }
            Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
        });
    }

    public void endProcess() {
        executor.execute(() -> {
            initialFunc(true);
            alarmManager.cancel(alarmIntent);
        });
    }

    public void restartProcess() {
        executor.execute(() -> {
            endProcess();
            startProcess();
        });
    }

    public void repeatProcess() {
        executor.execute(() -> {
            initialFunc(false);
            Calendar calendar = Calendar.getInstance(ULocale.ROOT);
            long time = calendar.getTimeInMillis() + settings.getInterval() * 60000;
            alarmManager.setAlarmClock(
                    new AlarmManager.AlarmClockInfo(time, alarmIntent),
                    alarmIntent
            );
        });
    }
}
