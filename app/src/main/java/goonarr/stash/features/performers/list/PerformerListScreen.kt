package goonarr.stash.features.performers.list

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import goonarr.stash.LocalScaffoldPadding
import goonarr.stash.core.model.Performer
import goonarr.stash.features.components.SortableListTopAppBar
import goonarr.stash.features.components.hideBottomNavOnScroll
import goonarr.stash.features.performers.PerformerCard
import goonarr.stash.features.performers.PerformerCompactCard
import goonarr.stash.features.performers.PerformerHeroCard
import goonarr.stash.features.performers.PerformerSortType
import goonarr.stash.features.performers.PerformerViewType

/**
 * The primary Performer List screen, showing all performers with filtering and sorting.
 *
 * @param viewModel The ViewModel providing the performer data and state.
 * @param onPerformerClick Callback when a performer is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformerListScreen(
    viewModel: PerformerListViewModel = hiltViewModel(),
    listState: LazyListState = rememberLazyListState(),
    gridLargeState: LazyGridState = rememberLazyGridState(),
    gridSmallState: LazyGridState = rememberLazyGridState(),
    onPerformerClick: (String) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) {
    val performers = viewModel.performers.collectAsLazyPagingItems()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val viewType by viewModel.viewType.collectAsState(initial = PerformerViewType.GRID_LARGE)
    val isRefreshing = performers.loadState.refresh is LoadState.Loading
    val isEmpty = performers.itemCount == 0
    // Calculate top padding for content (status bar + top bar height)
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topBarHeight = 64.dp
    val contentTopPadding = statusBarPadding + topBarHeight
    val haptic = goonarr.stash.util.LocalStashHapticFeedback.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    // States are now hoisted to the caller (StashNavHost)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Content Layer
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { performers.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .hideBottomNavOnScroll()
        ) {
            if (isEmpty && isRefreshing) {
                PerformerListLoadingState(viewType, contentTopPadding)
            } else {
                PerformerListContent(
                    pagingItems = performers,
                    viewType = viewType,
                    listState = listState,
                    gridLargeState = gridLargeState,
                    gridSmallState = gridSmallState,
                    onPerformerClick = onPerformerClick,
                    contentTopPadding = contentTopPadding,
                    animatedVisibilityScope = animatedVisibilityScope,
                    sharedTransitionScope = sharedTransitionScope
                )
            }
        }

        // Top App Bar Layer - Overlay removed as redesigned SortableListTopAppBar handles background
        SortableListTopAppBar(
            title = "Performers",
            count = viewModel.totalCount.collectAsState(initial = null).value,
            sortOptions = PerformerSortType.entries.toList(),
            currentSortType = viewModel.sortType,
            currentSortDirection = viewModel.sortDirection,
            defaultSortType = PerformerSortType.NAME,
            onUpdateSortType = { viewModel.updateSortType(it) },
            onUpdateSortDirection = { viewModel.updateSortDirection(it) },
            searchQuery = searchQuery,
            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
            searchPlaceholder = "Search performers...",
            layoutOptions = PerformerViewType.entries.toList(),
            currentLayoutType = viewModel.viewType,
            defaultLayoutType = PerformerViewType.GRID_LARGE,
            onUpdateLayoutType = { viewModel.updateViewType(it) },
            scrollBehavior = scrollBehavior
        )
    }
}

/**
 * The main content area for the Performer List, supporting different view types.
 *
 * @param pagingItems The paginated list of performers.
 * @param viewType The current view type (List, Large Grid, or Small Grid).
 * @param listState The state for list view scrolling.
 * @param gridState The state for grid view scrolling.
 * @param onPerformerClick Callback when a performer is clicked.
 * @param contentTopPadding The top padding to account for the top app bar.
 */
@Composable
fun PerformerListContent(
    pagingItems: LazyPagingItems<Performer>,
    viewType: PerformerViewType,
    listState: LazyListState,
    gridLargeState: LazyGridState,
    gridSmallState: LazyGridState,
    onPerformerClick: (String) -> Unit,
    contentTopPadding: Dp = 80.dp,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) {
    when (viewType) {
        PerformerViewType.LIST -> {
            PerformerList(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentTopPadding = contentTopPadding
            ) {
                items(
                    count = pagingItems.itemCount,
                    key = pagingItems.itemKey { it.id }
                ) { index ->
                    val performer = pagingItems[index]
                    if (performer != null) {
                        PerformerHeroCard(
                            performer = performer,
                            onClick = { onPerformerClick(performer.id) },
                            height = 612.dp,
                            modifier = Modifier.fillMaxWidth(),
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope
                        )
                    } else {
                        PerformerHeroCard(
                            performer = Performer(id = "", name = ""),
                            onClick = {},
                            isLoading = true,
                            height = 612.dp,
                            modifier = Modifier.fillMaxWidth(),
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope
                        )
                    }
                }
            }
        }
        PerformerViewType.GRID_LARGE -> {
            PerformerGrid(
                columns = 2,
                state = gridLargeState,
                modifier = Modifier.fillMaxSize(),
                contentTopPadding = contentTopPadding
            ) {
                items(
                    count = pagingItems.itemCount,
                    key = pagingItems.itemKey { it.id }
                ) { index ->
                    val performer = pagingItems[index]
                    if (performer != null) {
                        PerformerCard(
                            performer = performer,
                            onClick = { onPerformerClick(performer.id) },
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope
                        )
                    } else {
                        PerformerCard(
                            performer = Performer(id = "", name = ""),
                            onClick = {},
                            isLoading = true,
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope
                        )
                    }
                }
            }
        }
        PerformerViewType.GRID_SMALL -> {
            PerformerGrid(
                columns = 3,
                state = gridSmallState,
                contentPadding = PaddingValues(
                    top = contentTopPadding + 16.dp,
                    start = 8.dp,
                    end = 8.dp,
                    bottom = LocalScaffoldPadding.current.calculateBottomPadding()
                ),
                spacing = 8.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    count = pagingItems.itemCount,
                    key = pagingItems.itemKey { it.id }
                ) { index ->
                    val performer = pagingItems[index]
                    if (performer != null) {
                        PerformerCompactCard(
                            performer = performer,
                            onClick = { onPerformerClick(performer.id) },
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope
                        )
                    } else {
                        PerformerCompactCard(
                            performer = Performer(id = "", name = ""),
                            onClick = {},
                            isLoading = true,
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
 * A loading state for the Performer List, displaying shimmer cards.
 *
 * @param viewType The current view type to determine the card style.
 * @param contentTopPadding The top padding to account for the top app bar.
 */
@Composable
fun PerformerListLoadingState(
    viewType: PerformerViewType = PerformerViewType.GRID_LARGE,
    contentTopPadding: Dp = 80.dp
) {
    when (viewType) {
        PerformerViewType.LIST -> {
            PerformerList(
                modifier = Modifier.fillMaxSize(),
                contentTopPadding = contentTopPadding
            ) {
                items(4) {
                    PerformerHeroCard(
                        performer = Performer(id = "", name = ""),
                        onClick = {},
                        height = 612.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        PerformerViewType.GRID_LARGE -> {
            PerformerGrid(
                columns = 2,
                modifier = Modifier.fillMaxSize(),
                contentTopPadding = contentTopPadding
            ) {
                items(6) {
                    PerformerCard(
                        performer = Performer(id = "", name = ""),
                        onClick = {},
                        isLoading = true
                    )
                }
            }
        }
        PerformerViewType.GRID_SMALL -> {
            PerformerGrid(
                columns = 3,
                contentPadding = PaddingValues(top = contentTopPadding, start = 8.dp, end = 8.dp),
                spacing = 8.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                items(9) {
                    PerformerCompactCard(
                        performer = Performer(id = "", name = ""),
                        onClick = {},
                        isLoading = true
                    )
                }
            }
        }
    }
}

/**
 * A grid-based layout for performer items.
 */
@Composable
fun PerformerGrid(
    columns: Int = 2,
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    contentPadding: PaddingValues? = null,
    contentTopPadding: Dp = 80.dp,
    spacing: Dp = 16.dp,
    content: LazyGridScope.() -> Unit
) {
    val padding = contentPadding ?: PaddingValues(
        top = contentTopPadding + 16.dp,
        start = 16.dp,
        end = 16.dp,
        bottom = LocalScaffoldPadding.current.calculateBottomPadding()
    )
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        state = state,
        contentPadding = padding,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
        modifier = modifier,
        content = content
    )
}

/**
 * A list-based layout for performer items.
 */
@Composable
fun PerformerList(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentTopPadding: Dp = 80.dp,
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    LazyColumn(
        state = state,
        contentPadding = PaddingValues(
            top = contentTopPadding + 16.dp,
            start = 16.dp,
            end = 16.dp,
            bottom = LocalScaffoldPadding.current.calculateBottomPadding()
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier,
        content = content
    )
}
