@file:androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)

package com.example.audioplayer

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.audioplayer.core.database.AppDatabase
import com.example.audioplayer.core.model.AudioSourceType
import com.example.audioplayer.core.model.AudioTrack
import com.example.audioplayer.core.network.smb.SmbClient
import com.example.audioplayer.core.playback.AppDataSourceFactory
import com.example.audioplayer.core.playback.LocalMediaProxyServer
import com.example.audioplayer.core.repository.ConnectionRepository
import com.example.audioplayer.core.security.AndroidCredentialStore
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CastProxyTest {
    @Test
    fun proxy_servesLocalFileWithRangeSupport() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val sourceFile = File(context.cacheDir, "cast-proxy-source.bin").apply {
            writeBytes(ByteArray(1024) { it.toByte() })
        }
        val connections = ConnectionRepository(database.connectionDao(), AndroidCredentialStore(context))
        val factory = AppDataSourceFactory(context, SmbClient(), connections)
        val proxy = LocalMediaProxyServer(context, factory)
        try {
            val publicUrl = proxy.start(
                AudioTrack(
                    id = "proxy",
                    title = "proxy",
                    artist = null,
                    album = null,
                    durationMillis = 1000L,
                    uri = sourceFile.toURI().toString(),
                    sourceType = AudioSourceType.LOCAL,
                ),
            )
            val parsedPublicUrl = URL(publicUrl)
            val localUrl = "http://127.0.0.1:${parsedPublicUrl.port}${parsedPublicUrl.path}"
            val connection = URL(localUrl).openConnection() as HttpURLConnection
            connection.setRequestProperty("Range", "bytes=0-9")
            assertEquals(206, connection.responseCode)
            val body = connection.inputStream.use { it.readBytes() }
            assertEquals(10, body.size)
        } finally {
            proxy.stop()
            database.close()
            sourceFile.delete()
        }
    }
}