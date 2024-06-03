package com.example.alarm.model

data class Settings(
    val id: Long,
    var melody: String = "default",
    var vibration: Boolean = true,
    var interval: Int = 5,
    var repetitions: Int = 3,
    var disableType: Int = 0
    )