package com.example.audioplayer.feature.playlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audioplayer.core.model.AudioTrack
import com.example.audioplayer.core.model.PlaylistItem
import com.example.audioplayer.core.playlist.PlaylistSelection
import com.example.audioplayer.core.model.PlaylistSummary
import com.example.audioplayer.core.playback.PlaybackController
import com.example.audioplayer.core.repository.PlaylistRepository
import com.example.audioplayer.core.storage.LocalMediaRepository
import com.example.audioplayer.core.storage.LocalFolderNode
import com.example.audioplayer.core.storage.LocalFolderTree
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PlaylistListViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {
    val playlists: StateFlow<List<PlaylistSummary>> = playlistRepository.observeSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun create(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            playlistRepository.create(name)
            _message.value = "播放列表已创建"
        }
    }

    fun delete(playlistId: Long) {
        viewModelScope.launch {
            playlistRepository.delete(playlistId)
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

data class PlaylistDetailUiState(
    val name: String = "",
    val items: List<PlaylistItem> = emptyList(),
    val localTracks: List<AudioTrack> = emptyList(),
    val showAddLocalDialog: Boolean = false,
    val selectionMode: Boolean = false,
    val selectedItemIds: Set<Long> = emptySet(),
)

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val playlistRepository: PlaylistRepository,
    private val localMediaRepository: LocalMediaRepository,
    private val playbackController: PlaybackController,
) : ViewModel() {
    val playlistId: Long = requireNotNull(savedStateHandle["playlistId"])
    private val _state = MutableStateFlow(PlaylistDetailUiState())
    val state: StateFlow<PlaylistDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = _state.value.copy(name = playlistRepository.getSummary(playlistId)?.name.orEmpty())
        }
        viewModelScope.launch {
            playlistRepository.observeItems(playlistId).collect { items ->
                _state.value = _state.value.copy(items = items)
            }
        }
    }

    fun playAll(startIndex: Int = 0) {
        playAt(startIndex)
    }

    fun playAt(index: Int) {
        val tracks = _state.value.items.map(PlaylistItem::toAudioTrack)
        if (tracks.isNotEmpty()) {
            playbackController.play(tracks, index.coerceIn(tracks.indices))
        }
    }

    fun playNext(item: PlaylistItem) {
        playbackController.playNext(item.toAudioTrack())
    }

    fun addToQueue(item: PlaylistItem) {
        playbackController.addToQueue(item.toAudioTrack())
    }

    fun enterSelectionMode() {
        _state.value = _state.value.copy(selectionMode = true, selectedItemIds = emptySet())
    }

    fun exitSelectionMode() {
        _state.value = _state.value.copy(selectionMode = false, selectedItemIds = emptySet())
    }

    fun toggleSelection(itemId: Long) {
        val selected = _state.value.selectedItemIds
        _state.value = _state.value.copy(
            selectedItemIds = if (itemId in selected) selected - itemId else selected + itemId,
        )
    }

    fun selectAll() {
        _state.value = _state.value.copy(
            selectedItemIds = _state.value.items.map { it.id }.toSet(),
        )
    }

    fun playSelected() {
        val selected = _state.value.selectedItemIds
        val tracks = _state.value.items
            .filter { it.id in selected }
            .map(PlaylistItem::toAudioTrack)
        if (tracks.isNotEmpty()) {
            playbackController.play(tracks, 0)
            exitSelectionMode()
        }
    }

    fun removeSelected() {
        val selected = _state.value.selectedItemIds
        if (selected.isEmpty()) return
        viewModelScope.launch {
            selected.forEach { itemId ->
                playlistRepository.removeItem(playlistId, itemId)
            }
            _state.value = _state.value.copy(selectedItemIds = emptySet())
        }
    }

    fun moveSelected(direction: Int) {
        val selected = _state.value.selectedItemIds
        if (selected.isEmpty()) return
        viewModelScope.launch {
            val ordered = if (direction < 0) {
                _state.value.items.map { it.id }.filter { it in selected }
            } else {
                _state.value.items.map { it.id }.filter { it in selected }.reversed()
            }
            ordered.forEach { itemId ->
                playlistRepository.moveItem(playlistId, itemId, direction)
            }
        }
    }

    fun remove(itemId: Long) {
        viewModelScope.launch { playlistRepository.removeItem(playlistId, itemId) }
    }

    fun move(itemId: Long, direction: Int) {
        viewModelScope.launch { playlistRepository.moveItem(playlistId, itemId, direction) }
    }

    fun openAddLocalDialog() {
        viewModelScope.launch {
            val tracks = runCatching { localMediaRepository.scan() }.getOrDefault(emptyList())
            _state.value = _state.value.copy(localTracks = tracks, showAddLocalDialog = true)
        }
    }

    fun closeAddLocalDialog() {
        _state.value = _state.value.copy(showAddLocalDialog = false)
    }

    fun addLocalTracks(tracks: List<AudioTrack>) {
        viewModelScope.launch {
            playlistRepository.addTracks(playlistId, tracks)
            _state.value = _state.value.copy(showAddLocalDialog = false)
        }
    }
}

data class LocalPlaylistPickerUiState(
    val isLoading: Boolean = true,
    val root: LocalFolderNode? = null,
    val currentPath: String? = null,
    val selectedTrackIds: Set<String> = emptySet(),
    val message: String? = null,
)

@HiltViewModel
class LocalPlaylistPickerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val localMediaRepository: LocalMediaRepository,
    private val playlistRepository: PlaylistRepository,
) : ViewModel() {
    private val playlistId: Long = requireNotNull(savedStateHandle["playlistId"])
    private val _state = MutableStateFlow(LocalPlaylistPickerUiState())
    val state: StateFlow<LocalPlaylistPickerUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { localMediaRepository.scan() }
                .onSuccess { tracks ->
                    val root = LocalFolderTree.build(tracks)
                    _state.value = LocalPlaylistPickerUiState(
                        isLoading = false,
                        root = root,
                        currentPath = root.path,
                    )
                }
                .onFailure { error ->
                    _state.value = LocalPlaylistPickerUiState(
                        isLoading = false,
                        message = error.message ?: "读取本地音乐失败",
                    )
                }
        }
    }

    fun openFolder(node: LocalFolderNode) {
        _state.value = _state.value.copy(currentPath = node.path)
    }

    fun openParent() {
        val root = _state.value.root ?: return
        val currentPath = _state.value.currentPath ?: return
        if (currentPath == root.path) return
        val parentPath = currentPath.substringBeforeLast('/').ifBlank { "/" }
        _state.value = _state.value.copy(
            currentPath = if (parentPath == root.path) root.path else parentPath,
        )
    }

    fun toggle(track: AudioTrack) {
        val selected = _state.value.selectedTrackIds
        _state.value = _state.value.copy(
            selectedTrackIds = if (track.id in selected) {
                selected - track.id
            } else {
                selected + track.id
            },
        )
    }

    fun addSelected(onComplete: () -> Unit) {
        val root = _state.value.root ?: return
        val selected = _state.value.selectedTrackIds
        if (selected.isEmpty()) return
        val tracks = flattenTracks(root).filter { it.id in selected }
        viewModelScope.launch {
            runCatching {
                playlistRepository.addTracks(playlistId, tracks)
            }.onSuccess {
                onComplete()
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    message = error.message ?: "添加本地歌曲失败",
                )
            }
        }
    }

    fun clearMessage() {
        _state.value = _state.value.copy(message = null)
    }

    private fun flattenTracks(node: LocalFolderNode): List<AudioTrack> =
        node.tracks + node.childFolders.flatMap(::flattenTracks)
}
