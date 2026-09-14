package com.example.audioplayer.feature.connection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audioplayer.core.model.ConnectionProtocol
import com.example.audioplayer.core.model.RemoteConnection
import com.example.audioplayer.core.repository.ConnectionRepository
import com.example.audioplayer.core.repository.RemoteFileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ConnectionEditorUiState(
    val id: String = "",
    val name: String = "",
    val protocol: ConnectionProtocol = ConnectionProtocol.WEBDAV,
    val host: String = "",
    val port: String = "5006",
    val username: String = "",
    val password: String = "",
    val share: String = "",
    val basePath: String = "/",
    val domain: String = "",
    val useHttps: Boolean = true,
    val isSaving: Boolean = false,
    val isTesting: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false,
    val saved: Boolean = false,
)

@HiltViewModel
class ConnectionEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val connectionRepository: ConnectionRepository,
    private val remoteFileRepository: RemoteFileRepository,
) : ViewModel() {
    private val connectionId: String? = savedStateHandle["connectionId"]
    private val _state = MutableStateFlow(ConnectionEditorUiState(id = connectionId.orEmpty()))
    val state: StateFlow<ConnectionEditorUiState> = _state.asStateFlow()

    init {
        if (!connectionId.isNullOrBlank()) {
            viewModelScope.launch {
                val connection = connectionRepository.get(connectionId) ?: return@launch
                _state.value = connection.toUiState()
            }
        }
    }

    fun updateName(value: String) = update { copy(name = value) }
    fun updateHost(value: String) = update { copy(host = value.trim()) }
    fun updatePort(value: String) = update { copy(port = value.filter(Char::isDigit)) }
    fun updateUsername(value: String) = update { copy(username = value) }
    fun updatePassword(value: String) = update { copy(password = value) }
    fun updateShare(value: String) = update { copy(share = value.trim()) }
    fun updateBasePath(value: String) = update { copy(basePath = value.ifBlank { "/" }) }
    fun updateDomain(value: String) = update { copy(domain = value.trim()) }
    fun updateUseHttps(value: Boolean) = update { copy(useHttps = value) }

    fun updateProtocol(protocol: ConnectionProtocol) {
        update {
            copy(
                protocol = protocol,
                port = if (protocol == ConnectionProtocol.SMB) "445" else "5006",
            )
        }
    }

    fun testConnection() {
        val connection = buildConnection() ?: return
        viewModelScope.launch {
            update { copy(isTesting = true, message = null) }
            try {
                remoteFileRepository.test(connection)
                update { copy(isTesting = false, message = "连接成功", isError = false) }
            } catch (exception: Exception) {
                update {
                    copy(
                        isTesting = false,
                        message = exception.message ?: "连接失败",
                        isError = true,
                    )
                }
            }
        }
    }

    fun save() {
        val connection = buildConnection() ?: return
        viewModelScope.launch {
            update { copy(isSaving = true, message = null) }
            try {
                connectionRepository.save(connection)
                update { copy(isSaving = false, saved = true) }
            } catch (exception: Exception) {
                update {
                    copy(
                        isSaving = false,
                        message = exception.message ?: "保存失败",
                        isError = true,
                    )
                }
            }
        }
    }

    private fun buildConnection(): RemoteConnection? {
        val state = _state.value
        val error = when {
            state.name.isBlank() -> "请输入连接名称"
            state.host.isBlank() -> "请输入服务器地址"
            state.username.isBlank() -> "请输入用户名"
            state.password.isBlank() -> "请输入密码"
            state.protocol == ConnectionProtocol.SMB && state.share.isBlank() -> "请输入共享文件夹"
            else -> null
        }
        if (error != null) {
            update { copy(message = error, isError = true) }
            return null
        }

        return RemoteConnection(
            id = state.id.ifBlank { UUID.randomUUID().toString() },
            name = state.name.trim(),
            protocol = state.protocol,
            host = state.host.trim(),
            port = state.port.toIntOrNull(),
            username = state.username.trim(),
            password = state.password,
            share = state.share.ifBlank { null },
            basePath = state.basePath.ifBlank { "/" },
            domain = state.domain.ifBlank { null },
            useHttps = state.useHttps,
        )
    }

    private fun RemoteConnection.toUiState() = ConnectionEditorUiState(
        id = id,
        name = name,
        protocol = protocol,
        host = host,
        port = port?.toString().orEmpty(),
        username = username,
        password = password,
        share = share.orEmpty(),
        basePath = basePath,
        domain = domain.orEmpty(),
        useHttps = useHttps,
    )

    private fun update(transform: ConnectionEditorUiState.() -> ConnectionEditorUiState) {
        _state.value = _state.value.transform()
    }
}