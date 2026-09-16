package com.example.audioplayer.feature.timer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audioplayer.core.model.TimerAction
import com.example.audioplayer.core.model.TimerNextRunCalculator
import com.example.audioplayer.core.model.TimerTask
import com.example.audioplayer.ui.sketch.SketchEmptyState
import com.example.audioplayer.ui.sketch.SketchIconAction
import com.example.audioplayer.ui.sketch.SketchSpacing
import com.example.audioplayer.ui.sketch.SketchTimerCard
import com.example.audioplayer.ui.sketch.SketchTimerUiModel
import com.example.audioplayer.ui.sketch.SketchTopBar
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 定时任务列表页。
 */
@Composable
fun TimerListScreen(
    onAddTimer: () -> Unit,
    onEditTimer: (TimerTask) -> Unit,
    viewModel: TimerListViewModel = hiltViewModel(),
) {
    val timers by viewModel.timers.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.Transparent,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            SketchTopBar(
                title = "定时任务",
                actions = {
                    SketchIconAction(
                        icon = Icons.Default.Add,
                        contentDescription = "添加定时",
                        onClick = onAddTimer,
                    )
                },
            )
            if (timers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    SketchEmptyState(
                        title = "还没有定时任务",
                        description = "可以设置定时开始播放或停止播放。",
                        actionLabel = "新建定时",
                        onAction = onAddTimer,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = SketchSpacing.Sm),
                ) {
                    items(timers, key = { it.id }) { task ->
                        SketchTimerCard(
                            timer = SketchTimerUiModel(
                                id = task.id.toString(),
                                name = task.name,
                                time = "%02d:%02d".format(task.hour, task.minute),
                                action = if (task.action == TimerAction.START) {
                                    com.example.audioplayer.ui.sketch.SketchTimerAction.START
                                } else {
                                    com.example.audioplayer.ui.sketch.SketchTimerAction.STOP
                                },
                                repeatText = buildString {
                                    append(repeatSummary(task))
                                    TimerNextRunCalculator.nextRun(task, LocalDateTime.now())?.let {
                                        append(" · 下次 ")
                                        append(it.format(NEXT_RUN_FORMAT))
                                    }
                                },
                                sourceText = if (task.action == TimerAction.START) {
                                    "${task.sourceType?.name.orEmpty()} · ${task.sourcePath.orEmpty()}"
                                } else {
                                    "当前播放队列"
                                },
                                enabled = task.enabled,
                            ),
                            onClick = { onEditTimer(task) },
                            onEnabledChange = { viewModel.toggle(task, it) },
                            onDelete = { viewModel.delete(task) },
                        )
                    }
                }
            }
        }
    }
}

private val NEXT_RUN_FORMAT = DateTimeFormatter.ofPattern("MM-dd HH:mm")

private fun repeatSummary(task: TimerTask): String {
    if (task.repeatDays.isEmpty()) return "仅一次"
    if (task.repeatDays.size == 7) return "每天"
    return task.repeatDays
        .sortedBy { it.value }
        .joinToString("、") { day ->
            when (day.value) {
                1 -> "周一"
                2 -> "周二"
                3 -> "周三"
                4 -> "周四"
                5 -> "周五"
                6 -> "周六"
                else -> "周日"
            }
        }
}
