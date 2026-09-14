package com.example.audioplayer.core.model

data class RemoteConnection(
    val id: String,
    val name: String,
    val protocol: ConnectionProtocol,
    val host: String,
    val port: Int?,
    val username: String,
    val password: String,
    val share: String? = null,
    val basePath: String = "/",
    val domain: String? = null,
    val useHttps: Boolean = true,
) {
    val displayAddress: String
        get() = buildString {
            append(protocol.name.lowercase())
            append("://")
            append(host)
            port?.let {
                append(':')
                append(it)
            }
            if (protocol == ConnectionProtocol.SMB && !share.isNullOrBlank()) {
                append('/')
                append(share)
            }
        }.let { address ->
            if (protocol == ConnectionProtocol.WEBDAV && basePath != "/") {
                address.trimEnd('/') + basePath
            } else {
                address
            }
        }
}