package com.example.audioplayer.ui

fun formatDuration(durationMillis: Long): String {
    if (durationMillis <= 0L) return "--:--"
    val totalSeconds = durationMillis / 1_000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}