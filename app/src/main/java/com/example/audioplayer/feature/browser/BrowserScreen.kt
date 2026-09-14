package com.example.audioplayer.feature.browser

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audioplayer.core.network.RemoteEntry
import com.example.audioplayer.core.network.RemotePath

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    onBack: () -> Unit,
    onOpenPlayer: () -> Unit,
    viewModel: BrowserViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    var pendingPlaylistEntry by remember { mutableStateOf<RemoteEntry?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.path,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.load() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                label = { Text("搜索当前文件夹") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("仅显示音频", modifier = Modifier.weight(1f))
                Switch(checked = state.audioOnly, onCheckedChange = { viewModel.toggleAudioOnly() })
            }

            when {
                state.isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                    ) {
                        Text(state.errorMessage.orEmpty())
                        androidx.compose.material3.Button(onClick = { viewModel.load() }) {
                            Text("重试")
                        }
                    }
                }

                else -> {
                    LazyColumn {
                        RemotePath.parent(state.path)?.let { parent ->
                            item(key = "parent") {
                                ListItem(
                                    headlineContent = { Text("返回上级") },
                                    leadingContent = { Icon(Icons.Default.ArrowBack, contentDescription = null) },
                                    modifier = Modifier.clickable {
                                        viewModel.load(parent)
                                    },
                                )
                            }
                        }
                        items(state.visibleEntries, key = { it.path }) { entry ->
                            EntryRow(
                                entry = entry,
                                onClick = {
                                    if (entry.isDirectory) {
                                        viewModel.openDirectory(entry)
                                    } else {
                                        viewModel.play(entry)
                                        onOpenPlayer()
                                    }
                                },
                                onAddToPlaylist = if (entry.isDirectory) null else {
                                    { pendingPlaylistEntry = entry }
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    pendingPlaylistEntry?.let { entry ->
        AlertDialog(
            onDismissRequest = { pendingPlaylistEntry = null },
            title = { Text("加入播放列表") },
            text = {
                Column {
                    if (playlists.isEmpty()) {
                        Text("还没有播放列表，请先到播放列表页面创建")
                    } else {
                        playlists.forEach { playlist ->
                            TextButton(
                                onClick = {
                                    viewModel.addToPlaylist(playlist.id, entry)
                                    pendingPlaylistEntry = null
                                },
                            ) {
                                Text(playlist.name)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { pendingPlaylistEntry = null }) { Text("关闭") }
            },
        )
    }
}

@Composable
private fun EntryRow(
    entry: RemoteEntry,
    onClick: () -> Unit,
    onAddToPlaylist: (() -> Unit)?,
) {
    ListItem(
        headlineContent = {
            Text(entry.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        leadingContent = {
            Icon(
                imageVector = if (entry.isDirectory) Icons.Default.Folder else Icons.Default.AudioFile,
                contentDescription = null,
            )
        },
        trailingContent = onAddToPlaylist?.let {
            {
                androidx.compose.material3.OutlinedButton(onClick = it) {
                    Text("加列表")
                }
            }
        },
        modifier = Modifier.clickable(onClick = onClick),
    )
}