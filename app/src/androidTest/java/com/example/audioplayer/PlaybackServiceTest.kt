package com.example.audioplayer

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.audioplayer.core.playback.PlaybackController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaybackServiceTest {
    @Test
    fun controller_connectsToMediaSessionService() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val controller = PlaybackController(context)
        controller.connect()

        val state = withTimeout(10_000L) {
            controller.state.first { it.isConnected }
        }
        assertTrue(state.isConnected)
    }
}