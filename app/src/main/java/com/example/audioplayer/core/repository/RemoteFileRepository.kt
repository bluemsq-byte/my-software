package com.example.audioplayer.core.repository

import android.net.Uri
import com.example.audioplayer.core.model.AudioSourceType
import com.example.audioplayer.core.model.AudioTrack
import com.example.audioplayer.core.model.ConnectionProtocol
import com.example.audioplayer.core.model.RemoteConnection
import com.example.audioplayer.core.network.RemoteEntry
import com.example.audioplayer.core.network.RemotePath
import com.example.audioplayer.core.network.smb.SmbClient
import com.example.audioplayer.core.network.webdav.WebDavClient
import com.example.audioplayer.core.playback.RemotePlaybackQueueBuilder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteFileRepository @Inject constructor(
    private val connectionRepository: ConnectionRepository,
    private val webDavClient: WebDavClient,
    private val smbClient: SmbClient,
) {
    suspend fun list(connectionId: String, path: String = "/"): List<RemoteEntry> {
        val connection = requireNotNull(connectionRepository.get(connectionId)) {
            "Connection not found: $connectionId"
        }
        return when (connection.protocol) {
            ConnectionProtocol.WEBDAV -> webDavClient.list(connection.toWebDavConfig(), path)
            ConnectionProtocol.SMB -> smbClient.list(connection.toSmbConfig(), path)
        }
    }

    suspend fun test(connection: RemoteConnection) {
        when (connection.protocol) {
            ConnectionProtocol.WEBDAV -> webDavClient.testConnection(connection.toWebDavConfig())
            ConnectionProtocol.SMB -> smbClient.testConnection(connection.toSmbConfig())
        }
    }

    fun buildQueue(
        connectionId: String,
        path: String,
        entries: List<RemoteEntry>,
    ): List<AudioTrack> {
        val connection = requireNotNull(connectionRepository.getCached(connectionId)) {
            "Connection must be cached before building playback queue"
        }
        val sourceType = when (connection.protocol) {
            ConnectionProtocol.WEBDAV -> AudioSourceType.WEBDAV
            ConnectionProtocol.SMB -> AudioSourceType.SMB
        }
        return RemotePlaybackQueueBuilder.build(
            entries = entries,
            sourceType = sourceType,
            connectionId = connectionId,
            uriFactory = { entry ->
                when (connection.protocol) {
                    ConnectionProtocol.WEBDAV -> webDavClient.resolveUrl(
                        connection.toWebDavConfig().baseUrl,
                        entry.path,
                    ).toString()

                    ConnectionProtocol.SMB -> Uri.Builder()
                        .scheme("smb")
                        .authority(connectionId)
                        .appendPath(RemotePath.normalize(entry.path).trimStart('/'))
                        .build()
                        .toString()
                }
            },
        )
    }
}