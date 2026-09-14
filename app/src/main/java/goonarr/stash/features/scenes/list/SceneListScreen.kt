package goonarr.stash.features.scenes.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import goonarr.stash.LocalScaffoldPadding
import goonarr.stash.core.model.Scene
import goonarr.stash.features.components.SortableListTopAppBar
import goonarr.stash.features.components.hideBottomNavOnScroll
import goonarr.stash.features.scenes.SceneLayoutType
import goonarr.stash.features.scenes.SceneSortType
import kotlinx.coroutines.flow.Flow

/**
 * The primary Scene List screen, showing all scenes with filtering and sorting.
 *
 * @param viewModel The ViewModel providing the scene data and state.
 * @param onSceneClick Callback when a scene is clicked, optionally passing a start timestamp.
 * @param onPerformerClick Callback when a performer is clicked.
 * @param onStudioClick Callback when a studio is clicked.
 */
@Composable
fun SceneListScreen(
    viewModel: SceneListViewModel = hiltViewModel(),
    listState: LazyListState = rememberLazyListState(),
    compactState: LazyListState = rememberLazyListState(),
    gridState: LazyGridState = rememberLazyGridState(),
    onSceneClick: (String, Double?) -> Unit,
    onPerformerClick: (String) -> Unit = {},
    onStudioClick: (String) -> Unit = {},
) {
    SceneListScaffold(
        title = "Scenes",
        scenes = viewModel.scenes.collectAsLazyPagingItems(),
        listState = listState,
        compactState = compactState,
        gridState = gridState,
        searchQueryFlow = viewModel.searchQuery,
        sortTypeFlow = viewModel.sortType,
        sortDirectionFlow = viewModel.sortDirection,
        layoutTypeFlow = viewModel.layoutType,
        totalCountFlow = viewModel.totalCount,
        onUpdateSearchQuery = viewModel::updateSearchQuery,
        onUpdateSortType = viewModel::updateSortType,
        onUpdateSortDirection = viewModel::updateSortDirection,
        onUpdateLayoutType = viewModel::updateLayoutType,
        onSceneClick = onSceneClick,
        onPerformerClick = onPerformerClick,
        onStudioClick = onStudioClick,
        isTransparentTopBar = false,
        header = {}
    )
}

/**
 * A scaffold for the Scene List screen, providing search, sorting, and layout options.
 *
 * @param title The title to display in the top app bar.
 * @param scenes The paginated list of scenes.
 * @param searchQueryFlow Flow providing the current search query.
 * @param sortTypeFlow Flow providing the current sort type.
 * @param sortDirectionFlow Flow providing the current sort direction.
 * @param layoutTypeFlow Flow providing the current layout type.
 * @param totalCountFlow Flow providing the total count of scenes.
 * @param onUpdateSearchQuery Callback to update the search query.
 * @param onUpdateSortType Callback to update the sort type.
 * @param onUpdateSortDirection Callback to update the sort direction.
 * @param onUpdateLayoutType Callback to update the layout type.
 * @param onSceneClick Callback when a scene is clicked.
 * @param onPerformerClick Callback when a performer is clicked.
 * @param onStudioClick Callback when a studio is clicked.
 * @param onBackClick Callback when the back button is clicked.
 * @param isTransparentTopBar Whether the top app bar should have a transparent background initially.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SceneListScaffold(
    title: String,
    scenes: LazyPagingItems<Scene>,
    listState: LazyListState = rememberLazyListState(),
    compactState: LazyListState = rememberLazyListState(),
    gridState: LazyGridState = rememberLazyGridState(),
    searchQueryFlow: Flow<String>,
    sortTypeFlow: Flow<SceneSortType>,
    sortDirectionFlow: Flow<String>,
    layoutTypeFlow: Flow<SceneLayoutType>,
    totalCountFlow: Flow<Int?>,
    onUpdateSearchQuery: (String) -> Unit,
    onUpdateSortType: (SceneSortType) -> Unit,
    onUpdateSortDirection: (String) -> Unit,
    onUpdateLayoutType: (SceneLayoutType) -> Unit,
    onSceneClick: (String, Double?) -> Unit,
    onPerformerClick: (String) -> Unit,
    onStudioClick: (String) -> Unit,
    onBackClick: (() -> Unit)? = null,
    isTransparentTopBar: Boolean = false,
    scrollableHeader: @Composable () -> Unit = {},
    header: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.background
) {
    val searchQuery by searchQueryFlow.collectAsState(initial = "")
    val layoutType by layoutTypeFlow.collectAsState(initial = SceneLayoutType.LIST)
    val totalCount by totalCountFlow.collectAsState(initial = null)
    val isRefreshing = scenes.loadState.refresh is LoadState.Loading
    val isEmpty = scenes.itemCount == 0
    // Calculate top padding for content (status bar + top bar height)
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
        LocalScaffoldPadding.current.calculateBottomPadding()
    val topBarHeight = 64.dp
    val contentTopPadding = statusBarPadding + topBarHeight

    // Create scroll behavior.
    // Use pinned scroll behavior so it always updates its tonal background on scroll.
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(containerColor)
    ) {
        // Content Layer
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { scenes.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .hideBottomNavOnScroll()
        ) {
            if (isEmpty && isRefreshing) {
                SceneListLoadingState(
                    contentTopPadding = contentTopPadding,
                    contentBottomPadding = navigationBarPadding,
                    layoutType = layoutType,
                    clipToPadding = true,
                    scrollableHeader = scrollableHeader,
                    header = header,
                    containerColor = containerColor
                )
            } else {
                SceneListContent(
                    scenes = scenes,
                    layoutType = layoutType,
                    listState = listState,
                    compactState = compactState,
                    gridState = gridState,
                    onSceneClick = onSceneClick,
                    onPerformerClick = onPerformerClick,
                    onStudioClick = onStudioClick,
                    contentTopPadding = contentTopPadding,
                    contentBottomPadding = navigationBarPadding,
                    clipToPadding = true,
                    scrollableHeader = scrollableHeader,
                    header = header,
                    containerColor = containerColor
                )
            }
        }

        // Top App Bar Layer - Overlay
        val topAppBarColors = if (isTransparentTopBar) {
            TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = MaterialTheme.colorScheme.surface
            )
        } else {
            TopAppBarDefaults.topAppBarColors()
        }

        SortableListTopAppBar(
            title = title,
            count = totalCount,
            sortOptions = SceneSortType.entries.toList(),
            currentSortType = sortTypeFlow,
            currentSortDirection = sortDirectionFlow,
            defaultSortType = SceneSortType.DATE,
            onUpdateSortType = onUpdateSortType,
            onUpdateSortDirection = onUpdateSortDirection,
            searchQuery = searchQuery,
            onSearchQueryChange = onUpdateSearchQuery,
            searchPlaceholder = "Search scenes...",
            layoutOptions = SceneLayoutType.entries.toList(),
            currentLayoutType = layoutTypeFlow,
            defaultLayoutType = SceneLayoutType.LIST,
            onUpdateLayoutType = onUpdateLayoutType,
            onBackClick = onBackClick,
            scrollBehavior = scrollBehavior,
            colors = topAppBarColors,
            actions = actions
        )
    }
}
