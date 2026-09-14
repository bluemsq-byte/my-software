package com.example.audioplayer.feature.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.audioplayer.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentPlayScreen(
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val recent by viewModel.recentPlays.collectAsStateWithLifecycle()

    Scaffold(
            containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("最近播放") },
                actions = {
                    if (recent.isNotEmpty()) {
                        IconButton(onClick = viewModel::clearRecentPlays) {
                            Icon(Icons.Default.Delete, contentDescription = "清空最近播放")
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (recent.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("还没有最近播放记录")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(recent, key = { it.mediaId }) { item ->
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        ListItem(
                            headlineContent = { Text(item.title) },
                            supportingContent = { Text(item.artist ?: "未知歌手") },
                            leadingContent = { Icon(Icons.Default.MusicNote, contentDescription = null) },
                            modifier = Modifier.clickable { viewModel.playRecent(item) },
                        )
                    }
                }
            }
        }
    }
}
