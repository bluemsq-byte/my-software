package com.example.audioplayer.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audioplayer.core.database.ConnectionEntity
import com.example.audioplayer.core.repository.ConnectionRepository
import com.example.audioplayer.core.settings.SettingsRepository
import com.example.audioplayer.core.storage.LocalMediaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val connectionRepository: ConnectionRepository,
    private val settingsRepository: SettingsRepository,
    private val localMediaRepository: LocalMediaRepository,
) : ViewModel() {
    val connections: StateFlow<List<ConnectionEntity>> = connectionRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val backgroundPlaybackEnabled: StateFlow<Boolean> = settingsRepository.backgroundPlaybackEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun setBackgroundPlaybackEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setBackgroundPlaybackEnabled(enabled)
        }
    }

    fun deleteConnection(connection: ConnectionEntity) {
        viewModelScope.launch {
            connectionRepository.get(connection.id)?.let { connectionRepository.delete(it) }
        }
    }

    fun rescanLocalMusic() {
        viewModelScope.launch {
            runCatching { localMediaRepository.scan() }
        }
    }
}