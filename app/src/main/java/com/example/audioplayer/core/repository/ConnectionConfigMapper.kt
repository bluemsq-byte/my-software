package com.example.audioplayer.core.repository

import com.example.audioplayer.core.model.ConnectionProtocol
import com.example.audioplayer.core.model.RemoteConnection
import com.example.audioplayer.core.network.RemotePath
import com.example.audioplayer.core.network.smb.SmbConnectionConfig
import com.example.audioplayer.core.network.webdav.WebDavConnectionConfig

fun RemoteConnection.toSmbConfig(): SmbConnectionConfig {
    require(protocol == ConnectionProtocol.SMB) { "Connection is not SMB" }
    return SmbConnectionConfig(
        id = id,
        host = host,
        port = port ?: 445,
        username = username,
        password = password,
        domain = domain.orEmpty(),
        share = requireNotNull(selectedShare ?: share),
    )
}

fun RemoteConnection.toWebDavConfig(): WebDavConnectionConfig {
    require(protocol == ConnectionProtocol.WEBDAV) { "Connection is not WebDAV" }
    val scheme = if (useHttps) "https" else "http"
    val authority = buildString {
        append(host)
        port?.let {
            append(':')
            append(it)
        }
    }
    val path = RemotePath.normalize(basePath)
    val baseUrl = if (path == "/") {
        "$scheme://$authority/"
    } else {
        "$scheme://$authority${path.trimEnd('/')}/"
    }
    return WebDavConnectionConfig(
        id = id,
        baseUrl = baseUrl,
        username = username,
        password = password,
    )
}