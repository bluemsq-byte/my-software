package com.example.audioplayer.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.audioplayer.core.model.ConnectionProtocol

@Entity(tableName = "connections")
data class ConnectionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val protocol: ConnectionProtocol,
    val host: String,
    val port: Int?,
    val username: String,
    val share: String?,
    val selectedShare: String?,
    val basePath: String,
    val domain: String?,
    val useHttps: Boolean,
)