package com.example.audioplayer.core.network.smb

data class SmbConnectionConfig(
    val id: String,
    val host: String,
    val port: Int = 445,
    val username: String,
    val password: String,
    val domain: String = "",
    val share: String,
)