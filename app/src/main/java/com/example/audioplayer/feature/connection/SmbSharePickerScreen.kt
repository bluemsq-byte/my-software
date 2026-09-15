package com.example.audioplayer.feature.connection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audioplayer.ui.sketch.SketchBaseScreen
import com.example.audioplayer.ui.sketch.SketchDesign
import com.example.audioplayer.ui.sketch.SketchEmptyState
import com.example.audioplayer.ui.sketch.SketchFolderRow
import com.example.audioplayer.ui.sketch.SketchFolderUiModel
import com.example.audioplayer.ui.sketch.SketchIconAction
import com.example.audioplayer.ui.sketch.SketchSpacing
import com.example.audioplayer.ui.sketch.SketchTopBar

/**
 * SMB 共享文件夹选择页。
 */
@Composable
fun SmbSharePickerScreen(
    onBack: () -> Unit,
    onSelected: () -> Unit,
    viewModel: SmbSharePickerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    SketchBaseScreen {
        Scaffold(
            containerColor = Color.Transparent,
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                SketchTopBar(
                    title = "选择共享文件夹",
                    onBack = onBack,
                    actions = {
                        SketchIconAction(
                            icon = Icons.Default.Refresh,
                            contentDescription = "刷新共享文件夹",
                            onClick = viewModel::load,
                        )
                    },
                )
                when {
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
                                title = "无法读取共享文件夹",
                                description = state.errorMessage.orEmpty(),
                                actionLabel = "重试",
                                onAction = viewModel::load,
                            )
                        }
                    }

                    state.shares.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            SketchEmptyState(
                                title = "没有找到共享文件夹",
                                description = "请检查账号权限后重新读取。",
                                actionLabel = "重新读取",
                                onAction = viewModel::load,
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = SketchSpacing.Sm),
                        ) {
                            items(state.shares, key = { it }) { share ->
                                SketchFolderRow(
                                    folder = SketchFolderUiModel(
                                        id = share,
                                        name = share,
                                        songCount = 0,
                                        description = if (share == state.selectedShare) {
                                            "当前已选择"
                                        } else {
                                            "NAS 共享文件夹"
                                        },
                                    ),
                                    onClick = { viewModel.select(share, onSelected) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
