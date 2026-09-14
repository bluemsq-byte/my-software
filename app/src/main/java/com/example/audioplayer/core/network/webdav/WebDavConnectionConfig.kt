package com.example.audioplayer.core.network.webdav

data class WebDavConnectionConfig(
    val id: String,
    val baseUrl: String,
    val username: String,
    val password: String,
)