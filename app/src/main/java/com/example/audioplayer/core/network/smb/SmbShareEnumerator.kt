package com.example.audioplayer.core.network.smb

import com.example.audioplayer.core.network.RemoteException
import java.net.UnknownHostException
import java.util.Properties
import jcifs.config.PropertyConfiguration
import jcifs.context.BaseContext
import jcifs.smb.NtlmPasswordAuthenticator
import jcifs.smb.SmbAuthException
import jcifs.smb.SmbException
import jcifs.smb.SmbFile

class SmbShareEnumerator {
    fun listShares(
        host: String,
        port: Int,
        domain: String,
        username: String,
        password: String,
    ): List<String> {
        val properties = Properties().apply {
            setProperty("jcifs.smb.client.enableSMB2", "true")
            setProperty("jcifs.smb.client.disableSMB1", "true")
            setProperty("jcifs.smb.client.responseTimeout", "15000")
            setProperty("jcifs.smb.client.connTimeout", "15000")
            setProperty("jcifs.smb.client.soTimeout", "15000")
        }
        val authenticator = NtlmPasswordAuthenticator(domain, username, password)
        val context = BaseContext(PropertyConfiguration(properties)).withCredentials(authenticator)
        val rootUrl = "smb://$host:$port/"

        return try {
            val root = SmbFile(rootUrl, context)
            try {
                SmbShareNameFilter.normalize(root.list().toList())
            } finally {
                root.close()
            }
        } catch (exception: SmbAuthException) {
            throw RemoteException.AuthenticationFailed()
        } catch (exception: SmbException) {
            throw RemoteException.ProtocolError("无法读取 SMB 共享列表：${exception.message.orEmpty()}")
        } catch (exception: UnknownHostException) {
            throw RemoteException.Unreachable("找不到 NAS，请检查地址和网络", exception)
        } catch (exception: Exception) {
            throw RemoteException.Unreachable("无法连接 NAS：${exception.message.orEmpty()}", exception)
        }
    }
}