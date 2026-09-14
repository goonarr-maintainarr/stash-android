package goonarr.stash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import goonarr.stash.core.database.SettingsStore
import goonarr.stash.features.player.GlobalPlayerState
import goonarr.stash.features.player.MiniPlayer
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType

/**
 * A CompositionLocal to provide the Scaffold's padding to nested composables.
 */
val LocalScaffoldPadding = staticCompositionLocalOf { PaddingValues(0.dp) }
val LocalSetBottomBarVisibility = staticCompositionLocalOf<((Boolean) -> Unit)?> { null }

/**
 * The main screen of the application, acting as the root container for navigation.
 *
 * It sets up the [Scaffold] with a bottom navigation bar and hosts the [StashNavHost].
 * It also includes the [MiniPlayer] overlay that persists across screen transitions.
 *
 * @param globalPlayerState The global state of the video player, used for the mini player overlay.
 */
@Composable
fun MainScreen(globalPlayerState: GlobalPlayerState) {
    val navController = rememberNavController()
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination

    val navigationItems = listOf(
        NavigationItem("Home", "home", Icons.Default.Home),
        NavigationItem("Scenes", "scene_list", Icons.Default.PlayArrow),
        NavigationItem("Performers", "performer_list", Icons.Default.Person),
        NavigationItem("Studios", "studio_list", Icons.Default.Business),
        NavigationItem("Settings", "settings", Icons.Default.Settings)
    )

    val context = LocalContext.current
    val settingsStore = SettingsStore(context)
    val hideLabels by settingsStore.hideBottomBarLabels.collectAsState(initial = false)

    var bottomBarVisible by remember { mutableStateOf(true) }

    Scaffold(
        bottomBar = {
            val currentRoute = currentDestination?.route
            val showBottomBar = currentRoute != null && (
                navigationItems.any { it.route == currentRoute } ||
                    currentRoute.startsWith("scene_list_view_all")
                )

            AnimatedVisibility(
                visible = showBottomBar && bottomBarVisible,
                enter = slideInVertically { it } + expandVertically(expandFrom = Alignment.Top),
                exit = slideOutVertically { it } + shrinkVertically(shrinkTowards = Alignment.Top)
            ) {
                StashBottomNavigation(
                    navController = navController,
                    currentDestination = currentDestination,
                    items = navigationItems,
                    hideLabels = hideLabels
                )
            }
        }
    ) { innerPadding ->
        CompositionLocalProvider(LocalScaffoldPadding provides innerPadding) {
            val isEdgeToEdge = currentDestination?.route?.let { route ->
                route == "home" ||
                    route == "scene_list" ||
                    route == "performer_list" ||
                    route == "studio_list" ||
                    route.startsWith("studio_detail") ||
                    route == "settings" ||
                    route == "stats" ||
                    route.startsWith("scene_detail") ||
                    route.startsWith("scene_edit") ||
                    route.startsWith("scene_scrape") ||
                    route.startsWith("scene_add_marker") ||
                    route.startsWith("performer_detail") ||
                    route.startsWith("performer_edit") ||
                    route.startsWith("performer_scrape") ||
                    route.startsWith("tag_list") ||
                    route.startsWith("tag_detail") ||

                    route.startsWith("configure_home")
            } == true

            // Reset bottom bar visibility when route changes
            LaunchedEffect(currentDestination?.route) {
                bottomBarVisible = true
            }

            CompositionLocalProvider(
                LocalSetBottomBarVisibility provides { visible -> bottomBarVisible = visible }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    StashNavHost(
                        navController = navController,
                        modifier = Modifier.padding(bottom = if (isEdgeToEdge) 0.dp else innerPadding.calculateBottomPadding())
                    )

                    // Mini Player Overlay (Free Floating)
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        MiniPlayer(
                            playerState = globalPlayerState,
                            onExpand = {
                                globalPlayerState.restore()
                                globalPlayerState.currentSceneId?.let { sceneId ->
                                    navController.navigate("scene_detail/$sceneId") {
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * The bottom navigation bar for the application.
 *
 * Displays navigation items and handles navigation events with state saving/restoration
 * and haptic feedback.
 *
 * @param navController The [NavController] used for navigation.
 * @param currentDestination The current [NavDestination] to determine the selected item.
 * @param items The list of [NavigationItem]s to display.
 * @param hideLabels Whether to hide the text labels on the navigation items.
 */
@Composable
fun StashBottomNavigation(
    modifier: Modifier = Modifier,
    navController: NavController,
    currentDestination: NavDestination?,
    items: List<NavigationItem>,
    hideLabels: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.surface
) {
    val haptic = LocalStashHapticFeedback.current

    NavigationBar(
        modifier = modifier
            .height(if (hideLabels) 72.dp else 92.dp),
        containerColor = containerColor,
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        items.forEach { item ->
            NavigationBarItem(
                modifier = Modifier.offset(y = (-8).dp),
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = if (hideLabels) {
                    null
                } else {
                    { Text(item.label) }
                },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    haptic.perform(StashHapticFeedbackType.Selection)
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

/**
 * Data class representing an item in the bottom navigation bar.
 *
 * @property label The text label to display.
 * @property route The navigation route associated with this item.
 * @property icon The icon to display.
 */
data class NavigationItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)

@Preview(showBackground = true)
@Composable
fun MainPreview() {
//    val context = androidx.compose.ui.platform.LocalContext.current
//    val mockPlayerState = remember { GlobalPlayerState(context) }
//
//    StashTheme {
//        MainScreen(mockPlayerState)
//    }
}
