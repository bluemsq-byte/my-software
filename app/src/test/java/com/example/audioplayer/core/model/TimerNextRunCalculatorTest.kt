package com.example.audioplayer.core.model

import com.google.common.truth.Truth.assertThat
import java.time.DayOfWeek
import java.time.LocalDateTime
import org.junit.Test

class TimerNextRunCalculatorTest {
    @Test
    fun nextRun_oneTimeTask_returnsTodayWhenTimeIsStillAhead() {
        val task = task(
            enabled = true,
            repeatDays = emptySet(),
            hour = 18,
            minute = 30,
        )

        val result = TimerNextRunCalculator.nextRun(task, LocalDateTime.of(2026, 9, 14, 8, 0))

        assertThat(result).isEqualTo(LocalDateTime.of(2026, 9, 14, 18, 30))
    }

    @Test
    fun nextRun_oneTimeTask_returnsNullAfterTimePassed() {
        val task = task(
            enabled = true,
            repeatDays = emptySet(),
            hour = 7,
            minute = 0,
        )

        val result = TimerNextRunCalculator.nextRun(task, LocalDateTime.of(2026, 9, 14, 8, 0))

        assertThat(result).isNull()
    }

    @Test
    fun nextRun_weeklyTask_skipsUnselectedDays() {
        val task = task(
            enabled = true,
            repeatDays = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
            hour = 9,
            minute = 0,
        )

        val result = TimerNextRunCalculator.nextRun(task, LocalDateTime.of(2026, 9, 14, 8, 0))

        assertThat(result).isEqualTo(LocalDateTime.of(2026, 9, 19, 9, 0))
    }

    @Test
    fun nextRun_disabledTask_returnsNull() {
        val task = task(
            enabled = false,
            repeatDays = setOf(DayOfWeek.MONDAY),
            hour = 9,
            minute = 0,
        )

        assertThat(TimerNextRunCalculator.nextRun(task, LocalDateTime.of(2026, 9, 14, 8, 0))).isNull()
    }

    private fun task(
        enabled: Boolean,
        repeatDays: Set<DayOfWeek>,
        hour: Int,
        minute: Int,
    ) = TimerTask(
        name = "test",
        action = TimerAction.START,
        hour = hour,
        minute = minute,
        repeatDays = repeatDays,
        enabled = enabled,
    )
}