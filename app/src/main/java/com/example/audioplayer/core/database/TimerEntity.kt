package com.example.audioplayer.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.audioplayer.core.model.TimerAction
import com.example.audioplayer.core.model.TimerSourceType

@Entity(tableName = "timers")
data class TimerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val action: TimerAction,
    val hour: Int,
    val minute: Int,
    val repeatDaysMask: Int,
    val enabled: Boolean,
    val sourceType: TimerSourceType?,
    val connectionId: String?,
    val sourcePath: String?,
    val createdAtEpochMillis: Long,
    val lastRunEpochMillis: Long?,
)