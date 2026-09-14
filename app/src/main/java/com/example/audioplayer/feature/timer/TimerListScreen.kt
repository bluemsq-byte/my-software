package com.example.audioplayer.feature.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audioplayer.core.model.TimerAction
import com.example.audioplayer.core.model.TimerTask

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerListScreen(
    onAddTimer: () -> Unit,
    onEditTimer: (TimerTask) -> Unit,
    viewModel: TimerListViewModel = hiltViewModel(),
) {
    val timers by viewModel.timers.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("定时任务") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTimer) {
                Icon(Icons.Default.Add, contentDescription = "添加定时")
            }
        },
    ) { padding ->
        if (timers.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("还没有定时任务")
                Spacer(Modifier.height(12.dp))
                Button(onClick = onAddTimer) { Text("新建定时") }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(timers, key = { it.id }) { task ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(task.name, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "%02d:%02d · %s".format(
                                            task.hour,
                                            task.minute,
                                            if (task.action == TimerAction.START) "开始播放" else "停止播放",
                                        ),
                                    )
                                    Text(
                                        repeatSummary(task),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    if (task.action == TimerAction.START) {
                                        Text(
                                            "${task.sourceType?.name.orEmpty()} · ${task.sourcePath.orEmpty()}",
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
                                Switch(
                                    checked = task.enabled,
                                    onCheckedChange = { viewModel.toggle(task, it) },
                                )
                                IconButton(onClick = { viewModel.delete(task) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "删除")
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { onEditTimer(task) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text("编辑")
                            }
                        }
                    }
                }
            }
        }
    }
}

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