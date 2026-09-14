package com.example.audioplayer.core.database

data class PlaylistSummaryRow(
    val id: Long,
    val name: String,
    val itemCount: Int,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)