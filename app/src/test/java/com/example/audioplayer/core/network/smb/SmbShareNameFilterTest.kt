package com.example.audioplayer.core.network.smb

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SmbShareNameFilterTest {
    @Test
    fun normalize_hidesSystemSharesAndSortsNames() {
        val result = SmbShareNameFilter.normalize(
            listOf("Music/", "video/", "IPC$", "music/", "  ", "Documents"),
        )

        assertThat(result).containsExactly("Documents", "Music", "video").inOrder()
    }
}
