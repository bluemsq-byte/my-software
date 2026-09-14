package com.example.audioplayer.feature.browser

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audioplayer.core.network.RemoteEntry
import com.example.audioplayer.core.network.RemotePath
import com.example.audioplayer.core.playback.PlaybackController
import com.example.audioplayer.core.repository.RemoteFileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BrowserUiState(
    val path: String = "/",
    val entries: List<RemoteEntry> = emptyList(),
    val isLoading: Boolean = false,
    val audioOnly: Boolean = false,
    val errorMessage: String? = null,
) {
    val visibleEntries: List<RemoteEntry>
        get() = if (audioOnly) entries.filter { it.isAudioFile } else entries
}

@HiltViewModel
class BrowserViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val remoteFileRepository: RemoteFileRepository,
    private val playbackController: PlaybackController,
) : ViewModel() {
    private val connectionId: String = requireNotNull(savedStateHandle["connectionId"])
    private val initialPath: String = savedStateHandle.get<String>("path").orEmpty().ifBlank { "/" }
    private val _state = MutableStateFlow(BrowserUiState(path = RemotePath.normalize(initialPath)))
    val state: StateFlow<BrowserUiState> = _state.asStateFlow()

    init {
        load(_state.value.path)
    }

    fun load(path: String = _state.value.path) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                path = RemotePath.normalize(path),
                isLoading = true,
                errorMessage = null,
            )
            try {
                val entries = remoteFileRepository.list(connectionId, _state.value.path)
                _state.value = _state.value.copy(entries = entries, isLoading = false)
            } catch (exception: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "读取目录失败",
                )
            }
        }
    }

    fun openDirectory(entry: RemoteEntry) {
        load(entry.path)
    }

    fun toggleAudioOnly() {
        _state.value = _state.value.copy(audioOnly = !_state.value.audioOnly)
    }

    fun play(entry: RemoteEntry) {
        viewModelScope.launch {
            val state = _state.value
            val queue = remoteFileRepository.buildQueue(connectionId, state.path, state.entries)
            val index = queue.indexOfFirst { it.remotePath == entry.path }
            if (index >= 0) playbackController.play(queue, index)
        }
    }
}