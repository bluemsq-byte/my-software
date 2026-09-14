package com.example.audioplayer.core.playlist

import com.example.audioplayer.core.model.AudioSourceType
import com.example.audioplayer.core.model.PlaylistItem
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PlaylistSelectionTest {
    @Test
    fun selectedInPlaylistOrder_preservesOriginalOrderNotClickOrder() {
        val items = listOf(item(1), item(2), item(3))
        val selected = PlaylistSelection.selectedInPlaylistOrder(items, setOf(3L, 1L))
        assertThat(selected.map { it.id }).containsExactly(1L, 3L).inOrder()
    }

    private fun item(id: Long) = PlaylistItem(
        id = id,
        playlistId = 1,
        position = (id - 1).toInt(),
        mediaId = "media-$id",
        title = "song-$id",
        artist = null,
        album = null,
        durationMillis = 0,
        uri = "content://$id",
        artworkUri = null,
        sourceType = AudioSourceType.LOCAL,
        connectionId = null,
        remotePath = null,
    )
}