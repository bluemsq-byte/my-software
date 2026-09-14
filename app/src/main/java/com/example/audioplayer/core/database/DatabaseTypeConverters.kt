package com.example.audioplayer.core.database

import androidx.room.TypeConverter
import com.example.audioplayer.core.model.AudioSourceType
import com.example.audioplayer.core.model.ConnectionProtocol
import com.example.audioplayer.core.model.TimerAction
import com.example.audioplayer.core.model.TimerSelectionType
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

    @TypeConverter
    fun fromTimerSelectionType(value: TimerSelectionType): String = value.name

    @TypeConverter
    fun toTimerSelectionType(value: String): TimerSelectionType = TimerSelectionType.valueOf(value)

    @TypeConverter
    fun fromAudioSourceType(value: AudioSourceType): String = value.name

    @TypeConverter
    fun toAudioSourceType(value: String): AudioSourceType = AudioSourceType.valueOf(value)
}