package com.example.audioplayer.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audioplayer.core.database.ConnectionEntity
import com.example.audioplayer.core.model.AudioTrack
import com.example.audioplayer.core.playback.PlaybackController
import com.example.audioplayer.core.repository.ConnectionRepository
import com.example.audioplayer.core.storage.LocalFolderGrouper
import com.example.audioplayer.core.storage.LocalMediaRepository
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
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val localMediaRepository: LocalMediaRepository,
    private val connectionRepository: ConnectionRepository,
    private val playbackController: PlaybackController,
) : ViewModel() {
    private val _localState = MutableStateFlow(LocalLibraryUiState())
    val localState: StateFlow<LocalLibraryUiState> = _localState.asStateFlow()

    val connections: StateFlow<List<ConnectionEntity>> = connectionRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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

    fun play(tracks: List<AudioTrack>, startIndex: Int) {
        playbackController.play(tracks, startIndex)
    }

    fun deleteConnection(connection: ConnectionEntity) {
        viewModelScope.launch {
            val model = connectionRepository.get(connection.id) ?: return@launch
            connectionRepository.delete(model)
        }
    }
}