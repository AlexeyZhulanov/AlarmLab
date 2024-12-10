package com.example.alarm.retrofit;

public class AlarmRequestEntity {
    private int id;
    private int hours;
    private int minutes;
    private String name;

    public AlarmRequestEntity(int id, int hours, int minutes, String name) {
        this.id = id;
        this.hours = hours;
        this.minutes = minutes;
        this.name = name;
    }
}
