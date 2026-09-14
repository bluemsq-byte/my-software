package com.example.audioplayer.feature.timer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audioplayer.core.database.ConnectionEntity
import com.example.audioplayer.core.model.TimerAction
import com.example.audioplayer.core.model.TimerSelectionType
import com.example.audioplayer.core.model.TimerSourceType
import com.example.audioplayer.core.model.TimerTask
import com.example.audioplayer.core.repository.ConnectionRepository
import com.example.audioplayer.core.repository.RemoteFileRepository
import com.example.audioplayer.core.repository.TimerRepository
import com.example.audioplayer.core.scheduler.TimerScheduler
import com.example.audioplayer.core.storage.LocalFolderGrouper
import com.example.audioplayer.core.storage.LocalMediaRepository
import com.example.audioplayer.core.network.RemoteEntry
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
    val selectionType: TimerSelectionType = TimerSelectionType.FOLDER,
    val connectionId: String? = null,
    val sourcePath: String = "",
    val selectedFiles: List<String> = emptyList(),
    val localTracks: List<com.example.audioplayer.core.model.AudioTrack> = emptyList(),
    val localFolders: List<LocalMusicFolder> = emptyList(),
    val pickerVisible: Boolean = false,
    val pickerPath: String = "/",
    val pickerEntries: List<RemoteEntry> = emptyList(),
    val pickerLoading: Boolean = false,
    val pickerError: String? = null,

    val isSaving: Boolean = false,
    val message: String? = null,
    val saved: Boolean = false,
)

@HiltViewModel
class TimerEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val timerRepository: TimerRepository,
    private val connectionRepository: ConnectionRepository,
    private val remoteFileRepository: RemoteFileRepository,
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
            _state.value = _state.value.copy(localTracks = tracks, localFolders = folders)
        }
    }

    fun updateName(value: String) = update { copy(name = value) }
    fun updateAction(value: TimerAction) = update { copy(action = value) }
    fun updateTime(hour: Int, minute: Int) = update { copy(hour = hour, minute = minute) }
    fun updateEnabled(value: Boolean) = update { copy(enabled = value) }
    fun updateSourceType(value: TimerSourceType) = update { copy(sourceType = value) }
    fun updateSelectionType(value: TimerSelectionType) = update { copy(selectionType = value) }
    fun toggleLocalFile(path: String) = update {
        copy(selectedFiles = if (path in selectedFiles) selectedFiles - path else selectedFiles + path)
    }
    fun updateConnectionId(value: String?) = update { copy(connectionId = value) }
    fun updateSourcePath(value: String) = update { copy(sourcePath = value) }

    fun openRemotePicker() {
        val source = _state.value.sourceType
        if (source == TimerSourceType.LOCAL_FOLDER) return
        loadPicker(_state.value.sourcePath.ifBlank { "/" })
    }

    fun closePicker() = update { copy(pickerVisible = false, pickerError = null) }

    fun loadPicker(path: String) {
        val source = _state.value.sourceType
        val connectionId = _state.value.connectionId
        if (source == TimerSourceType.LOCAL_FOLDER || connectionId.isNullOrBlank()) {
            update { copy(pickerError = "请先选择 NAS 连接") }
            return
        }
        viewModelScope.launch {
            update { copy(pickerVisible = true, pickerLoading = true, pickerPath = path, pickerError = null) }
            try {
                val entries = remoteFileRepository.list(connectionId, path)
                update { copy(pickerEntries = entries, pickerLoading = false) }
            } catch (exception: Exception) {
                update { copy(pickerLoading = false, pickerError = exception.message ?: "读取目录失败") }
            }
        }
    }

    fun openPickerParent() {
        val parent = com.example.audioplayer.core.network.RemotePath.parent(_state.value.pickerPath) ?: return
        loadPicker(parent)
    }

    fun openPickerDirectory(entry: RemoteEntry) {
        if (entry.isDirectory) loadPicker(entry.path)
    }

    fun togglePickerFile(entry: RemoteEntry) {
        if (entry.isDirectory) return
        update {
            copy(
                selectedFiles = if (entry.path in selectedFiles) {
                    selectedFiles - entry.path
                } else {
                    selectedFiles + entry.path
                },
            )
        }
    }

    fun choosePickerFolder() {
        update {
            copy(
                sourcePath = pickerPath,
                pickerVisible = false,
                pickerError = null,
            )
        }
    }

    fun confirmPickerFiles() {
        update { copy(sourcePath = pickerPath, pickerVisible = false, pickerError = null) }
    }

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
            snapshot.action != TimerAction.START -> null
            snapshot.sourceType == TimerSourceType.LOCAL_FOLDER &&
                snapshot.selectionType == TimerSelectionType.FOLDER &&
                snapshot.sourcePath.isBlank() -> "请选择本地音乐文件夹"
            snapshot.sourceType == TimerSourceType.LOCAL_FOLDER &&
                snapshot.selectionType == TimerSelectionType.FILES &&
                snapshot.selectedFiles.isEmpty() -> "请选择至少一个本地文件"
            snapshot.sourceType != TimerSourceType.LOCAL_FOLDER &&
                snapshot.connectionId.isNullOrBlank() -> "请选择 NAS 连接"
            snapshot.sourceType != TimerSourceType.LOCAL_FOLDER &&
                snapshot.selectionType == TimerSelectionType.FOLDER &&
                snapshot.sourcePath.isBlank() -> "请选择 NAS 音乐文件夹"
            snapshot.sourceType != TimerSourceType.LOCAL_FOLDER &&
                snapshot.selectionType == TimerSelectionType.FILES &&
                snapshot.selectedFiles.isEmpty() -> "请选择至少一个 NAS 文件"
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
        selectionType = selectionType,
        connectionId = connectionId,
        sourcePath = sourcePath.orEmpty(),
        selectedFiles = selectedFiles,
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
        selectionType = selectionType,
        connectionId = if (
            action == TimerAction.START && sourceType != TimerSourceType.LOCAL_FOLDER
        ) connectionId else null,
        sourcePath = if (action == TimerAction.START) sourcePath.trim() else null,
        selectedFiles = if (
            action == TimerAction.START && selectionType == TimerSelectionType.FILES
        ) selectedFiles else emptyList(),
    )

    private fun update(transform: TimerEditorUiState.() -> TimerEditorUiState) {
        _state.value = _state.value.transform()
    }
}