package com.example.audioplayer.core.storage

import com.example.audioplayer.core.model.AudioSourceType
import com.example.audioplayer.core.model.AudioTrack
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class LocalFolderTreeTest {
    @Test
    fun build_usesCommonRootAndNestedFolders() {
        val root = LocalFolderTree.build(
            listOf(
                track("one", "/Music/华语/歌手A/one.mp3"),
                track("two", "/Music/华语/歌手B/two.mp3"),
                track("three", "/Music/English/three.mp3"),
            ),
        )

        assertThat(root.path).isEqualTo("/Music")
        assertThat(root.childFolders.map(LocalFolderNode::name))
            .containsExactly("English", "华语")
            .inOrder()

        val chinese = root.childFolders.first { it.name == "华语" }
        assertThat(chinese.childFolders.map(LocalFolderNode::name))
            .containsExactly("歌手A", "歌手B")
            .inOrder()
    }

    @Test
    fun breadcrumbs_describeCurrentFolder() {
        val root = LocalFolderTree.build(
            listOf(track("one", "/Music/华语/歌手A/one.mp3")),
        )
        val current = LocalFolderTree.findNode(root, "/Music/华语/歌手A")

        assertThat(current).isNotNull()
        assertThat(LocalFolderTree.breadcrumbs(root, current!!.path).map { it.name })
            .containsExactly("歌手A")
            .inOrder()
    }

    private fun track(id: String, path: String) = AudioTrack(
        id = id,
        title = id,
        artist = null,
        album = null,
        durationMillis = 1_000L,
        uri = "content://$id",
        sourceType = AudioSourceType.LOCAL,
        remotePath = path.substringBeforeLast('/'),
    )
}
