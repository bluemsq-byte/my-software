package com.example.audioplayer

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.audioplayer.core.database.AppDatabase
import com.example.audioplayer.core.model.AudioSourceType
import com.example.audioplayer.core.model.AudioTrack
import com.example.audioplayer.core.playback.PlaybackController
import com.example.audioplayer.core.repository.RecentPlayRepository
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaybackServiceTest {
    @Test
    fun controller_playsLocalWaveWithoutCrashing(): Unit = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val controller = PlaybackController(context, RecentPlayRepository(database.recentPlayDao()))
        val file = File(context.cacheDir, "playback-test.wav")
        file.writeBytes(createWaveTone())
        withContext(Dispatchers.Main) { controller.connect() }
        withTimeout(10_000L) { controller.state.first { it.isConnected } }
        withContext(Dispatchers.Main) { controller.play(
            listOf(
                AudioTrack(
                    id = "local-file-test",
                    title = "Local file test",
                    artist = "test",
                    album = "test",
                    durationMillis = 1000L,
                    uri = file.toURI().toString(),
                    sourceType = AudioSourceType.LOCAL,
                ),
            ),
        ) }
        val state = withTimeout(10_000L) {
            controller.state.first { it.currentTrackId == "local-file-test" }
        }
        assertTrue(state.currentTrackId == "local-file-test")
        withContext(Dispatchers.Main) { controller.stop() }
        database.close()
        file.delete()
    }

    private fun createWaveTone(): ByteArray {
        val sampleRate = 8_000
        val sampleCount = sampleRate
        val dataSize = sampleCount * 2
        val buffer = ByteBuffer.allocate(44 + dataSize).order(ByteOrder.LITTLE_ENDIAN)
        buffer.put("RIFF".toByteArray())
        buffer.putInt(36 + dataSize)
        buffer.put("WAVE".toByteArray())
        buffer.put("fmt ".toByteArray())
        buffer.putInt(16)
        buffer.putShort(1)
        buffer.putShort(1)
        buffer.putInt(sampleRate)
        buffer.putInt(sampleRate * 2)
        buffer.putShort(2)
        buffer.putShort(16)
        buffer.put("data".toByteArray())
        buffer.putInt(dataSize)
        repeat(sampleCount) { index ->
            val sample = (kotlin.math.sin(index * 2.0 * Math.PI * 440.0 / sampleRate) * 8_000).toInt()
            buffer.putShort(sample.toShort())
        }
        return buffer.array()
    }

    @Test
    fun controller_connectsToMediaSessionService() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val controller = PlaybackController(context, RecentPlayRepository(database.recentPlayDao()))
        controller.connect()

        val state = withTimeout(10_000L) {
            controller.state.first { it.isConnected }
        }
        assertTrue(state.isConnected)
        database.close()
    }
}