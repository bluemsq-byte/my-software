package com.example.audioplayer.ui

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.audioplayer.core.playback.PlaybackController
import com.example.audioplayer.feature.browser.BrowserScreen
import com.example.audioplayer.feature.connection.ConnectionEditorScreen
import com.example.audioplayer.feature.library.LibraryScreen
import com.example.audioplayer.feature.player.PlayerScreen
import com.example.audioplayer.feature.settings.SettingsScreen
import com.example.audioplayer.feature.timer.TimerEditorScreen
import com.example.audioplayer.feature.timer.TimerListScreen
import com.example.audioplayer.ui.components.MiniPlayer

private const val ROUTE_LIBRARY = "library"
private const val ROUTE_TIMERS = "timers"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_PLAYER = "player"
private const val ROUTE_CONNECTION_EDITOR = "connection"
private const val ROUTE_TIMER_EDITOR = "timer_editor"
private const val ROUTE_BROWSER = "browser"

@Composable
fun AudioPlayerApp(playbackController: PlaybackController) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route.orEmpty()
    val playbackState by playbackController.state.collectAsStateWithLifecycle()

    playbackController.connect()

    Scaffold(
        bottomBar = {
            if (currentRoute != ROUTE_PLAYER) {
                Column {
                    MiniPlayer(
                        state = playbackState,
                        controller = playbackController,
                        onClick = { navController.navigate(ROUTE_PLAYER) },
                    )
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentRoute == ROUTE_LIBRARY,
                            onClick = {
                                navController.navigate(ROUTE_LIBRARY) {
                                    popUpTo(ROUTE_LIBRARY) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            icon = { Icon(Icons.Default.Home, contentDescription = null) },
                            label = { Text("首页") },
                        )
                        NavigationBarItem(
                            selected = currentRoute == ROUTE_TIMERS,
                            onClick = { navController.navigate(ROUTE_TIMERS) { launchSingleTop = true } },
                            icon = { Icon(Icons.Default.Timer, contentDescription = null) },
                            label = { Text("定时") },
                        )
                        NavigationBarItem(
                            selected = currentRoute == ROUTE_SETTINGS,
                            onClick = { navController.navigate(ROUTE_SETTINGS) { launchSingleTop = true } },
                            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                            label = { Text("设置") },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_LIBRARY,
            modifier = Modifier.padding(padding),
        ) {
            composable(ROUTE_LIBRARY) {
                LibraryScreen(
                    onAddConnection = { navController.navigate(ROUTE_CONNECTION_EDITOR) },
                    onOpenConnection = { connection ->
                        navController.navigate(browserRoute(connection.id))
                    },
                )
            }
            composable(ROUTE_TIMERS) {
                TimerListScreen(
                    onAddTimer = { navController.navigate(ROUTE_TIMER_EDITOR) },
                    onEditTimer = { timer ->
                        navController.navigate("$ROUTE_TIMER_EDITOR?timerId=${timer.id}")
                    },
                )
            }
            composable(
                route = "$ROUTE_TIMER_EDITOR?timerId={timerId}",
                arguments = listOf(
                    navArgument("timerId") {
                        type = NavType.LongType
                        defaultValue = 0L
                    },
                ),
            ) {
                TimerEditorScreen(
                    onSaved = { navController.popBackStack() },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(ROUTE_SETTINGS) {
                SettingsScreen()
            }
            composable(ROUTE_PLAYER) {
                PlayerScreen(
                    playbackController = playbackController,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = "$ROUTE_CONNECTION_EDITOR?connectionId={connectionId}",
                arguments = listOf(
                    navArgument("connectionId") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                ),
            ) {
                ConnectionEditorScreen(
                    onSaved = { navController.popBackStack() },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = "$ROUTE_BROWSER/{connectionId}?path={path}",
                arguments = listOf(
                    navArgument("connectionId") { type = NavType.StringType },
                    navArgument("path") {
                        type = NavType.StringType
                        defaultValue = "/"
                    },
                ),
            ) {
                BrowserScreen(
                    onBack = { navController.popBackStack() },
                    onOpenPlayer = { navController.navigate(ROUTE_PLAYER) },
                )
            }
        }
    }
}


private fun browserRoute(connectionId: String, path: String = "/"): String {
    return "$ROUTE_BROWSER/${Uri.encode(connectionId)}?path=${Uri.encode(path)}"
}