package com.example.audioplayer.feature.connection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audioplayer.core.model.ConnectionProtocol

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
            containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(if (state.id.isBlank()) "添加 NAS" else "编辑 NAS") },
                navigationIcon = {
                    OutlinedButton(onClick = onBack) { Text("返回") }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = state.protocol == ConnectionProtocol.SMB,
                    onClick = { viewModel.updateProtocol(ConnectionProtocol.SMB) },
                    label = { Text("SMB") },
                )
                FilterChip(
                    selected = state.protocol == ConnectionProtocol.WEBDAV,
                    onClick = { viewModel.updateProtocol(ConnectionProtocol.WEBDAV) },
                    label = { Text("WebDAV") },
                )
            }

            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::updateName,
                label = { Text("连接名称") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.host,
                onValueChange = viewModel::updateHost,
                label = { Text("服务器地址或 IP") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.port,
                onValueChange = viewModel::updatePort,
                label = { Text("端口") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.username,
                onValueChange = viewModel::updateUsername,
                label = { Text("用户名") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::updatePassword,
                label = { Text("密码") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.protocol == ConnectionProtocol.SMB) {
                state.selectedShare?.let { share ->
                    Text("已选择共享文件夹：$share")
                }
                OutlinedTextField(
                    value = state.domain,
                    onValueChange = viewModel::updateDomain,
                    label = { Text("域（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                OutlinedTextField(
                    value = state.basePath,
                    onValueChange = viewModel::updateBasePath,
                    label = { Text("WebDAV 路径") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Switch(checked = state.useHttps, onCheckedChange = viewModel::updateUseHttps)
                    Spacer(Modifier.padding(4.dp))
                    Text("使用 HTTPS")
                }
            }

            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = viewModel::testConnection,
                    enabled = !state.isTesting && !state.isSaving,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (state.isTesting) "测试中…" else "测试连接")
                }
                Button(
                    onClick = viewModel::save,
                    enabled = !state.isSaving && !state.isTesting,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(if (state.isSaving) "保存中…" else "保存")
                }
            }
        }
    }
}