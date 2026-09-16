@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package com.example.audioplayer.core.storage

import android.content.Context
import android.net.Uri
import androidx.media3.datasource.DataSpec
import com.example.audioplayer.core.model.AudioSourceType
import com.example.audioplayer.core.model.AudioTrack
import com.example.audioplayer.core.playback.AppDataSourceFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 把当前网络歌曲下载到应用私有目录，并返回可离线播放的本地轨道。
 * 保留 connectionId/remotePath，供播放列表去重使用。
 */
@Singleton
class NetworkDownloadRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataSourceFactory: AppDataSourceFactory,
) {
    suspend fun download(track: AudioTrack): AudioTrack = withContext(Dispatchers.IO) {
        if (track.sourceType == AudioSourceType.LOCAL) return@withContext track

        val directory = File(context.filesDir, DOWNLOAD_DIRECTORY).apply { mkdirs() }
        val extension = track.remotePath
            ?.substringAfterLast('.', "")
            ?.takeIf { it.isNotBlank() }
            ?: track.uri.substringBefore('?').substringAfterLast('.', "mp3")
        val target = File(directory, "${track.id.hashCode().toUInt().toString(16)}.$extension")
        if (target.exists() && target.length() > 0L) {
            return@withContext track.toLocalFile(target)
        }

        val temporary = File(directory, "${target.name}.part")
        val source = dataSourceFactory.createDataSource()
        try {
            source.open(DataSpec(Uri.parse(track.uri)))
            temporary.outputStream().buffered().use { output ->
                val buffer = ByteArray(DOWNLOAD_BUFFER_SIZE)
                while (true) {
                    val read = source.read(buffer, 0, buffer.size)
                    if (read < 0) break
                    output.write(buffer, 0, read)
                }
            }
            if (!temporary.renameTo(target)) {
                temporary.copyTo(target, overwrite = true)
                temporary.delete()
            }
            track.toLocalFile(target)
        } catch (exception: Exception) {
            temporary.delete()
            throw IllegalStateException("下载失败：${exception.message ?: track.title}", exception)
        } finally {
            source.close()
        }
    }

    private fun AudioTrack.toLocalFile(file: File) = copy(
        id = "local-download:${id}",
        uri = Uri.fromFile(file).toString(),
        sourceType = AudioSourceType.LOCAL,
    )

    private companion object {
        const val DOWNLOAD_DIRECTORY = "network_downloads"
        const val DOWNLOAD_BUFFER_SIZE = 64 * 1024
    }
}
