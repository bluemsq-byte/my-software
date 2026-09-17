package com.example.audioplayer.ui

import android.net.Uri
import android.animation.ValueAnimator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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
import com.example.audioplayer.feature.connection.AddNetworkMusicScreen
import com.example.audioplayer.feature.connection.ConnectionEditorScreen
import com.example.audioplayer.feature.connection.SmbSharePickerScreen
import com.example.audioplayer.feature.library.LibraryScreen
import com.example.audioplayer.feature.library.RecentPlayScreen
import com.example.audioplayer.feature.player.PlayerScreen
import com.example.audioplayer.feature.playlist.PlaylistDetailScreen
import com.example.audioplayer.feature.playlist.PlaylistListScreen
import com.example.audioplayer.feature.playlist.PlaylistNetworkSourceScreen
import com.example.audioplayer.feature.playlist.LocalPlaylistPickerScreen
import com.example.audioplayer.feature.settings.SettingsScreen
import com.example.audioplayer.feature.search.GlobalSearchScreen
import com.example.audioplayer.feature.timer.TimerEditorScreen
import com.example.audioplayer.feature.timer.TimerListScreen
import com.example.audioplayer.ui.sketch.SketchBaseScreen
import com.example.audioplayer.ui.sketch.SketchBottomNavigation
import com.example.audioplayer.ui.sketch.SketchMiniPlayer
import com.example.audioplayer.ui.sketch.SketchMiniPlayerUiModel

private const val ROUTE_LIBRARY = "library"
private const val ROUTE_RECENT = "recent"
private const val ROUTE_TIMERS = "timers"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_PLAYLISTS = "playlists"
private const val ROUTE_PLAYLIST_DETAIL = "playlist_detail"
private const val ROUTE_PLAYER = "player"
private const val ROUTE_CONNECTION_EDITOR = "connection"
private const val ROUTE_ADD_NETWORK_MUSIC = "add_network_music"
private const val ROUTE_TIMER_EDITOR = "timer_editor"
private const val ROUTE_BROWSER = "browser"
private const val ROUTE_SMB_SHARES = "smb_shares"
private const val ROUTE_GLOBAL_SEARCH = "global_search"
private const val ROUTE_PLAYLIST_NETWORK = "playlist_network_sources"
private const val ROUTE_PLAYLIST_LOCAL = "playlist_local_sources"

private val MAIN_BOTTOM_NAV_ROUTES = setOf(
    ROUTE_LIBRARY,
    ROUTE_TIMERS,
    ROUTE_PLAYLISTS,
    ROUTE_SETTINGS,
    ROUTE_PLAYER,
    ROUTE_BROWSER,
)

@Composable
fun AudioPlayerApp(playbackController: PlaybackController) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route.orEmpty()
    val currentRouteBase = currentRoute.substringBefore('?').substringBefore('/')
    val playbackState by playbackController.state.collectAsStateWithLifecycle()

    playbackController.connect()

    SketchBaseScreen(
        darkTheme = currentRouteBase == ROUTE_PLAYER,
        useMaterialTheme = true,
        forceTheme = true,
    ) {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            bottomBar = {
                if (currentRouteBase in MAIN_BOTTOM_NAV_ROUTES) {
                    Column {
                        AnimatedVisibility(
                            visible = playbackState.currentTrackId != null && currentRouteBase != ROUTE_PLAYER,
                            enter = slideInVertically { it },
                            exit = slideOutVertically { it },
                        ) {
                            SketchMiniPlayer(
                                state = SketchMiniPlayerUiModel(
                                    title = playbackState.title,
                                    artist = playbackState.artist.orEmpty(),
                                    isPlaying = playbackState.isPlaying,
                                ),
                                onPlayPause = playbackController::playPause,
                                onNext = playbackController::next,
                                onClick = { navController.navigate(ROUTE_PLAYER) },
                            )
                        }
                        SketchBottomNavigation(
                            selectedIndex = when (currentRouteBase) {
                                ROUTE_LIBRARY, ROUTE_BROWSER, ROUTE_PLAYER -> if (currentRouteBase == ROUTE_PLAYER) -1 else 0
                                ROUTE_PLAYLISTS -> 1
                                ROUTE_TIMERS -> 2
                                ROUTE_SETTINGS -> 3
                                else -> -1
                            },
                            onSelected = { index ->
                                when (index) {
                                    0 -> navController.navigate(ROUTE_LIBRARY) {
                                        popUpTo(ROUTE_LIBRARY) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                    1 -> navController.navigate(ROUTE_PLAYLISTS) { launchSingleTop = true }
                                    2 -> navController.navigate(ROUTE_TIMERS) { launchSingleTop = true }
                                    3 -> navController.navigate(ROUTE_SETTINGS) { launchSingleTop = true }
                                }
                            },
                        )
                    }
                }
            },
        ) { padding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_LIBRARY,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(tween(animationDuration())) },
            exitTransition = { fadeOut(tween(animationDuration())) },
            popEnterTransition = { fadeIn(tween(animationDuration())) },
            popExitTransition = { fadeOut(tween(animationDuration())) },
        ) {
            composable(
                route = "$ROUTE_LIBRARY?tab={tab}",
                arguments = listOf(
                    navArgument("tab") {
                        type = NavType.StringType
                        defaultValue = "local"
                    },
                ),
            ) { entry ->
                LibraryScreen(
                    initialTab = if (entry.arguments?.getString("tab") == "network") 1 else 0,
                    onAddConnection = { navController.navigate(ROUTE_ADD_NETWORK_MUSIC) },
                    onOpenSearch = { navController.navigate(ROUTE_GLOBAL_SEARCH) },
                    onOpenRecent = { navController.navigate(ROUTE_RECENT) },
                    onOpenConnection = { connection ->
                        if (connection.protocol == com.example.audioplayer.core.model.ConnectionProtocol.SMB &&
                            connection.selectedShare.isNullOrBlank()
                        ) {
                            navController.navigate("$ROUTE_SMB_SHARES/${Uri.encode(connection.id)}")
                        } else {
                            navController.navigate(browserRoute(connection.id))
                        }
                    },
                    onEditConnection = { connection ->
                        navController.navigate("$ROUTE_CONNECTION_EDITOR?connectionId=${Uri.encode(connection.id)}")
                    },
                )
            }
            composable(ROUTE_RECENT) {
                RecentPlayScreen(onBack = { navController.popBackStack() })
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
            composable(ROUTE_PLAYLISTS) {
                PlaylistListScreen(
                    onCreate = {},
                    onOpen = { playlistId ->
                        navController.navigate("$ROUTE_PLAYLIST_DETAIL/$playlistId")
                    },
                )
            }
            composable(
                route = "$ROUTE_PLAYLIST_DETAIL/{playlistId}",
                arguments = listOf(
                    navArgument("playlistId") { type = NavType.LongType },
                ),
            ) {
                PlaylistDetailScreen(
                    onBack = { navController.popBackStack() },
                    onAddLocalSongs = { playlistId ->
                        navController.navigate("$ROUTE_PLAYLIST_LOCAL/$playlistId")
                    },
                    onAddNetworkSongs = {
                        val playlistId = it
                        navController.navigate("$ROUTE_PLAYLIST_NETWORK/$playlistId")
                    },
                )
            }
            composable(
                route = "$ROUTE_PLAYLIST_LOCAL/{playlistId}",
                arguments = listOf(
                    navArgument("playlistId") { type = NavType.LongType },
                ),
            ) { entry ->
                val playlistId = entry.arguments?.getLong("playlistId") ?: return@composable
                LocalPlaylistPickerScreen(
                    onBack = { navController.popBackStack() },
                    onAdded = {
                        navController.popBackStack(
                            route = "$ROUTE_PLAYLIST_DETAIL/$playlistId",
                            inclusive = false,
                        )
                    },
                )
            }
            composable(
                route = "$ROUTE_PLAYLIST_NETWORK/{playlistId}",
                arguments = listOf(
                    navArgument("playlistId") { type = NavType.LongType },
                ),
            ) { entry ->
                val playlistId = entry.arguments?.getLong("playlistId") ?: return@composable
                PlaylistNetworkSourceScreen(
                    onBack = { navController.popBackStack() },
                    onAddConnection = { navController.navigate(ROUTE_ADD_NETWORK_MUSIC) },
                    onConnectionSelected = { connection ->
                        if (
                            connection.protocol ==
                            com.example.audioplayer.core.model.ConnectionProtocol.SMB &&
                            connection.selectedShare.isNullOrBlank()
                        ) {
                            navController.navigate(
                                "$ROUTE_SMB_SHARES/${Uri.encode(connection.id)}" +
                                    "?playlistId=$playlistId",
                            )
                        } else {
                            navController.navigate(
                                browserRoute(connection.id, playlistId = playlistId),
                            )
                        }
                    },
                )
            }
            composable(ROUTE_SETTINGS) {
                SettingsScreen()
            }
            composable(
                route = ROUTE_PLAYER,
                enterTransition = {
                    slideInVertically(tween(animationDuration())) { it / 3 } + fadeIn(tween(animationDuration()))
                },
                exitTransition = {
                    slideOutVertically(tween(animationDuration())) { it / 3 } + fadeOut(tween(animationDuration()))
                },
            ) {
                PlayerScreen(
                    playbackController = playbackController,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(ROUTE_ADD_NETWORK_MUSIC) {
                AddNetworkMusicScreen(
                    onBack = { navController.popBackStack() },
                    onChooseProtocol = { protocol ->
                        navController.navigate(
                            "$ROUTE_CONNECTION_EDITOR?protocol=${protocol.name}",
                        )
                    },
                )
            }
            composable(ROUTE_GLOBAL_SEARCH) {
                GlobalSearchScreen(
                    onBack = { navController.popBackStack() },
                    onOpenPlaylist = { playlistId ->
                        navController.navigate("$ROUTE_PLAYLIST_DETAIL/$playlistId")
                    },
                )
            }
            composable(
                route = "$ROUTE_CONNECTION_EDITOR?connectionId={connectionId}&protocol={protocol}",
                arguments = listOf(
                    navArgument("connectionId") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("protocol") {
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
                route = "$ROUTE_SMB_SHARES/{connectionId}?playlistId={playlistId}",
                arguments = listOf(
                    navArgument("connectionId") { type = NavType.StringType },
                    navArgument("playlistId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                ),
            ) { entry ->
                val selectedConnectionId = entry.arguments?.getString("connectionId").orEmpty()
                val playlistId = entry.arguments?.getLong("playlistId")?.takeIf { it > 0L }
                SmbSharePickerScreen(
                    onBack = { navController.popBackStack() },
                    onSelected = {
                        navController.navigate(
                            browserRoute(selectedConnectionId, playlistId = playlistId),
                        )
                    },
                )
            }
            composable(
                route = "$ROUTE_BROWSER/{connectionId}?path={path}&playlistId={playlistId}",
                arguments = listOf(
                    navArgument("connectionId") { type = NavType.StringType },
                    navArgument("path") {
                        type = NavType.StringType
                        defaultValue = "/"
                    },
                    navArgument("playlistId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                ),
            ) { entry ->
                val playlistId = entry.arguments?.getLong("playlistId")?.takeIf { it > 0L }
                BrowserScreen(
                    onBack = { navController.popBackStack() },
                    onOpenPlayer = { navController.navigate(ROUTE_PLAYER) },
                    onPlaylistAdded = {
                        playlistId?.let { targetId ->
                            navController.popBackStack(
                                route = "$ROUTE_PLAYLIST_DETAIL/$targetId",
                                inclusive = false,
                            )
                        } ?: navController.popBackStack()
                    },
                )
            }
        }
        }
    }
}


private fun animationDuration(): Int = if (ValueAnimator.areAnimatorsEnabled()) 260 else 0

private fun browserRoute(
    connectionId: String,
    path: String = "/",
    playlistId: Long? = null,
): String {
    val playlistQuery = playlistId?.let { "&playlistId=$it" }.orEmpty()
    return "$ROUTE_BROWSER/${Uri.encode(connectionId)}?path=${Uri.encode(path)}$playlistQuery"
}
