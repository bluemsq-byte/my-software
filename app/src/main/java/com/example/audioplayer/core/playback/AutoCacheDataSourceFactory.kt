@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package com.example.audioplayer.core.playback

import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import androidx.media3.datasource.cache.Cache
import androidx.media3.datasource.cache.CacheDataSource
import android.net.Uri

/**
 * 只缓存网络音频，本地 content/file URI 继续走原始数据源。
 */
class AutoCacheDataSourceFactory(
    cache: Cache,
    private val upstreamFactory: DataSource.Factory,
) : DataSource.Factory {
    private val cacheFactory = CacheDataSource.Factory()
        .setCache(cache)
        .setUpstreamDataSourceFactory(upstreamFactory)

    override fun createDataSource(): DataSource = RoutingDataSource(
        cacheFactory.createDataSource(),
        upstreamFactory.createDataSource(),
    )

    private class RoutingDataSource(
        private val cacheDataSource: DataSource,
        private val upstreamDataSource: DataSource,
    ) : DataSource {
        private var delegate: DataSource? = null
        private var listener: TransferListener? = null

        override fun open(dataSpec: DataSpec): Long {
            close()
            val scheme = dataSpec.uri.scheme?.lowercase()
            delegate = if (scheme == "http" || scheme == "https" || scheme == "smb") {
                cacheDataSource
            } else {
                upstreamDataSource
            }
            listener?.let(delegate!!::addTransferListener)
            return delegate!!.open(dataSpec)
        }

        override fun read(buffer: ByteArray, offset: Int, length: Int): Int =
            delegate?.read(buffer, offset, length) ?: androidx.media3.common.C.RESULT_END_OF_INPUT

        override fun getUri(): Uri? = delegate?.uri

        override fun close() {
            delegate?.close()
            delegate = null
        }

        override fun addTransferListener(transferListener: TransferListener) {
            listener = transferListener
            delegate?.addTransferListener(transferListener)
        }

        override fun getResponseHeaders(): Map<String, List<String>> =
            delegate?.responseHeaders.orEmpty()
    }
}
