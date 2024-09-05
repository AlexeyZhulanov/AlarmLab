package com.example.alarm.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;


@Dao
public interface AlarmDao {
    @Query("SELECT * FROM alarms ORDER BY time_hours ASC, time_minutes ASC")
    List<AlarmsGetTuple> selectAlarms();

    @Query("SELECT COUNT(*) FROM alarms WHERE time_hours = :hours AND time_minutes = :minutes")
    int countAlarmsWithTime(int hours, int minutes);

    @Query("SELECT COUNT(*) FROM alarms WHERE time_hours = :hours AND time_minutes = :minutes AND name = :name")
    int countAlarmsWithTimeAndName(int hours, int minutes, String name);

    @Insert
    void addAlarm(AlarmDbEntity alarmDbEntity);

    @Update(entity = AlarmDbEntity.class)
    void updateEnabled(AlarmUpdateEnabledTuple updateEnabledTuple);

    @Update
    void updateAlarm(AlarmDbEntity alarmDbEntity);

    @Delete(entity = AlarmDbEntity.class)
    void deleteAlarm(AlarmDbEntity alarmDbEntity);
}
