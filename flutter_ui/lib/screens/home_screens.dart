import 'package:flutter/material.dart';

import '../design/design_tokens.dart';
import '../design/glass_components.dart';
import '../models/ui_models.dart';
import '../widgets/track_widgets.dart';

class PermissionScreen extends StatelessWidget {
  const PermissionScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return GlassBackground(
      child: Scaffold(
        backgroundColor: AppColors.transparent,
        appBar: AppBar(title: const Text('音频播放器')),
        body: Center(
          child: Padding(
            padding: const EdgeInsets.all(AppSpacing.xl),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                const AlbumPlaceholder(size: 110),
                const SizedBox(height: AppSpacing.xl),
                Text(
                  '访问手机中的音乐',
                  style: Theme.of(context).textTheme.titleLarge,
                ),
                const SizedBox(height: AppSpacing.sm),
                Text(
                  '允许后即可扫描本地音乐。也可以先连接 NAS。',
                  textAlign: TextAlign.center,
                  style: Theme.of(context).textTheme.bodyLarge,
                ),
                const SizedBox(height: AppSpacing.xl),
                AppPrimaryButton(
                  label: '允许访问音频',
                  expanded: true,
                  onPressed: () {
                    // TODO: 接入 Android/iOS 媒体权限申请。
                  },
                ),
                const SizedBox(height: AppSpacing.md),
                TextButton(
                  onPressed: () {
                    // TODO: 跳转网络音乐连接。
                  },
                  child: const Text('暂不授权，先连接网络音乐'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class HomeLocalScreen extends StatefulWidget {
  const HomeLocalScreen({super.key});

  @override
  State<HomeLocalScreen> createState() => _HomeLocalScreenState();
}

class _HomeLocalScreenState extends State<HomeLocalScreen> {
  var showFolders = true;

  @override
  Widget build(BuildContext context) {
    return GlassBackground(
      child: Scaffold(
        backgroundColor: AppColors.transparent,
        appBar: AppBar(
          title: const Text('音乐'),
          actions: [
            AppIconButton(
              icon: Icons.add_rounded,
              tooltip: '添加网络音乐',
              onPressed: () {
                // TODO: 打开添加网络音乐页面。
              },
            ),
          ],
        ),
        body: ListView(
          padding: const EdgeInsets.only(bottom: AppSpacing.xl),
          children: [
            const Padding(
              padding: EdgeInsets.symmetric(horizontal: AppSpacing.lg),
              child: _SearchField(hint: '搜索本地音乐'),
            ),
            const _LibraryTabs(selectedIndex: 0),
            Padding(
              padding: const EdgeInsets.symmetric(
                horizontal: AppSpacing.lg,
                vertical: AppSpacing.sm,
              ),
              child: Row(
                children: [
                  Text('共 128 首', style: Theme.of(context).textTheme.bodyMedium),
                  const Spacer(),
                  ChoiceChip(
                    label: const Text('歌曲'),
                    selected: !showFolders,
                    onSelected: (_) => setState(() => showFolders = false),
                  ),
                  const SizedBox(width: AppSpacing.sm),
                  ChoiceChip(
                    label: const Text('文件夹'),
                    selected: showFolders,
                    onSelected: (_) => setState(() => showFolders = true),
                  ),
                ],
              ),
            ),
            if (showFolders) ...[
              GlassFolderRow(
                folder: const MockFolder(name: 'Music', subtitle: '36 首歌曲'),
                onTap: () {
                  // TODO: 打开本地文件夹。
                },
              ),
              GlassFolderRow(
                folder: const MockFolder(name: 'Download', subtitle: '18 首歌曲'),
                onTap: () {
                  // TODO: 打开本地文件夹。
                },
              ),
              GlassFolderRow(
                folder: const MockFolder(name: 'Family', subtitle: '42 首歌曲'),
                onTap: () {
                  // TODO: 打开本地文件夹。
                },
              ),
            ] else
              for (final track in mockTracks.take(4))
                GlassTrackRow(
                  track: track,
                  trailingText: track.duration,
                  onTap: () {
                    // TODO: 播放当前歌曲。
                  },
                  actions: [
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
                  ],
                ),
          ],
        ),
      ),
    );
  }
}

class HomeNetworkScreen extends StatelessWidget {
  const HomeNetworkScreen({super.key});

  @override
  Widget build(BuildContext context) {
    const connections = [
      MockConnection(
        name: '家里的群晖',
        type: 'SMB',
        status: '已连接',
        icon: Icons.storage_rounded,
      ),
      MockConnection(
        name: 'WebDAV 音乐',
        type: 'WebDAV',
        status: '上次使用',
        icon: Icons.cloud_rounded,
      ),
    ];

    return GlassBackground(
      child: Scaffold(
        backgroundColor: AppColors.transparent,
        appBar: AppBar(
          title: const Text('音乐'),
          actions: [
            AppIconButton(
              icon: Icons.add_rounded,
              tooltip: '添加网络音乐',
              onPressed: () {},
            ),
          ],
        ),
        body: ListView(
          padding: const EdgeInsets.only(bottom: AppSpacing.xl),
          children: [
            const Padding(
              padding: EdgeInsets.symmetric(horizontal: AppSpacing.lg),
              child: _SearchField(hint: '搜索网络音乐'),
            ),
            const _LibraryTabs(selectedIndex: 1),
            for (final connection in connections)
              GlassFolderRow(
                folder: MockFolder(
                  name: connection.name,
                  subtitle: '${connection.type} · ${connection.status}',
                  icon: connection.icon,
                ),
                trailing: PopupMenuButton<String>(
                  tooltip: '更多操作',
                  icon: const Icon(Icons.more_vert_rounded),
                  onSelected: (_) {
                    // TODO: 接入编辑或删除网络连接。
                  },
                  itemBuilder: (context) => const [
                    PopupMenuItem(value: 'edit', child: Text('编辑')),
                    PopupMenuItem(value: 'delete', child: Text('删除')),
                  ],
                ),
                onTap: () {
                  // TODO: 进入共享文件夹或 WebDAV 根目录。
                },
              ),
            GlassFolderRow(
              folder: const MockFolder(
                name: '添加网络音乐',
                subtitle: '连接 SMB 或 WebDAV',
                icon: Icons.add_rounded,
              ),
              onTap: () {
                // TODO: 打开添加网络音乐面板。
              },
            ),
          ],
        ),
      ),
    );
  }
}

class AddNetworkMusicScreen extends StatelessWidget {
  const AddNetworkMusicScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return GlassBackground(
      child: Scaffold(
        backgroundColor: AppColors.transparent,
        appBar: AppBar(title: const Text('添加网络音乐')),
        body: Center(
          child: Padding(
            padding: const EdgeInsets.all(AppSpacing.xl),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                GlassCard(
                  padding: const EdgeInsets.all(AppSpacing.lg),
                  onTap: () {
                    // TODO: 进入 SMB 连接表单。
                  },
                  child: const _NetworkOption(
                    title: 'SMB 网络音乐',
                    description: '连接群晖共享文件夹',
                    icon: Icons.storage_rounded,
                  ),
                ),
                const SizedBox(height: AppSpacing.md),
                GlassCard(
                  padding: const EdgeInsets.all(AppSpacing.lg),
                  onTap: () {
                    // TODO: 进入 WebDAV 连接表单。
                  },
                  child: const _NetworkOption(
                    title: 'WebDAV 网络音乐',
                    description: '通过 URL 访问网络音乐目录',
                    icon: Icons.cloud_rounded,
                  ),
                ),
                const SizedBox(height: AppSpacing.lg),
                AppSecondaryButton(
                  label: '取消',
                  expanded: true,
                  onPressed: () => Navigator.maybePop(context),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _SearchField extends StatelessWidget {
  const _SearchField({required this.hint});

  final String hint;

  @override
  Widget build(BuildContext context) {
    return TextField(
      readOnly: true,
      decoration: InputDecoration(
        hintText: hint,
        prefixIcon: const Icon(Icons.search_rounded),
        filled: true,
        fillColor: Theme.of(context).colorScheme.surface.withValues(alpha: 0.68),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(AppRadius.card),
          borderSide: BorderSide.none,
        ),
      ),
    );
  }
}

class _LibraryTabs extends StatelessWidget {
  const _LibraryTabs({required this.selectedIndex});

  final int selectedIndex;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(
        horizontal: AppSpacing.lg,
        vertical: AppSpacing.md,
      ),
      child: SegmentedButton<int>(
        segments: const [
          ButtonSegment(value: 0, label: Text('本地音乐')),
          ButtonSegment(value: 1, label: Text('网络音乐')),
        ],
        selected: {selectedIndex},
        onSelectionChanged: (_) {
          // TODO: 接入真实 Tab 状态。
        },
      ),
    );
  }
}

class _NetworkOption extends StatelessWidget {
  const _NetworkOption({
    required this.title,
    required this.description,
    required this.icon,
  });

  final String title;
  final String description;
  final IconData icon;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Icon(icon, size: 28),
        const SizedBox(width: AppSpacing.md),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(title, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.w700)),
              const SizedBox(height: AppSpacing.xs),
              Text(description, style: Theme.of(context).textTheme.bodyMedium),
            ],
          ),
        ),
        const Icon(Icons.chevron_right_rounded),
      ],
    );
  }
}
