import 'package:flutter/material.dart';

import '../design/design_tokens.dart';
import '../design/glass_components.dart';
import '../models/ui_models.dart';
import '../widgets/track_widgets.dart';

class ConnectionEditorScreen extends StatelessWidget {
  const ConnectionEditorScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return GlassBackground(
      child: Scaffold(
        backgroundColor: AppColors.transparent,
        appBar: AppBar(
          title: const Text('添加 SMB'),
          actions: [
            AppIconButton(
              icon: Icons.check_rounded,
              tooltip: '保存连接',
              onPressed: () {
                // TODO: 校验并保存网络连接。
              },
            ),
          ],
        ),
        body: ListView(
          padding: const EdgeInsets.all(AppSpacing.lg),
          children: [
            SegmentedButton<String>(
              segments: const [
                ButtonSegment(value: 'smb', label: Text('SMB')),
                ButtonSegment(value: 'webdav', label: Text('WebDAV')),
              ],
              selected: const {'smb'},
              onSelectionChanged: (_) {
                // TODO: 切换连接协议。
              },
            ),
            const SizedBox(height: AppSpacing.lg),
            const _GlassTextField(label: '连接名称'),
            const _GlassTextField(label: '服务器地址或 IP'),
            const _GlassTextField(label: '端口', value: '445'),
            const _GlassTextField(label: '用户名'),
            const _GlassTextField(label: '密码', obscureText: true),
            const SizedBox(height: AppSpacing.lg),
            Row(
              children: [
                Expanded(
                  child: AppSecondaryButton(
                    label: '测试连接',
                    expanded: true,
                    onPressed: () {
                      // TODO: 调用 SMB/WebDAV 连接测试。
                    },
                  ),
                ),
                const SizedBox(width: AppSpacing.md),
                Expanded(
                  child: AppPrimaryButton(
                    label: '保存',
                    expanded: true,
                    onPressed: () {
                      // TODO: 保存连接。
                    },
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class SmbSharePickerScreen extends StatelessWidget {
  const SmbSharePickerScreen({super.key});

  @override
  Widget build(BuildContext context) {
    const shares = ['music', 'homes', 'video'];
    return GlassBackground(
      child: Scaffold(
        backgroundColor: AppColors.transparent,
        appBar: AppBar(
          title: const Text('选择共享文件夹'),
          actions: [
            AppIconButton(
              icon: Icons.refresh_rounded,
              tooltip: '刷新共享文件夹',
              onPressed: () {},
            ),
          ],
        ),
        body: ListView(
          children: [
            for (final share in shares)
              GlassFolderRow(
                folder: MockFolder(name: share, subtitle: '共享文件夹'),
                onTap: () {
                  // TODO: 选择共享文件夹并进入目录。
                },
              ),
          ],
        ),
      ),
    );
  }
}

class BrowserScreen extends StatelessWidget {
  const BrowserScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return GlassBackground(
      child: Scaffold(
        backgroundColor: AppColors.transparent,
        appBar: AppBar(
          title: const Text('music / 华语'),
          actions: [
            AppIconButton(
              icon: Icons.search_rounded,
              tooltip: '搜索当前文件夹',
              onPressed: () {},
            ),
            AppIconButton(
              icon: Icons.more_vert_rounded,
              tooltip: '更多操作',
              onPressed: () {},
            ),
          ],
        ),
        body: ListView(
          children: [
            const _FolderOverview(),
            for (final track in mockTracks.take(8))
              GlassTrackRow(
                track: track,
                trailingText: track.duration,
                actions: trackActions(),
                onTap: () {
                  // TODO: 播放当前网络音乐。
                },
              ),
          ],
        ),
      ),
    );
  }

  List<TrackMenuAction> trackActions() => [
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
        TrackMenuAction(
          label: '加入播放列表',
          icon: Icons.playlist_add_check_rounded,
          onSelected: () {},
        ),
      ];
}

class TrackMenuPreviewScreen extends StatelessWidget {
  const TrackMenuPreviewScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return GlassBackground(
      child: Scaffold(
        backgroundColor: AppColors.transparent,
        appBar: AppBar(title: const Text('歌曲操作')),
        body: Stack(
          children: [
            ListView(
              children: [
                const _FolderOverview(),
                GlassTrackRow(
                  track: mockTracks.first,
                  trailingText: mockTracks.first.duration,
                  actions: const [],
                  onTap: () {},
                ),
              ],
            ),
            Positioned(
              right: AppSpacing.lg,
              top: 156,
              child: GlassCard(
                padding: const EdgeInsets.all(AppSpacing.sm),
                child: Column(
                  children: const [
                    _PreviewMenuItem(icon: Icons.play_arrow_rounded, label: '立即播放'),
                    _PreviewMenuItem(icon: Icons.queue_play_next_rounded, label: '下一首播放'),
                    _PreviewMenuItem(icon: Icons.playlist_add_rounded, label: '加入播放队列'),
                    _PreviewMenuItem(icon: Icons.playlist_add_check_rounded, label: '加入播放列表'),
                    _PreviewMenuItem(icon: Icons.info_outline_rounded, label: '查看文件信息'),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _FolderOverview extends StatelessWidget {
  const _FolderOverview();

  @override
  Widget build(BuildContext context) {
    return GlassCard(
      margin: const EdgeInsets.all(AppSpacing.lg),
      padding: const EdgeInsets.all(AppSpacing.md),
      child: Row(
        children: [
          const AlbumPlaceholder(),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('1、泰勒斯威夫特', style: Theme.of(context).textTheme.titleLarge),
                const SizedBox(height: AppSpacing.xs),
                Text('13 首歌曲', style: Theme.of(context).textTheme.bodyMedium),
                const SizedBox(height: AppSpacing.md),
                Row(
                  children: [
                    AppIconButton(
                      icon: Icons.shuffle_rounded,
                      tooltip: '随机播放',
                      onPressed: () {},
                    ),
                    AppIconButton(
                      icon: Icons.playlist_add_rounded,
                      tooltip: '加入播放队列',
                      onPressed: () {},
                    ),
                    AppIconButton(
                      icon: Icons.library_music_rounded,
                      tooltip: '加入播放列表',
                      onPressed: () {},
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _GlassTextField extends StatelessWidget {
  const _GlassTextField({
    required this.label,
    this.value,
    this.obscureText = false,
  });

  final String label;
  final String? value;
  final bool obscureText;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(bottom: AppSpacing.md),
      child: TextFormField(
        initialValue: value,
        obscureText: obscureText,
        decoration: InputDecoration(
          labelText: label,
          filled: true,
          fillColor: Theme.of(context).colorScheme.surface.withValues(alpha: 0.68),
          border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(AppRadius.card),
            borderSide: BorderSide.none,
          ),
        ),
      ),
    );
  }
}

class _PreviewMenuItem extends StatelessWidget {
  const _PreviewMenuItem({
    required this.icon,
    required this.label,
  });

  final IconData icon;
  final String label;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(
        horizontal: AppSpacing.md,
        vertical: AppSpacing.md,
      ),
      child: Row(
        children: [
          Icon(icon, size: 20),
          const SizedBox(width: AppSpacing.md),
          Text(label),
        ],
      ),
    );
  }
}
