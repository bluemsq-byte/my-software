package com.example.audioplayer.core.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.audioplayer.MainActivity
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
            if (canScheduleExactAlarms()) {
                val showIntent = PendingIntent.getActivity(
                    context,
                    task.id.toInt(),
                    Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent),
                    pendingIntent,
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent,
                )
            }
        } catch (exception: SecurityException) {
            runCatching {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent,
                )
            }
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
            runCatching { schedule(task) }
        }
    }

    /**
     * 兜底守护闹钟：即使某次单任务闹钟被系统清理，也会定期重新补排。
     */
    fun ensureWatchdog() {
        val intent = Intent(context, BootReceiver::class.java)
            .setAction(ACTION_RESCHEDULE_TIMERS)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            WATCHDOG_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + WATCHDOG_INTERVAL_MILLIS,
            WATCHDOG_INTERVAL_MILLIS,
            pendingIntent,
        )
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
        const val ACTION_RESCHEDULE_TIMERS = "com.example.audioplayer.action.RESCHEDULE_TIMERS"
        private const val WATCHDOG_REQUEST_CODE = 9_001
        private const val WATCHDOG_INTERVAL_MILLIS = 12L * 60L * 60L * 1_000L
    }
}
