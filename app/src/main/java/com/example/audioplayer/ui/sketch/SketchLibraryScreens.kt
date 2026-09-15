package com.example.audioplayer.ui.sketch

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 草图 01：首次启动与音频权限。
 *
 * 只负责空状态布局。申请权限、判断权限结果等业务逻辑由调用方在
 * `onAllowAccess` 和 `onConnectNetwork` 中接入。
 */
@Composable
fun Sketch01FirstLaunchScreen(
    onBack: () -> Unit = {},
    onAllowAccess: () -> Unit = {},
    onConnectNetwork: () -> Unit = {},
) {
    SketchBaseScreen {
        Column(modifier = Modifier.fillMaxSize()) {
            SketchTopBar(
                title = "音频播放器",
                onBack = onBack,
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = SketchSpacing.Page),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                SketchEmptyState(
                    title = "访问手机中的音乐",
                    description = "允许后即可扫描本地音乐。也可以先连接 NAS。",
                    actionLabel = "允许访问音频",
                    onAction = onAllowAccess,
                )
                Text(
                    text = "暂不授权，先连接网络音乐",
                    color = SketchDesign.colors.muted,
                    style = SketchTextStyles.Auxiliary,
                    modifier = Modifier
                        .clickable(onClick = onConnectNetwork)
                        .padding(top = SketchSpacing.Md),
                )
            }
        }
    }
}

/**
 * 草图 02：首页 · 本地音乐。
 *
 * 页面只消费 UI 模型；文件夹扫描、播放全部和点击歌曲的副作用由外层传入。
 */
@Composable
fun Sketch02LocalLibraryScreen(
    query: String = "",
    folders: List<SketchFolderUiModel> = emptyList(),
    songCount: Int = 0,
    selectedViewMode: Int = 1,
    miniPlayer: SketchMiniPlayerUiModel? = null,
    onQueryChange: (String) -> Unit = {},
    onAdd: () -> Unit = {},
    onTabSelected: (Int) -> Unit = {},
    onViewModeSelected: (Int) -> Unit = {},
    onRefresh: () -> Unit = {},
    onFolderClick: (SketchFolderUiModel) -> Unit = {},
    onMiniPlayPause: () -> Unit = {},
    onMiniNext: () -> Unit = {},
    onNavSelected: (Int) -> Unit = {},
) {
    SketchBaseScreen {
        SketchMainScaffold(
            selectedNavIndex = 0,
            onNavSelected = onNavSelected,
            miniPlayer = miniPlayer,
            onMiniPlayPause = onMiniPlayPause,
            onMiniNext = onMiniNext,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                SketchTopBar(
                    title = "音乐",
                    actions = {
                        SketchIconAction(
                            icon = Icons.Default.Add,
                            contentDescription = "添加网络音乐",
                            onClick = onAdd,
                        )
                    },
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = SketchSpacing.Md),
                    verticalArrangement = Arrangement.spacedBy(SketchSpacing.Sm),
                ) {
                    SketchSearchField(
                        value = query,
                        placeholder = "搜索本地音乐",
                        onValueChange = onQueryChange,
                        modifier = Modifier.padding(horizontal = SketchSpacing.Page),
                    )
                    SketchSegmentedTabs(
                        titles = listOf("本地音乐", "网络音乐"),
                        selectedIndex = 0,
                        onSelected = onTabSelected,
                        modifier = Modifier.padding(horizontal = SketchSpacing.Page),
                    )
                    SketchToolbar {
                        Text(
                            text = "共 $songCount 首",
                            color = SketchDesign.colors.muted,
                            style = SketchTextStyles.Auxiliary,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        SketchPill(
                            label = "歌曲",
                            selected = selectedViewMode == 0,
                            onClick = { onViewModeSelected(0) },
                        )
                        SketchPill(
                            label = "文件夹",
                            selected = selectedViewMode == 1,
                            onClick = { onViewModeSelected(1) },
                        )
                        SketchPill(
                            label = "刷新",
                            onClick = onRefresh,
                        )
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            bottom = SketchSpacing.Md,
                        ),
                    ) {
                        items(folders, key = { it.id }) { folder ->
                            SketchFolderRow(
                                folder = folder,
                                onClick = { onFolderClick(folder) },
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 草图 03：首页 · 网络音乐文件夹。
 */
@Composable
fun Sketch03NetworkLibraryScreen(
    query: String = "",
    connections: List<SketchConnectionUiModel> = emptyList(),
    onQueryChange: (String) -> Unit = {},
    onAdd: () -> Unit = {},
    onTabSelected: (Int) -> Unit = {},
    onConnectionClick: (SketchConnectionUiModel) -> Unit = {},
    onConnectionMore: (SketchConnectionUiModel) -> Unit = {},
    onNavSelected: (Int) -> Unit = {},
) {
    SketchBaseScreen {
        SketchMainScaffold(
            selectedNavIndex = 0,
            onNavSelected = onNavSelected,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                SketchTopBar(
                    title = "音乐",
                    actions = {
                        SketchIconAction(
                            icon = Icons.Default.Add,
                            contentDescription = "添加网络音乐",
                            onClick = onAdd,
                        )
                    },
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = SketchSpacing.Md),
                    verticalArrangement = Arrangement.spacedBy(SketchSpacing.Sm),
                ) {
                    SketchSearchField(
                        value = query,
                        placeholder = "搜索网络音乐",
                        onValueChange = onQueryChange,
                        modifier = Modifier.padding(horizontal = SketchSpacing.Page),
                    )
                    SketchSegmentedTabs(
                        titles = listOf("本地音乐", "网络音乐"),
                        selectedIndex = 1,
                        onSelected = onTabSelected,
                        modifier = Modifier.padding(horizontal = SketchSpacing.Page),
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            top = SketchSpacing.Sm,
                            bottom = SketchSpacing.Md,
                        ),
                    ) {
                        items(connections, key = { it.id }) { connection ->
                            SketchConnectionRow(
                                connection = connection,
                                onClick = { onConnectionClick(connection) },
                                onMore = { onConnectionMore(connection) },
                            )
                        }
                    }
                }
            }
        }
    }
}
