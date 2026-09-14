package com.example.audioplayer.feature.timer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audioplayer.core.database.ConnectionEntity
import com.example.audioplayer.core.model.TimerAction
import com.example.audioplayer.core.model.TimerSourceType
import com.example.audioplayer.core.model.TimerTask
import com.example.audioplayer.core.repository.ConnectionRepository
import com.example.audioplayer.core.repository.TimerRepository
import com.example.audioplayer.core.scheduler.TimerScheduler
import com.example.audioplayer.core.storage.LocalFolderGrouper
import com.example.audioplayer.core.storage.LocalMediaRepository
import com.example.audioplayer.core.storage.LocalMusicFolder
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TimerEditorUiState(
    val id: Long = 0L,
    val name: String = "",
    val action: TimerAction = TimerAction.START,
    val hour: Int = 7,
    val minute: Int = 30,
    val repeatDays: Set<DayOfWeek> = emptySet(),
    val enabled: Boolean = true,
    val sourceType: TimerSourceType = TimerSourceType.LOCAL_FOLDER,
    val connectionId: String? = null,
    val sourcePath: String = "",
    val localFolders: List<LocalMusicFolder> = emptyList(),
    val isSaving: Boolean = false,
    val message: String? = null,
    val saved: Boolean = false,
)

@HiltViewModel
class TimerEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val timerRepository: TimerRepository,
    private val connectionRepository: ConnectionRepository,
    private val localMediaRepository: LocalMediaRepository,
    private val scheduler: TimerScheduler,
) : ViewModel() {
    private val timerId: Long = savedStateHandle.get<Long>("timerId") ?: 0L
    private val _state = MutableStateFlow(TimerEditorUiState(id = timerId))
    val state: StateFlow<TimerEditorUiState> = _state.asStateFlow()

    val connections: StateFlow<List<ConnectionEntity>> = connectionRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            if (timerId > 0L) {
                timerRepository.get(timerId)?.let { task ->
                    _state.value = task.toUiState()
                }
            }
            val tracks = runCatching { localMediaRepository.scan() }.getOrDefault(emptyList())
            val folders = LocalFolderGrouper.group(tracks)
            _state.value = _state.value.copy(localFolders = folders)
        }
    }

    fun updateName(value: String) = update { copy(name = value) }
    fun updateAction(value: TimerAction) = update { copy(action = value) }
    fun updateTime(hour: Int, minute: Int) = update { copy(hour = hour, minute = minute) }
    fun updateEnabled(value: Boolean) = update { copy(enabled = value) }
    fun updateSourceType(value: TimerSourceType) = update { copy(sourceType = value) }
    fun updateConnectionId(value: String?) = update { copy(connectionId = value) }
    fun updateSourcePath(value: String) = update { copy(sourcePath = value) }

    fun toggleRepeatDay(day: DayOfWeek) {
        update {
            copy(
                repeatDays = if (day in repeatDays) repeatDays - day else repeatDays + day,
            )
        }
    }

    fun save() {
        val snapshot = _state.value
        val validationError = when {
            snapshot.name.isBlank() -> "请输入定时名称"
            snapshot.action == TimerAction.START && snapshot.sourceType == TimerSourceType.LOCAL_FOLDER &&
                snapshot.sourcePath.isBlank() -> "请选择本地音乐文件夹"
            snapshot.action == TimerAction.START && snapshot.sourceType != TimerSourceType.LOCAL_FOLDER &&
                snapshot.connectionId.isNullOrBlank() -> "请选择 NAS 连接"
            snapshot.action == TimerAction.START && snapshot.sourceType != TimerSourceType.LOCAL_FOLDER &&
                snapshot.sourcePath.isBlank() -> "请输入 NAS 音乐路径"
            else -> null
        }
        if (validationError != null) {
            update { copy(message = validationError) }
            return
        }

        viewModelScope.launch {
            update { copy(isSaving = true, message = null) }
            try {
                val task = snapshot.toTimerTask()
                val savedId = timerRepository.save(task)
                val finalTask = task.copy(id = if (task.id == 0L) savedId else task.id)
                if (finalTask.enabled) scheduler.schedule(finalTask) else scheduler.cancel(finalTask.id)
                update { copy(id = finalTask.id, isSaving = false, saved = true) }
            } catch (exception: Exception) {
                update {
                    copy(
                        isSaving = false,
                        message = exception.message ?: "保存定时失败",
                    )
                }
            }
        }
    }

    private fun TimerTask.toUiState() = TimerEditorUiState(
        id = id,
        name = name,
        action = action,
        hour = hour,
        minute = minute,
        repeatDays = repeatDays,
        enabled = enabled,
        sourceType = sourceType ?: TimerSourceType.LOCAL_FOLDER,
        connectionId = connectionId,
        sourcePath = sourcePath.orEmpty(),
    )

    private fun TimerEditorUiState.toTimerTask() = TimerTask(
        id = id,
        name = name.trim(),
        action = action,
        hour = hour,
        minute = minute,
        repeatDays = repeatDays,
        enabled = enabled,
        sourceType = if (action == TimerAction.START) sourceType else null,
        connectionId = if (
            action == TimerAction.START && sourceType != TimerSourceType.LOCAL_FOLDER
        ) connectionId else null,
        sourcePath = if (action == TimerAction.START) sourcePath.trim() else null,
    )

    private fun update(transform: TimerEditorUiState.() -> TimerEditorUiState) {
        _state.value = _state.value.transform()
    }
}