package com.example.audioplayer.core.network.webdav

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

object WebDavConnectionMatcher {
    fun matches(baseUrl: String, targetUrl: String): Boolean {
        val base = baseUrl.toHttpUrlOrNull() ?: return false
        val target = targetUrl.toHttpUrlOrNull() ?: return false
        if (!base.host.equals(target.host, ignoreCase = true)) return false
        if (base.port != target.port) return false

        val baseSegments = base.pathSegments.filter { it.isNotBlank() }
        val targetSegments = target.pathSegments.filter { it.isNotBlank() }
        return targetSegments.size >= baseSegments.size &&
            baseSegments.indices.all { index ->
                targetSegments[index] == baseSegments[index]
            }
    }
}