import 'package:flutter/material.dart';

import '../design/design_tokens.dart';
import '../design/glass_components.dart';
import '../models/ui_models.dart';
import '../widgets/track_widgets.dart';

class PlayerScreen extends StatelessWidget {
  const PlayerScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return GlassBackground(
      child: Scaffold(
        backgroundColor: AppColors.transparent,
        appBar: AppBar(
          title: const Text('正在播放'),
          actions: [
            AppIconButton(
              icon: Icons.speaker_rounded,
              tooltip: '选择播放设备',
              onPressed: () {
                // TODO: 打开播放设备选择。
              },
            ),
            AppIconButton(
              icon: Icons.timer_rounded,
              tooltip: '倒计时停止',
              onPressed: () {
                // TODO: 打开倒计时设置。
              },
            ),
          ],
        ),
        body: ListView(
          padding: const EdgeInsets.fromLTRB(
            AppSpacing.lg,
            AppSpacing.sm,
            AppSpacing.lg,
            AppSpacing.xl,
          ),
          children: [
            GlassCard(
              padding: const EdgeInsets.all(AppSpacing.md),
              child: Row(
                children: [
                  Icon(Icons.speaker_rounded, color: scheme.primary),
                  const SizedBox(width: AppSpacing.sm),
                  const Expanded(child: Text('客厅音箱')),
                  const Text('切换播放设备'),
                  const Icon(Icons.chevron_right_rounded),
                ],
              ),
            ),
            const SizedBox(height: AppSpacing.md),
            const Center(
              child: AlbumPlaceholder(
                size: 292,
                radius: AppRadius.playerCover,
              ),
            ),
            const SizedBox(height: AppSpacing.xl),
            const Text(
              'Shake It Off',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: AppFontSize.sectionTitle,
                fontWeight: FontWeight.w700,
              ),
            ),
            const SizedBox(height: AppSpacing.xs),
            Text(
              'Taylor Swift · 1989',
              textAlign: TextAlign.center,
              style: TextStyle(
                fontSize: AppFontSize.body,
                color: scheme.onSurfaceVariant,
              ),
            ),
            const SizedBox(height: AppSpacing.lg),
            const _ProgressBar(),
            const SizedBox(height: AppSpacing.md),
            const _PlayerControls(),
            const SizedBox(height: AppSpacing.xl),
            Row(
              children: [
                const Expanded(
                  child: Text(
                    '接下来播放',
                    style: TextStyle(
                      fontSize: AppFontSize.songTitle,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
                Text('队列 13 首', style: Theme.of(context).textTheme.bodyMedium),
              ],
            ),
            const SizedBox(height: AppSpacing.sm),
            for (var index = 0; index < 4; index++)
              GlassTrackRow(
                track: mockTracks[index],
                trailingText: mockTracks[index].duration,
                selected: index == 0,
                actions: [
                  TrackMenuAction(
                    label: '立即播放',
                    icon: Icons.play_arrow_rounded,
                    onSelected: () {},
                  ),
                  TrackMenuAction(
                    label: '上移',
                    icon: Icons.keyboard_arrow_up_rounded,
                    enabled: index > 0,
                    onSelected: () {},
                  ),
                  TrackMenuAction(
                    label: '下移',
                    icon: Icons.keyboard_arrow_down_rounded,
                    enabled: index < 3,
                    onSelected: () {},
                  ),
                  TrackMenuAction(
                    label: '从队列移除',
                    icon: Icons.delete_outline_rounded,
                    onSelected: () {},
                  ),
                ],
                onTap: () {
                  // TODO: 播放队列中的当前歌曲。
                },
              ),
          ],
        ),
      ),
    );
  }
}

class DevicePickerScreen extends StatelessWidget {
  const DevicePickerScreen({super.key});

  @override
  Widget build(BuildContext context) {
    const devices = [
      MockDevice(
        name: '手机扬声器',
        type: '本机播放',
        icon: Icons.phone_android_rounded,
        selected: true,
      ),
      MockDevice(
        name: 'SoundCore Mini',
        type: '蓝牙音响',
        icon: Icons.bluetooth_rounded,
      ),
      MockDevice(
        name: '客厅电视',
        type: 'Chromecast / Google Cast',
        icon: Icons.cast_rounded,
      ),
      MockDevice(
        name: '客厅投影',
        type: 'Android 媒体路由',
        icon: Icons.tv_rounded,
      ),
    ];
    return GlassBackground(
      child: Scaffold(
        backgroundColor: AppColors.transparent,
        appBar: AppBar(title: const Text('选择播放设备')),
        body: ListView(
          children: [
            Padding(
              padding: const EdgeInsets.all(AppSpacing.lg),
              child: Row(
                children: [
                  const ChoiceChip(label: Text('本机播放'), selected: true),
                  const SizedBox(width: AppSpacing.sm),
                  ActionChip(
                    avatar: const Icon(Icons.refresh_rounded, size: 18),
                    label: const Text('刷新设备'),
                    onPressed: () {
                      // TODO: 刷新 MediaRouter 和 Cast 设备。
                    },
                  ),
                ],
              ),
            ),
            for (final device in devices)
              GlassCard(
                margin: const EdgeInsets.symmetric(
                  horizontal: AppSpacing.lg,
                  vertical: AppSpacing.xs,
                ),
                padding: const EdgeInsets.all(AppSpacing.md),
                onTap: () {
                  // TODO: 切换播放设备。
                },
                child: Row(
                  children: [
                    Icon(device.icon, size: 26),
                    const SizedBox(width: AppSpacing.md),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(device.name, style: Theme.of(context).textTheme.titleMedium),
                          const SizedBox(height: AppSpacing.xs),
                          Text(device.type, style: Theme.of(context).textTheme.bodyMedium),
                        ],
                      ),
                    ),
                    Icon(device.selected ? Icons.check_rounded : Icons.chevron_right_rounded),
                  ],
                ),
              ),
          ],
        ),
      ),
    );
  }
}

class _ProgressBar extends StatelessWidget {
  const _ProgressBar();

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        ClipRRect(
          borderRadius: BorderRadius.circular(AppRadius.pill),
          child: LinearProgressIndicator(
            value: 0.42,
            minHeight: 5,
            backgroundColor: Theme.of(context).colorScheme.surface.withValues(alpha: 0.28),
          ),
        ),
        const SizedBox(height: AppSpacing.xs),
        Row(
          children: [
            const Text('01:22', style: TextStyle(fontSize: AppFontSize.caption)),
            const Spacer(),
            const Text('03:37', style: TextStyle(fontSize: AppFontSize.caption)),
          ],
        ),
      ],
    );
  }
}

class _PlayerControls extends StatelessWidget {
  const _PlayerControls();

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceAround,
      children: [
        IconButton(
          tooltip: '切换播放模式',
          onPressed: () {
            // TODO: 顺序播放、列表循环、单曲循环切换。
          },
          icon: const Icon(Icons.repeat_rounded),
        ),
        IconButton(
          tooltip: '上一首',
          onPressed: () {},
          icon: const Icon(Icons.skip_previous_rounded, size: 30),
        ),
        SizedBox(
          width: 66,
          height: 66,
          child: FilledButton(
            onPressed: () {
              // TODO: 播放或暂停。
            },
            style: FilledButton.styleFrom(
              shape: const CircleBorder(),
              backgroundColor: scheme.primary,
              padding: EdgeInsets.zero,
            ),
            child: const Icon(Icons.pause_rounded, size: 34),
          ),
        ),
        IconButton(
          tooltip: '下一首',
          onPressed: () {},
          icon: const Icon(Icons.skip_next_rounded, size: 30),
        ),
        IconButton(
          tooltip: '倒计时停止',
          onPressed: () {},
          icon: const Icon(Icons.timer_rounded),
        ),
      ],
    );
  }
}
