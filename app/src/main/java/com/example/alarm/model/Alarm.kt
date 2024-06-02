package com.example.alarm.model

data class Alarm(
    val id: Long,
    var timeHours: Int = 7,
    var timeMinutes: Int = 0,
    var name: String = "default",
    var enabled: Boolean = false
)