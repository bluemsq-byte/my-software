package com.example.audioplayer.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audioplayer.core.database.ConnectionEntity
import com.example.audioplayer.core.model.AudioTrack
import com.example.audioplayer.core.model.PlaylistSummary
import com.example.audioplayer.core.network.RemoteEntry
import com.example.audioplayer.core.playback.PlaybackController
import com.example.audioplayer.core.repository.ConnectionRepository
import com.example.audioplayer.core.repository.PlaylistRepository
import com.example.audioplayer.core.repository.RemoteFileRepository
import com.example.audioplayer.core.search.SearchMatcher
import com.example.audioplayer.core.storage.LocalMediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

data class RemoteSearchTrack(
    val connectionId: String,
    val connectionName: String,
    val entry: RemoteEntry,
    val queue: List<AudioTrack>,
)

data class GlobalSearchUiState(
    val query: String = "",
    val isSearching: Boolean = false,
    val localTracks: List<AudioTrack> = emptyList(),
    val remoteTracks: List<RemoteSearchTrack> = emptyList(),
    val playlists: List<PlaylistSummary> = emptyList(),
    val errors: List<String> = emptyList(),
)

/**
 * 全局搜索：本地和缓存优先，所有已保存 NAS 后台并行搜索。
 */
@HiltViewModel
class GlobalSearchViewModel @Inject constructor(
    private val localMediaRepository: LocalMediaRepository,
    private val connectionRepository: ConnectionRepository,
    private val remoteFileRepository: RemoteFileRepository,
    private val playlistRepository: PlaylistRepository,
    private val playbackController: PlaybackController,
) : ViewModel() {
    private val _state = MutableStateFlow(GlobalSearchUiState())
    val state: StateFlow<GlobalSearchUiState> = _state.asStateFlow()
    private var searchJob: Job? = null
    private var cachedLocalTracks: List<AudioTrack>? = null

    fun updateQuery(query: String) {
        _state.value = _state.value.copy(query = query)
        searchJob?.cancel()
        if (query.isBlank()) {
            _state.value = GlobalSearchUiState()
            return
        }
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MILLIS)
            search(query)
        }
    }

    fun playLocal(track: AudioTrack) {
        val tracks = _state.value.localTracks
        val index = tracks.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        playbackController.play(tracks, index)
    }

    fun playNext(track: AudioTrack) {
        playbackController.playNext(track)
    }

    fun addToQueue(track: AudioTrack) {
        playbackController.addToQueue(track)
    }

    fun playRemote(result: RemoteSearchTrack) {
        val index = result.queue.indexOfFirst { it.remotePath == result.entry.path }
        if (index >= 0) playbackController.play(result.queue, index)
    }

    private suspend fun search(query: String) {
        _state.value = _state.value.copy(isSearching = true, errors = emptyList())
        val localTracks = runCatching {
            cachedLocalTracks ?: localMediaRepository.scan().also { cachedLocalTracks = it }
        }.getOrDefault(emptyList())
        val matchingLocal = localTracks.filter { track ->
            SearchMatcher.matches(query, track.title, track.artist, track.album, track.remotePath)
        }
        val playlists = playlistRepository.observeSummaries()
            .first()
            .filter { playlist -> SearchMatcher.matches(query, playlist.name) }
        val connections = connectionRepository.observeAll().first()
        val remoteResults = searchConnections(query, connections)
        _state.value = _state.value.copy(
            isSearching = false,
            localTracks = matchingLocal,
            remoteTracks = remoteResults.first,
            playlists = playlists,
            errors = remoteResults.second,
        )
    }

    private suspend fun searchConnections(
        query: String,
        connections: List<ConnectionEntity>,
    ): Pair<List<RemoteSearchTrack>, List<String>> = coroutineScope {
        val jobs = connections.map { connection ->
            async {
                runCatching {
                    withTimeout(REMOTE_SEARCH_TIMEOUT_MILLIS) {
                        val entries = remoteFileRepository.list(connection.id, "/")
                        val matching = entries.filter { entry ->
                            entry.isAudioFile &&
                                SearchMatcher.matches(query, entry.name, entry.path)
                        }
                        if (matching.isEmpty()) {
                            emptyList()
                        } else {
                            val queue = remoteFileRepository.buildQueue(connection.id, "/", entries)
                            matching.map { entry ->
                                RemoteSearchTrack(
                                    connectionId = connection.id,
                                    connectionName = connection.name,
                                    entry = entry,
                                    queue = queue,
                                )
                            }
                        }
                    }
                }.fold(
                    onSuccess = { results -> results to null },
                    onFailure = { error ->
                        emptyList<RemoteSearchTrack>() to
                            "${connection.name}：${error.message ?: "搜索失败"}"
                    },
                )
            }
        }
        val results = jobs.map { it.await() }
        results.flatMap { it.first } to results.mapNotNull { it.second }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 320L
        const val REMOTE_SEARCH_TIMEOUT_MILLIS = 8_000L
    }
}
