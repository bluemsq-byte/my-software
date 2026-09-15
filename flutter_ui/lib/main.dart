import 'package:flutter/material.dart';

import 'design/app_theme.dart';
import 'design/design_tokens.dart';
import 'screens/home_screens.dart';
import 'screens/library_screens.dart';
import 'screens/timer_settings_screens.dart';
import 'widgets/app_shell.dart';

void main() {
  runApp(const AudioPlayerUiApp());
}

class AudioPlayerUiApp extends StatelessWidget {
  const AudioPlayerUiApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: '音频播放器',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.build(
        color: AppThemeColor.teal,
        brightness: Brightness.light,
      ),
      darkTheme: AppTheme.build(
        color: AppThemeColor.teal,
        brightness: Brightness.dark,
      ),
      home: const AppShell(
        pages: [
          HomeLocalScreen(),
          RecentScreen(),
          PlaylistListScreen(),
          TimerListScreen(),
          SettingsScreen(),
        ],
      ),
    );
  }
}
