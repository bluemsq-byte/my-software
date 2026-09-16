package com.example.audioplayer.ui.sketch

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 页面背景和主题包装。所有草图页面都从这里进入，确保背景层级一致。
 */
@Composable
fun SketchBaseScreen(
    darkTheme: Boolean = false,
    colorTheme: SketchColorTheme = SketchColorTheme.TEAL,
    useMaterialTheme: Boolean = false,
    forceTheme: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    SketchTheme(
        darkTheme = darkTheme,
        colorTheme = colorTheme,
        useMaterialTheme = useMaterialTheme,
        forceTheme = forceTheme,
    ) {
        val colors = SketchDesign.colors
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            colors.pageBackground,
                            colors.primary.copy(alpha = SketchOpacity.Selected),
                            colors.pageBackground,
                        ),
                    ),
                ),
            content = content,
        )
    }
}

/**
 * 草图底部导航和迷你播放器的统一容器。
 */
@Composable
fun SketchMainScaffold(
    selectedNavIndex: Int,
    onNavSelected: (Int) -> Unit = {},
    miniPlayer: SketchMiniPlayerUiModel? = null,
    onMiniPlayPause: () -> Unit = {},
    onMiniNext: () -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f), content = content)
        miniPlayer?.let {
            SketchMiniPlayer(
                state = it,
                onPlayPause = onMiniPlayPause,
                onNext = onMiniNext,
            )
        }
        SketchBottomNavigation(
            selectedIndex = selectedNavIndex,
            onSelected = onNavSelected,
        )
    }
}

/**
 * 透明顶栏，结构与草图中的 appbar 一致。
 */
@Composable
fun SketchTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    navigationIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    navigationContentDescription: String = "返回",
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = SketchDesign.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(SketchSizes.TopBarHeight)
                .padding(horizontal = SketchSpacing.Page),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SketchSpacing.Sm),
        ) {
            if (onBack != null) {
                SketchIconAction(
                    icon = navigationIcon,
                    contentDescription = navigationContentDescription,
                    onClick = onBack,
                )
            }
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                color = colors.ink,
                style = SketchTextStyles.SectionTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            actions()
        }
        HorizontalDivider(
            thickness = SketchStroke.Border,
            color = colors.border,
        )
    }
}

/**
 * 圆形图标按钮统一保持 48dp 触摸区域。
 */
@Composable
fun SketchIconAction(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit = {},
    selected: Boolean = false,
) {
    val colors = SketchDesign.colors
    Box(
        modifier = Modifier
            .size(SketchSizes.TouchTarget)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(SketchSizes.IconAction)
                .clip(CircleShape)
                .background(if (selected) colors.primary else colors.glass)
                .border(BorderStroke(SketchStroke.Border, colors.border), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(SketchSizes.Icon),
                tint = if (selected) colors.onDark else colors.ink,
            )
        }
    }
}

/**
 * 毛玻璃卡片组件。卡片内部不再嵌套第二个毛玻璃卡片。
 */
@Composable
fun SketchGlassCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(SketchSpacing.Md),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = SketchDesign.colors
    val shape = RoundedCornerShape(SketchRadius.Card)
    val clickableModifier = if (onClick == null) {
        Modifier
    } else {
        Modifier.clickable(onClick = onClick)
    }
    Column(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        colors.glassStrong,
                        colors.glass,
                    ),
                ),
            )
            .border(BorderStroke(SketchStroke.Border, colors.border), shape)
            .then(clickableModifier)
            .padding(contentPadding),
        content = content,
    )
}

@Composable
fun SketchSearchField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = SketchDesign.colors
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = SketchTextStyles.RowSubtitle.copy(color = colors.ink),
        cursorBrush = Brush.verticalGradient(listOf(colors.primary, colors.primary)),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = SketchSizes.FieldMinHeight)
                    .clip(RoundedCornerShape(SketchRadius.Control))
                    .background(colors.glass)
                    .border(
                        BorderStroke(SketchStroke.Border, colors.border),
                        RoundedCornerShape(SketchRadius.Control),
                    )
                    .padding(horizontal = SketchSpacing.Md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SketchSpacing.Sm),
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(SketchSizes.SettingsIcon),
                    tint = colors.muted,
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = colors.muted,
                            style = SketchTextStyles.RowSubtitle,
                        )
                    }
                    innerTextField()
                }
            }
        },
    )
}

/**
 * 可编辑输入框，承接连接编辑和定时编辑等真实表单。
 */
@Composable
fun SketchEditableField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    password: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val colors = SketchDesign.colors
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(SketchSpacing.Xs),
    ) {
        Text(
            text = label,
            color = colors.muted,
            style = SketchTextStyles.Auxiliary,
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            textStyle = SketchTextStyles.RowSubtitle.copy(color = colors.ink),
            cursorBrush = Brush.verticalGradient(listOf(colors.primary, colors.primary)),
            keyboardOptions = keyboardOptions,
            visualTransformation = if (password) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = SketchSizes.FieldMinHeight)
                        .clip(RoundedCornerShape(SketchRadius.Control))
                        .background(colors.glass)
                        .border(
                            BorderStroke(SketchStroke.Border, colors.border),
                            RoundedCornerShape(SketchRadius.Control),
                        )
                        .padding(horizontal = SketchSpacing.Md, vertical = SketchSpacing.Md),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            color = colors.muted.copy(alpha = SketchOpacity.Muted),
                            style = SketchTextStyles.RowSubtitle,
                        )
                    }
                    innerTextField()
                }
            },
        )
    }
}

@Composable
fun SketchSegmentedTabs(
    titles: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = SketchDesign.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SketchSpacing.Sm),
    ) {
        titles.forEachIndexed { index, title ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = SketchSizes.TouchTarget)
                    .clip(RoundedCornerShape(SketchRadius.Control))
                    .background(if (selected) colors.primary else colors.glass)
                    .border(
                        BorderStroke(SketchStroke.Border, if (selected) Color.Transparent else colors.border),
                        RoundedCornerShape(SketchRadius.Control),
                    )
                    .clickable { onSelected(index) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = title,
                    color = if (selected) colors.onDark else colors.muted,
                    style = SketchTextStyles.Button,
                )
            }
        }
    }
}

@Composable
fun SketchPill(
    label: String,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    val colors = SketchDesign.colors
    Box(
        modifier = Modifier
            .heightIn(min = SketchSpacing.Xxl)
            .clip(RoundedCornerShape(SketchRadius.Pill))
            .background(if (selected) colors.primary else colors.glass)
            .border(
                BorderStroke(SketchStroke.Border, if (selected) Color.Transparent else colors.border),
                RoundedCornerShape(SketchRadius.Pill),
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = SketchSpacing.Md),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = when {
                !enabled -> colors.muted.copy(alpha = SketchOpacity.Disabled)
                selected -> colors.onDark
                else -> colors.muted
            },
            style = SketchTextStyles.Auxiliary,
        )
    }
}

@Composable
fun SketchToolbar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SketchSpacing.Page, vertical = SketchSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SketchSpacing.Sm),
        content = content,
    )
}

@Composable
fun SketchPrimaryButton(
    label: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = SketchDesign.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = SketchSizes.TouchTarget)
            .clip(RoundedCornerShape(SketchRadius.Control))
            .background(colors.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = colors.onDark, style = SketchTextStyles.Button)
    }
}

@Composable
fun SketchSecondaryButton(
    label: String,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = SketchDesign.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = SketchSizes.TouchTarget)
            .clip(RoundedCornerShape(SketchRadius.Control))
            .background(colors.glass)
            .border(
                BorderStroke(SketchStroke.Border, colors.border),
                RoundedCornerShape(SketchRadius.Control),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = colors.ink, style = SketchTextStyles.Button)
    }
}

@Composable
fun SketchField(
    label: String,
    value: String,
    onClick: () -> Unit = {},
    trailingArrow: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val colors = SketchDesign.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = SketchSizes.FieldMinHeight)
            .clip(RoundedCornerShape(SketchRadius.Control))
            .background(colors.glass)
            .border(
                BorderStroke(SketchStroke.Border, colors.border),
                RoundedCornerShape(SketchRadius.Control),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = SketchSpacing.Md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SketchSpacing.Md),
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = colors.muted,
            style = SketchTextStyles.RowSubtitle,
        )
        Text(
            text = value,
            color = colors.ink,
            style = SketchTextStyles.RowSubtitle,
        )
        if (trailingArrow) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.muted,
            )
        }
    }
}

@Composable
fun SketchFolderRow(
    folder: SketchFolderUiModel,
    onClick: () -> Unit = {},
) {
    SketchRowCard(
        title = folder.name,
        subtitle = folder.description ?: "${folder.songCount} 首歌曲",
        leading = {
            SketchIconTile(
                icon = Icons.Default.Folder,
                contentDescription = null,
            )
        },
        trailing = {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "进入文件夹",
                tint = SketchDesign.colors.muted,
            )
        },
        onClick = onClick,
    )
}

@Composable
fun SketchTrackRow(
    track: SketchTrackUiModel,
    onPlay: () -> Unit = {},
    onMore: (() -> Unit)? = null,
    actions: List<SketchMenuActionUiModel> = emptyList(),
    showDuration: Boolean = true,
    showSelection: Boolean = false,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    SketchRowCard(
        title = track.title,
        subtitle = listOf(track.artist, track.album)
            .filter { it.isNotBlank() }
            .joinToString(" · "),
        leading = {
            if (showSelection) {
                SketchSelectionBox(selected = track.selected)
            } else {
                SketchIconTile(
                    icon = Icons.Default.MusicNote,
                    contentDescription = null,
                )
            }
        },
        trailing = {
            if (showDuration) {
                Text(
                    text = track.duration,
                    color = SketchDesign.colors.muted,
                    style = SketchTextStyles.Auxiliary,
                )
            }
            if (showSelection) {
                SketchSelectionBox(selected = track.selected)
            } else {
                Box {
                    SketchMoreButton(
                        contentDescription = "更多操作",
                        onClick = {
                            if (actions.isEmpty()) {
                                onMore?.invoke()
                            } else {
                                menuExpanded = true
                            }
                        },
                    )
                    if (actions.isNotEmpty()) {
                        SketchActionDropdown(
                            expanded = menuExpanded,
                            actions = actions,
                            onDismiss = { menuExpanded = false },
                        )
                    }
                }
            }
        },
        onClick = onPlay,
        selected = track.playing,
    )
}

@Composable
fun SketchConnectionRow(
    connection: SketchConnectionUiModel,
    onClick: () -> Unit = {},
    onMore: (() -> Unit)? = null,
    actions: List<SketchMenuActionUiModel> = emptyList(),
) {
    var menuExpanded by remember { mutableStateOf(false) }
    SketchRowCard(
        title = connection.name,
        subtitle = "${connection.protocol} · ${connection.status}",
        leading = {
            SketchIconTile(
                icon = if (connection.protocol.equals("WebDAV", ignoreCase = true)) {
                    Icons.Default.Cloud
                } else {
                    Icons.Default.Storage
                },
                contentDescription = null,
            )
        },
        trailing = {
            if (connection.selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "已选择",
                    tint = SketchDesign.colors.primary,
                )
            } else {
                Box {
                    SketchMoreButton(
                        contentDescription = "连接更多操作",
                        onClick = {
                            if (actions.isEmpty()) {
                                onMore?.invoke()
                            } else {
                                menuExpanded = true
                            }
                        },
                    )
                    if (actions.isNotEmpty()) {
                        SketchActionDropdown(
                            expanded = menuExpanded,
                            actions = actions,
                            onDismiss = { menuExpanded = false },
                        )
                    }
                }
            }
        },
        onClick = onClick,
    )
}

@Composable
fun SketchPlaylistRow(
    playlist: SketchPlaylistUiModel,
    onClick: () -> Unit = {},
    actions: List<SketchMenuActionUiModel> = emptyList(),
) {
    var menuExpanded by remember { mutableStateOf(false) }
    SketchRowCard(
        title = playlist.name,
        subtitle = "${playlist.songCount} 首歌曲 · ${playlist.source}",
        leading = {
            SketchIconTile(
                icon = Icons.AutoMirrored.Filled.QueueMusic,
                contentDescription = null,
            )
        },
        trailing = {
            if (actions.isEmpty()) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "打开播放列表",
                    tint = SketchDesign.colors.muted,
                )
            } else {
                Box {
                    SketchMoreButton(
                        contentDescription = "播放列表更多操作",
                        onClick = { menuExpanded = true },
                    )
                    SketchActionDropdown(
                        expanded = menuExpanded,
                        actions = actions,
                        onDismiss = { menuExpanded = false },
                    )
                }
            }
        },
        onClick = onClick,
    )
}

@Composable
fun SketchDeviceRow(
    device: SketchDeviceUiModel,
    onClick: () -> Unit = {},
) {
    SketchRowCard(
        title = device.name,
        subtitle = device.description,
        leading = {
            SketchIconTile(
                icon = when (device.kind) {
                    SketchDeviceKind.PHONE -> Icons.Default.PhoneAndroid
                    SketchDeviceKind.BLUETOOTH -> Icons.Default.Bluetooth
                    SketchDeviceKind.CAST -> Icons.Default.Tv
                    SketchDeviceKind.MEDIA_ROUTE -> Icons.Default.Cast
                },
                contentDescription = null,
            )
        },
        trailing = {
            if (device.selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "当前设备",
                    tint = SketchDesign.colors.primary,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "切换设备",
                    tint = SketchDesign.colors.muted,
                )
            }
        },
        onClick = onClick,
    )
}

@Composable
fun SketchSettingRow(
    title: String,
    subtitle: String,
    trailingText: String? = null,
    onClick: () -> Unit = {},
) {
    SketchRowCard(
        title = title,
        subtitle = subtitle,
        leading = null,
        trailing = {
            if (trailingText != null) {
                Text(
                    text = trailingText,
                    color = SketchDesign.colors.muted,
                    style = SketchTextStyles.Auxiliary,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = SketchDesign.colors.muted,
                )
            }
        },
        onClick = onClick,
    )
}

@Composable
fun SketchContinuePlayingCard(
    title: String,
    artist: String,
    isPlaying: Boolean,
    onClick: () -> Unit = {},
) {
    val colors = SketchDesign.colors
    SketchGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SketchSpacing.Page, vertical = SketchSpacing.Sm),
        contentPadding = PaddingValues(SketchSpacing.Md),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SketchSpacing.Md),
        ) {
            SketchArtworkPlaceholder(modifier = Modifier.size(SketchSizes.HeroArtwork))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isPlaying) "正在播放" else "继续播放",
                    color = colors.primary,
                    style = SketchTextStyles.Auxiliary,
                )
                Text(
                    text = title,
                    color = colors.ink,
                    style = SketchTextStyles.RowTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = artist,
                    color = colors.muted,
                    style = SketchTextStyles.RowSubtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            SketchIconAction(
                icon = Icons.Default.PlayArrow,
                contentDescription = "继续播放",
                onClick = onClick,
            )
        }
    }
}

@Composable
fun SketchSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit = {},
) {
    SketchRowCard(
        title = title,
        subtitle = subtitle,
        leading = null,
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = SketchDesign.colors.onDark,
                    checkedTrackColor = SketchDesign.colors.primary,
                    uncheckedThumbColor = SketchDesign.colors.muted,
                    uncheckedTrackColor = SketchDesign.colors.glass,
                ),
            )
        },
        onClick = {},
    )
}

@Composable
private fun SketchRowCard(
    title: String,
    subtitle: String,
    leading: (@Composable () -> Unit)?,
    trailing: @Composable RowScope.() -> Unit,
    onClick: () -> Unit,
    selected: Boolean = false,
) {
    val colors = SketchDesign.colors
    SketchGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SketchSpacing.Page, vertical = SketchSpacing.Xs),
        contentPadding = PaddingValues(SketchSpacing.Md),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = SketchSizes.RowMinHeight),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SketchSpacing.Md),
        ) {
            leading?.invoke()
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (selected) colors.primary else colors.ink,
                    style = SketchTextStyles.RowTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    color = colors.muted,
                    style = SketchTextStyles.RowSubtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            trailing()
        }
    }
}

@Composable
private fun SketchIconTile(
    icon: ImageVector,
    contentDescription: String?,
) {
    val colors = SketchDesign.colors
    Box(
        modifier = Modifier
            .size(SketchSizes.IconTile)
            .clip(RoundedCornerShape(SketchRadius.Control))
            .background(
                Brush.linearGradient(
                    listOf(colors.primary, colors.primaryDark),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(SketchSizes.Icon),
            tint = colors.onDark,
        )
    }
}

@Composable
fun SketchSelectionBox(selected: Boolean) {
    val colors = SketchDesign.colors
    Box(
        modifier = Modifier.size(SketchSizes.TouchTarget),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (selected) {
                Icons.Default.CheckCircle
            } else {
                Icons.Default.RadioButtonUnchecked
            },
            contentDescription = if (selected) "已选择" else "未选择",
            modifier = Modifier.size(SketchSizes.Icon),
            tint = if (selected) colors.primary else colors.muted,
        )
    }
}

@Composable
private fun SketchMoreButton(
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(SketchSizes.TouchTarget)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = contentDescription,
            modifier = Modifier.size(SketchSizes.Icon),
            tint = SketchDesign.colors.muted,
        )
    }
}

@Composable
fun SketchBottomNavigation(
    selectedIndex: Int,
    onSelected: (Int) -> Unit = {},
) {
    val colors = SketchDesign.colors
    val items = listOf(
        SketchNavItemUiModel("首页") to Icons.Default.Home,
        SketchNavItemUiModel("播放列表") to Icons.AutoMirrored.Filled.QueueMusic,
        SketchNavItemUiModel("定时") to Icons.Default.Timer,
        SketchNavItemUiModel("设置") to Icons.Default.Settings,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
                .height(SketchSizes.BottomNavigation)
                .background(colors.navigation)
            .border(BorderStroke(SketchStroke.Border, colors.border)),
    ) {
        items.forEachIndexed { index, (item, icon) ->
            val selected = index == selectedIndex
            Column(
                modifier = Modifier
                    .weight(1f)
                    .sizeIn(minWidth = SketchSizes.TouchTarget)
                    .clickable { onSelected(index) }
                    .padding(vertical = SketchSpacing.Sm),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = item.label,
                    modifier = Modifier.size(SketchSizes.SettingsIcon),
                    tint = if (selected) colors.primary else colors.muted,
                )
                Text(
                    text = item.label,
                    color = if (selected) colors.primary else colors.muted,
                    style = SketchTextStyles.Navigation,
                )
            }
        }
    }
}

@Composable
fun SketchMiniPlayer(
    state: SketchMiniPlayerUiModel,
    onPlayPause: () -> Unit = {},
    onNext: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    val colors = SketchDesign.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(SketchSizes.MiniPlayer)
            .background(colors.miniPlayer)
            .clickable(onClick = onClick)
            .padding(horizontal = SketchSpacing.Md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SketchSpacing.Md),
    ) {
        SketchMiniCover()
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = state.title,
                color = colors.onDark,
                style = SketchTextStyles.RowSubtitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = state.artist,
                color = colors.onDark.copy(alpha = SketchOpacity.Muted),
                style = SketchTextStyles.Auxiliary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        SketchDarkIconButton(
            icon = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (state.isPlaying) "暂停" else "播放",
            onClick = onPlayPause,
        )
        SketchDarkIconButton(
            icon = Icons.Default.SkipNext,
            contentDescription = "下一首",
            onClick = onNext,
        )
    }
}

@Composable
private fun SketchMiniCover() {
    Box(
        modifier = Modifier
            .size(SketchSizes.TouchTarget)
            .clip(RoundedCornerShape(SketchRadius.Control))
            .background(
                Brush.linearGradient(
                    listOf(
                        SketchBaseColors.MiniCoverStart,
                        SketchBaseColors.MiniCoverEnd,
                    ),
                ),
            ),
    )
}

@Composable
private fun SketchDarkIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(SketchSizes.TouchTarget)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = SketchDesign.colors.onDark,
        )
    }
}

@Composable
fun SketchEmptyState(
    title: String,
    description: String,
    actionLabel: String,
    onAction: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(SketchSpacing.Xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(SketchSpacing.Md),
    ) {
        SketchArtworkPlaceholder(
            modifier = Modifier.size(SketchSizes.OnboardingArtwork),
        )
        Text(
            text = title,
            color = SketchDesign.colors.ink,
            style = SketchTextStyles.RowTitle,
            textAlign = TextAlign.Center,
        )
        Text(
            text = description,
            color = SketchDesign.colors.muted,
            style = SketchTextStyles.RowSubtitle,
            textAlign = TextAlign.Center,
        )
        SketchPrimaryButton(
            label = actionLabel,
            onClick = onAction,
            modifier = Modifier.widthIn(max = SketchSizes.DialogActionMaxWidth),
        )
    }
}

@Composable
fun SketchArtworkPlaceholder(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = SketchRadius.Album,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    listOf(
                        SketchBaseColors.ArtworkPink,
                        SketchBaseColors.ArtworkSand,
                        SketchBaseColors.ArtworkMint,
                    ),
                ),
            )
            .border(
                BorderStroke(
                    SketchStroke.Border,
                    SketchDesign.colors.onDark.copy(alpha = SketchOpacity.Border),
                ),
                RoundedCornerShape(cornerRadius),
            ),
    )
}

@Composable
fun SketchPlayerControls(
    isPlaying: Boolean,
    playbackModeIcon: ImageVector,
    onPlayPause: () -> Unit = {},
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
    onPlaybackMode: () -> Unit = {},
    onDevices: () -> Unit = {},
) {
    val colors = SketchDesign.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        SketchDarkIconButton(
            icon = playbackModeIcon,
            contentDescription = "切换播放模式",
            onClick = onPlaybackMode,
        )
        SketchDarkIconButton(
            icon = Icons.Default.SkipPrevious,
            contentDescription = "上一首",
            onClick = onPrevious,
        )
        Box(
            modifier = Modifier
                .size(SketchSizes.PlayerButton)
                .clip(CircleShape)
                .background(colors.primary)
                .clickable(onClick = onPlayPause),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "暂停" else "播放",
                modifier = Modifier.size(SketchSpacing.Xxl),
                tint = colors.onDark,
            )
        }
        SketchDarkIconButton(
            icon = Icons.Default.SkipNext,
            contentDescription = "下一首",
            onClick = onNext,
        )
        SketchDarkIconButton(
            icon = Icons.Default.Devices,
            contentDescription = "选择播放设备",
            onClick = onDevices,
        )
    }
}

@Composable
fun SketchProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val colors = SketchDesign.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(SketchSpacing.Xs)
                .clip(RoundedCornerShape(SketchRadius.Pill))
                .background(colors.onDark.copy(alpha = SketchOpacity.DarkBorder)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(SketchSpacing.Xs)
                    .clip(RoundedCornerShape(SketchRadius.Pill))
                    .background(colors.primary),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = SketchSpacing.Xs),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("01:22", color = colors.muted, style = SketchTextStyles.Auxiliary)
            Text("03:37", color = colors.muted, style = SketchTextStyles.Auxiliary)
        }
    }
}

@Composable
fun SketchQueueRow(
    indexText: String,
    track: SketchTrackUiModel,
    active: Boolean = false,
    onClick: () -> Unit = {},
    onMore: (() -> Unit)? = null,
    actions: List<SketchMenuActionUiModel> = emptyList(),
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val colors = SketchDesign.colors
    SketchGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SketchSpacing.Page, vertical = SketchSpacing.Xs),
        contentPadding = PaddingValues(SketchSpacing.Sm),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = SketchSizes.TouchTarget),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SketchSpacing.Sm),
        ) {
            Box(
                modifier = Modifier
                    .size(SketchSpacing.Xl)
                    .clip(CircleShape)
                    .background(if (active) colors.selectedFill else colors.glass),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = indexText,
                    color = if (active) colors.primary else colors.muted,
                    style = SketchTextStyles.Auxiliary,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    color = if (active) colors.primary else colors.ink,
                    style = SketchTextStyles.Time,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = if (active) "正在播放" else track.artist,
                    color = colors.muted,
                    style = SketchTextStyles.Auxiliary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box {
                SketchMoreButton(
                    contentDescription = "队列更多操作",
                    onClick = {
                        if (actions.isEmpty()) {
                            onMore?.invoke()
                        } else {
                            menuExpanded = true
                        }
                    },
                )
                if (actions.isNotEmpty()) {
                    SketchActionDropdown(
                        expanded = menuExpanded,
                        actions = actions,
                        onDismiss = { menuExpanded = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun SketchActionDropdown(
    expanded: Boolean,
    actions: List<SketchMenuActionUiModel>,
    onDismiss: () -> Unit,
) {
    val colors = SketchDesign.colors
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .clip(RoundedCornerShape(SketchRadius.Card))
            .background(colors.glassStrong),
    ) {
        actions.forEach { action ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = action.label,
                        color = if (action.enabled) colors.ink else colors.muted,
                        style = SketchTextStyles.RowSubtitle,
                    )
                },
                enabled = action.enabled,
                onClick = {
                    onDismiss()
                    action.onClick()
                },
            )
        }
    }
}

@Composable
fun SketchActionMenu(
    actions: List<SketchMenuActionUiModel>,
    onAction: (SketchMenuActionUiModel) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = SketchDesign.colors
    SketchGlassCard(
        modifier = modifier.widthIn(
            min = SketchSizes.ActionMenuMinWidth,
            max = SketchSizes.ActionMenuMaxWidth,
        ),
        contentPadding = PaddingValues(SketchSpacing.Sm),
    ) {
        actions.forEach { action ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = SketchSizes.TouchTarget)
                    .clip(RoundedCornerShape(SketchRadius.Control))
                    .clickable { onAction(action) }
                    .padding(horizontal = SketchSpacing.Md),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = action.label,
                    color = colors.ink,
                    style = SketchTextStyles.RowSubtitle,
                )
            }
        }
    }
}

@Composable
fun SketchThemeSwatchGrid(
    themes: List<SketchThemeUiModel>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SketchSpacing.Page),
        verticalArrangement = Arrangement.spacedBy(SketchSpacing.Sm),
    ) {
        themes.chunked(2).forEachIndexed { rowIndex, rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SketchSpacing.Sm),
            ) {
                rowItems.forEachIndexed { columnIndex, theme ->
                    val index = rowIndex * 2 + columnIndex
                    SketchThemeSwatch(
                        theme = theme,
                        selected = index == selectedIndex,
                        onClick = { onSelected(index) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SketchThemeSwatch(
    theme: SketchThemeUiModel,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(SketchSizes.ThemeSwatchHeight)
            .clip(RoundedCornerShape(SketchRadius.Card))
            .background(Brush.linearGradient(listOf(theme.startColor, theme.endColor)))
            .border(
                BorderStroke(
                    SketchStroke.Border,
                    if (selected) SketchDesign.colors.ink else Color.Transparent,
                ),
                RoundedCornerShape(SketchRadius.Card),
            )
            .clickable(onClick = onClick)
            .padding(SketchSpacing.Md),
        contentAlignment = Alignment.BottomStart,
    ) {
        Text(
            text = theme.name,
            color = Color.White,
            style = SketchTextStyles.Button,
        )
    }
}

@Composable
fun SketchDeviceBar(
    deviceName: String,
    onClick: () -> Unit = {},
) {
    val colors = SketchDesign.colors
    SketchGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SketchSpacing.Page),
        contentPadding = PaddingValues(
            horizontal = SketchSpacing.Md,
            vertical = SketchSpacing.Sm,
        ),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = SketchSizes.FieldMinHeight),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SketchSpacing.Sm),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = null,
                tint = colors.primary,
            )
            Text(
                text = deviceName,
                modifier = Modifier.weight(1f),
                color = colors.muted,
                style = SketchTextStyles.Auxiliary,
            )
            Text(
                text = "切换播放设备",
                color = colors.muted,
                style = SketchTextStyles.Auxiliary,
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.muted,
            )
        }
    }
}

@Composable
fun SketchPlaybackModeIcon(
    modeIndex: Int,
): ImageVector = when (modeIndex % 3) {
    0 -> Icons.Default.Shuffle
    1 -> Icons.Default.Repeat
    else -> Icons.Default.RepeatOne
}

/**
 * 供页面使用的小型文本标题，避免页面重复写 TextStyle。
 */
@Composable
fun SketchSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = SketchTextStyles.SectionTitle,
) {
    Text(
        text = text,
        modifier = modifier,
        color = SketchDesign.colors.ink,
        style = style,
    )
}
