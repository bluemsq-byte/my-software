package com.example.audioplayer.feature.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audioplayer.core.model.toAudioTrack
import com.example.audioplayer.ui.formatDuration
import com.example.audioplayer.ui.sketch.SketchEmptyState
import com.example.audioplayer.ui.sketch.SketchIconAction
import com.example.audioplayer.ui.sketch.SketchMenuActionUiModel
import com.example.audioplayer.ui.sketch.SketchSpacing
import com.example.audioplayer.ui.sketch.SketchTopBar
import com.example.audioplayer.ui.sketch.SketchTrackRow
import com.example.audioplayer.ui.sketch.SketchTrackUiModel

/**
 * 最近播放页。保留原分页逻辑，列表行替换为规范中的毛玻璃歌曲行。
 */
@Composable
fun RecentPlayScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val recent by viewModel.recentPlays.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.Transparent,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            SketchTopBar(
                title = "最近播放",
                actions = {
                    if (recent.isNotEmpty()) {
                        SketchIconAction(
                            icon = Icons.Default.DeleteSweep,
                            contentDescription = "清空最近播放",
                            onClick = viewModel::clearRecentPlays,
                        )
                    }
                },
            )
            if (recent.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    SketchEmptyState(
                        title = "还没有最近播放记录",
                        description = "播放过的音乐会显示在这里。",
                        actionLabel = "返回首页",
                        onAction = {},
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = SketchSpacing.Sm),
                ) {
                    items(recent, key = { it.mediaId }) { item ->
                        SketchTrackRow(
                            track = SketchTrackUiModel(
                                id = item.mediaId,
                                title = item.title,
                                artist = item.artist ?: "未知歌手",
                                album = item.album.orEmpty(),
                                duration = formatDuration(item.durationMillis),
                            ),
                            onPlay = { viewModel.playRecent(item) },
                            actions = listOf(
                                SketchMenuActionUiModel("立即播放") {
                                    viewModel.playRecent(item)
                                },
                                SketchMenuActionUiModel("下一首播放") {
                                    viewModel.playNext(item.toAudioTrack())
                                },
                                SketchMenuActionUiModel("加入播放队列") {
                                    viewModel.addToQueue(item.toAudioTrack())
                                },
                            ),
                        )
                    }
                }
            }
        }
    }
}
