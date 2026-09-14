package goonarr.stash

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import goonarr.stash.features.home.HomeScreen
import goonarr.stash.features.home.configure.ConfigureHomeScreen
import goonarr.stash.features.home.discovery.SceneDiscoveryScreen
import goonarr.stash.features.performers.detail.PerformerDetailScreen
import goonarr.stash.features.performers.edit.EditPerformerScreen
import goonarr.stash.features.performers.list.PerformerListScreen
import goonarr.stash.features.performers.scrape.PerformerScrapeScreen
import goonarr.stash.features.scenes.detail.AddMarkerScreen
import goonarr.stash.features.scenes.detail.SceneDetailScreen
import goonarr.stash.features.scenes.edit.EditSceneScreen
import goonarr.stash.features.scenes.list.SceneListScreen
import goonarr.stash.features.settings.SettingsScreen
import goonarr.stash.features.stats.StatsScreen
import goonarr.stash.features.studios.detail.StudioDetailScreen
import goonarr.stash.features.studios.edit.EditStudioScreen
import goonarr.stash.features.tags.detail.TagDetailScreen
import goonarr.stash.features.tags.edit.EditTagScreen
import goonarr.stash.features.tags.list.TagListScreen
import timber.log.Timber

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun StashNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Hoist scroll states for lists to preserve position across navigation
    val performerListState = rememberLazyListState()
    val performerGridLargeState = rememberLazyGridState()
    val performerGridSmallState = rememberLazyGridState()

    val sceneListState = rememberLazyListState()
    val sceneCompactState = rememberLazyListState()
    val sceneGridState = rememberLazyGridState()

    val tagListState = rememberLazyListState()

    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = modifier,
            enterTransition = {
                val topLevelRoutes = listOf("home", "scene_list", "performer_list", "studio_list", "settings")
                val fromIndex = topLevelRoutes.indexOf(initialState.destination.route)
                val toIndex = topLevelRoutes.indexOf(targetState.destination.route)

                if (fromIndex != -1 && toIndex != -1) {
                    // Top-level tab switch
                    if (toIndex > fromIndex) {
                        // Moving right -> Slide in from right
                        slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300))
                    } else {
                        // Moving left -> Slide in from left
                        slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300))
                    }
                } else {
                    // Drill-down or other -> Default Scale + Fade
                    scaleIn(initialScale = 0.95f, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
                }
            },
            exitTransition = {
                val topLevelRoutes = listOf("home", "scene_list", "performer_list", "studio_list", "settings")
                val fromIndex = topLevelRoutes.indexOf(initialState.destination.route)
                val toIndex = topLevelRoutes.indexOf(targetState.destination.route)

                if (fromIndex != -1 && toIndex != -1) {
                    // Top-level tab switch
                    if (toIndex > fromIndex) {
                        // Moving right -> Old slides out to left
                        slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300))
                    } else {
                        // Moving left -> Old slides out to right
                        slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
                    }
                } else {
                    // Drill-down or other -> Default Scale + Fade
                    scaleOut(targetScale = 1.05f, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                }
            },
            popEnterTransition = {
                val topLevelRoutes = listOf("home", "scene_list", "performer_list", "studio_list", "settings")
                val fromIndex = topLevelRoutes.indexOf(initialState.destination.route)
                val toIndex = topLevelRoutes.indexOf(targetState.destination.route)
                if (fromIndex != -1 && toIndex != -1) {
                    // Top-level tab switch (back movement between tabs usually doesn't happen with pop,
                    // strictly speaking, unless we support back stack on tabs.
                    // But if it does happen (e.g. system back), we want consistent reverse animation)
                    if (toIndex > fromIndex) {
                        slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300))
                    } else {
                        slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(300))
                    }
                } else {
                    scaleIn(initialScale = 1.05f, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300))
                }
            },
            popExitTransition = {
                val topLevelRoutes = listOf("home", "scene_list", "performer_list", "studio_list", "settings")
                val fromIndex = topLevelRoutes.indexOf(initialState.destination.route)
                val toIndex = topLevelRoutes.indexOf(targetState.destination.route)

                if (fromIndex != -1 && toIndex != -1) {
                    if (toIndex > fromIndex) {
                        slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300))
                    } else {
                        slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(300))
                    }
                } else {
                    scaleOut(targetScale = 0.95f, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                }
            }
        ) {
            composable("home") {
                // Disable bottom bar hiding on Home screen (horizontal scrolling carousels)

                val savedStateHandle = it.savedStateHandle
                val tagsChanged = savedStateHandle.get<Boolean>("tagsChanged") ?: false

                HomeScreen(
                    onSceneClick = { sceneId, timestamp ->
                        Timber.d("🎬 Navigation: Home -> SceneDetail for id: $sceneId at $timestamp")
                        val route = if (timestamp != null) "scene_detail/$sceneId?timestamp=$timestamp" else "scene_detail/$sceneId"
                        navController.navigate(route)
                    },
                    onPerformerClick = { performerId ->
                        Timber.d("🎭 Navigation: Home -> PerformerDetail for id: $performerId")
                        navController.navigate("performer_detail/$performerId")
                    },
                    onTagsClick = {
                        Timber.d("🏷️ Navigation: Home -> TagList")
                        navController.navigate("tag_list")
                    },
                    onSeeAllClick = { sort, direction, title, count, sceneIds ->
                        Timber.d(
                            "🔍 Navigation: Home -> SeeAll for $title " +
                                "(count=$count, sort=$sort, direction=$direction, sceneIds=${sceneIds.size})"
                        )
                        // Parse tag_id or performer_id if they are embedded in sort
                        val tagId = if (sort.startsWith("tag_id:")) sort.substringAfter("tag_id:") else null
                        val performerId = if (sort.startsWith("performer_id:")) sort.substringAfter("performer_id:") else null
                        val cleanSort = if (tagId != null || performerId != null) "date" else sort

                        val route = StringBuilder("scene_list_view_all")
                        route.append("?title=${android.net.Uri.encode(title)}")
                        route.append("&sort=${android.net.Uri.encode(cleanSort)}")
                        route.append("&direction=${android.net.Uri.encode(direction)}")
                        if (count != null) route.append("&count=$count")
                        if (tagId != null) route.append("&tagId=${android.net.Uri.encode(tagId)}")
                        if (performerId != null) route.append("&performerId=${android.net.Uri.encode(performerId)}")
                        // Pass scene IDs for random sort to preserve order
                        if (cleanSort == "random" && sceneIds.isNotEmpty()) {
                            route.append("&sceneIds=${android.net.Uri.encode(sceneIds.joinToString(","))}")
                        }

                        navController.navigate(route.toString())
                    },
                    onConfigureClick = {
                        Timber.d("⚙️ Navigation: Home -> ConfigureHome")
                        navController.navigate("configure_home")
                    },
                    onStatsClick = {
                        Timber.d("📊 Navigation: Home -> Stats")
                        navController.navigate("stats")
                    },
                    onStudioClick = { studioId ->
                        Timber.d("🏢 Navigation: Home -> StudioDetail for id: $studioId")
                        navController.navigate("studio_detail/$studioId")
                    },
                    shouldRefresh = tagsChanged,
                    onRefreshConsumed = {
                        Timber.d("🏢 Navigation: Home -> Tags have changed, refreshing home page")
                        savedStateHandle.remove<Boolean>("tagsChanged")
                    },
                    animatedVisibilityScope = this,
                    sharedTransitionScope = this@SharedTransitionLayout
                )
            }

            composable(
                route = "configure_home",
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    )
                }
            ) {
                // Disable bottom bar hiding on configuration screens

                ConfigureHomeScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToTags = {
                        Timber.d("🏷️ Navigation: ConfigureHome -> TagList")
                        navController.navigate("tag_list")
                    },
                    onTagLongPress = { tagId ->
                        Timber.d("🏷️ Navigation: ConfigureHome -> TagDetail for id: $tagId")
                        navController.navigate("tag_detail/$tagId")
                    }
                )
            }

            composable(
                route = "stats",
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    )
                }
            ) {
                // Disable bottom bar hiding on stats screen

                StatsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(
                route = "tag_detail/{tagId}",
                arguments = listOf(navArgument("tagId") { type = NavType.StringType })
            ) {
                // This composable is intended for TagDetailScreen, but the provided snippet
                // incorrectly shows TagListScreen. Assuming the user wants to add a placeholder
                // or will correct this later, we'll insert the provided code as is.
                // For a correct implementation, this should call TagDetailScreen.
                TagDetailScreen(
                    tagId = it.arguments?.getString("tagId") ?: return@composable,
                    onBackClick = { navController.popBackStack() },
                    onDeleteSuccess = {
                        // Set flag to reset search, then navigate back to tag list
                        navController.getBackStackEntry("tag_list")
                            .savedStateHandle["resetSearch"] = true
                        navController.popBackStack("tag_list", inclusive = false)
                    },
                    onSceneClick = { id, timestamp ->
                        Timber.d("🎬 Navigation: TagDetail -> SceneDetail for id: $id")
                        navController.navigate("scene_detail/$id?timestamp=${timestamp ?: 0.0}")
                    },
                    onPerformerClick = { id ->
                        Timber.d("🎭 Navigation: TagDetail -> PerformerDetail for id: $id")
                        navController.navigate("performer_detail/$id")
                    },
                    onStudioClick = { id ->
                        Timber.d("🏢 Navigation: TagDetail -> StudioDetail for id: $id")
                        navController.navigate("studio_detail/$id")
                    },
                    onEditClick = { id ->
                        Timber.d("📝 Navigation: TagDetail -> EditTag for id: $id")
                        navController.navigate("tag_edit/$id")
                    },
                    onTagClick = { id ->
                        Timber.d("🏷️ Navigation: TagDetail -> TagDetail for id: $id")
                        navController.navigate("tag_detail/$id")
                    },
                    animatedVisibilityScope = this,
                    sharedTransitionScope = this@SharedTransitionLayout
                )
            }
            composable(
                route = "tag_edit/{tagId}",
                arguments = listOf(navArgument("tagId") { type = NavType.StringType }),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    )
                }
            ) { backStackEntry ->
                val tagId = backStackEntry.arguments?.getString("tagId") ?: ""
                EditTagScreen(
                    tagId = tagId,
                    onBackClick = { navController.popBackStack() },
                    onSaveSuccess = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("tagsChanged", true)
                        navController.popBackStack()
                    }
                )
            }
            composable(
                route = "tag_list"
            ) { backStackEntry ->
                // Disable bottom bar hiding for tag list

                val resetSearch = backStackEntry.savedStateHandle.get<Boolean>("resetSearch") ?: false

                TagListScreen(
                    navController = navController,
                    listState = tagListState,
                    onTagClick = { tagId ->
                        navController.navigate("tag_detail/$tagId")
                    },
                    onTagsChanged = {
                        // Set result to signal home should refresh
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("tagsChanged", true)
                    },
                    resetSearch = resetSearch,
                    onResetSearchConsumed = {
                        backStackEntry.savedStateHandle["resetSearch"] = false
                    },
                    animatedVisibilityScope = this,
                    sharedTransitionScope = this@SharedTransitionLayout
                )
            }
            composable("scene_list") {
                // Enable bottom bar hiding for long scrollable scene list

                SceneListScreen(
                    listState = sceneListState,
                    compactState = sceneCompactState,
                    gridState = sceneGridState,
                    onSceneClick = { sceneId, timestamp ->
                        Timber.d("🎬 Navigation: SceneList -> SceneDetail for id: $sceneId at $timestamp")
                        val route = if (timestamp != null) "scene_detail/$sceneId?timestamp=$timestamp" else "scene_detail/$sceneId"
                        navController.navigate(route)
                    },
                    onPerformerClick = { performerId ->
                        Timber.d("🎭 Navigation: SceneList -> PerformerDetail for id: $performerId")
                        navController.navigate("performer_detail/$performerId")
                    },
                    onStudioClick = { studioId ->
                        Timber.d("🏢 Navigation: SceneList -> StudioDetail for id: $studioId")
                        navController.navigate("studio_detail/$studioId")
                    }
                )
            }
            composable(
                route = "scene_list_view_all?title={title}&count={count}&sort={sort}" +
                    "&direction={direction}&tagId={tagId}&performerId={performerId}&sceneIds={sceneIds}",
                arguments = listOf(
                    navArgument("title") {
                        type = NavType.StringType
                        defaultValue = "Scenes"
                    },
                    navArgument("count") {
                        type = NavType.IntType
                        defaultValue = -1
                    },
                    navArgument("sort") {
                        type = NavType.StringType
                        defaultValue = "date"
                    },
                    navArgument("direction") {
                        type = NavType.StringType
                        defaultValue = "DESC"
                    },
                    navArgument("tagId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("performerId") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("sceneIds") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                // Disable bottom bar hiding for discovery/view all screen

                val title = android.net.Uri.decode(backStackEntry.arguments?.getString("title") ?: "Scenes")
                SceneDiscoveryScreen(
                    title = title,
                    onBackClick = { navController.popBackStack() },
                    onSceneClick = { sceneId, timestamp ->
                        Timber.d("🎬 Navigation: Discovery -> SceneDetail for id: $sceneId at $timestamp")
                        val route = if (timestamp != null) "scene_detail/$sceneId?timestamp=$timestamp" else "scene_detail/$sceneId"
                        navController.navigate(route)
                    },
                    onPerformerClick = { performerId ->
                        navController.navigate("performer_detail/$performerId")
                    },
                    onStudioClick = { studioId ->
                        Timber.d("🏢 Navigation: Discovery -> StudioDetail for id: $studioId")
                        navController.navigate("studio_detail/$studioId")
                    }
                )
            }
            composable(
                route = "scene_detail/{sceneId}?timestamp={timestamp}",
                arguments = listOf(
                    navArgument("sceneId") { type = NavType.StringType },
                    navArgument("timestamp") {
                        type = NavType.FloatType
                        nullable = false
                        defaultValue = -1f
                    }
                ),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(300))
                }
            ) { backStackEntry ->
                val sceneId = backStackEntry.arguments?.getString("sceneId") ?: ""
                val timestampArg = backStackEntry.arguments?.getFloat("timestamp") ?: -1f
                val timestamp = if (timestampArg >= 0) timestampArg.toDouble() else null

                SceneDetailScreen(
                    sceneId = sceneId,
                    startTimestamp = timestamp,
                    onBackClick = { navController.popBackStack() },
                    onPerformerClick = { performerId ->
                        Timber.d("🎭 Navigation: SceneDetail -> PerformerDetail for id: $performerId")
                        navController.navigate("performer_detail/$performerId")
                    },
                    onEditClick = { id ->
                        Timber.d("📝 Navigation: SceneDetail -> EditScene for id: $id")
                        navController.navigate("scene_edit/$id")
                    },
                    onScrapeClick = { id ->
                        Timber.d("🌐 Navigation: SceneDetail -> ScrapeScene for id: $id")
                        navController.navigate("scene_scrape/$id")
                    },
                    onAddMarkerClick = { id, seconds ->
                        Timber.d("📍 Navigation: SceneDetail -> AddMarker for id: $id at $seconds")
                        navController.navigate("scene_add_marker/$id/$seconds")
                    },
                    onTagClick = { tagId ->
                        Timber.d("🏷️ Navigation: SceneDetail -> TagDetail for id: $tagId")
                        navController.navigate("tag_detail/$tagId")
                    },
                    onStudioClick = { studioId ->
                        Timber.d("🏢 Navigation: SceneDetail -> StudioDetail for id: $studioId")
                        navController.navigate("studio_detail/$studioId")
                    },
                    animatedVisibilityScope = this,
                    sharedTransitionScope = this@SharedTransitionLayout
                )
            }
            composable(
                route = "scene_edit/{sceneId}",
                arguments = listOf(navArgument("sceneId") { type = NavType.StringType }),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    )
                }
            ) { backStackEntry ->
                val sceneId = backStackEntry.arguments?.getString("sceneId") ?: ""
                EditSceneScreen(
                    sceneId = sceneId,
                    onBackClick = { navController.popBackStack() },
                    onSaveSuccess = {
                        navController.popBackStack()
                    }
                )
            }
            composable(
                route = "scene_scrape/{sceneId}",
                arguments = listOf(navArgument("sceneId") { type = NavType.StringType }),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    )
                }
            ) { backStackEntry ->
                val sceneId = backStackEntry.arguments?.getString("sceneId") ?: ""
                goonarr.stash.features.scenes.scrape.SceneScrapeScreen(
                    sceneId = sceneId,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                route = "scene_add_marker/{sceneId}/{initialSeconds}",
                arguments = listOf(
                    navArgument("sceneId") { type = NavType.StringType },
                    navArgument("initialSeconds") { type = NavType.StringType }
                ),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    )
                }
            ) {
                AddMarkerScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("performer_list") {
                // Enable bottom bar hiding for long scrollable performer list

                PerformerListScreen(
                    listState = performerListState,
                    gridLargeState = performerGridLargeState,
                    gridSmallState = performerGridSmallState,
                    onPerformerClick = { performerId ->
                        Timber.d("🎭 Navigation: PerformerList -> PerformerDetail for id: $performerId")
                        navController.navigate("performer_detail/$performerId")
                    },
                    animatedVisibilityScope = this,
                    sharedTransitionScope = this@SharedTransitionLayout
                )
            }
            composable(
                route = "performer_detail/{performerId}",
                arguments = listOf(navArgument("performerId") { type = NavType.StringType })
            ) { backStackEntry ->
                val performerId = backStackEntry.arguments?.getString("performerId") ?: ""

                // Observe refresh result from EditPerformerScreen
                val refreshTrigger by backStackEntry.savedStateHandle.getLiveData<Boolean>("refresh_performer").observeAsState()

                PerformerDetailScreen(
                    performerId = performerId,
                    refreshTrigger = refreshTrigger,
                    onRefreshConsumed = { backStackEntry.savedStateHandle["refresh_performer"] = false },
                    onBackClick = { navController.popBackStack() },
                    onSceneClick = { sceneId, timestamp ->
                        Timber.d("🎬 Navigation: PerformerDetail -> SceneDetail for id: $sceneId at $timestamp")
                        val route = if (timestamp != null) "scene_detail/$sceneId?timestamp=$timestamp" else "scene_detail/$sceneId"
                        navController.navigate(route)
                    },
                    onPerformerClick = { clickedPerformerId ->
                        Timber.d("🎭 Navigation: PerformerDetail -> PerformerDetail for id: $clickedPerformerId")
                        navController.navigate("performer_detail/$clickedPerformerId")
                    },
                    onTagClick = { tagId ->
                        Timber.d("🏷️ Navigation: PerformerDetail -> TagDetail for id: $tagId")
                        navController.navigate("tag_detail/$tagId")
                    },
                    onStudioClick = { studioId ->
                        Timber.d("🏢 Navigation: PerformerDetail -> StudioDetail for id: $studioId")
                        navController.navigate("studio_detail/$studioId")
                    },
                    onEditClick = { id ->
                        Timber.d("📝 Navigation: PerformerDetail -> EditPerformer for id: $id")
                        navController.navigate("performer_edit/$id")
                    },
                    onScrapeClick = { id ->
                        Timber.d("🔎 Navigation: PerformerDetail -> PerformerScrape for id: $id")
                        navController.navigate("performer_scrape/$id")
                    },
                    animatedVisibilityScope = this,
                    sharedTransitionScope = this@SharedTransitionLayout
                )
            }
            composable(
                route = "performer_scrape/{performerId}",
                arguments = listOf(navArgument("performerId") { type = NavType.StringType }),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    )
                }
            ) { backStackEntry ->
                val performerId = backStackEntry.arguments?.getString("performerId") ?: ""
                PerformerScrapeScreen(
                    performerId = performerId,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                route = "performer_edit/{performerId}",
                arguments = listOf(navArgument("performerId") { type = NavType.StringType }),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    )
                }
            ) { backStackEntry ->
                val performerId = backStackEntry.arguments?.getString("performerId") ?: ""
                EditPerformerScreen(
                    performerId = performerId,
                    onBackClick = { navController.popBackStack() },
                    onSaveSuccess = {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("refresh_performer", true)
                        navController.popBackStack()
                    }
                )
            }
            composable("studio_list") {
                // Enable bottom bar hiding for scrollable studio list

                goonarr.stash.features.studios.list.StudioListScreen(
                    onStudioClick = { studioId ->
                        Timber.d("🏢 Navigation: StudioList -> StudioDetail for id: $studioId")
                        navController.navigate("studio_detail/$studioId")
                    },
                    animatedVisibilityScope = this,
                    sharedTransitionScope = this@SharedTransitionLayout
                )
            }
            composable(
                route = "studio_detail/{studioId}",
                arguments = listOf(navArgument("studioId") { type = NavType.StringType })
            ) { backStackEntry ->
                val studioId = backStackEntry.arguments?.getString("studioId") ?: ""
                StudioDetailScreen(
                    studioId = studioId,
                    onBackClick = { navController.popBackStack() },
                    onSceneClick = { sceneId, timestamp ->
                        val route = if (timestamp != null) "scene_detail/$sceneId?timestamp=$timestamp" else "scene_detail/$sceneId"
                        navController.navigate(route)
                    },
                    onPerformerClick = { performerId ->
                        navController.navigate("performer_detail/$performerId")
                    },
                    onTagClick = { tagId ->
                        Timber.d("🏷️ Navigation: StudioDetail -> TagDetail for id: $tagId")
                        navController.navigate("tag_detail/$tagId")
                    },
                    onStudioClick = { clickedStudioId ->
                        if (clickedStudioId != studioId) {
                            navController.navigate("studio_detail/$clickedStudioId")
                        }
                    },
                    onEditClick = { id ->
                        navController.navigate("studio_edit/$id")
                    },
                    onScrapeClick = { id ->
                        navController.navigate("studio_scrape/$id")
                    },
                    animatedVisibilityScope = this,
                    sharedTransitionScope = this@SharedTransitionLayout
                )
            }
            composable(
                route = "studio_edit/{studioId}",
                arguments = listOf(navArgument("studioId") { type = NavType.StringType }),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    )
                }
            ) { backStackEntry ->
                val studioId = backStackEntry.arguments?.getString("studioId") ?: ""
                EditStudioScreen(
                    studioId = studioId,
                    onBackClick = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }
            composable(
                route = "studio_scrape/{studioId}",
                arguments = listOf(navArgument("studioId") { type = NavType.StringType }),
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(300)
                    )
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(300)
                    )
                }
            ) { backStackEntry ->
                val studioId = backStackEntry.arguments?.getString("studioId") ?: ""
                goonarr.stash.features.studios.scrape.StudioScrapeScreen(
                    studioId = studioId,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("settings") {
                // Disable bottom bar hiding on Settings screen (all content on one page)

                SettingsScreen()
            }
        }
    }
}
