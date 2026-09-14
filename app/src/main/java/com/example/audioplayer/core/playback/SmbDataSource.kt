@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package com.example.audioplayer.core.playback

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSourceException
import androidx.media3.datasource.DataSpec
import com.example.audioplayer.core.network.RemotePath
import com.example.audioplayer.core.network.smb.SmbClient
import com.example.audioplayer.core.network.smb.SmbConnectionConfig
import com.example.audioplayer.core.network.smb.SmbReadableFile
import java.io.IOException

class SmbDataSource(
    private val smbClient: SmbClient,
    private val configProvider: (String) -> SmbConnectionConfig?,
) : DataSource {
    private var currentUri: Uri? = null
    private var file: SmbReadableFile? = null
    private var position: Long = 0L
    private var bytesRemaining: Long = C.LENGTH_UNSET.toLong()

    override fun open(dataSpec: DataSpec): Long {
        close()
        val connectionId = dataSpec.uri.host
            ?: throw DataSourceException("SMB 地址缺少连接标识", PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND)
        val config = configProvider(connectionId)
            ?: throw DataSourceException("找不到 SMB 连接配置", PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND)
        val remotePath = RemotePath.normalize(
            dataSpec.uri.pathSegments.joinToString(separator = "/", prefix = "/"),
        )

        val openedFile = try {
            smbClient.openFile(config, remotePath)
        } catch (exception: Exception) {
            throw DataSourceException(exception, PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED)
        }

        val available = openedFile.length - dataSpec.position
        if (available < 0) {
            openedFile.close()
            throw DataSourceException(PlaybackException.ERROR_CODE_IO_READ_POSITION_OUT_OF_RANGE)
        }

        file = openedFile
        currentUri = dataSpec.uri
        position = dataSpec.position
        bytesRemaining = if (dataSpec.length == C.LENGTH_UNSET.toLong()) {
            available
        } else {
            minOf(available, dataSpec.length)
        }
        return bytesRemaining
    }

    override fun read(
        buffer: ByteArray,
        offset: Int,
        length: Int,
    ): Int {
        if (length == 0) return 0
        if (bytesRemaining == 0L) return C.RESULT_END_OF_INPUT
        if (bytesRemaining < 0) return C.RESULT_END_OF_INPUT

        val readLength = minOf(length.toLong(), bytesRemaining).toInt()
        val read = try {
            file?.read(buffer, position, offset, readLength)
                ?: throw IOException("SMB 文件尚未打开")
        } catch (exception: Exception) {
            throw DataSourceException(exception, PlaybackException.ERROR_CODE_IO_UNSPECIFIED)
        }

        if (read == -1) {
            bytesRemaining = 0
            return C.RESULT_END_OF_INPUT
        }

        position += read
        if (bytesRemaining != C.LENGTH_UNSET.toLong()) {
            bytesRemaining -= read
        }
        return read
    }

    override fun getUri(): Uri? = currentUri

    override fun close() {
        try {
            file?.close()
        } catch (_: Exception) {
            // Closing is best-effort.
        } finally {
            file = null
            currentUri = null
            position = 0L
            bytesRemaining = C.LENGTH_UNSET.toLong()
        }
    }

    override fun addTransferListener(transferListener: androidx.media3.datasource.TransferListener) {
        // Transfer progress is reported by the player at a higher level.
    }

    override fun getResponseHeaders(): Map<String, List<String>> = emptyMap()

    class Factory(
        private val smbClient: SmbClient,
        private val configProvider: (String) -> SmbConnectionConfig?,
    ) : DataSource.Factory {
        override fun createDataSource(): DataSource = SmbDataSource(smbClient, configProvider)
    }
}