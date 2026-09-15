import 'package:flutter/material.dart';

/// 所有设计值来自《音频播放器产品设计规范》和全量草图。
/// UI 页面禁止直接写裸色值、字号、间距和圆角，统一从这里读取。
abstract final class AppSpacing {
  static const double xs = 4;
  static const double sm = 8;
  static const double md = 12;
  static const double lg = 16;
  static const double xl = 24;
  static const double xxl = 32;
}

abstract final class AppRadius {
  static const double card = 8;
  static const double cover = 8;
  static const double playerCover = 14;
  static const double pill = 999;
}

abstract final class AppFontSize {
  static const double pageTitle = 28;
  static const double sectionTitle = 20;
  static const double songTitle = 16;
  static const double body = 14;
  static const double caption = 12;
  static const double time = 13;
}

abstract final class AppColors {
  static const Color ink = Color(0xFF17201E);
  static const Color muted = Color(0xFF64716E);
  static const Color line = Color(0x1A17201E);
  static const Color page = Color(0xFFEEF2F1);
  static const Color white = Colors.white;
  static const Color success = Color(0xFF16A34A);
  static const Color warning = Color(0xFFD97706);
  static const Color danger = Color(0xFFDC2626);
  static const Color disabled = Color(0x6117201E);
  static const Color transparent = Colors.transparent;
  static const Color darkPage = Color(0xFF0D1413);
  static const Color darkSurface = Color(0xFF192220);
}

enum AppThemeColor {
  skyBlue(
    label: '天空蓝',
    primary: Color(0xFF277CC1),
    secondary: Color(0xFF42B4AC),
    darkPrimary: Color(0xFF90CAF9),
  ),
  forestGreen(
    label: '森林绿',
    primary: Color(0xFF3E8D58),
    secondary: Color(0xFF9EC76B),
    darkPrimary: Color(0xFFA5D6A7),
  ),
  violet(
    label: '紫罗兰',
    primary: Color(0xFF7E4FBB),
    secondary: Color(0xFFB17BD3),
    darkPrimary: Color(0xFFCE93D8),
  ),
  sunsetOrange(
    label: '日落橙',
    primary: Color(0xFFD4762B),
    secondary: Color(0xFFF1B65B),
    darkPrimary: Color(0xFFFFB74D),
  ),
  sakuraPink(
    label: '樱花粉',
    primary: Color(0xFFC94F7C),
    secondary: Color(0xFFF0A6BD),
    darkPrimary: Color(0xFFF48FB1),
  ),
  teal(
    label: '青绿色',
    primary: Color(0xFF0D9488),
    secondary: Color(0xFF5BBFAF),
    darkPrimary: Color(0xFF80CBC4),
  );

  const AppThemeColor({
    required this.label,
    required this.primary,
    required this.secondary,
    required this.darkPrimary,
  });

  final String label;
  final Color primary;
  final Color secondary;
  final Color darkPrimary;
}

enum AppDarkMode {
  system('自动深色模式'),
  light('浅色'),
  dark('深色');

  const AppDarkMode(this.label);
  final String label;
}

abstract final class AppDurations {
  static const Duration fast = Duration(milliseconds: 150);
  static const Duration normal = Duration(milliseconds: 260);
  static const Duration slow = Duration(milliseconds: 350);
}
