package com.example.audioplayer

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class V12UiTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun settings_showsAppearanceAndRemovesOldSections() {
        composeRule.onAllNodesWithText("设置")[0].performClick()
        composeRule.onNodeWithText("自动深色模式").assertIsDisplayed()
        composeRule.onNodeWithText("颜色主题").assertIsDisplayed()
        composeRule.onAllNodesWithText("NAS 连接").assertCountEquals(0)
        composeRule.onAllNodesWithText("蓝牙输出").assertCountEquals(0)
    }

    @Test
    fun timerEditor_displaysSundayWeekday() {
        composeRule.onNodeWithText("定时").performClick()
        composeRule.onNodeWithContentDescription("添加定时").performClick()
        composeRule.onNodeWithText("日").assertIsDisplayed()
    }
}