package com.example.alarm.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    version = 1,
    entities = [
        AlarmDbEntity::class
    ]
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun getAlarmDao(): AlarmDao
}