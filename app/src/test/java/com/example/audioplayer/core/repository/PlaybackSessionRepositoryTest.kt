package com.example.audioplayer.core.repository

import com.example.audioplayer.core.database.PlaybackQueueEntity
import com.example.audioplayer.core.database.PlaybackSessionDao
import com.example.audioplayer.core.database.PlaybackSessionEntity
import com.example.audioplayer.core.model.AudioSourceType
import com.example.audioplayer.core.model.AudioTrack
import com.example.audioplayer.core.playback.PlaybackMode
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PlaybackSessionRepositoryTest {
    private val dao = FakePlaybackSessionDao()
    private val repository = PlaybackSessionRepository(dao)

    @Test
    fun saveAndLoad_restoresQueuePositionAndMode() = runTest {
        val tracks = listOf(
            track("one", "第一首"),
            track("two", "第二首"),
        )

        repository.save(
            queue = tracks,
            currentIndex = 1,
            positionMillis = 42_000L,
            playbackMode = PlaybackMode.REPEAT_ALL,
        )
        val restored = repository.load()

        assertThat(restored).isNotNull()
        assertThat(restored!!.queue.map(AudioTrack::id)).containsExactly("one", "two").inOrder()
        assertThat(restored.currentIndex).isEqualTo(1)
        assertThat(restored.positionMillis).isEqualTo(42_000L)
        assertThat(restored.playbackMode).isEqualTo(PlaybackMode.REPEAT_ALL)
    }

    @Test
    fun clear_removesRestorableSession() = runTest {
        repository.save(
            queue = listOf(track("one", "第一首")),
            currentIndex = 0,
            positionMillis = 1_000L,
            playbackMode = PlaybackMode.SEQUENTIAL,
        )

        repository.clear()

        assertThat(repository.load()).isNull()
    }

    private fun track(id: String, title: String) = AudioTrack(
        id = id,
        title = title,
        artist = "测试歌手",
        album = "测试专辑",
        durationMillis = 180_000L,
        uri = "content://test/$id",
        sourceType = AudioSourceType.LOCAL,
    )

    private class FakePlaybackSessionDao : PlaybackSessionDao {
        private var session: PlaybackSessionEntity? = null
        private var queue: List<PlaybackQueueEntity> = emptyList()

        override suspend fun getSession(): PlaybackSessionEntity? = session

        override suspend fun getQueue(): List<PlaybackQueueEntity> =
            queue.sortedBy(PlaybackQueueEntity::position)

        override suspend fun upsertSession(session: PlaybackSessionEntity) {
            this.session = session
        }

        override suspend fun insertQueue(items: List<PlaybackQueueEntity>) {
            queue = items
        }

        override suspend fun deleteQueue() {
            queue = emptyList()
        }

        override suspend fun deleteSession() {
            session = null
        }
    }
}
