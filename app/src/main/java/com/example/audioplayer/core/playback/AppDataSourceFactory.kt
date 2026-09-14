@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package com.example.audioplayer.core.playback

import android.content.Context
import android.net.Uri
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.datasource.TransferListener
import com.example.audioplayer.core.model.ConnectionProtocol
import com.example.audioplayer.core.network.smb.SmbClient
import com.example.audioplayer.core.network.webdav.WebDavConnectionMatcher
import com.example.audioplayer.core.repository.ConnectionRepository
import com.example.audioplayer.core.repository.toSmbConfig
import com.example.audioplayer.core.repository.toWebDavConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.Credentials

@Singleton
class AppDataSourceFactory @Inject constructor(
    @ApplicationContext context: Context,
    private val smbClient: SmbClient,
    private val connectionRepository: ConnectionRepository,
) : DataSource.Factory {
    private val defaultSourceFactory = DefaultDataSource.Factory(context)
    private val resolvingSourceFactory = ResolvingDataSource.Factory(defaultSourceFactory) { dataSpec ->
        resolveWebDavRequest(dataSpec)
    }

    override fun createDataSource(): DataSource {
        return RoutingDataSource(
            smbFactory = SmbDataSource.Factory(smbClient) { id ->
                connectionRepository.getCached(id)?.toSmbConfig()
            },
            defaultFactory = resolvingSourceFactory,
        )
    }

    private fun resolveWebDavRequest(dataSpec: DataSpec): DataSpec {
        val connection = connectionRepository.cachedConnections()
            .firstOrNull { connection ->
                connection.protocol == ConnectionProtocol.WEBDAV &&
                    WebDavConnectionMatcher.matches(connection.toWebDavConfig().baseUrl, dataSpec.uri.toString())
            }
            ?: return dataSpec

        val headers = dataSpec.httpRequestHeaders.toMutableMap().apply {
            put("Authorization", Credentials.basic(connection.username, connection.password))
        }
        return dataSpec.buildUpon()
            .setHttpRequestHeaders(headers)
            .build()
    }

    private class RoutingDataSource(
        private val smbFactory: DataSource.Factory,
        private val defaultFactory: DataSource.Factory,
    ) : DataSource {
        private var delegate: DataSource? = null
        private var listener: TransferListener? = null

        override fun open(dataSpec: DataSpec): Long {
            close()
            val selected = if (dataSpec.uri.scheme.equals(SMB_SCHEME, ignoreCase = true)) {
                smbFactory.createDataSource()
            } else {
                defaultFactory.createDataSource()
            }
            listener?.let(selected::addTransferListener)
            delegate = selected
            return selected.open(dataSpec)
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
            return delegate?.read(buffer, offset, length) ?: androidx.media3.common.C.RESULT_END_OF_INPUT
        }

        override fun getUri(): Uri? = delegate?.uri

        override fun close() {
            try {
                delegate?.close()
            } finally {
                delegate = null
            }
        }

        override fun addTransferListener(transferListener: TransferListener) {
            listener = transferListener
            delegate?.addTransferListener(transferListener)
        }

        override fun getResponseHeaders(): Map<String, List<String>> {
            return delegate?.responseHeaders.orEmpty()
        }

        private companion object {
            const val SMB_SCHEME = "smb"
        }
    }
}