@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package com.example.audioplayer.core.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.audioplayer.core.model.TimerAction
import com.example.audioplayer.core.playback.PlaybackService
import com.example.audioplayer.core.repository.TimerRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {
    @Inject lateinit var timerRepository: TimerRepository
    @Inject lateinit var scheduler: TimerScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TimerScheduler.ACTION_TIMER) return
        val timerId = intent.getLongExtra(TimerScheduler.EXTRA_TIMER_ID, -1L)
        if (timerId <= 0L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = timerRepository.get(timerId) ?: return@launch
                if (!task.enabled) return@launch

                val updated = task.copy(
                    enabled = !task.isOneTime,
                    lastRunEpochMillis = System.currentTimeMillis(),
                )
                timerRepository.save(updated)
                scheduler.schedule(updated)

                val serviceIntent = Intent(context, PlaybackService::class.java).apply {
                    action = when (task.action) {
                        TimerAction.START -> PlaybackService.ACTION_START_TIMER
                        TimerAction.STOP -> PlaybackService.ACTION_STOP_TIMER
                    }
                    putExtra(PlaybackService.EXTRA_TIMER_ID, timerId)
                }
                if (task.action == TimerAction.START) {
                    ContextCompat.startForegroundService(context, serviceIntent)
                } else {
                    runCatching { context.startService(serviceIntent) }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}