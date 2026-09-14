package com.example.audioplayer.feature.library

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audioplayer.core.database.ConnectionEntity
import com.example.audioplayer.core.model.AudioTrack
import com.example.audioplayer.core.model.RecentPlay
import com.example.audioplayer.core.storage.MediaPermission
import com.example.audioplayer.ui.formatDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    initialTab: Int = 0,
    onAddConnection: () -> Unit,
    onOpenConnection: (ConnectionEntity) -> Unit,
    onEditConnection: (ConnectionEntity) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val localState by viewModel.localState.collectAsStateWithLifecycle()
    val connections by viewModel.connections.collectAsStateWithLifecycle()
    val testingConnectionId by viewModel.testingConnectionId.collectAsStateWithLifecycle()
    val recentPlays by viewModel.recentPlays.collectAsStateWithLifecycle()
    val connectionMessage by viewModel.connectionMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var localViewIsFolders by remember { mutableStateOf(false) }
    var hasPermission by remember { mutableStateOf(MediaPermission.isGranted(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasPermission = granted
        if (granted) viewModel.loadLocal(force = true)
    }

    LaunchedEffect(connectionMessage) {
        connectionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearConnectionMessage()
        }
    }

    LaunchedEffect(hasPermission) {
        if (hasPermission) viewModel.loadLocal()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("音乐库") },
                actions = {
                    IconButton(onClick = onAddConnection) {
                        Icon(Icons.Default.Add, contentDescription = "添加网络音乐")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("本地音乐") },
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("网络音乐") },
                )
            }

            if (recentPlays.isNotEmpty()) {
                RecentPlaySection(
                    items = recentPlays,
                    onPlay = viewModel::playRecent,
                    onClear = viewModel::clearRecentPlays,
                )
            }

            if (selectedTab == 0) {
                LocalMusicContent(
                    state = localState,
                    hasPermission = hasPermission,
                    showFolders = localViewIsFolders,
                    onToggleView = { localViewIsFolders = !localViewIsFolders },
                    onRequestPermission = { permissionLauncher.launch(MediaPermission.permission) },
                    onRefresh = { viewModel.loadLocal(force = true) },
                    onSearch = viewModel::updateLocalSearchQuery,
                    onPlay = viewModel::play,
                )
            } else {
                NetworkMusicContent(
                    connections = connections,
                    onAddConnection = onAddConnection,
                    onOpenConnection = onOpenConnection,
                    onDeleteConnection = viewModel::deleteConnection,
                    onEditConnection = onEditConnection,
                    onTestConnection = viewModel::testConnection,
                    testingConnectionId = testingConnectionId,
                )
            }
        }
    }
}

@Composable
private fun LocalMusicContent(
    state: LocalLibraryUiState,
    hasPermission: Boolean,
    showFolders: Boolean,
    onToggleView: () -> Unit,
    onRequestPermission: () -> Unit,
    onRefresh: () -> Unit,
    onSearch: (String) -> Unit,
    onPlay: (List<AudioTrack>, Int) -> Unit,
) {
    when {
        !hasPermission -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("需要访问手机中的音频文件")
                Spacer(Modifier.height(16.dp))
                Button(onClick = onRequestPermission) {
                    Text("允许访问音频")
                }
            }
        }

        state.isLoading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
                Spacer(Modifier.height(12.dp))
                Text("正在扫描本地音乐…")
            }
        }

        state.errorMessage != null -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(12.dp))
                Button(onClick = onRefresh) { Text("重新扫描") }
            }
        }

        state.tracks.isEmpty() -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("没有找到本地音乐")
                Spacer(Modifier.height(12.dp))
                Button(onClick = onRefresh) { Text("重新扫描") }
            }
        }

        else -> {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearch,
                label = { Text("搜索本地音乐") },
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
                Text(
                    text = "共 ${state.tracks.size} 首",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedButton(onClick = onToggleView) {
                    Text(if (showFolders) "按歌曲" else "按文件夹")
                }
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "重新扫描")
                }
            }

            if (showFolders) {
                LazyColumn {
                    items(state.visibleFolders, key = { it.path }) { folder ->
                        ListItem(
                            headlineContent = { Text(folder.name) },
                            supportingContent = { Text("${folder.tracks.size} 首") },
                            leadingContent = {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                            )
                        },
                            modifier = Modifier.clickable {
                                onPlay(folder.tracks, 0)
                            },
                        )
                    }
                }
            } else {
                LazyColumn {
                    items(state.visibleTracks, key = { it.id }) { track ->
                        AudioTrackRow(
                            track = track,
                            onClick = {
                                onPlay(state.visibleTracks, state.visibleTracks.indexOf(track))
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentPlaySection(
    items: List<RecentPlay>,
    onPlay: (RecentPlay) -> Unit,
    onClear: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("最近播放", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            OutlinedButton(onClick = onClear) { Text("清空") }
        }
        LazyRow(contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)) {
            items(items, key = { it.mediaId }) { item ->
                Card(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .clickable { onPlay(item) },
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(item.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            item.artist ?: if (item.sourceType.name == "LOCAL") "本地音乐" else "NAS 音乐",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NetworkMusicContent(
    connections: List<ConnectionEntity>,
    onAddConnection: () -> Unit,
    onOpenConnection: (ConnectionEntity) -> Unit,
    onDeleteConnection: (ConnectionEntity) -> Unit,
    onEditConnection: (ConnectionEntity) -> Unit,
    onTestConnection: (ConnectionEntity) -> Unit,
    testingConnectionId: String?,
) {
    if (connections.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("还没有连接群晖 NAS")
            Spacer(Modifier.height(8.dp))
            Text(
                "第一版支持 SMB 和 WebDAV",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))
            Button(onClick = onAddConnection) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("添加 NAS")
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(connections, key = { it.id }) { connection ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenConnection(connection) },
                ) {
                    ListItem(
                        headlineContent = { Text(connection.name) },
                        supportingContent = {
                            Text(
                                "${connection.protocol.name} · ${connection.host}${connection.port?.let { ":$it" }.orEmpty()}",
                            )
                        },
                        leadingContent = { Icon(Icons.Default.Folder, contentDescription = null) },
                        trailingContent = {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                OutlinedButton(
                                    onClick = { onTestConnection(connection) },
                                    enabled = testingConnectionId != connection.id,
                                ) {
                                    Text(if (testingConnectionId == connection.id) "测试中" else "测试")
                                }
                                OutlinedButton(onClick = { onEditConnection(connection) }) {
                                    Text("编辑")
                                }
                                OutlinedButton(onClick = { onDeleteConnection(connection) }) {
                                    Text("删除")
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun AudioTrackRow(
    track: AudioTrack,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = {
            Text(track.artist ?: "未知歌手", maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        leadingContent = { Icon(Icons.Default.MusicNote, contentDescription = null) },
        trailingContent = { Text(formatDuration(track.durationMillis)) },
        modifier = Modifier.clickable(onClick = onClick),
    )
}