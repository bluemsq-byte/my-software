import 'dart:ui';

import 'package:flutter/material.dart';

import '../design/design_tokens.dart';
import '../design/glass_components.dart';
import '../models/ui_models.dart';
import 'track_widgets.dart';

class AppShell extends StatefulWidget {
  const AppShell({
    required this.pages,
    this.currentTrack = const MockTrack(
      title: 'Shake It Off',
      artist: 'Taylor Swift',
      album: '1989',
      duration: '03:37',
    ),
    super.key,
  });

  final List<Widget> pages;
  final MockTrack currentTrack;

  @override
  State<AppShell> createState() => _AppShellState();
}

class _AppShellState extends State<AppShell> {
  var index = 0;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      extendBody: true,
      backgroundColor: AppColors.transparent,
      body: IndexedStack(index: index, children: widget.pages),
      bottomNavigationBar: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          GlassMiniPlayer(track: widget.currentTrack),
          GlassBottomNav(
            currentIndex: index,
            onChanged: (value) {
              // TODO: 接入真实页面路由和播放状态订阅。
              setState(() => index = value);
            },
          ),
        ],
      ),
    );
  }
}

class GlassMiniPlayer extends StatelessWidget {
  const GlassMiniPlayer({
    required this.track,
    this.onTap,
    super.key,
  });

  final MockTrack track;
  final VoidCallback? onTap;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: const Color(0xEB191F1E),
      child: InkWell(
        onTap: onTap,
        child: SizedBox(
          height: 58,
          child: Row(
            children: [
              const SizedBox(width: AppSpacing.md),
              const AlbumPlaceholder(size: 42, radius: 5),
              const SizedBox(width: AppSpacing.md),
              Expanded(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      track.title,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: const TextStyle(
                        color: AppColors.white,
                        fontSize: AppFontSize.body,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    Text(
                      track.artist,
                      style: const TextStyle(
                        color: Color(0xA6FFFFFF),
                        fontSize: AppFontSize.caption,
                      ),
                    ),
                  ],
                ),
              ),
              IconButton(
                tooltip: '暂停',
                onPressed: () {
                  // TODO: 接入播放暂停。
                },
                icon: const Icon(Icons.pause_rounded, color: AppColors.white),
              ),
              IconButton(
                tooltip: '下一首',
                onPressed: () {
                  // TODO: 接入下一首。
                },
                icon: const Icon(Icons.skip_next_rounded, color: AppColors.white),
              ),
              const SizedBox(width: AppSpacing.sm),
            ],
          ),
        ),
      ),
    );
  }
}

class GlassBottomNav extends StatelessWidget {
  const GlassBottomNav({
    required this.currentIndex,
    required this.onChanged,
    super.key,
  });

  final int currentIndex;
  final ValueChanged<int> onChanged;

  static const items = [
    _NavItem('首页', Icons.home_rounded),
    _NavItem('最近', Icons.history_rounded),
    _NavItem('播放列表', Icons.queue_music_rounded),
    _NavItem('定时', Icons.timer_rounded),
    _NavItem('设置', Icons.settings_rounded),
  ];

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return ClipRect(
      child: BackdropFilter(
        filter: ImageFilter.blur(sigmaX: 20, sigmaY: 20),
        child: Container(
          height: 58,
          decoration: BoxDecoration(
            color: scheme.surface.withValues(alpha: 0.88),
            border: Border(top: BorderSide(color: scheme.outlineVariant)),
          ),
          child: Row(
            children: [
              for (var i = 0; i < items.length; i++)
                Expanded(
                  child: InkWell(
                    onTap: () => onChanged(i),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(
                          items[i].icon,
                          size: 20,
                          color: i == currentIndex ? scheme.primary : scheme.onSurfaceVariant,
                        ),
                        const SizedBox(height: 2),
                        Text(
                          items[i].label,
                          style: TextStyle(
                            fontSize: 10,
                            fontWeight: i == currentIndex ? FontWeight.w700 : FontWeight.w400,
                            color: i == currentIndex ? scheme.primary : scheme.onSurfaceVariant,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }
}

class _NavItem {
  const _NavItem(this.label, this.icon);
  final String label;
  final IconData icon;
}
