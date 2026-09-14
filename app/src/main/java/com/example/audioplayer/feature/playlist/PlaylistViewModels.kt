package com.example.audioplayer.feature.playlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audioplayer.core.model.AudioTrack
import com.example.audioplayer.core.model.PlaylistItem
import com.example.audioplayer.core.model.PlaylistSummary
import com.example.audioplayer.core.playback.PlaybackController
import com.example.audioplayer.core.repository.PlaylistRepository
import com.example.audioplayer.core.storage.LocalMediaRepository
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
        val tracks = _state.value.items.map(PlaylistItem::toAudioTrack)
        if (tracks.isNotEmpty()) playbackController.play(tracks, startIndex.coerceIn(tracks.indices))
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