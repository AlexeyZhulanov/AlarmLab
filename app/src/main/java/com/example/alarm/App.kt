package com.example.alarm

import android.app.Application
import com.example.alarm.model.AlarmService

class App: Application() {
    //делаем аларм сервис синглтоном(позволяет получить доступ к алармссервис почти отовсюду)
    val alarmsService = AlarmService()
}