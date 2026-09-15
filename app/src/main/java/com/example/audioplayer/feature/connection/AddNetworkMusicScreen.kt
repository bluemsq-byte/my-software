package com.example.audioplayer.feature.connection

import androidx.compose.runtime.Composable
import com.example.audioplayer.core.model.ConnectionProtocol
import com.example.audioplayer.ui.sketch.Sketch04AddNetworkMusicScreen

/**
 * 添加网络音乐入口。布局直接使用草图中的协议选择面板。
 */
@Composable
fun AddNetworkMusicScreen(
    onBack: () -> Unit,
    onChooseProtocol: (ConnectionProtocol) -> Unit,
) {
    Sketch04AddNetworkMusicScreen(
        onClose = onBack,
        onSmbSelected = { onChooseProtocol(ConnectionProtocol.SMB) },
        onWebDavSelected = { onChooseProtocol(ConnectionProtocol.WEBDAV) },
        onCancel = onBack,
    )
}
