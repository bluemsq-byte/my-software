import 'package:flutter/material.dart';

import '../design/design_tokens.dart';
import '../design/glass_components.dart';
import '../models/ui_models.dart';
import '../widgets/track_widgets.dart';

class RecentScreen extends StatelessWidget {
  const RecentScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return GlassBackground(
      child: Scaffold(
        backgroundColor: AppColors.transparent,
        appBar: AppBar(
          title: const Text('最近播放'),
          actions: [
            AppIconButton(
              icon: Icons.delete_outline_rounded,
              tooltip: '清空最近播放',
              onPressed: () {
                // TODO: 清空最近播放记录。
              },
            ),
          ],
        ),
        body: ListView(
          children: [
            for (final track in mockTracks.take(4))
              GlassTrackRow(
                track: track,
                trailingText: '刚刚',
                actions: standardTrackActions(),
                onTap: () {
                  // TODO: 播放最近播放歌曲。
                },
              ),
          ],
        ),
      ),
    );
  }
}

class PlaylistListScreen extends StatelessWidget {
  const PlaylistListScreen({super.key});

  @override
  Widget build(BuildContext context) {
    const playlists = [
      MockFolder(name: '我的收藏', subtitle: '26 首歌曲 · 本地与 NAS', icon: Icons.queue_music_rounded),
      MockFolder(name: '客厅周末', subtitle: '18 首歌曲 · NAS', icon: Icons.album_rounded),
      MockFolder(name: '驾驶歌单', subtitle: '42 首歌曲 · 本地', icon: Icons.directions_car_rounded),
    ];
    return GlassBackground(
      child: Scaffold(
        backgroundColor: AppColors.transparent,
        appBar: AppBar(
          title: const Text('播放列表'),
          actions: [
            AppIconButton(
              icon: Icons.add_rounded,
              tooltip: '新建播放列表',
              onPressed: () {
                // TODO: 新建播放列表。
              },
            ),
          ],
        ),
        body: ListView(
          children: [
            for (final playlist in playlists)
              GlassFolderRow(
                folder: playlist,
                onTap: () {
                  // TODO: 打开播放列表详情。
                },
              ),
          ],
        ),
      ),
    );
  }
}

class PlaylistDetailScreen extends StatefulWidget {
  const PlaylistDetailScreen({super.key});

  @override
  State<PlaylistDetailScreen> createState() => _PlaylistDetailScreenState();
}

class _PlaylistDetailScreenState extends State<PlaylistDetailScreen> {
  var selectionMode = false;
  final selected = <String>{};

  @override
  Widget build(BuildContext context) {
    return GlassBackground(
      child: Scaffold(
        backgroundColor: AppColors.transparent,
        appBar: AppBar(
          title: const Text('我的收藏'),
          actions: [
            TextButton(
              onPressed: () {
                setState(() {
                  selectionMode = !selectionMode;
                  if (!selectionMode) selected.clear();
                });
              },
              child: Text(selectionMode ? '取消选择' : '选择'),
            ),
          ],
        ),
        body: ListView(
          children: [
            if (selectionMode)
              Padding(
                padding: const EdgeInsets.all(AppSpacing.lg),
                child: Row(
                  children: [
                    Expanded(
                      child: AppSecondaryButton(
                        label: '全选',
                        expanded: true,
                        onPressed: () {
                          setState(() {
                            selected
                              ..clear()
                              ..addAll(mockTracks.map((track) => track.title));
                          });
                        },
                      ),
                    ),
                    const SizedBox(width: AppSpacing.md),
                    Expanded(
                      child: AppPrimaryButton(
                        label: '播放所选（${selected.length}）',
                        expanded: true,
                        onPressed: selected.isEmpty
                            ? null
                            : () {
                                // TODO: 播放已选择歌曲。
                              },
                      ),
                    ),
                  ],
                ),
              )
            else
              Padding(
                padding: const EdgeInsets.all(AppSpacing.lg),
                child: Row(
                  children: [
                    Expanded(
                      child: AppPrimaryButton(
                        label: '播放全部',
                        expanded: true,
                        onPressed: () {
                          // TODO: 从第一首开始播放。
                        },
                      ),
                    ),
                    const SizedBox(width: AppSpacing.md),
                    Expanded(
                      child: AppSecondaryButton(
                        label: '添加本地',
                        expanded: true,
                        onPressed: () {},
                      ),
                    ),
                    const SizedBox(width: AppSpacing.md),
                    Expanded(
                      child: AppSecondaryButton(
                        label: '添加网络',
                        expanded: true,
                        onPressed: () {},
                      ),
                    ),
                  ],
                ),
              ),
            for (var index = 0; index < mockTracks.length; index++)
              GlassTrackRow(
                track: mockTracks[index],
                showCheckbox: selectionMode,
                checkboxValue: selected.contains(mockTracks[index].title),
                onCheckboxChanged: (_) => _toggle(mockTracks[index].title),
                onTap: selectionMode
                    ? () => _toggle(mockTracks[index].title)
                    : () {
                        // TODO: 从当前索引开始播放整个列表。
                      },
                actions: [
                  TrackMenuAction(
                    label: '上移',
                    icon: Icons.keyboard_arrow_up_rounded,
                    onSelected: () {},
                  ),
                  TrackMenuAction(
                    label: '下移',
                    icon: Icons.keyboard_arrow_down_rounded,
                    onSelected: () {},
                  ),
                  TrackMenuAction(
                    label: '从列表移除',
                    icon: Icons.delete_outline_rounded,
                    onSelected: () {},
                  ),
                ],
              ),
          ],
        ),
      ),
    );
  }

  void _toggle(String title) {
    setState(() {
      if (!selected.add(title)) selected.remove(title);
    });
  }
}

List<TrackMenuAction> standardTrackActions() => [
      TrackMenuAction(
        label: '立即播放',
        icon: Icons.play_arrow_rounded,
        onSelected: () {},
      ),
      TrackMenuAction(
        label: '下一首播放',
        icon: Icons.queue_play_next_rounded,
        onSelected: () {},
      ),
      TrackMenuAction(
        label: '加入播放队列',
        icon: Icons.playlist_add_rounded,
        onSelected: () {},
      ),
    ];
