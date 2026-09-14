package com.example.audioplayer.core.repository

import com.example.audioplayer.core.database.ConnectionDao
import com.example.audioplayer.core.database.ConnectionEntity
import com.example.audioplayer.core.model.ConnectionProtocol
import com.example.audioplayer.core.model.RemoteConnection
import com.example.audioplayer.core.security.CredentialStore
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class ConnectionRepository @Inject constructor(
    private val connectionDao: ConnectionDao,
    private val credentialStore: CredentialStore,
) {
    private val cache = ConcurrentHashMap<String, RemoteConnection>()

    fun observeAll(): Flow<List<ConnectionEntity>> = connectionDao.observeAll()

    suspend fun get(id: String): RemoteConnection? {
        val entity = connectionDao.getById(id) ?: return null
        return entity.toModel(credentialStore.read(id).orEmpty()).also {
            cache[id] = it
        }
    }

    fun getCached(id: String): RemoteConnection? = cache[id]

    fun cachedConnections(): List<RemoteConnection> = cache.values.toList()

    suspend fun save(connection: RemoteConnection) {
        connectionDao.upsert(connection.toEntity())
        credentialStore.save(connection.id, connection.password)
        cache[connection.id] = connection
    }

    suspend fun delete(connection: RemoteConnection) {
        connectionDao.delete(connection.toEntity())
        credentialStore.remove(connection.id)
        cache.remove(connection.id)
    }

    private fun ConnectionEntity.toModel(password: String) = RemoteConnection(
        id = id,
        name = name,
        protocol = protocol,
        host = host,
        port = port,
        username = username,
        password = password,
        share = share,
        basePath = basePath,
        domain = domain,
        selectedShare = selectedShare,
        useHttps = useHttps,
    )

    private fun RemoteConnection.toEntity() = ConnectionEntity(
        id = id,
        name = name,
        protocol = protocol,
        host = host,
        port = port,
        username = username,
        share = share,
        selectedShare = selectedShare,
        basePath = if (protocol == ConnectionProtocol.WEBDAV) basePath else "/",
        domain = domain,
        useHttps = useHttps,
    )
}