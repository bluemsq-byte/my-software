import 'package:flutter/material.dart';

import '../design/design_tokens.dart';
import '../design/glass_components.dart';
import '../models/ui_models.dart';

class TrackMenuAction {
  const TrackMenuAction({
    required this.label,
    required this.onSelected,
    this.icon,
    this.enabled = true,
  });

  final String label;
  final VoidCallback onSelected;
  final IconData? icon;
  final bool enabled;
}

/// 歌曲行公共组件，右侧三点菜单是固定的视觉入口。
class GlassTrackRow extends StatelessWidget {
  const GlassTrackRow({
    required this.track,
    required this.actions,
    this.onTap,
    this.trailingText,
    this.selected = false,
    this.showCheckbox = false,
    this.checkboxValue = false,
    this.onCheckboxChanged,
    super.key,
  });

  final MockTrack track;
  final List<TrackMenuAction> actions;
  final VoidCallback? onTap;
  final String? trailingText;
  final bool selected;
  final bool showCheckbox;
  final bool checkboxValue;
  final ValueChanged<bool>? onCheckboxChanged;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return GlassCard(
      margin: const EdgeInsets.symmetric(
        horizontal: AppSpacing.lg,
        vertical: AppSpacing.xs,
      ),
      onTap: onTap,
      padding: const EdgeInsets.symmetric(
        horizontal: AppSpacing.sm,
        vertical: AppSpacing.sm,
      ),
      child: Row(
        children: [
          if (showCheckbox) ...[
            Checkbox(
              value: checkboxValue,
              onChanged: onCheckboxChanged == null ? null : (value) => onCheckboxChanged!(value ?? false),
            ),
            const SizedBox(width: AppSpacing.sm),
          ],
          Container(
            width: 38,
            height: 38,
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(AppRadius.cover),
              color: selected ? scheme.primary : scheme.primary.withValues(alpha: 0.14),
            ),
            child: Icon(
              Icons.music_note_rounded,
              size: 20,
              color: selected ? scheme.onPrimary : scheme.primary,
            ),
          ),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  track.title,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                    fontSize: AppFontSize.songTitle,
                    fontWeight: FontWeight.w500,
                  ),
                ),
                const SizedBox(height: AppSpacing.xs),
                Text(
                  [track.artist, if (track.album != null) track.album!].join(' · '),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(
                    fontSize: AppFontSize.caption,
                    color: scheme.onSurfaceVariant,
                  ),
                ),
              ],
            ),
          ),
          if (trailingText != null) ...[
            const SizedBox(width: AppSpacing.sm),
            Text(
              trailingText!,
              style: TextStyle(
                fontSize: AppFontSize.time,
                color: scheme.onSurfaceVariant,
              ),
            ),
          ],
          _TrackMoreButton(actions: actions),
        ],
      ),
    );
  }
}

class _TrackMoreButton extends StatelessWidget {
  const _TrackMoreButton({required this.actions});

  final List<TrackMenuAction> actions;

  @override
  Widget build(BuildContext context) {
    return PopupMenuButton<int>(
      tooltip: '更多操作',
      icon: const Icon(Icons.more_vert_rounded),
      itemBuilder: (context) => [
        for (var index = 0; index < actions.length; index++)
          PopupMenuItem<int>(
            value: index,
            enabled: actions[index].enabled,
            child: Row(
              children: [
                if (actions[index].icon != null) ...[
                  Icon(actions[index].icon),
                  const SizedBox(width: AppSpacing.md),
                ],
                Text(actions[index].label),
              ],
            ),
          ),
      ],
      onSelected: (index) {
        // TODO: 接入真实播放、队列和播放列表业务。
        actions[index].onSelected();
      },
    );
  }
}

class GlassFolderRow extends StatelessWidget {
  const GlassFolderRow({
    required this.folder,
    this.onTap,
    this.trailing,
    super.key,
  });

  final MockFolder folder;
  final VoidCallback? onTap;
  final Widget? trailing;

  @override
  Widget build(BuildContext context) {
    final scheme = Theme.of(context).colorScheme;
    return GlassCard(
      margin: const EdgeInsets.symmetric(
        horizontal: AppSpacing.lg,
        vertical: AppSpacing.xs,
      ),
      onTap: onTap,
      padding: const EdgeInsets.all(AppSpacing.md),
      child: Row(
        children: [
          Container(
            width: 40,
            height: 40,
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(AppRadius.cover),
              color: scheme.primary.withValues(alpha: 0.14),
            ),
            child: Icon(folder.icon, color: scheme.primary),
          ),
          const SizedBox(width: AppSpacing.md),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  folder.name,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                    fontSize: AppFontSize.songTitle,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                const SizedBox(height: AppSpacing.xs),
                Text(
                  folder.subtitle,
                  style: TextStyle(
                    fontSize: AppFontSize.caption,
                    color: scheme.onSurfaceVariant,
                  ),
                ),
              ],
            ),
          ),
          trailing ?? const Icon(Icons.chevron_right_rounded),
        ],
      ),
    );
  }
}

/// 草图中的方形占位封面，接入项目时替换为真实专辑封面。
class AlbumPlaceholder extends StatelessWidget {
  const AlbumPlaceholder({
    this.size = 82,
    this.radius = AppRadius.cover,
    super.key,
  });

  final double size;
  final double radius;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: size,
      height: size,
      decoration: BoxDecoration(
        gradient: albumGradient,
        borderRadius: BorderRadius.circular(radius),
      ),
    );
  }
}
