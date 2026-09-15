package com.example.audioplayer.ui.sketch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 草图 04：添加网络音乐面板。
 */
@Composable
fun Sketch04AddNetworkMusicScreen(
    onClose: () -> Unit = {},
    onSmbSelected: () -> Unit = {},
    onWebDavSelected: () -> Unit = {},
    onCancel: () -> Unit = {},
) {
    SketchBaseScreen {
        Column(modifier = Modifier.fillMaxSize()) {
            SketchTopBar(
                title = "添加网络音乐",
                onBack = onClose,
                navigationIcon = Icons.Default.Close,
                navigationContentDescription = "关闭",
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = SketchSpacing.Page),
                verticalArrangement = Arrangement.Center,
            ) {
                SketchGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(SketchSpacing.Lg),
                    onClick = onSmbSelected,
                ) {
                    SketchSectionTitle("SMB 网络音乐")
                    Text(
                        text = "连接群晖共享文件夹",
                        color = SketchDesign.colors.muted,
                        style = SketchTextStyles.RowSubtitle,
                        modifier = Modifier.padding(top = SketchSpacing.Xs),
                    )
                }
                Spacer(modifier = Modifier.padding(top = SketchSpacing.Md))
                SketchGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(SketchSpacing.Lg),
                    onClick = onWebDavSelected,
                ) {
                    SketchSectionTitle("WebDAV 网络音乐")
                    Text(
                        text = "通过 URL 访问网络音乐目录",
                        color = SketchDesign.colors.muted,
                        style = SketchTextStyles.RowSubtitle,
                        modifier = Modifier.padding(top = SketchSpacing.Xs),
                    )
                }
                Spacer(modifier = Modifier.padding(top = SketchSpacing.Md))
                SketchSecondaryButton(
                    label = "取消",
                    onClick = onCancel,
                )
            }
        }
    }
}

/**
 * 草图 05：添加 / 编辑连接。
 */
@Composable
fun Sketch05ConnectionEditorScreen(
    protocolIndex: Int = 0,
    connectionName: String = "",
    server: String = "",
    port: String = "445",
    username: String = "",
    password: String = "",
    onBack: () -> Unit = {},
    onSave: () -> Unit = {},
    onProtocolSelected: (Int) -> Unit = {},
    onTestConnection: () -> Unit = {},
) {
    SketchBaseScreen {
        Column(modifier = Modifier.fillMaxSize()) {
            SketchTopBar(
                title = if (protocolIndex == 0) "添加 SMB" else "添加 WebDAV",
                onBack = onBack,
                actions = {
                    SketchIconAction(
                        icon = Icons.Default.Check,
                        contentDescription = "保存",
                        onClick = onSave,
                    )
                },
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(SketchSpacing.Page),
                verticalArrangement = Arrangement.spacedBy(SketchSpacing.Md),
            ) {
                SketchSegmentedTabs(
                    titles = listOf("SMB", "WebDAV"),
                    selectedIndex = protocolIndex,
                    onSelected = onProtocolSelected,
                )
                SketchField(label = "连接名称", value = connectionName)
                SketchField(label = "服务器地址或 IP", value = server)
                SketchField(label = "端口", value = port)
                SketchField(label = "用户名", value = username)
                SketchField(label = "密码", value = password)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SketchSpacing.Md),
                ) {
                    SketchSecondaryButton(
                        label = "测试连接",
                        onClick = onTestConnection,
                        modifier = Modifier.weight(1f),
                    )
                    SketchPrimaryButton(
                        label = "保存",
                        onClick = onSave,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/**
 * 草图 06：SMB 共享文件夹选择。
 */
@Composable
fun Sketch06SmbSharePickerScreen(
    shares: List<SketchFolderUiModel> = emptyList(),
    onBack: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onShareSelected: (SketchFolderUiModel) -> Unit = {},
) {
    SketchBaseScreen {
        Column(modifier = Modifier.fillMaxSize()) {
            SketchTopBar(
                title = "选择共享文件夹",
                onBack = onBack,
                actions = {
                    SketchIconAction(
                        icon = Icons.Default.Refresh,
                        contentDescription = "刷新共享文件夹",
                        onClick = onRefresh,
                    )
                },
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = SketchSpacing.Sm),
            ) {
                items(shares, key = { it.id }) { share ->
                    SketchFolderRow(
                        folder = share,
                        onClick = { onShareSelected(share) },
                    )
                }
            }
        }
    }
}
