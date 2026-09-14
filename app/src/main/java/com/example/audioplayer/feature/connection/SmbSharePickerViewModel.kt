package com.example.audioplayer.feature.connection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audioplayer.core.repository.ConnectionRepository
import com.example.audioplayer.core.repository.RemoteFileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SmbSharePickerUiState(
    val connectionName: String = "",
    val shares: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedShare: String? = null,
)

@HiltViewModel
class SmbSharePickerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val connectionRepository: ConnectionRepository,
    private val remoteFileRepository: RemoteFileRepository,
) : ViewModel() {
    private val connectionId: String = requireNotNull(savedStateHandle["connectionId"])
    private val _state = MutableStateFlow(SmbSharePickerUiState())
    val state: StateFlow<SmbSharePickerUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            try {
                val connection = requireNotNull(connectionRepository.get(connectionId))
                val shares = remoteFileRepository.listSmbShares(connection)
                _state.value = SmbSharePickerUiState(
                    connectionName = connection.name,
                    shares = shares,
                    isLoading = false,
                )
            } catch (exception: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "无法读取共享文件夹",
                )
            }
        }
    }

    fun select(share: String, onSelected: () -> Unit) {
        viewModelScope.launch {
            val connection = connectionRepository.get(connectionId) ?: return@launch
            connectionRepository.save(connection.copy(selectedShare = share))
            _state.value = _state.value.copy(selectedShare = share)
            onSelected()
        }
    }
}