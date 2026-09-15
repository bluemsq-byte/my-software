import 'package:flutter/material.dart';

import 'design_tokens.dart';

abstract final class AppTheme {
  static ThemeData build({
    required AppThemeColor color,
    required Brightness brightness,
  }) {
    final isDark = brightness == Brightness.dark;
    final baseScheme = ColorScheme.fromSeed(
      seedColor: color.primary,
      brightness: brightness,
    );
    final scheme = baseScheme.copyWith(
      primary: isDark ? color.darkPrimary : color.primary,
      secondary: color.secondary,
      surface: isDark ? AppColors.darkSurface : AppColors.white,
    );

    return ThemeData(
      useMaterial3: true,
      brightness: brightness,
      colorScheme: scheme,
      scaffoldBackgroundColor: isDark ? AppColors.darkPage : AppColors.page,
      splashFactory: InkSparkle.splashFactory,
      appBarTheme: AppBarTheme(
        elevation: 0,
        centerTitle: false,
        backgroundColor: AppColors.transparent,
        foregroundColor: scheme.onSurface,
        titleTextStyle: TextStyle(
          color: scheme.onSurface,
          fontSize: AppFontSize.sectionTitle,
          fontWeight: FontWeight.w700,
        ),
      ),
      textTheme: TextTheme(
        headlineMedium: TextStyle(
          fontSize: AppFontSize.pageTitle,
          fontWeight: FontWeight.w600,
          color: scheme.onSurface,
        ),
        titleLarge: TextStyle(
          fontSize: AppFontSize.sectionTitle,
          fontWeight: FontWeight.w600,
          color: scheme.onSurface,
        ),
        titleMedium: TextStyle(
          fontSize: AppFontSize.songTitle,
          fontWeight: FontWeight.w600,
          color: scheme.onSurface,
        ),
        bodyLarge: TextStyle(
          fontSize: AppFontSize.body,
          fontWeight: FontWeight.w400,
          color: scheme.onSurface,
        ),
        bodyMedium: TextStyle(
          fontSize: AppFontSize.caption,
          fontWeight: FontWeight.w400,
          color: scheme.onSurfaceVariant,
        ),
      ),
    );
  }
}
