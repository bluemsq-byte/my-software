package com.example.audioplayer

import androidx.room.Room
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.audioplayer.core.database.AppDatabase
import com.example.audioplayer.core.model.AudioSourceType
import com.example.audioplayer.core.model.AudioTrack
import com.example.audioplayer.core.model.TimerAction
import com.example.audioplayer.core.model.TimerSelectionType
import com.example.audioplayer.core.model.TimerSourceType
import com.example.audioplayer.core.model.TimerTask
import com.example.audioplayer.core.repository.PlaylistRepository
import com.example.audioplayer.core.repository.RecentPlayRepository
import com.example.audioplayer.core.repository.TimerRepository
import com.example.audioplayer.core.settings.CacheManager
import com.example.audioplayer.feature.settings.BluetoothRouteManager
import com.google.common.truth.Truth.assertThat
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PersistenceFeaturesTest {
    private lateinit var database: AppDatabase

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun recentPlays_areOrderedNewestFirst() = runBlocking {
        val repository = RecentPlayRepository(database.recentPlayDao())
        repository.record(track("one", "第一首"))
        Thread.sleep(2)
        repository.record(track("two", "第二首"))

        val recent = repository.observeRecent().first()
        assertThat(recent.map { it.title }).containsExactly("第二首", "第一首").inOrder()
    }

    @Test
    fun playlists_preserveMixedTracksAndOrder() = runBlocking {
        val repository = PlaylistRepository(database.playlistDao())
        val playlistId = repository.create("收藏")
        repository.addTracks(
            playlistId,
            listOf(
                track("local", "本地歌曲"),
                track("nas", "NAS 歌曲", AudioSourceType.WEBDAV, "nas"),
            ),
        )
        val items = repository.observeItems(playlistId).first()
        assertThat(items.map { it.title }).containsExactly("本地歌曲", "NAS 歌曲").inOrder()

        repository.moveItem(playlistId, items.last().id, -1)
        assertThat(repository.observeItems(playlistId).first().map { it.title })
            .containsExactly("NAS 歌曲", "本地歌曲").inOrder()
    }

    @Test
    fun playlistAdd_doesNotDuplicateSameNetworkTrack() = runBlocking {
        val repository = PlaylistRepository(database.playlistDao())
        val playlistId = repository.create("网络收藏")
        val track = track("nas-track", "NAS 歌曲", AudioSourceType.SMB, "nas")

        repository.addTracks(playlistId, listOf(track))
        repository.addTracks(playlistId, listOf(track, track.copy(title = "重复标题")))

        val items = repository.observeItems(playlistId).first()
        assertThat(items).hasSize(1)
        assertThat(items.single().title).isEqualTo("NAS 歌曲")
    }

    @Test
    fun timerRepository_preservesSelectedFileOrder() = runBlocking {
        val repository = TimerRepository(database.timerDao(), database.timerFileDao())
        val id = repository.save(
            TimerTask(
                name = "测试文件定时",
                action = TimerAction.START,
                hour = 8,
                minute = 0,
                repeatDays = emptySet(),
                enabled = true,
                sourceType = TimerSourceType.LOCAL_FOLDER,
                selectionType = TimerSelectionType.FILES,
                selectedFiles = listOf("content://a", "content://b"),
            ),
        )

        assertThat(repository.get(id)?.selectedFiles).containsExactly("content://a", "content://b").inOrder()
    }

    @Test
    fun cacheManager_clearsCacheWithoutTouchingDatabase() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val mediaCache = SimpleCache(
            File(context.cacheDir, "test-media-cache-${System.nanoTime()}"),
            NoOpCacheEvictor(),
            StandaloneDatabaseProvider(context),
        )
        val manager = CacheManager(context, OkHttpClient(), mediaCache)
        val file = File(context.cacheDir, "test-cache-entry").apply {
            parentFile?.mkdirs()
            writeText("cache")
        }
        assertThat(manager.usage().totalBytes).isAtLeast(file.length())
        manager.clear()
        assertThat(file.exists()).isFalse()
        mediaCache.release()
    }

    @Test
    fun bluetoothRouteManager_canReadRoutesWithoutCrashing() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        lateinit var manager: BluetoothRouteManager
        instrumentation.runOnMainSync {
            manager = BluetoothRouteManager(context)
            manager.refresh()
        }
        assertThat(manager.devices.value).isNotNull()
    }

    private fun track(
        id: String,
        title: String,
        sourceType: AudioSourceType = AudioSourceType.LOCAL,
        connectionId: String? = null,
    ) = AudioTrack(
        id = id,
        title = title,
        artist = "artist",
        album = "album",
        durationMillis = 1000L,
        uri = "content://$id",
        sourceType = sourceType,
        connectionId = connectionId,
        remotePath = "/$id.mp3",
    )
}
