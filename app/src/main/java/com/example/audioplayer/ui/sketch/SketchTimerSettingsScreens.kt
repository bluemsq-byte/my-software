package com.example.audioplayer.ui.sketch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 草图 14：定时任务列表。
 */
@Composable
fun Sketch14TimerListScreen(
    timers: List<SketchTimerUiModel> = emptyList(),
    onAdd: () -> Unit = {},
    onTimerClick: (SketchTimerUiModel) -> Unit = {},
    onTimerEnabledChange: (SketchTimerUiModel, Boolean) -> Unit = { _, _ -> },
    onNavSelected: (Int) -> Unit = {},
) {
    SketchBaseScreen {
        SketchMainScaffold(
            selectedNavIndex = 2,
            onNavSelected = onNavSelected,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                SketchTopBar(
                    title = "定时任务",
                    actions = {
                        SketchIconAction(
                            icon = Icons.Default.Add,
                            contentDescription = "新建定时",
                            onClick = onAdd,
                        )
                    },
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = SketchSpacing.Sm),
                ) {
                    items(timers, key = { it.id }) { timer ->
                        SketchTimerCard(
                            timer = timer,
                            onClick = { onTimerClick(timer) },
                            onEnabledChange = { enabled ->
                                onTimerEnabledChange(timer, enabled)
                            },
                        )
                    }
                }
            }
        }
    }
}

/**
 * 草图 15：新建定时与文件选择。
 */
@Composable
fun Sketch15TimerEditorScreen(
    name: String = "",
    actionIndex: Int = 0,
    time: String = "07:30",
    selectedDays: Set<Int> = emptySet(),
    source: String = "",
    selectedFileOrFolder: String = "",
    onBack: () -> Unit = {},
    onSave: () -> Unit = {},
    onActionSelected: (Int) -> Unit = {},
    onDaySelected: (Int) -> Unit = {},
    onChooseFileOrFolder: () -> Unit = {},
) {
    SketchBaseScreen {
        Column(modifier = Modifier.fillMaxSize()) {
            SketchTopBar(
                title = "新建定时",
                onBack = onBack,
                actions = {
                    SketchIconAction(
                        icon = Icons.Default.Check,
                        contentDescription = "保存定时",
                        onClick = onSave,
                    )
                },
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(SketchSpacing.Page),
                verticalArrangement = Arrangement.spacedBy(SketchSpacing.Md),
            ) {
                item {
                    SketchField(label = "定时名称", value = name)
                }
                item {
                    SketchSegmentedTabs(
                        titles = listOf("开始播放", "停止播放"),
                        selectedIndex = actionIndex,
                        onSelected = onActionSelected,
                    )
                }
                item {
                    SketchField(label = "时间", value = time)
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(SketchSpacing.Sm)) {
                        SketchDayRow(
                            days = listOf("一", "二", "三", "四", "五"),
                            dayIndexes = listOf(0, 1, 2, 3, 4),
                            selectedDays = selectedDays,
                            onDaySelected = onDaySelected,
                        )
                        SketchDayRow(
                            days = listOf("六", "日"),
                            dayIndexes = listOf(5, 6),
                            selectedDays = selectedDays,
                            onDaySelected = onDaySelected,
                        )
                    }
                }
                item {
                    SketchField(label = "音乐来源", value = source)
                }
                item {
                    SketchField(
                        label = "选择文件或文件夹",
                        value = selectedFileOrFolder,
                        trailingArrow = true,
                        onClick = onChooseFileOrFolder,
                    )
                }
                item {
                    SketchPrimaryButton(
                        label = "保存定时",
                        onClick = onSave,
                    )
                }
            }
        }
    }
}

/**
 * 草图 16：设置与外观。
 */
@Composable
fun Sketch16SettingsScreen(
    backgroundPlayback: Boolean = true,
    automaticDarkMode: Boolean = true,
    manualThemeIndex: Int = 0,
    themes: List<SketchThemeUiModel> = emptyList(),
    selectedThemeIndex: Int = 0,
    cacheText: String = "",
    permissionsText: String = "",
    onBackgroundPlaybackChange: (Boolean) -> Unit = {},
    onAutomaticDarkModeChange: (Boolean) -> Unit = {},
    onManualThemeSelected: (Int) -> Unit = {},
    onThemeSelected: (Int) -> Unit = {},
    onClearCache: () -> Unit = {},
    onPermissionsClick: () -> Unit = {},
    onNavSelected: (Int) -> Unit = {},
) {
    SketchBaseScreen {
        SketchMainScaffold(
            selectedNavIndex = 3,
            onNavSelected = onNavSelected,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                SketchTopBar(title = "设置")
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = SketchSpacing.Sm),
                    verticalArrangement = Arrangement.spacedBy(SketchSpacing.Sm),
                ) {
                    item {
                        SketchSwitchRow(
                            title = "后台播放",
                            subtitle = "退出页面后继续播放",
                            checked = backgroundPlayback,
                            onCheckedChange = onBackgroundPlaybackChange,
                        )
                    }
                    item {
                        SketchSwitchRow(
                            title = "自动深色模式",
                            subtitle = "跟随手机系统",
                            checked = automaticDarkMode,
                            onCheckedChange = onAutomaticDarkModeChange,
                        )
                    }
                    item {
                        SketchToolbar {
                            SketchPill(
                                label = "浅色",
                                selected = manualThemeIndex == 0,
                                onClick = { onManualThemeSelected(0) },
                            )
                            SketchPill(
                                label = "深色",
                                selected = manualThemeIndex == 1,
                                onClick = { onManualThemeSelected(1) },
                            )
                        }
                    }
                    item {
                        SketchThemeSwatchGrid(
                            themes = themes,
                            selectedIndex = selectedThemeIndex,
                            onSelected = onThemeSelected,
                        )
                    }
                    item {
                        SketchSettingRow(
                            title = "缓存占用",
                            subtitle = "总计 $cacheText",
                            trailingText = "清理",
                            onClick = onClearCache,
                        )
                    }
                    item {
                        SketchSettingRow(
                            title = "权限",
                            subtitle = permissionsText,
                            onClick = onPermissionsClick,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 定时卡片公共组件。
 */
@Composable
fun SketchTimerCard(
    timer: SketchTimerUiModel,
    onClick: () -> Unit = {},
    onEnabledChange: (Boolean) -> Unit = {},
) {
    SketchGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SketchSpacing.Page, vertical = SketchSpacing.Xs),
        contentPadding = PaddingValues(SketchSpacing.Md),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SketchSpacing.Md),
        ) {
            Text(
                text = timer.time,
                color = SketchDesign.colors.ink,
                style = SketchTextStyles.SectionTitle,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = timer.name,
                    color = SketchDesign.colors.ink,
                    style = SketchTextStyles.RowTitle,
                    maxLines = 1,
                )
                Text(
                    text = if (timer.action == SketchTimerAction.START) {
                        "开始播放 · ${timer.repeatText}"
                    } else {
                        "停止播放 · ${timer.repeatText}"
                    },
                    color = SketchDesign.colors.muted,
                    style = SketchTextStyles.Auxiliary,
                    maxLines = 2,
                )
                if (timer.sourceText.isNotBlank()) {
                    Text(
                        text = timer.sourceText,
                        color = SketchDesign.colors.muted,
                        style = SketchTextStyles.Auxiliary,
                        maxLines = 1,
                    )
                }
            }
            Switch(
                checked = timer.enabled,
                onCheckedChange = onEnabledChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = SketchDesign.colors.onDark,
                    checkedTrackColor = SketchDesign.colors.primary,
                    uncheckedThumbColor = SketchDesign.colors.muted,
                    uncheckedTrackColor = SketchDesign.colors.glass,
                ),
            )
        }
    }
}

@Composable
private fun SketchDayRow(
    days: List<String>,
    dayIndexes: List<Int>,
    selectedDays: Set<Int>,
    onDaySelected: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SketchSpacing.Sm),
    ) {
        days.forEachIndexed { position, label ->
            val dayIndex = dayIndexes[position]
            SketchPill(
                label = label,
                selected = dayIndex in selectedDays,
                onClick = { onDaySelected(dayIndex) },
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}
