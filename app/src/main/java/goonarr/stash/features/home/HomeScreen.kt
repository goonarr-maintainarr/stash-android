package goonarr.stash.features.home

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import goonarr.stash.LocalBlurNsfw
import goonarr.stash.LocalScaffoldPadding
import goonarr.stash.util.LocalStashHapticFeedback

/**
 * The main Home screen of the application.
 *
 * This screen displays a list of scene carousels based on various criteria (e.g., Random, Recent, Tags).
 * It acts as a stateful wrapper around [HomeScreenContent], handling ViewModel interaction and navigation events.
 *
 * @param viewModel The [HomeViewModel] instance, injected via Hilt.
 * @param onSceneClick Callback when a scene is clicked.
 * @param onPerformerClick Callback when a performer is clicked.
 * @param onStudioClick Callback when a studio is clicked.
 * @param onSeeAllClick Callback when "See All" is clicked on a carousel, passing sort type, direction, and title.
 * @param onConfigureClick Callback when the configuration button is clicked.
 * @param onStatsClick Callback when the stats button is clicked.
 * @param shouldRefresh Whether the screen should trigger a data refresh.
 * @param onRefreshConsumed Callback when the refresh event has been consumed.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onSceneClick: (String, Double?) -> Unit,
    onPerformerClick: (String) -> Unit,
    onStudioClick: (String) -> Unit = {},
    onTagsClick: () -> Unit,
    onSeeAllClick: (String, String, String, Int?, List<String>) -> Unit,
    onConfigureClick: () -> Unit,
    onStatsClick: () -> Unit,
    onStashDBSceneClick: (String) -> Unit = {},
    onStashDBSeeAllClick: () -> Unit = {},
    shouldRefresh: Boolean = false,
    onRefreshConsumed: () -> Unit = {},
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing = uiState is HomeUiState.Loading
    val isEmpty = uiState is HomeUiState.Error // Or if success but no sections

    // Trigger refresh when returning from tags screen
    LaunchedEffect(shouldRefresh) {
        if (shouldRefresh) {
            onRefreshConsumed()
        }
    }

    HomeScreenContent(
        uiState = uiState,
        onRefresh = { viewModel.refreshWithSync() },
        onSceneClick = onSceneClick,
        onPerformerClick = onPerformerClick,
        onStudioClick = onStudioClick,
        onTagsClick = onTagsClick,
        onSeeAllClick = onSeeAllClick,
        onConfigureClick = onConfigureClick,
        onStatsClick = onStatsClick,
        onBlurToggle = { isBlurred -> viewModel.toggleBlurNsfw(isBlurred) },
        onStashDBSceneClick = onStashDBSceneClick,
        onStashDBSeeAllClick = onStashDBSeeAllClick,
        animatedVisibilityScope = animatedVisibilityScope,
        sharedTransitionScope = sharedTransitionScope
    )
}

/**
 * The stateless content composable for the Home screen.
 *
 * Displays the list of scene carousels or loading/error states within a [Scaffold] and [PullToRefreshBox].
 *
 * @param uiState The current state of the home screen UI.
 * @param onRefresh Callback to trigger a manual refresh.
 * @param onSceneClick Callback when a scene is clicked.
 * @param onPerformerClick Callback when a performer is clicked.
 * @param onStudioClick Callback when a studio is clicked.
 * @param onTagsClick Callback for the tags action.
 * @param onSeeAllClick Callback when "See All" is clicked, passing sort params, title and count.
 * @param onConfigureClick Callback for the configuration action.
 * @param onStatsClick Callback for the stats action.
 * @param onBlurToggle Callback to toggle the global blur/NSFW setting.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onRefresh: () -> Unit,
    onSceneClick: (String, Double?) -> Unit,
    onPerformerClick: (String) -> Unit,
    onStudioClick: (String) -> Unit = {},
    onTagsClick: () -> Unit,
    onSeeAllClick: (String, String, String, Int?, List<String>) -> Unit,
    onConfigureClick: () -> Unit,
    onStatsClick: () -> Unit,
    onBlurToggle: (Boolean) -> Unit,
    onStashDBSceneClick: (String) -> Unit = {},
    onStashDBSeeAllClick: () -> Unit = {},
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    val haptic = LocalStashHapticFeedback.current
    val isBlurred = LocalBlurNsfw.current
    var showMenu by remember { mutableStateOf(false) }

    // Track scroll state to derive overlappedFraction
    val listState = rememberLazyListState()
    val overlappedFraction by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) {
                1f
            } else {
                // Normalize scroll offset: 0 at top, 1 after scrolling ~100dp
                (listState.firstVisibleItemScrollOffset / 300f).coerceIn(0f, 1f)
            }
        }
    }
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topBarHeight = 64.dp
    val contentTopPadding = statusBarPadding + topBarHeight

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Content Layer (first, so top bar overlays it)
        PullToRefreshBox(
            isRefreshing = uiState is HomeUiState.Loading,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            when (uiState) {
                is HomeUiState.Loading -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        contentPadding = PaddingValues(
                            top = contentTopPadding,
                            bottom = LocalScaffoldPadding.current.calculateBottomPadding() + 48.dp
                        )
                    ) {
                        // 5 base categories + 2 for typical followed tags
                        items(7) {
                            SceneCarouselRowShimmer()
                        }
                    }
                }

                is HomeUiState.Success -> {
                    if (uiState.sections.isEmpty()) {
                        HomeEmptyState(onConfigureClick = onConfigureClick)
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(
                                top = contentTopPadding,
                                bottom = LocalScaffoldPadding.current.calculateBottomPadding() + 48.dp
                            )
                        ) {
                            itemsIndexed(
                                items = uiState.sections,
                                key = { _, section -> section.id }
                            ) { index, section ->
                                when (section) {
                                    is HomeSectionData.SceneSection -> {
                                        SceneCarouselRow(
                                            title = section.title,
                                            count = section.count,
                                            scenes = section.scenes,
                                            sort = section.sort,
                                            direction = section.direction,
                                            onSceneClick = onSceneClick,
                                            onPerformerClick = onPerformerClick,
                                            onStudioClick = onStudioClick,
                                            onSeeAllClick = { sort, direction, sceneIds ->
                                                onSeeAllClick(sort, direction, section.title, section.count, sceneIds)
                                            }
                                        )
                                    }

                                    is HomeSectionData.PerformerSection -> {
                                        PerformerCarouselRow(
                                            title = section.title,
                                            count = section.count,
                                            performers = section.performers,
                                            onPerformerClick = onPerformerClick,
                                            onSeeAllClick = {
                                                // TODO: Navigate to performer list filtered
                                            },
                                            animatedVisibilityScope = animatedVisibilityScope,
                                            sharedTransitionScope = sharedTransitionScope
                                        )
                                    }

                                    is HomeSectionData.StudioSection -> {
                                        StudioCarouselRow(
                                            title = section.title,
                                            count = section.count,
                                            studios = section.studios,
                                            onStudioClick = onStudioClick,
                                            onSeeAllClick = {
                                                // TODO: Navigate to studio list filtered
                                            }
                                        )
                                    }

                                    is HomeSectionData.TagSection -> {
                                        TagCarouselRow(
                                            title = section.title,
                                            count = section.count,
                                            tags = section.tags,
                                            onTagClick = { tagId ->
                                                // Navigate to tag detail
                                                // TODO: Add onTagClick callback to HomeScreen
                                            },
                                            onSeeAllClick = {
                                                // TODO: Navigate to tag list filtered
                                            }
                                        )
                                    }

                                    is HomeSectionData.StashDBSceneSection -> {
                                        StashDBSceneCarouselRow(
                                            title = section.title,
                                            count = section.count,
                                            scenes = section.scenes,
                                            onSceneClick = onStashDBSceneClick,
                                            onSeeAllClick = onStashDBSeeAllClick
                                        )
                                    }

                                    is HomeSectionData.StashDBLoadingSection -> {
                                        StashDBSceneCarouselRowShimmer()
                                    }
                                }
                            }
                        }
                    }
                }

                is HomeUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }

        // Top Bar Layer (second, overlays content)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = statusBarPadding + 8.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 8.dp
                ),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                goonarr.stash.features.components.FilterChip(
                    label = null,
                    icon = Icons.Default.MoreVert,
                    onClick = { showMenu = true },
                    isExpanded = showMenu,
                    overlappedFraction = overlappedFraction
                )
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(goonarr.stash.StashTokens.Radius.Card),
                    shadowElevation = 8.dp
                ) {
                    // Tags
                    DropdownMenuItem(
                        text = { Text("Tags", color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            showMenu = false
                            haptic.perform(goonarr.stash.util.StashHapticFeedbackType.Selection)
                            onTagsClick()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Style,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    // Configure Home Screen
                    DropdownMenuItem(
                        text = { Text("Change Rows", color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            showMenu = false
                            haptic.perform(goonarr.stash.util.StashHapticFeedbackType.Selection)
                            onConfigureClick()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    // Library Stats
                    DropdownMenuItem(
                        text = { Text("Library Stats", color = MaterialTheme.colorScheme.onSurface) },
                        onClick = {
                            showMenu = false
                            haptic.perform(goonarr.stash.util.StashHapticFeedbackType.Selection)
                            onStatsClick()
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    // Enable/Disable NSFW Blur
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (isBlurred) "Disable NSFW Blur" else "Enable NSFW Blur",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        onClick = {
                            showMenu = false
                            haptic.perform(goonarr.stash.util.StashHapticFeedbackType.Light)
                            onBlurToggle(isBlurred)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = if (isBlurred) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeEmptyState(
    onConfigureClick: () -> Unit
) {
    val haptic = LocalStashHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(goonarr.stash.StashTokens.Radius.Card)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.padding(bottom = 16.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )

                Text(
                    text = "Welcome to Stash",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your home screen is currently empty. Reorganize or add content using the 'Change Rows' option in the menu.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        haptic.perform(goonarr.stash.util.StashHapticFeedbackType.Selection)
                        onConfigureClick()
                    },
                    shape = RoundedCornerShape(goonarr.stash.StashTokens.Radius.Button),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp).size(18.dp)
                    )
                    Text("Change Rows")
                }
            }
        }
    }
}
