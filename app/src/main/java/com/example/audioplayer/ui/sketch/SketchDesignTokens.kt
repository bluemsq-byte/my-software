package com.example.audioplayer.ui.sketch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 全量草图的视觉令牌。
 *
 * 这里的颜色取自 `design/sketches.html` 的 CSS 变量，字号、间距和圆角取自
 * `PRODUCT_DESIGN_SPEC.md`。页面组件不得再直接写颜色、字号、间距和圆角，
 * 统一从这里引用，避免后续设计和实现漂移。
 */
@Immutable
data class SketchPalette(
    val pageBackground: Color,
    val ink: Color,
    val muted: Color,
    val primary: Color,
    val primaryDark: Color,
    val secondary: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val glassStrong: Color,
    val glass: Color,
    val border: Color,
    val navigation: Color,
    val miniPlayer: Color,
    val onDark: Color,
    val selectedFill: Color,
)

/**
 * 基础颜色与草图 CSS 一一对应。
 */
object SketchBaseColors {
    val Page = Color(0xFFEEF2F1)
    val Ink = Color(0xFF17201E)
    val Muted = Color(0xFF64716E)
    val Teal = Color(0xFF0D9488)
    val TealDark = Color(0xFF0F766E)
    val Blue = Color(0xFF2563EB)
    val Amber = Color(0xFFD97706)
    val Danger = Color(0xFFDC2626)
    val Success = Color(0xFF16A34A)
    val ArtworkPink = Color(0xFFF8B6C2)
    val ArtworkSand = Color(0xFFF1D0B8)
    val ArtworkMint = Color(0xFF9FD5CC)
    val MiniCoverStart = Color(0xFFCDB7EF)
    val MiniCoverEnd = Color(0xFF8ED5C9)
    val DarkPage = Color(0xFF111817)
    val DarkInk = Color(0xFFF3F7F6)
    val DarkMuted = Color(0xFFA8B5B2)
}

private val LightSketchPalette = SketchPalette(
    pageBackground = SketchBaseColors.Page,
    ink = SketchBaseColors.Ink,
    muted = SketchBaseColors.Muted,
    primary = SketchBaseColors.Teal,
    primaryDark = SketchBaseColors.TealDark,
    secondary = SketchBaseColors.Blue,
    success = SketchBaseColors.Success,
    warning = SketchBaseColors.Amber,
    danger = SketchBaseColors.Danger,
    glassStrong = Color.White.copy(alpha = SketchOpacity.GlassStrong),
    glass = Color.White.copy(alpha = SketchOpacity.Glass),
    border = SketchBaseColors.Ink.copy(alpha = SketchOpacity.Border),
    navigation = Color.White.copy(alpha = SketchOpacity.Navigation),
    miniPlayer = SketchBaseColors.Ink.copy(alpha = SketchOpacity.MiniPlayer),
    onDark = Color.White,
    selectedFill = SketchBaseColors.Teal.copy(alpha = SketchOpacity.Selected),
)

private val DarkSketchPalette = SketchPalette(
    pageBackground = SketchBaseColors.DarkPage,
    ink = SketchBaseColors.DarkInk,
    muted = SketchBaseColors.DarkMuted,
    primary = SketchBaseColors.Teal,
    primaryDark = SketchBaseColors.TealDark,
    secondary = SketchBaseColors.Blue,
    success = SketchBaseColors.Success,
    warning = SketchBaseColors.Amber,
    danger = SketchBaseColors.Danger,
    glassStrong = Color.White.copy(alpha = SketchOpacity.DarkGlassStrong),
    glass = Color.White.copy(alpha = SketchOpacity.DarkGlass),
    border = Color.White.copy(alpha = SketchOpacity.DarkBorder),
    navigation = SketchBaseColors.DarkPage.copy(alpha = SketchOpacity.Navigation),
    miniPlayer = SketchBaseColors.Ink.copy(alpha = SketchOpacity.MiniPlayer),
    onDark = Color.White,
    selectedFill = SketchBaseColors.Teal.copy(alpha = SketchOpacity.Selected),
)

/**
 * 六种主题色仅作为后续切换入口保留，页面布局只消费 primary/primaryDark。
 */
enum class SketchColorTheme(
    val displayName: String,
    val primary: Color,
    val primaryDark: Color,
) {
    SKY_BLUE("天空蓝", SketchBaseColors.Blue, Color(0xFF1D4ED8)),
    FOREST_GREEN("森林绿", Color(0xFF3E8D58), Color(0xFF2F6E43)),
    VIOLET("紫罗兰", Color(0xFF7E4FBB), Color(0xFF633B96)),
    SUNSET_ORANGE("日落橙", Color(0xFFD4762B), Color(0xFFB85C18)),
    SAKURA_PINK("樱粉", Color(0xFFC2185B), Color(0xFF9C1449)),
    TEAL("青绿", SketchBaseColors.Teal, SketchBaseColors.TealDark),
}

private val LocalSketchPalette = staticCompositionLocalOf { LightSketchPalette }

object SketchDesign {
    val colors: SketchPalette
        @Composable get() = LocalSketchPalette.current
}

/**
 * UI 参考层的主题入口。接入业务时可把当前 AppThemeColor 映射到 primary。
 */
@Composable
fun SketchTheme(
    darkTheme: Boolean = false,
    colorTheme: SketchColorTheme = SketchColorTheme.TEAL,
    content: @Composable () -> Unit,
) {
    val base = if (darkTheme) DarkSketchPalette else LightSketchPalette
    val palette = base.copy(
        primary = colorTheme.primary,
        primaryDark = colorTheme.primaryDark,
        selectedFill = colorTheme.primary.copy(alpha = SketchOpacity.Selected),
    )
    CompositionLocalProvider(LocalSketchPalette provides palette, content = content)
}

/**
 * 规范中的基础间距：4 / 8 / 12 / 16 / 24 / 32dp。
 */
object SketchSpacing {
    val Xs = 4.dp
    val Sm = 8.dp
    val Md = 12.dp
    val Lg = 16.dp
    val Xl = 24.dp
    val Xxl = 32.dp
    val Page = Lg
}

/**
 * 规范中的卡片和控件圆角。胶囊按钮仍沿用系统胶囊形状。
 */
object SketchRadius {
    val Card = 8.dp
    val Control = 8.dp
    val Album = 8.dp
    val Pill = 999.dp
}

object SketchStroke {
    val Border = 1.dp
}

object SketchOpacity {
    const val GlassStrong = 0.80f
    const val Glass = 0.68f
    const val Border = 0.18f
    const val Muted = 0.70f
    const val Disabled = 0.38f
    const val Selected = 0.12f
    const val Navigation = 0.80f
    const val MiniPlayer = 0.80f
    const val DarkGlassStrong = 0.16f
    const val DarkGlass = 0.10f
    const val DarkBorder = 0.14f
}

/**
 * 未在规范中给出具体数值的组件尺寸，均按 4dp 基础网格推导；
 * 主要按钮触摸区不小于 48dp，播放主按钮使用规范上限 64dp。
 */
object SketchSizes {
    val TouchTarget = 48.dp
    val Icon = 24.dp
    val IconTile = 40.dp
    val IconAction = 40.dp
    val TopBarHeight = 56.dp
    val BottomNavigation = 64.dp
    val MiniPlayer = 64.dp
    val RowMinHeight = 64.dp
    val FieldMinHeight = 48.dp
    val PlayerButton = 64.dp
    val ArtworkMax = 320.dp
    val HeroArtwork = 80.dp
    val OnboardingArtwork = 112.dp
    val ThemeSwatchHeight = 72.dp
    val SettingsIcon = 20.dp
    val DialogActionMaxWidth = 280.dp
    val ActionMenuMinWidth = 200.dp
    val ActionMenuMaxWidth = 240.dp
}

/**
 * 所有文字都使用系统无衬线字体，只覆盖规范要求的字号和字重。
 */
object SketchTextStyles {
    val PageTitle = TextStyle(
        fontSize = 28.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.SemiBold,
    )
    val SectionTitle = TextStyle(
        fontSize = 20.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.SemiBold,
    )
    val RowTitle = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Medium,
    )
    val RowSubtitle = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Normal,
    )
    val Auxiliary = TextStyle(
        fontSize = 12.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Normal,
    )
    val Time = TextStyle(
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Normal,
    )
    val Button = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
    )
    val Navigation = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
    )
}

object SketchDefaults {
    const val PlayerProgress = 0.42f
    const val ArtworkAspectRatio = 1f
}
