package com.example.audioplayer.core.network

sealed class RemoteException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause) {
    class Unreachable(message: String, cause: Throwable? = null) : RemoteException(message, cause)
    class AuthenticationFailed(message: String = "账号或密码错误") : RemoteException(message)
    class PathNotFound(message: String = "服务器上找不到该路径") : RemoteException(message)
    class CertificateRejected(message: String = "HTTPS 证书不受信任") : RemoteException(message)
    class ProtocolError(message: String) : RemoteException(message)
}