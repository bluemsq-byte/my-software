import 'package:flutter/material.dart';

import '../design/design_tokens.dart';
import '../design/glass_components.dart';
import '../models/ui_models.dart';

class TimerListScreen extends StatelessWidget {
  const TimerListScreen({super.key});

  @override
  Widget build(BuildContext context) {
    const tasks = [
      MockTimerTask(
        name: '早晨音乐',
        time: '07:30',
        action: '开始播放',
        repeat: '每天',
        source: 'NAS / music / Morning',
      ),
      MockTimerTask(
        name: '睡眠停止',
        time: '23:30',
        action: '停止播放',
        repeat: '每天',
        source: '当前播放队列',
        enabled: false,
      ),
    ];
    return GlassBackground(
      child: Scaffold(
        backgroundColor: AppColors.transparent,
        appBar: AppBar(
          title: const Text('定时任务'),
          actions: [
            AppIconButton(
              icon: Icons.add_rounded,
              tooltip: '新建定时',
              onPressed: () {},
            ),
          ],
        ),
        body: ListView(
          children: [
            for (final task in tasks)
              GlassCard(
                margin: const EdgeInsets.symmetric(
                  horizontal: AppSpacing.lg,
                  vertical: AppSpacing.sm,
                ),
                padding: const EdgeInsets.all(AppSpacing.md),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: Text(task.name, style: Theme.of(context).textTheme.titleMedium),
                        ),
                        Switch(
                          value: task.enabled,
                          onChanged: (_) {
                            // TODO: 启用或停用定时任务。
                          },
                        ),
                      ],
                    ),
                    Text('${task.time} · ${task.action}'),
                    const SizedBox(height: AppSpacing.xs),
                    Text(
                      '${task.repeat} · ${task.source}',
                      style: Theme.of(context).textTheme.bodyMedium,
                    ),
                  ],
                ),
              ),
          ],
        ),
      ),
    );
  }
}

class TimerEditorScreen extends StatelessWidget {
  const TimerEditorScreen({super.key});

  static const weekdays = ['一', '二', '三', '四', '五', '六', '日'];

  @override
  Widget build(BuildContext context) {
    return GlassBackground(
      child: Scaffold(
        backgroundColor: AppColors.transparent,
        appBar: AppBar(
          title: const Text('新建定时'),
          actions: [
            AppIconButton(
              icon: Icons.check_rounded,
              tooltip: '保存定时',
              onPressed: () {},
            ),
          ],
        ),
        body: ListView(
          padding: const EdgeInsets.all(AppSpacing.lg),
          children: [
            const TextField(
              decoration: InputDecoration(labelText: '定时名称', hintText: '早晨音乐'),
            ),
            const SizedBox(height: AppSpacing.md),
            SegmentedButton<String>(
              segments: const [
                ButtonSegment(value: 'start', label: Text('开始播放')),
                ButtonSegment(value: 'stop', label: Text('停止播放')),
              ],
              selected: const {'start'},
              onSelectionChanged: (_) {},
            ),
            const SizedBox(height: AppSpacing.md),
            const TextField(
              readOnly: true,
              decoration: InputDecoration(labelText: '时间', hintText: '07:30'),
            ),
            const SizedBox(height: AppSpacing.lg),
            const Text('重复'),
            const SizedBox(height: AppSpacing.sm),
            Wrap(
              spacing: AppSpacing.sm,
              runSpacing: AppSpacing.xs,
              children: [
                for (final day in weekdays)
                  FilterChip(
                    label: Text(day),
                    selected: day == '日',
                    onSelected: (_) {
                      // TODO: 更新重复星期。
                    },
                  ),
              ],
            ),
            const SizedBox(height: AppSpacing.lg),
            const ListTile(
              contentPadding: EdgeInsets.zero,
              title: Text('音乐来源'),
              subtitle: Text('NAS / SMB'),
              trailing: Icon(Icons.chevron_right_rounded),
            ),
            const ListTile(
              contentPadding: EdgeInsets.zero,
              title: Text('选择文件或文件夹'),
              trailing: Icon(Icons.chevron_right_rounded),
            ),
            const SizedBox(height: AppSpacing.xl),
            AppPrimaryButton(
              label: '保存定时',
              expanded: true,
              onPressed: () {
                // TODO: 保存定时任务。
              },
            ),
          ],
        ),
      ),
    );
  }
}

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return GlassBackground(
      child: Scaffold(
        backgroundColor: AppColors.transparent,
        appBar: AppBar(title: const Text('设置')),
        body: ListView(
          padding: const EdgeInsets.all(AppSpacing.lg),
          children: [
            _SettingsRow(
              title: '后台播放',
              subtitle: '退出页面后继续播放音乐',
              trailing: Switch(
                value: true,
                onChanged: (_) {},
              ),
            ),
            _SettingsRow(
              title: '自动深色模式',
              subtitle: '跟随手机系统',
              trailing: Switch(
                value: false,
                onChanged: (_) {},
              ),
            ),
            const SizedBox(height: AppSpacing.md),
            const Text('颜色主题'),
            const SizedBox(height: AppSpacing.sm),
            GridView.count(
              crossAxisCount: 2,
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              crossAxisSpacing: AppSpacing.sm,
              mainAxisSpacing: AppSpacing.sm,
              childAspectRatio: 2.7,
              children: [
                for (final theme in AppThemeColor.values)
                  _ThemeTile(
                    color: theme,
                    selected: theme == AppThemeColor.teal,
                  ),
              ],
            ),
            const SizedBox(height: AppSpacing.md),
            _SettingsRow(
              title: '缓存占用',
              subtitle: '总计 18.4 MB',
              trailing: TextButton(
                onPressed: () {
                  // TODO: 清除缓存。
                },
                child: const Text('清理'),
              ),
            ),
            _SettingsRow(
              title: '权限',
              subtitle: '本地音乐、通知、精确闹钟',
              trailing: const Icon(Icons.chevron_right_rounded),
            ),
          ],
        ),
      ),
    );
  }
}

class _SettingsRow extends StatelessWidget {
  const _SettingsRow({
    required this.title,
    required this.subtitle,
    required this.trailing,
  });

  final String title;
  final String subtitle;
  final Widget trailing;

  @override
  Widget build(BuildContext context) {
    return GlassCard(
      margin: const EdgeInsets.only(bottom: AppSpacing.sm),
      padding: const EdgeInsets.all(AppSpacing.md),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(title, style: Theme.of(context).textTheme.titleMedium),
                const SizedBox(height: AppSpacing.xs),
                Text(subtitle, style: Theme.of(context).textTheme.bodyMedium),
              ],
            ),
          ),
          trailing,
        ],
      ),
    );
  }
}

class _ThemeTile extends StatelessWidget {
  const _ThemeTile({
    required this.color,
    required this.selected,
  });

  final AppThemeColor color;
  final bool selected;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: () {
        // TODO: 保存颜色主题。
      },
      borderRadius: BorderRadius.circular(AppRadius.card),
      child: Ink(
        decoration: BoxDecoration(
          gradient: LinearGradient(colors: [color.primary, color.secondary]),
          borderRadius: BorderRadius.circular(AppRadius.card),
        ),
        padding: const EdgeInsets.all(AppSpacing.md),
        child: Align(
          alignment: Alignment.bottomLeft,
          child: Text(
            '${selected ? '✓ ' : ''}${color.label}',
            style: const TextStyle(
              color: AppColors.white,
              fontSize: AppFontSize.body,
              fontWeight: FontWeight.w600,
            ),
          ),
        ),
      ),
    );
  }
}
