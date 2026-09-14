package com.example.audioplayer.feature.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audioplayer.core.model.TimerTask
import com.example.audioplayer.core.repository.TimerRepository
import com.example.audioplayer.core.scheduler.TimerScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class TimerListViewModel @Inject constructor(
    private val timerRepository: TimerRepository,
    private val scheduler: TimerScheduler,
) : ViewModel() {
    val timers: StateFlow<List<TimerTask>> = timerRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggle(task: TimerTask, enabled: Boolean) {
        viewModelScope.launch {
            val updated = task.copy(enabled = enabled)
            timerRepository.save(updated)
            if (enabled) scheduler.schedule(updated) else scheduler.cancel(task.id)
        }
    }

    fun delete(task: TimerTask) {
        viewModelScope.launch {
            scheduler.cancel(task.id)
            timerRepository.delete(task)
        }
    }
}