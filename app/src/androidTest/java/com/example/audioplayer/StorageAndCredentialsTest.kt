package com.example.audioplayer

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.example.audioplayer.core.database.AppDatabase
import com.example.audioplayer.core.database.ConnectionEntity
import com.example.audioplayer.core.model.ConnectionProtocol
import com.example.audioplayer.core.security.AndroidCredentialStore
import com.example.audioplayer.core.storage.LocalMediaRepository
import com.google.common.truth.Truth.assertThat
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StorageAndCredentialsTest {
    @get:Rule
    val mediaPermission: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.READ_MEDIA_AUDIO,
    )

    private lateinit var context: Context
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun credentialStore_encryptsAndRemovesPassword() = runBlocking {
        val store = AndroidCredentialStore(context)
        store.save("nas", "机密密码123")
        assertThat(store.read("nas")).isEqualTo("机密密码123")
        store.remove("nas")
        assertThat(store.read("nas")).isNull()
    }

    @Test
    fun roomDatabase_persistsConnectionMetadataInMemory() = runBlocking {
        val entity = ConnectionEntity(
            id = "room-test",
            name = "测试 NAS",
            protocol = ConnectionProtocol.SMB,
            host = "192.168.1.20",
            port = 445,
            username = "user",
            share = "music",
            selectedShare = "music",
            basePath = "/",
            domain = null,
            useHttps = true,
        )
        database.connectionDao().upsert(entity)
        assertThat(database.connectionDao().getById("room-test")).isEqualTo(entity)
    }

    @Test
    fun localMediaRepository_indexesGeneratedWaveFile() = runBlocking {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return@runBlocking
        val displayName = "audio_player_test_${System.currentTimeMillis()}.wav"
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav")
            put(MediaStore.Audio.Media.RELATIVE_PATH, "Music/AudioPlayerTests")
            put(MediaStore.Audio.Media.IS_PENDING, 1)
            put(MediaStore.Audio.Media.IS_MUSIC, 1)
            put(MediaStore.Audio.Media.TITLE, "Audio Player Test Tone")
            put(MediaStore.Audio.Media.DURATION, 1_000)
        }
        val uri = context.contentResolver.insert(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            values,
        ) ?: error("Unable to insert test audio")

        try {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(createWaveTone())
            } ?: error("Unable to open test audio output")
            context.contentResolver.update(
                uri,
                ContentValues().apply {
                    put(MediaStore.Audio.Media.IS_PENDING, 0)
                    put(MediaStore.Audio.Media.IS_MUSIC, 1)
                },
                null,
                null,
            )

            val filePath = context.contentResolver.query(
                uri,
                arrayOf(MediaStore.Audio.Media.DATA),
                null,
                null,
                null,
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
            if (filePath != null) {
                val latch = CountDownLatch(1)
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(filePath),
                    arrayOf("audio/wav"),
                ) { _, _ -> latch.countDown() }
                latch.await(10, TimeUnit.SECONDS)
            }

            val tracks = LocalMediaRepository(context).scan()
            val insertedId = uri.lastPathSegment
            assertThat(tracks.any { it.id == "local:$insertedId" }).isTrue()
        } finally {
            context.contentResolver.delete(uri, null, null)
        }
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
}