package com.example.audioplayer

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appStartsAndBottomNavigationOpensCoreScreens() {
        composeRule.onNodeWithText("音乐库").assertIsDisplayed()

        composeRule.onNodeWithText("定时").performClick()
        composeRule.onNodeWithText("定时任务").assertIsDisplayed()

        composeRule.onAllNodesWithText("设置")[0].performClick()
        composeRule.onNodeWithText("后台播放").assertIsDisplayed()
    }
}