package com.example.alarm.model

data class Alarm(
    val id: Long,
    var timeHours: Int = 7,
    var timeMinutes: Int = 0,
    var interval: Int = 5,
    var name: String? = null,
    var isEnabled: Boolean = false
)