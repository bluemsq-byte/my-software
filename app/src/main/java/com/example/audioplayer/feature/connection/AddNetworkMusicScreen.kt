package com.example.audioplayer.feature.connection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.audioplayer.core.model.ConnectionProtocol

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNetworkMusicScreen(
    onBack: () -> Unit,
    onChooseProtocol: (ConnectionProtocol) -> Unit,
) {
    Scaffold(
            containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("添加网络音乐") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text("选择网络音乐协议")
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { onChooseProtocol(ConnectionProtocol.SMB) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Folder, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text("SMB 网络音乐")
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { onChooseProtocol(ConnectionProtocol.WEBDAV) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Cloud, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text("WebDAV 网络音乐")
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("返回")
            }
        }
    }
}
