package com.example.alarm.model;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import javax.inject.Inject;

public class Settings implements Parcelable {
    private long id;
    private String melody;
    private Boolean vibration;
    private int interval;
    private int repetitions;

    @Inject
    public Settings(long id) {
        this.id = id;
        this.melody = "default";
        this.vibration = true;
        this.interval = 5;
        this.repetitions = 3;
    }

    protected Settings(Parcel in) {
        id = in.readLong();
        melody = in.readString();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibration = in.readBoolean();
        }
        interval = in.readInt();
        repetitions = in.readInt();
    }

    public long getId() {
        return id;
    }
    public String getMelody() {
        return melody;
    }
    public Boolean getVibration() {
        return vibration;
    }
    public int getInterval() {
        return interval;
    }
    public int getRepetitions() {
        return repetitions;
    }
    public void setMelody(String melody) {
        this.melody = melody;
    }
    public void setVibration(Boolean vibration) {
        this.vibration = vibration;
    }
    public void setInterval(int interval) {
        this.interval = interval;
    }
    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            parcel.writeBoolean(vibration);
        }
        parcel.writeInt(interval);
        parcel.writeInt(repetitions);
    }
}
