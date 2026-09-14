package com.example.audioplayer.core.database

import androidx.room.TypeConverter
import com.example.audioplayer.core.model.ConnectionProtocol
import com.example.audioplayer.core.model.TimerAction
import com.example.audioplayer.core.model.TimerSourceType

class DatabaseTypeConverters {
    @TypeConverter
    fun fromConnectionProtocol(value: ConnectionProtocol): String = value.name

    @TypeConverter
    fun toConnectionProtocol(value: String): ConnectionProtocol = ConnectionProtocol.valueOf(value)

    @TypeConverter
    fun fromTimerAction(value: TimerAction): String = value.name

    @TypeConverter
    fun toTimerAction(value: String): TimerAction = TimerAction.valueOf(value)

    @TypeConverter
    fun fromTimerSourceType(value: TimerSourceType?): String? = value?.name

    @TypeConverter
    fun toTimerSourceType(value: String?): TimerSourceType? = value?.let(TimerSourceType::valueOf)
}