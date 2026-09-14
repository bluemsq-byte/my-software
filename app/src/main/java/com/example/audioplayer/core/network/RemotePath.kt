package com.example.audioplayer.core.network

object RemotePath {
    fun normalize(path: String): String {
        val segments = path
            .replace('\\', '/')
            .split('/')
            .filter { it.isNotBlank() && it != "." }

        val resolved = ArrayDeque<String>()
        for (segment in segments) {
            when (segment) {
                ".." -> if (resolved.isNotEmpty()) resolved.removeLast()
                else -> resolved.addLast(segment)
            }
        }

        return if (resolved.isEmpty()) "/" else resolved.joinToString(separator = "/", prefix = "/")
    }

    fun join(base: String, child: String): String {
        if (child.isBlank()) return normalize(base)
        return normalize(normalize(base).trimEnd('/') + "/" + child.trimStart('/'))
    }

    fun parent(path: String): String? {
        val normalized = normalize(path)
        if (normalized == "/") return null
        val index = normalized.lastIndexOf('/')
        return if (index <= 0) "/" else normalized.substring(0, index)
    }

    fun name(path: String): String {
        val normalized = normalize(path)
        return if (normalized == "/") "" else normalized.substringAfterLast('/')
    }

    fun toSmb(path: String): String = normalize(path)
        .removePrefix("/")
        .replace('/', '\\')

    fun fromSmb(path: String): String = normalize(path.replace('\\', '/'))
}