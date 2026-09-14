package com.example.audioplayer.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "timer_files",
    foreignKeys = [
        ForeignKey(
            entity = TimerEntity::class,
            parentColumns = ["id"],
            childColumns = ["timerId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("timerId")],
)
data class TimerFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val timerId: Long,
    val position: Int,
    val path: String,
)