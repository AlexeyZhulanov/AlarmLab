package com.example.alarm.model;

import android.os.Parcel;
import android.os.Parcelable;

import javax.inject.Inject;

public class Settings implements Parcelable {
    private long id;
    private String melody;
    private int vibration;
    private int interval;
    private int repetitions;
    private int disableType;

    @Inject
    public Settings(long id) {
        this.id = id;
        this.melody = "default";
        this.vibration = 1;
        this.interval = 5;
        this.repetitions = 3;
        this.disableType = 0;
    }

    protected Settings(Parcel in) {
        id = in.readLong();
        melody = in.readString();
        vibration = in.readInt();
        interval = in.readInt();
        repetitions = in.readInt();
        disableType = in.readInt();
    }

    public long getId() {
        return id;
    }
    public String getMelody() {
        return melody;
    }
    public int getVibration() {
        return vibration;
    }
    public int getInterval() {
        return interval;
    }
    public int getRepetitions() {
        return repetitions;
    }
    public int getDisableType() {
        return disableType;
    }
    public void setMelody(String melody) {
        this.melody = melody;
    }
    public void setVibration(int vibration) {
        this.vibration = vibration;
    }
    public void setInterval(int interval) {
        this.interval = interval;
    }
    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }
    public void setDisableType(int disableType) {
        this.disableType = disableType;
    }

    public static final Creator<Settings> CREATOR = new Creator<Settings>() {
        @Override
        public Settings createFromParcel(Parcel in) {
            return new Settings(in);
        }

        @Override
        public Settings[] newArray(int size) {
            return new Settings[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(melody);
        parcel.writeInt(vibration);
        parcel.writeInt(interval);
        parcel.writeInt(repetitions);
        parcel.writeInt(disableType);
    }
}
