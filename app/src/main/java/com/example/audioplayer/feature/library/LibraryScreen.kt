package com.example.audioplayer.feature.library

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audioplayer.core.database.ConnectionEntity
import com.example.audioplayer.core.model.AudioTrack
import com.example.audioplayer.core.model.RecentPlay
import com.example.audioplayer.core.playback.PlaybackUiState
import com.example.audioplayer.core.storage.MediaPermission
import com.example.audioplayer.ui.formatDuration
import com.example.audioplayer.ui.sketch.SketchConnectionRow
import com.example.audioplayer.ui.sketch.SketchConnectionUiModel
import com.example.audioplayer.ui.sketch.SketchContinuePlayingCard
import com.example.audioplayer.ui.sketch.SketchDesign
import com.example.audioplayer.ui.sketch.SketchEmptyState
import com.example.audioplayer.ui.sketch.SketchFolderRow
import com.example.audioplayer.ui.sketch.SketchFolderUiModel
import com.example.audioplayer.ui.sketch.SketchGlassCard
import com.example.audioplayer.ui.sketch.SketchIconAction
import com.example.audioplayer.ui.sketch.SketchMenuActionUiModel
import com.example.audioplayer.ui.sketch.SketchPill
import com.example.audioplayer.ui.sketch.SketchSearchField
import com.example.audioplayer.ui.sketch.SketchSectionTitle
import com.example.audioplayer.ui.sketch.SketchSegmentedTabs
import com.example.audioplayer.ui.sketch.SketchSpacing
import com.example.audioplayer.ui.sketch.SketchTextStyles
import com.example.audioplayer.ui.sketch.SketchToolbar
import com.example.audioplayer.ui.sketch.SketchTopBar
import com.example.audioplayer.ui.sketch.SketchTrackRow
import com.example.audioplayer.ui.sketch.SketchTrackUiModel

/**
 * 首页。保留原 LibraryViewModel 的扫描、搜索、播放和 NAS 连接逻辑，
 * 只把页面结构和视觉替换为设计规范中的毛玻璃方案。
 */
@Composable
fun LibraryScreen(
    initialTab: Int = 0,
    onAddConnection: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenRecent: () -> Unit,
    onOpenConnection: (ConnectionEntity) -> Unit,
    onEditConnection: (ConnectionEntity) -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val localState by viewModel.localState.collectAsStateWithLifecycle()
    val connections by viewModel.connections.collectAsStateWithLifecycle()
    val connectionMessage by viewModel.connectionMessage.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val recentPlays by viewModel.recentPlays.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pane by remember {
        mutableStateOf(if (initialTab == 1) LibraryPane.NETWORK else LibraryPane.HOME)
    }
    var localViewMode by remember { mutableStateOf(LibraryViewMode.SONGS) }
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
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (pane) {
                LibraryPane.HOME -> {
                    SketchTopBar(
                        title = "音乐",
                        actions = {
                            SketchIconAction(
                                icon = Icons.Default.Search,
                                contentDescription = "全局搜索",
                                onClick = onOpenSearch,
                            )
                            SketchIconAction(
                                icon = Icons.Default.Add,
                                contentDescription = "添加网络音乐",
                                onClick = onAddConnection,
                            )
                        },
                    )
                    HomeContent(
                        playbackState = playbackState,
                        recentPlays = recentPlays,
                        connectionCount = connections.size,
                        songCount = localState.tracks.size,
                        onResumePlayback = viewModel::resumePlayback,
                        onOpenLocal = { pane = LibraryPane.LOCAL },
                        onOpenNetwork = { pane = LibraryPane.NETWORK },
                        onOpenRecent = onOpenRecent,
                        onPlayRecent = viewModel::playRecent,
                    )
                }

                LibraryPane.LOCAL -> {
                    SketchTopBar(
                        title = "本地音乐",
                        onBack = { pane = LibraryPane.HOME },
                        actions = {
                            SketchIconAction(
                                icon = Icons.Default.Search,
                                contentDescription = "全局搜索",
                                onClick = onOpenSearch,
                            )
                        },
                    )
                    LocalMusicContent(
                        state = localState,
                        hasPermission = hasPermission,
                        viewMode = localViewMode,
                        onViewModeChange = { localViewMode = it },
                        onRequestPermission = {
                            permissionLauncher.launch(MediaPermission.permission)
                        },
                        onOpenNetworkMusic = {
                            pane = LibraryPane.NETWORK
                        },
                        onRefresh = { viewModel.loadLocal(force = true) },
                        onSearch = viewModel::updateLocalSearchQuery,
                        onPlay = viewModel::play,
                        onPlayNext = viewModel::playNext,
                        onAddToQueue = viewModel::addToQueue,
                    )
                }

                LibraryPane.NETWORK -> {
                    SketchTopBar(
                        title = "网络音乐",
                        onBack = { pane = LibraryPane.HOME },
                        actions = {
                            SketchIconAction(
                                icon = Icons.Default.Add,
                                contentDescription = "添加网络音乐",
                                onClick = onAddConnection,
                            )
                        },
                    )
                    NetworkMusicContent(
                        connections = connections,
                        onAddConnection = onAddConnection,
                        onOpenConnection = onOpenConnection,
                        onDeleteConnection = viewModel::deleteConnection,
                        onEditConnection = onEditConnection,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    playbackState: PlaybackUiState,
    recentPlays: List<RecentPlay>,
    connectionCount: Int,
    songCount: Int,
    onResumePlayback: () -> Unit,
    onOpenLocal: () -> Unit,
    onOpenNetwork: () -> Unit,
    onOpenRecent: () -> Unit,
    onPlayRecent: (RecentPlay) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = SketchSpacing.Xl),
    ) {
        if (playbackState.currentTrackId != null) {
            item {
                SketchContinuePlayingCard(
                    title = playbackState.title,
                    artist = playbackState.artist.orEmpty(),
                    isPlaying = playbackState.isPlaying,
                    onClick = onResumePlayback,
                )
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SketchSpacing.Page, vertical = SketchSpacing.Xs),
                horizontalArrangement = Arrangement.spacedBy(SketchSpacing.Md),
            ) {
                HomeEntryCard(
                    title = "本地音乐",
                    subtitle = "$songCount 首",
                    icon = Icons.Default.Folder,
                    onClick = onOpenLocal,
                    modifier = Modifier.weight(1f),
                )
                HomeEntryCard(
                    title = "网络音乐",
                    subtitle = "$connectionCount 个连接",
                    icon = Icons.Default.Cloud,
                    onClick = onOpenNetwork,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = SketchSpacing.Page,
                        end = SketchSpacing.Page,
                        top = SketchSpacing.Lg,
                        bottom = SketchSpacing.Xs,
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SketchSectionTitle(
                    text = "最近播放",
                    modifier = Modifier.weight(1f),
                    style = SketchTextStyles.RowTitle,
                )
                Text(
                    text = "查看全部",
                    color = SketchDesign.colors.primary,
                    style = SketchTextStyles.Auxiliary,
                    modifier = Modifier.clickable(onClick = onOpenRecent),
                )
            }
        }
        if (recentPlays.isEmpty()) {
            item {
                Text(
                    text = "还没有最近播放记录",
                    color = SketchDesign.colors.muted,
                    style = SketchTextStyles.RowSubtitle,
                    modifier = Modifier.padding(
                        horizontal = SketchSpacing.Page,
                        vertical = SketchSpacing.Md,
                    ),
                )
            }
        } else {
            items(recentPlays.take(2), key = { "home-recent-${it.mediaId}" }) { item ->
                SketchTrackRow(
                    track = SketchTrackUiModel(
                        id = item.mediaId,
                        title = item.title,
                        artist = item.artist ?: "未知歌手",
                        album = item.album.orEmpty(),
                        duration = formatDuration(item.durationMillis),
                    ),
                    onPlay = { onPlayRecent(item) },
                    actions = listOf(
                        SketchMenuActionUiModel("立即播放") {
                            onPlayRecent(item)
                        },
                    ),
                )
            }
        }
    }
}

@Composable
private fun HomeEntryCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SketchGlassCard(
        modifier = modifier,
        contentPadding = PaddingValues(SketchSpacing.Md),
        onClick = onClick,
    ) {
        SketchIconAction(
            icon = icon,
            contentDescription = "",
        )
        Spacer(modifier = Modifier.padding(top = SketchSpacing.Sm))
        Text(
            text = title,
            color = SketchDesign.colors.ink,
            style = SketchTextStyles.RowTitle,
        )
        Text(
            text = subtitle,
            color = SketchDesign.colors.muted,
            style = SketchTextStyles.Auxiliary,
        )
    }
}

private enum class LibraryPane {
    HOME,
    LOCAL,
    NETWORK,
}

@Composable
private fun LocalMusicContent(
    state: LocalLibraryUiState,
    hasPermission: Boolean,
    viewMode: LibraryViewMode,
    onViewModeChange: (LibraryViewMode) -> Unit,
    onRequestPermission: () -> Unit,
    onOpenNetworkMusic: () -> Unit,
    onRefresh: () -> Unit,
    onSearch: (String) -> Unit,
    onPlay: (List<AudioTrack>, Int) -> Unit,
    onPlayNext: (AudioTrack) -> Unit,
    onAddToQueue: (AudioTrack) -> Unit,
) {
    when {
        !hasPermission -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                SketchEmptyState(
                    title = "访问手机中的音乐",
                    description = "允许后即可扫描本地音乐。也可以先连接 NAS。",
                    actionLabel = "允许访问音频",
                    onAction = onRequestPermission,
                )
                Text(
                    text = "暂不授权，先连接网络音乐",
                    color = SketchDesign.colors.primary,
                    style = SketchTextStyles.Auxiliary,
                    modifier = Modifier.padding(bottom = SketchSpacing.Xl),
                )
                SketchPill(
                    label = "连接网络音乐",
                    selected = true,
                    onClick = onOpenNetworkMusic,
                )
            }
        }

        state.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = SketchDesign.colors.primary)
            }
        }

        state.errorMessage != null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                SketchEmptyState(
                    title = "扫描本地音乐失败",
                    description = state.errorMessage,
                    actionLabel = "重新扫描",
                    onAction = onRefresh,
                )
            }
        }

        state.tracks.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                SketchEmptyState(
                    title = "没有找到本地音乐",
                    description = "请确认手机中已有音频文件。",
                    actionLabel = "重新扫描",
                    onAction = onRefresh,
                )
            }
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = SketchSpacing.Sm),
                verticalArrangement = Arrangement.spacedBy(SketchSpacing.Sm),
            ) {
                SketchSearchField(
                    value = state.searchQuery,
                    placeholder = "搜索本地音乐",
                    onValueChange = onSearch,
                    modifier = Modifier.padding(horizontal = SketchSpacing.Page),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = SketchSpacing.Page, vertical = SketchSpacing.Sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SketchSpacing.Sm),
                ) {
                    Text(
                        text = "共 ${state.tracks.size} 首",
                        color = SketchDesign.colors.muted,
                        style = SketchTextStyles.Auxiliary,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    LibraryViewMode.entries.forEach { mode ->
                        SketchPill(
                            label = mode.label,
                            selected = viewMode == mode,
                            onClick = { onViewModeChange(mode) },
                        )
                    }
                    SketchPill(label = "刷新", onClick = onRefresh)
                }
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = SketchSpacing.Md),
                ) {
                    when (viewMode) {
                        LibraryViewMode.SONGS -> items(
                            state.visibleTracks,
                            key = { it.id },
                        ) { track ->
                            SketchTrackRow(
                                track = SketchTrackUiModel(
                                    id = track.id.toString(),
                                    title = track.title,
                                    artist = track.artist ?: "未知歌手",
                                    album = track.album.orEmpty(),
                                    duration = formatDuration(track.durationMillis),
                                ),
                                onPlay = {
                                    val index = state.visibleTracks.indexOf(track).coerceAtLeast(0)
                                    onPlay(state.visibleTracks, index)
                                },
                                actions = listOf(
                                    SketchMenuActionUiModel("立即播放") {
                                        onPlay(listOf(track), 0)
                                    },
                                    SketchMenuActionUiModel("下一首播放") {
                                        onPlayNext(track)
                                    },
                                    SketchMenuActionUiModel("加入播放队列") {
                                        onAddToQueue(track)
                                    },
                                ),
                            )
                        }

                        LibraryViewMode.FOLDERS -> items(
                            state.visibleFolders,
                            key = { it.path },
                        ) { folder ->
                            SketchFolderRow(
                                folder = SketchFolderUiModel(
                                    id = folder.path,
                                    name = folder.name,
                                    songCount = folder.tracks.size,
                                ),
                                onClick = { onPlay(folder.tracks, 0) },
                            )
                        }

                        LibraryViewMode.ALBUMS -> items(
                            state.visibleTracks
                                .groupBy { it.album?.takeIf(String::isNotBlank) ?: "未知专辑" }
                                .toList(),
                            key = { it.first },
                        ) { (album, tracks) ->
                            SketchFolderRow(
                                folder = SketchFolderUiModel(
                                    id = "album-$album",
                                    name = album,
                                    songCount = tracks.size,
                                    description = "专辑",
                                ),
                                onClick = { onPlay(tracks, 0) },
                            )
                        }

                        LibraryViewMode.ARTISTS -> items(
                            state.visibleTracks
                                .groupBy { it.artist?.takeIf(String::isNotBlank) ?: "未知歌手" }
                                .toList(),
                            key = { it.first },
                        ) { (artist, tracks) ->
                            SketchFolderRow(
                                folder = SketchFolderUiModel(
                                    id = "artist-$artist",
                                    name = artist,
                                    songCount = tracks.size,
                                    description = "歌手",
                                ),
                                onClick = { onPlay(tracks, 0) },
                            )
                        }
                    }
                }
            }
        }
    }
}

private enum class LibraryViewMode(val label: String) {
    SONGS("歌曲"),
    FOLDERS("文件夹"),
    ALBUMS("专辑"),
    ARTISTS("歌手"),
}

@Composable
private fun NetworkMusicContent(
    connections: List<ConnectionEntity>,
    onAddConnection: () -> Unit,
    onOpenConnection: (ConnectionEntity) -> Unit,
    onDeleteConnection: (ConnectionEntity) -> Unit,
    onEditConnection: (ConnectionEntity) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = SketchSpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(SketchSpacing.Sm),
    ) {
        if (connections.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                SketchEmptyState(
                    title = "还没有连接 NAS",
                    description = "添加 SMB 或 WebDAV 网络音乐。",
                    actionLabel = "添加网络音乐",
                    onAction = onAddConnection,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = SketchSpacing.Sm,
                    bottom = SketchSpacing.Md,
                ),
            ) {
                item(key = "add-network") {
                    SketchFolderRow(
                        folder = SketchFolderUiModel(
                            id = "add-network",
                            name = "添加网络音乐",
                            songCount = 0,
                            description = "连接 SMB 或 WebDAV",
                        ),
                        onClick = onAddConnection,
                    )
                }
                items(connections, key = { it.id }) { connection ->
                    SketchConnectionRow(
                        connection = SketchConnectionUiModel(
                            id = connection.id,
                            name = connection.name,
                            protocol = connection.protocol.name,
                            status = if (connection.selectedShare.isNullOrBlank()) {
                                "已保存"
                            } else {
                                "共享文件夹：${connection.selectedShare}"
                            },
                        ),
                        onClick = { onOpenConnection(connection) },
                        actions = listOf(
                            SketchMenuActionUiModel("编辑连接") {
                                onEditConnection(connection)
                            },
                            SketchMenuActionUiModel("删除连接") {
                                onDeleteConnection(connection)
                            },
                        ),
                    )
                }
            }
        }
    }
}
