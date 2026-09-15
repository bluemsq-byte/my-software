import 'package:flutter/material.dart';

/// 仅用于 UI 草图的展示模型。接入真实项目时替换为业务实体。
class MockTrack {
  const MockTrack({
    required this.title,
    required this.artist,
    this.album,
    this.duration = '03:30',
    this.source = '本地',
    this.isPlaying = false,
  });

  final String title;
  final String artist;
  final String? album;
  final String duration;
  final String source;
  final bool isPlaying;
}

class MockFolder {
  const MockFolder({
    required this.name,
    required this.subtitle,
    this.icon = Icons.folder_rounded,
  });

  final String name;
  final String subtitle;
  final IconData icon;
}

class MockConnection {
  const MockConnection({
    required this.name,
    required this.type,
    required this.status,
    this.icon = Icons.storage_rounded,
  });

  final String name;
  final String type;
  final String status;
  final IconData icon;
}

class MockTimerTask {
  const MockTimerTask({
    required this.name,
    required this.time,
    required this.action,
    required this.repeat,
    required this.source,
    this.enabled = true,
  });

  final String name;
  final String time;
  final String action;
  final String repeat;
  final String source;
  final bool enabled;
}

class MockDevice {
  const MockDevice({
    required this.name,
    required this.type,
    required this.icon,
    this.selected = false,
  });

  final String name;
  final String type;
  final IconData icon;
  final bool selected;
}

const mockTracks = <MockTrack>[
  MockTrack(title: 'Shake It Off', artist: 'Taylor Swift', album: '1989', duration: '03:37'),
  MockTrack(title: 'Bad Blood', artist: 'Taylor Swift', album: '1989', duration: '03:30'),
  MockTrack(title: 'Love Story', artist: 'Taylor Swift', album: 'Fearless', duration: '03:55'),
  MockTrack(title: 'You Belong With Me', artist: 'Taylor Swift', album: 'Fearless', duration: '03:51'),
  MockTrack(title: 'Lover', artist: 'Taylor Swift', album: 'Lover', duration: '03:41'),
  MockTrack(title: 'Red (Taylor\'s Version)', artist: 'Taylor Swift', album: 'Red', duration: '03:43'),
  MockTrack(title: 'All Too Well (Taylor\'s Version)', artist: 'Taylor Swift', album: 'Red', duration: '05:29'),
  MockTrack(title: 'Sparks Fly', artist: 'Taylor Swift', album: 'Speak Now', duration: '04:23'),
];

const albumGradient = LinearGradient(
  begin: Alignment.topLeft,
  end: Alignment.bottomRight,
  colors: [
    Color(0xFF28415D),
    Color(0xFFD97F78),
    Color(0xFFE9CF8D),
    Color(0xFF20302D),
  ],
  stops: [0, 0.28, 0.52, 1],
);
