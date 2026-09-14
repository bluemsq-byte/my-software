package com.example.audioplayer.core.model

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

enum class TimerAction {
    START,
    STOP,
}

enum class TimerSourceType {
    LOCAL_FOLDER,
    SMB_FOLDER,
    WEBDAV_FOLDER,
}

data class TimerTask(
    val id: Long = 0L,
    val name: String,
    val action: TimerAction,
    val hour: Int,
    val minute: Int,
    val repeatDays: Set<DayOfWeek>,
    val enabled: Boolean,
    val sourceType: TimerSourceType? = null,
    val connectionId: String? = null,
    val sourcePath: String? = null,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val lastRunEpochMillis: Long? = null,
) {
    val time: LocalTime
        get() = LocalTime.of(hour, minute)

    val isOneTime: Boolean
        get() = repeatDays.isEmpty()
}

object TimerNextRunCalculator {
    fun nextRun(
        task: TimerTask,
        now: LocalDateTime,
    ): LocalDateTime? {
        if (!task.enabled) return null
        val today = now.toLocalDate()
        if (task.isOneTime) {
            val candidate = LocalDateTime.of(today, task.time)
            return if (candidate.isAfter(now)) {
                candidate
            } else {
                null
            }
        }

        for (offset in 0..7) {
            val date = today.plusDays(offset.toLong())
            if (date.dayOfWeek !in task.repeatDays) continue
            val candidate = LocalDateTime.of(date, task.time)
            if (candidate.isAfter(now)) return candidate
        }
        return null
    }
}