package com.example.alarm.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.alarm.model.Settings

@Entity(
    tableName = "settings",
    indices = [
        Index("melody", unique = true)
    ]
)
data class SettingsDbEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    var melody: String,
    var vibration: Boolean,
    var interval: Int,
    var repetitions: Int,
    @ColumnInfo(name = "disable_type") var disableType: Int
) {
    fun toSettings(): Settings = Settings(
        id = id,
        melody = melody,
        vibration = vibration,
        interval = interval,
        repetitions = repetitions,
        disableType = disableType
    )

    companion object {
        fun fromUserInput(settings: Settings): SettingsDbEntity = SettingsDbEntity(
            id = 0,
            melody = settings.melody,
            vibration = settings.vibration,
            interval = settings.interval,
            repetitions = settings.repetitions,
            disableType = settings.disableType
        )
    }
}