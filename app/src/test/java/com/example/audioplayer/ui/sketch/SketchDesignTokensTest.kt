package com.example.audioplayer.ui.sketch

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * 锁定设计规范中的关键数值，防止后续 UI 调整时无意修改颜色、字号、
 * 间距和圆角。该测试不涉及业务逻辑。
 */
class SketchDesignTokensTest {

    @Test
    fun spacing_matchesProductDesignSpec() {
        assertThat(SketchSpacing.Xs).isEqualTo(4.dp)
        assertThat(SketchSpacing.Sm).isEqualTo(8.dp)
        assertThat(SketchSpacing.Md).isEqualTo(12.dp)
        assertThat(SketchSpacing.Lg).isEqualTo(16.dp)
        assertThat(SketchSpacing.Xl).isEqualTo(24.dp)
        assertThat(SketchSpacing.Xxl).isEqualTo(32.dp)
        assertThat(SketchSpacing.Page).isEqualTo(16.dp)
    }

    @Test
    fun radius_matchesProductDesignSpec() {
        assertThat(SketchRadius.Card).isEqualTo(8.dp)
        assertThat(SketchRadius.Control).isEqualTo(8.dp)
        assertThat(SketchRadius.Album).isEqualTo(8.dp)
        assertThat(SketchStroke.Border).isEqualTo(1.dp)
    }

    @Test
    fun typography_matchesProductDesignSpec() {
        assertThat(SketchTextStyles.PageTitle.fontSize).isEqualTo(28.sp)
        assertThat(SketchTextStyles.SectionTitle.fontSize).isEqualTo(20.sp)
        assertThat(SketchTextStyles.RowTitle.fontSize).isEqualTo(16.sp)
        assertThat(SketchTextStyles.RowSubtitle.fontSize).isEqualTo(14.sp)
        assertThat(SketchTextStyles.Auxiliary.fontSize).isEqualTo(12.sp)
        assertThat(SketchTextStyles.Time.fontSize).isEqualTo(13.sp)
    }

    @Test
    fun accessibilityTouchTarget_matchesProductDesignSpec() {
        assertThat(SketchSizes.TouchTarget).isEqualTo(48.dp)
        assertThat(SketchSizes.PlayerButton).isEqualTo(64.dp)
        assertThat(SketchSizes.Icon).isEqualTo(24.dp)
    }
}
