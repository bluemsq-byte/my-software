@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package com.example.audioplayer.feature.timer

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.core.content.ContextCompat
import com.example.audioplayer.core.playback.PlaybackService
import com.example.audioplayer.core.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class SleepTimerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _remainingMillis = MutableStateFlow<Long?>(null)
    val remainingMillis: StateFlow<Long?> = _remainingMillis.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                val endAt = settingsRepository.sleepTimerEndAtMillis.first()
                _remainingMillis.value = endAt?.let { (it - System.currentTimeMillis()).coerceAtLeast(0L) }
                delay(1_000L)
            }
        }
    }

    fun start(minutes: Int) {
        val endAt = System.currentTimeMillis() + minutes * 60_000L
        val intent = Intent(context, PlaybackService::class.java).apply {
            action = PlaybackService.ACTION_SET_SLEEP_TIMER
            putExtra(PlaybackService.EXTRA_SLEEP_TIMER_END_AT, endAt)
        }
        ContextCompat.startForegroundService(context, intent)
    }

    fun cancel() {
        val intent = Intent(context, PlaybackService::class.java).apply {
            action = PlaybackService.ACTION_CANCEL_SLEEP_TIMER
        }
        runCatching { context.startService(intent) }
    }
}