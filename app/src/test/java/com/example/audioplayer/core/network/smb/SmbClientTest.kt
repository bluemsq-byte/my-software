package com.example.audioplayer.core.network.smb

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SmbClientTest {
    @Test
    fun list_mapsDirectoryItemsAndSortsDirectoriesFirst() {
        val session = FakeSmbShareSession().apply {
            entries["music"] = listOf(
                SmbDirectoryItem("song.mp3", "\\music\\song.mp3", false, 10L, null),
                SmbDirectoryItem("album", "\\music\\album", true, 0L, null),
            )
        }
        val client = SmbClient { session }
        val config = config()

        val result = client.list(config, "/music")

        assertThat(result.map { it.name }).containsExactly("album", "song.mp3").inOrder()
        assertThat(result.last().path).isEqualTo("/music/song.mp3")
        assertThat(result.last().size).isEqualTo(10L)
    }

    @Test
    fun openFile_usesConfiguredSharePath() {
        val session = FakeSmbShareSession()
        val client = SmbClient { session }

        client.openFile(config(), "/music/song.mp3").close()

        assertThat(session.openedPaths).containsExactly("music\\song.mp3")
    }

    private fun config() = SmbConnectionConfig(
        id = "nas",
        host = "192.168.1.2",
        username = "user",
        password = "password",
        share = "music",
    )

    private class FakeSmbShareSession : SmbShareSession {
        val entries = mutableMapOf<String, List<SmbDirectoryItem>>()
        val openedPaths = mutableListOf<String>()

        override fun list(path: String): List<SmbDirectoryItem> = entries[path].orEmpty()

        override fun openFile(path: String): SmbReadableFile {
            openedPaths += path
            return object : SmbReadableFile {
                override val length: Long = 0L
                override fun read(buffer: ByteArray, fileOffset: Long, bufferOffset: Int, length: Int) = -1
                override fun close() = Unit
            }
        }

        override fun close() = Unit
    }
}