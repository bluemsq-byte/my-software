package com.example.audioplayer.core.playlist

import com.example.audioplayer.core.model.PlaylistItem

object PlaylistSelection {
    fun selectedInPlaylistOrder(
        items: List<PlaylistItem>,
        selectedIds: Set<Long>,
    ): List<PlaylistItem> = items.filter { it.id in selectedIds }
}