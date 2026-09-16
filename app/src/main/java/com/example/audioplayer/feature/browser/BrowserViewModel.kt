package com.example.audioplayer.feature.browser

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audioplayer.core.network.RemoteEntry
import com.example.audioplayer.core.network.RemotePath
import com.example.audioplayer.core.playback.PlaybackController
import com.example.audioplayer.core.model.PlaylistSummary
import com.example.audioplayer.core.repository.PlaylistRepository
import com.example.audioplayer.core.repository.RemoteFileRepository
import com.example.audioplayer.core.search.SearchMatcher
import com.example.audioplayer.core.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BrowserUiState(
    val path: String = "/",
    val entries: List<RemoteEntry> = emptyList(),
    val isLoading: Boolean = false,
    val audioOnly: Boolean = false,
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val sortMode: BrowserSortMode = BrowserSortMode.NAME,
    val sortAscending: Boolean = true,
    val selectionMode: Boolean = false,
    val selectedPaths: Set<String> = emptySet(),
) {
    val visibleEntries: List<RemoteEntry>
        get() {
            val filtered = entries.filter { entry ->
                (!audioOnly || entry.isAudioFile) &&
                    SearchMatcher.matches(searchQuery, entry.name, entry.path)
            }
            val sortComparator: Comparator<RemoteEntry> = when (sortMode) {
                BrowserSortMode.NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
                BrowserSortMode.MODIFIED -> compareBy { it.lastModifiedEpochMillis ?: 0L }
                BrowserSortMode.SIZE -> compareBy { it.size }
            }
            val direction = if (sortAscending) sortComparator else sortComparator.reversed()
            return filtered.sortedWith(
                compareByDescending<RemoteEntry> { it.isDirectory }.then(direction),
            )
        }
}

enum class BrowserSortMode {
    NAME,
    MODIFIED,
    SIZE,
}

@HiltViewModel
class BrowserViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val remoteFileRepository: RemoteFileRepository,
    private val playlistRepository: PlaylistRepository,
    private val playbackController: PlaybackController,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val connectionId: String = requireNotNull(savedStateHandle["connectionId"])
    private val initialPath: String = savedStateHandle.get<String>("path").orEmpty().ifBlank { "/" }
    private val _state = MutableStateFlow(BrowserUiState(path = RemotePath.normalize(initialPath)))
    val state: StateFlow<BrowserUiState> = _state.asStateFlow()
    val playlists: StateFlow<List<PlaylistSummary>> = playlistRepository.observeSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            val savedPath = settingsRepository.lastRemotePath(connectionId).first()
            load(savedPath.orEmpty().ifBlank { initialPath })
        }
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
                settingsRepository.setLastRemotePath(connectionId, _state.value.path)
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

    fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    fun toggleAudioOnly() {
        _state.value = _state.value.copy(audioOnly = !_state.value.audioOnly)
    }

    fun cycleSortMode() {
        val next = when (_state.value.sortMode) {
            BrowserSortMode.NAME -> BrowserSortMode.MODIFIED
            BrowserSortMode.MODIFIED -> BrowserSortMode.SIZE
            BrowserSortMode.SIZE -> BrowserSortMode.NAME
        }
        _state.value = _state.value.copy(sortMode = next)
    }

    fun toggleSortDirection() {
        _state.value = _state.value.copy(sortAscending = !_state.value.sortAscending)
    }

    fun enterSelectionMode() {
        _state.value = _state.value.copy(selectionMode = true, selectedPaths = emptySet())
    }

    fun exitSelectionMode() {
        _state.value = _state.value.copy(selectionMode = false, selectedPaths = emptySet())
    }

    fun toggleSelection(entry: RemoteEntry) {
        if (!entry.isAudioFile) return
        val selected = _state.value.selectedPaths
        _state.value = _state.value.copy(
            selectedPaths = if (entry.path in selected) selected - entry.path else selected + entry.path,
        )
    }

    fun selectAll() {
        _state.value = _state.value.copy(
            selectedPaths = _state.value.visibleEntries
                .filter(RemoteEntry::isAudioFile)
                .map(RemoteEntry::path)
                .toSet(),
        )
    }

    fun playSelected() {
        val selected = _state.value.selectedPaths
        val selectedEntries = _state.value.visibleEntries.filter {
            it.isAudioFile && it.path in selected
        }
        if (selectedEntries.isEmpty()) return
        viewModelScope.launch {
            val queue = remoteFileRepository.buildQueue(connectionId, _state.value.path, selectedEntries)
            if (queue.isNotEmpty()) playbackController.play(queue, 0)
        }
    }

    fun addSelectedToQueue() {
        val selected = _state.value.selectedPaths
        val selectedEntries = _state.value.visibleEntries.filter {
            it.isAudioFile && it.path in selected
        }
        if (selectedEntries.isEmpty()) return
        viewModelScope.launch {
            remoteFileRepository.buildQueue(connectionId, _state.value.path, selectedEntries)
                .forEach(playbackController::addToQueue)
        }
    }

    fun addToPlaylist(playlistId: Long, entry: RemoteEntry) {
        viewModelScope.launch {
            val state = _state.value
            val queue = remoteFileRepository.buildQueue(connectionId, state.path, state.entries)
            queue.firstOrNull { it.remotePath == entry.path }?.let { track ->
                playlistRepository.addTracks(playlistId, listOf(track))
            }
        }
    }

    fun play(entry: RemoteEntry) {
        viewModelScope.launch {
            val state = _state.value
            val queue = remoteFileRepository.buildQueue(connectionId, state.path, state.entries)
            val index = queue.indexOfFirst { it.remotePath == entry.path }
            if (index >= 0) playbackController.play(queue, index)
        }
    }

    fun playFolder() {
        viewModelScope.launch {
            val state = _state.value
            val queue = remoteFileRepository.buildQueue(connectionId, state.path, state.entries)
            if (queue.isNotEmpty()) playbackController.play(queue, 0)
        }
    }

    fun playNext(entry: RemoteEntry) {
        viewModelScope.launch {
            val state = _state.value
            remoteFileRepository.buildQueue(connectionId, state.path, state.entries)
                .firstOrNull { it.remotePath == entry.path }
                ?.let(playbackController::playNext)
        }
    }

    fun addToQueue(entry: RemoteEntry) {
        viewModelScope.launch {
            val state = _state.value
            remoteFileRepository.buildQueue(connectionId, state.path, state.entries)
                .firstOrNull { it.remotePath == entry.path }
                ?.let(playbackController::addToQueue)
        }
    }
}
