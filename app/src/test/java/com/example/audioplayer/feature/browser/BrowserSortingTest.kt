package com.example.audioplayer.feature.browser

import com.example.audioplayer.core.network.RemoteEntry
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BrowserSortingTest {
    @Test
    fun visibleEntries_keepsDirectoriesFirstAndSortsByName() {
        val state = BrowserUiState(
            entries = listOf(
                RemoteEntry("z.mp3", "/z.mp3", isDirectory = false),
                RemoteEntry("b", "/b", isDirectory = true),
                RemoteEntry("a.mp3", "/a.mp3", isDirectory = false),
                RemoteEntry("a", "/a", isDirectory = true),
            ),
            sortMode = BrowserSortMode.NAME,
        )

        assertThat(state.visibleEntries.map(RemoteEntry::name))
            .containsExactly("a", "b", "a.mp3", "z.mp3")
            .inOrder()
    }

    @Test
    fun visibleEntries_respectsDescendingDirectionWithinGroups() {
        val state = BrowserUiState(
            entries = listOf(
                RemoteEntry("b", "/b", isDirectory = true),
                RemoteEntry("a", "/a", isDirectory = true),
                RemoteEntry("b.mp3", "/b.mp3", isDirectory = false),
                RemoteEntry("a.mp3", "/a.mp3", isDirectory = false),
            ),
            sortMode = BrowserSortMode.NAME,
            sortAscending = false,
        )

        assertThat(state.visibleEntries.map(RemoteEntry::name))
            .containsExactly("b", "a", "b.mp3", "a.mp3")
            .inOrder()
    }
}
