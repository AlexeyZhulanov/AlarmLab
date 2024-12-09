package com.example.alarm.retrofit;

public class AlarmRequestEntity {
    private int hours;
    private int minutes;

    public AlarmRequestEntity(int hours, int minutes) {
        this.hours = hours;
        this.minutes = minutes;
    }
}
