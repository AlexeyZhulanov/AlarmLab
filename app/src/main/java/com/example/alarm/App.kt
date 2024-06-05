package com.example.alarm

import android.app.Application
import com.example.alarm.model.AlarmService
import com.example.alarm.room.AlarmDao
import com.example.alarm.room.SettingsDao
import kotlinx.coroutines.Dispatchers

//class App: Application() {
//    //делаем аларм сервис синглтоном(позволяет получить доступ к алармссервис почти отовсюду)
//    val alarmsService = AlarmService()
//}