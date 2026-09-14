package com.example.audioplayer.core.storage

import com.example.audioplayer.core.model.AudioSourceType
import com.example.audioplayer.core.model.AudioTrack
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LocalFolderGrouperTest {
    @Test
    fun group_groupsTracksByFolderAndSortsNames() {
        val folders = LocalFolderGrouper.group(
            listOf(
                track("b", "/Music/B"),
                track("a", "/Music/A"),
                track("c", "/Music/A"),
                track("root", "/"),
            ),
        )

        assertThat(folders.map { it.name }).containsExactly("A", "B", "内部存储").inOrder()
        assertThat(folders.first().tracks.map { it.title }).containsExactly("a", "c").inOrder()
    }

    private fun track(title: String, folder: String) = AudioTrack(
        id = title,
        title = title,
        artist = null,
        album = null,
        durationMillis = 0L,
        uri = "content://track/$title",
        sourceType = AudioSourceType.LOCAL,
        remotePath = folder,
    )
}