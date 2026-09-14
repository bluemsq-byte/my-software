package com.example.audioplayer.core.network.smb

object SmbShareNameFilter {
    fun normalize(rawNames: List<String>): List<String> {
        return rawNames
            .map { it.trimEnd('/').substringAfterLast('/').trim() }
            .filter { it.isNotBlank() && !it.endsWith("\$") }
            .distinctBy { it.lowercase() }
            .sortedWith(String.CASE_INSENSITIVE_ORDER)
    }
}
