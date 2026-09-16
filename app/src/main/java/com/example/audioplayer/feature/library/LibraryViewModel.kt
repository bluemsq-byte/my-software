package com.example.audioplayer.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audioplayer.core.database.ConnectionEntity
import com.example.audioplayer.core.model.AudioTrack
import com.example.audioplayer.core.model.RecentPlay
import com.example.audioplayer.core.model.toAudioTrack
import com.example.audioplayer.core.playback.PlaybackController
import com.example.audioplayer.core.repository.ConnectionRepository
import com.example.audioplayer.core.repository.RecentPlayRepository
import com.example.audioplayer.core.repository.RemoteFileRepository
import com.example.audioplayer.core.storage.LocalFolderGrouper
import com.example.audioplayer.core.storage.LocalMediaRepository
import com.example.audioplayer.core.search.SearchMatcher
import com.example.audioplayer.core.storage.LocalMusicFolder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LocalLibraryUiState(
    val isLoading: Boolean = false,
    val tracks: List<AudioTrack> = emptyList(),
    val folders: List<LocalMusicFolder> = emptyList(),
    val errorMessage: String? = null,
    val searchQuery: String = "",
) {
    val visibleTracks: List<AudioTrack>
        get() = tracks.filter { track ->
            SearchMatcher.matches(searchQuery, track.title, track.artist, track.album, track.remotePath)
        }

    val visibleFolders: List<LocalMusicFolder>
        get() = folders.filter { folder ->
            SearchMatcher.matches(searchQuery, folder.name, folder.path)
        }
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val localMediaRepository: LocalMediaRepository,
    private val connectionRepository: ConnectionRepository,
    private val remoteFileRepository: RemoteFileRepository,
    private val recentPlayRepository: RecentPlayRepository,
    private val playbackController: PlaybackController,
) : ViewModel() {
    private val _localState = MutableStateFlow(LocalLibraryUiState())
    val localState: StateFlow<LocalLibraryUiState> = _localState.asStateFlow()

    private val _connectionMessage = MutableStateFlow<String?>(null)
    val connectionMessage: StateFlow<String?> = _connectionMessage.asStateFlow()
    private val _testingConnectionId = MutableStateFlow<String?>(null)
    val testingConnectionId: StateFlow<String?> = _testingConnectionId.asStateFlow()

    val recentPlays: StateFlow<List<RecentPlay>> = recentPlayRepository.observeRecent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val connections: StateFlow<List<ConnectionEntity>> = connectionRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val playbackState: StateFlow<com.example.audioplayer.core.playback.PlaybackUiState> =
        playbackController.state

    fun loadLocal(force: Boolean = false) {
        if (_localState.value.isLoading) return
        if (!force && _localState.value.tracks.isNotEmpty()) return

        viewModelScope.launch {
            _localState.value = _localState.value.copy(isLoading = true, errorMessage = null)
            try {
                val tracks = localMediaRepository.scan()
                _localState.value = LocalLibraryUiState(
                    tracks = tracks,
                    folders = LocalFolderGrouper.group(tracks),
                )
            } catch (exception: Exception) {
                _localState.value = _localState.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "扫描本地音乐失败",
                )
            }
        }
    }

    fun updateLocalSearchQuery(query: String) {
        _localState.value = _localState.value.copy(searchQuery = query)
    }

    fun play(tracks: List<AudioTrack>, startIndex: Int) {
        playbackController.play(tracks, startIndex)
    }

    fun playTrack(track: AudioTrack) {
        playbackController.play(listOf(track), 0)
    }

    fun playNext(track: AudioTrack) {
        playbackController.playNext(track)
    }

    fun addToQueue(track: AudioTrack) {
        playbackController.addToQueue(track)
    }

    fun resumePlayback() {
        if (playbackController.state.value.currentTrackId != null &&
            !playbackController.state.value.isPlaying
        ) {
            playbackController.playPause()
        }
    }

    fun testConnection(connection: ConnectionEntity) {
        viewModelScope.launch {
            _testingConnectionId.value = connection.id
            try {
                val model = connectionRepository.get(connection.id) ?: return@launch
                remoteFileRepository.test(model)
                _connectionMessage.value = "连接成功"
            } catch (exception: Exception) {
                _connectionMessage.value = exception.message ?: "连接失败"
            } finally {
                _testingConnectionId.value = null
            }
        }
    }

    fun clearConnectionMessage() {
        _connectionMessage.value = null
    }

    fun playRecent(item: RecentPlay) {
        val queue = recentPlays.value.map(RecentPlay::toAudioTrack)
        val index = queue.indexOfFirst { it.id == item.mediaId }.coerceAtLeast(0)
        playbackController.play(queue, index)
    }

    fun clearRecentPlays() {
        viewModelScope.launch { recentPlayRepository.clear() }
    }

    fun deleteConnection(connection: ConnectionEntity) {
        viewModelScope.launch {
            val model = connectionRepository.get(connection.id) ?: return@launch
            connectionRepository.delete(model)
        }
    }
}
