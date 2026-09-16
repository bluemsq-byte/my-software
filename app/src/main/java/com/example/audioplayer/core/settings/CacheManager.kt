@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package com.example.audioplayer.core.settings

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import androidx.media3.datasource.cache.Cache

data class CacheUsage(
    val totalBytes: Long,
    val networkBytes: Long,
) {
    val otherBytes: Long
        get() = (totalBytes - networkBytes).coerceAtLeast(0L)
}

@Singleton
class CacheManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: OkHttpClient,
    private val mediaCache: Cache,
) {
    suspend fun usage(): CacheUsage = withContext(Dispatchers.IO) {
        CacheUsage(
            totalBytes = directorySize(context.cacheDir),
            networkBytes = httpClient.cache?.size().orZero() + mediaCache.cacheSpace,
        )
    }

    suspend fun clear(): CacheUsage = withContext(Dispatchers.IO) {
        httpClient.cache?.evictAll()
        mediaCache.keys.toList().forEach { key -> mediaCache.removeResource(key) }
        context.cacheDir.listFiles()?.forEach { child ->
            if (child.name == "media_cache" || child.name == "http_cache") {
                return@forEach
            }
            if (child.canonicalPath.startsWith(context.cacheDir.canonicalPath) && child != context.cacheDir) {
                child.deleteRecursively()
            }
        }
        CacheUsage(totalBytes = 0L, networkBytes = 0L)
    }

    private fun directorySize(file: File): Long {
        if (!file.exists()) return 0L
        return file.walkTopDown()
            .filter(File::isFile)
            .sumOf(File::length)
    }

    private fun Long?.orZero(): Long = this ?: 0L
}
