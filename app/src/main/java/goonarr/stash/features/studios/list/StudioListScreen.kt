package goonarr.stash.features.studios.list

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import goonarr.stash.LocalScaffoldPadding
import goonarr.stash.LocalStashDynamicColors
import goonarr.stash.StashBlue
import goonarr.stash.features.components.SortableListTopAppBar
import goonarr.stash.features.components.hideBottomNavOnScroll
import goonarr.stash.features.studios.StudioSortType
import goonarr.stash.features.studios.StudioViewType
import goonarr.stash.rememberSceneColors
import kotlinx.coroutines.flow.flowOf

/**
 * Studios list screen displaying a 2-column grid of studio cards.
 */
@Composable
fun StudioListScreen(
    viewModel: StudioListViewModel = hiltViewModel(),
    onStudioClick: (String) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    val state by viewModel.state.collectAsState()
    val searchText by viewModel.searchText.collectAsState()
    val sortType by viewModel.sortType.collectAsState()
    val sortDirection by viewModel.sortDirection.collectAsState()
    val viewType by viewModel.viewType.collectAsState()
    val gridState = rememberLazyGridState()
    val listState = rememberLazyListState()
    val shouldLoadMore by remember(viewType) {
        derivedStateOf {
            val lastVisibleIndex = if (viewType == StudioViewType.GRID) {
                gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            } else {
                listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            }
            val totalItems = if (viewType == StudioViewType.GRID) {
                gridState.layoutInfo.totalItemsCount
            } else {
                listState.layoutInfo.totalItemsCount
            }
            lastVisibleIndex >= totalItems - 6
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            viewModel.loadMore()
        }
    }

    val studioColors = rememberSceneColors(StashBlue)

    CompositionLocalProvider(LocalStashDynamicColors provides studioColors) {
        StudioListContent(
            state = state,
            gridState = gridState,
            listState = listState,
            searchText = searchText,
            sortType = sortType,
            sortDirection = sortDirection,
            viewType = viewType,
            onStudioClick = onStudioClick,
            onRefresh = { viewModel.refresh() },
            onSearchQueryChange = viewModel::onSearchTextChange,
            onUpdateSortType = viewModel::updateSortType,
            onUpdateSortDirection = viewModel::updateSortDirection,
            onUpdateViewType = viewModel::updateViewType,
            animatedVisibilityScope = animatedVisibilityScope,
            sharedTransitionScope = sharedTransitionScope
        )
    }
}

/**
 * The content area for the Studio List screen.
 *
 * @param state The current UI state of the studio list.
 * @param gridState The state of the scrollable grid.
 * @param searchText The current search query.
 * @param sortType The current sort type.
 * @param sortDirection The current sort direction.
 * @param onStudioClick Callback when a studio is clicked.
 * @param onRefresh Callback to trigger a manual refresh.
 * @param onSearchQueryChange Callback to update the search query.
 * @param onUpdateSortType Callback to update the sort type.
 * @param onUpdateSortDirection Callback to update the sort direction.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioListContent(
    state: StudioListState,
    gridState: LazyGridState = rememberLazyGridState(),
    listState: LazyListState = rememberLazyListState(),
    searchText: String = "",
    sortType: StudioSortType = StudioSortType.NAME,
    sortDirection: String = "ASC",
    viewType: StudioViewType = StudioViewType.GRID,
    onStudioClick: (String) -> Unit,
    onRefresh: () -> Unit,
    onSearchQueryChange: (String) -> Unit = {},
    onUpdateSortType: (StudioSortType) -> Unit = {},
    onUpdateSortDirection: (String) -> Unit = {},
    onUpdateViewType: (StudioViewType) -> Unit = {},
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    val totalCount = if (state is StudioListState.Content) state.totalCount else null

    // Calculate top padding for content (status bar + top bar height)
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topBarHeight = 64.dp
    val contentTopPadding = statusBarPadding + topBarHeight
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Content Layer
        PullToRefreshBox(
            isRefreshing = state is StudioListState.Loading,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .hideBottomNavOnScroll()
        ) {
            when (state) {
                is StudioListState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = onRefresh
                    )
                }

                else -> {
                    StudioListLayout(
                        viewType = viewType,
                        state = state,
                        gridState = gridState,
                        listState = listState,
                        contentTopPadding = contentTopPadding,
                        onStudioClick = onStudioClick,
                        animatedVisibilityScope = animatedVisibilityScope,
                        sharedTransitionScope = sharedTransitionScope
                    )
                }
            }
        }

        // Top Bar Layer
        SortableListTopAppBar<StudioSortType, StudioViewType>(
            title = "Studios",
            count = totalCount,
            sortOptions = StudioSortType.entries.toList(),
            currentSortType = flowOf(sortType),
            currentSortDirection = flowOf(sortDirection),
            defaultSortType = StudioSortType.NAME,
            onUpdateSortType = onUpdateSortType,
            onUpdateSortDirection = onUpdateSortDirection,
            searchQuery = searchText,
            onSearchQueryChange = onSearchQueryChange,
            searchPlaceholder = "Search studios...",
            layoutOptions = StudioViewType.entries.toList(),
            currentLayoutType = flowOf(viewType),
            defaultLayoutType = StudioViewType.GRID,
            onUpdateLayoutType = onUpdateViewType,
            scrollBehavior = scrollBehavior
        )
    }
}

@Composable
private fun StudioListLayout(
    viewType: StudioViewType,
    state: StudioListState,
    gridState: LazyGridState,
    listState: LazyListState,
    contentTopPadding: androidx.compose.ui.unit.Dp,
    onStudioClick: (String) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    if (state is StudioListState.Empty) {
        EmptyState()
        return
    }

    val studios = (state as? StudioListState.Content)?.studios ?: emptyList()
    val isLoading = state is StudioListState.Loading

    when (viewType) {
        StudioViewType.GRID -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = contentTopPadding + 16.dp,
                    bottom = LocalScaffoldPadding.current.calculateBottomPadding()
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !isLoading
            ) {
                if (isLoading) {
                    items(6) {
                        StudioCardShimmer()
                    }
                } else {
                    itemsIndexed(
                        items = studios,
                        key = { _, studio -> studio.id }
                    ) { _, studio ->
                        StudioCard(
                            studio = studio,
                            imageUrl = studio.imagePath,
                            onClick = { onStudioClick(studio.id) },
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope
                        )
                    }
                }
            }
        }

        StudioViewType.LIST -> {
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = contentTopPadding + 16.dp,
                    bottom = LocalScaffoldPadding.current.calculateBottomPadding()
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !isLoading
            ) {
                if (isLoading) {
                    items(6) {
                        StudioCompactRowShimmer()
                    }
                } else {
                    itemsIndexed(
                        items = studios,
                        key = { _, studio -> studio.id }
                    ) { _, studio ->
                        StudioCompactRow(
                            studio = studio,
                            onClick = { onStudioClick(studio.id) },
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope
                        )
                    }
                }
            }
        }
    }
}

/**
 * An empty state for the Studio List, displayed when no studios match the current search or filters.
 */
@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Business,
            contentDescription = null,
            modifier = Modifier.padding(bottom = 16.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Text(
            text = "No Studios",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Studios will appear here when available",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * An error state for the Studio List, displayed when data loading fails.
 *
 * @param message The error message to display.
 * @param onRetry Callback to retry the data loading.
 */
@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error loading studios",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(vertical = 8.dp),
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}
