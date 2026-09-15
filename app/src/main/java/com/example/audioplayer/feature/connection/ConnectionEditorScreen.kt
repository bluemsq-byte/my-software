package com.example.audioplayer.feature.connection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audioplayer.core.model.ConnectionProtocol
import com.example.audioplayer.ui.sketch.SketchBaseScreen
import com.example.audioplayer.ui.sketch.SketchEditableField
import com.example.audioplayer.ui.sketch.SketchGlassCard
import com.example.audioplayer.ui.sketch.SketchIconAction
import com.example.audioplayer.ui.sketch.SketchPrimaryButton
import com.example.audioplayer.ui.sketch.SketchSecondaryButton
import com.example.audioplayer.ui.sketch.SketchSegmentedTabs
import com.example.audioplayer.ui.sketch.SketchSpacing
import com.example.audioplayer.ui.sketch.SketchSwitchRow
import com.example.audioplayer.ui.sketch.SketchTopBar

/**
 * 添加/编辑网络连接。表单结构和原 ViewModel 状态一一对应。
 */
@Composable
fun ConnectionEditorScreen(
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: ConnectionEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    LaunchedEffect(state.saved) {
        if (state.saved) onSaved()
    }

    SketchBaseScreen {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                SketchTopBar(
                    title = if (state.id.isBlank()) "添加 NAS" else "编辑 NAS",
                    onBack = onBack,
                    actions = {
                        SketchIconAction(
                            icon = Icons.Default.Check,
                            contentDescription = "保存连接",
                            onClick = viewModel::save,
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
                        selectedIndex = if (state.protocol == ConnectionProtocol.SMB) 0 else 1,
                        onSelected = { index ->
                            viewModel.updateProtocol(
                                if (index == 0) ConnectionProtocol.SMB else ConnectionProtocol.WEBDAV,
                            )
                        },
                    )
                    SketchEditableField(
                        label = "连接名称",
                        value = state.name,
                        onValueChange = viewModel::updateName,
                    )
                    SketchEditableField(
                        label = "服务器地址或 IP",
                        value = state.host,
                        onValueChange = viewModel::updateHost,
                    )
                    SketchEditableField(
                        label = "端口",
                        value = state.port,
                        onValueChange = viewModel::updatePort,
                    )
                    SketchEditableField(
                        label = "用户名",
                        value = state.username,
                        onValueChange = viewModel::updateUsername,
                    )
                    SketchEditableField(
                        label = "密码",
                        value = state.password,
                        onValueChange = viewModel::updatePassword,
                        password = true,
                    )
                    if (state.protocol == ConnectionProtocol.SMB) {
                        state.selectedShare?.let { share ->
                            SketchGlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(SketchSpacing.Md),
                            ) {
                                androidx.compose.material3.Text("已选择共享文件夹：$share")
                            }
                        }
                        SketchEditableField(
                            label = "域（可选）",
                            value = state.domain,
                            onValueChange = viewModel::updateDomain,
                        )
                    } else {
                        SketchEditableField(
                            label = "WebDAV 路径",
                            value = state.basePath,
                            onValueChange = viewModel::updateBasePath,
                        )
                        SketchSwitchRow(
                            title = "使用 HTTPS",
                            subtitle = "仅连接证书有效的 WebDAV 服务",
                            checked = state.useHttps,
                            onCheckedChange = viewModel::updateUseHttps,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(SketchSpacing.Md),
                    ) {
                        SketchSecondaryButton(
                            label = if (state.isTesting) "测试中…" else "测试连接",
                            onClick = viewModel::testConnection,
                            modifier = Modifier.weight(1f),
                        )
                        SketchPrimaryButton(
                            label = if (state.isSaving) "保存中…" else "保存",
                            onClick = viewModel::save,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}
