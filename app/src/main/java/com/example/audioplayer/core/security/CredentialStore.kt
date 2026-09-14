package com.example.audioplayer.core.security

interface CredentialStore {
    suspend fun save(connectionId: String, password: String)
    suspend fun read(connectionId: String): String?
    suspend fun remove(connectionId: String)
}