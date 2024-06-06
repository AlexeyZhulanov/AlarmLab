package com.example.alarm

import android.content.Context
import androidx.room.Room
import com.example.alarm.model.AlarmRepository
import com.example.alarm.model.AlarmService
import com.example.alarm.room.AppDatabase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

// Нужно добавить в активити строку или в фрагмент: Repositories.init(applicationContext)

object Repositories {
    private lateinit var applicationContext: Context

    private val database: AppDatabase by lazy<AppDatabase> {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "database.db")
            .createFromAsset("init_db.db")
            .build()
    }

    //private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    val alarmRepository: AlarmRepository by lazy {
        AlarmService(database.getAlarmDao(), database.getSettingsDao())
    }

    /**
     * Call this method in all application components that may be created at app startup/restoring
     * (e.g. in onCreate of activities and services)
     */
    fun init(context: Context) {
        applicationContext = context
    }
}