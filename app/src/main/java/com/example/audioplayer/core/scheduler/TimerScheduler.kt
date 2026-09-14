package com.example.audioplayer.core.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.audioplayer.core.model.TimerNextRunCalculator
import com.example.audioplayer.core.model.TimerTask
import com.example.audioplayer.core.repository.TimerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val timerRepository: TimerRepository,
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun canScheduleExactAlarms(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
    }

    suspend fun schedule(task: TimerTask) {
        cancel(task.id)
        if (!task.enabled) return

        val nextRun = TimerNextRunCalculator.nextRun(task, LocalDateTime.now())
            ?: return
        val triggerAtMillis = nextRun
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val pendingIntent = pendingIntent(task.id, task.action.name, PendingIntent.FLAG_UPDATE_CURRENT)
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent,
            )
        } catch (exception: SecurityException) {
            throw IllegalStateException("需要开启精确闹钟权限")
        }
    }

    fun cancel(taskId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java).setAction(ACTION_TIMER)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    suspend fun rescheduleAll() {
        timerRepository.getEnabled().forEach { task ->
            schedule(task)
        }
    }

    private fun pendingIntent(taskId: Long, action: String, flags: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
            .setAction(ACTION_TIMER)
            .putExtra(EXTRA_TIMER_ID, taskId)
            .putExtra(EXTRA_TIMER_ACTION, action)
        return PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            flags or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        const val ACTION_TIMER = "com.example.audioplayer.action.TIMER"
        const val EXTRA_TIMER_ID = "timer_id"
        const val EXTRA_TIMER_ACTION = "timer_action"
    }
}