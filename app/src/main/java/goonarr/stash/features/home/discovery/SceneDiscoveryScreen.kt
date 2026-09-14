package goonarr.stash.features.home.discovery

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import goonarr.stash.features.scenes.list.SceneListScaffold

/**
 * A specialized version of the Scene List screen used for "Discovery" or filtered views.
 *
 * @param viewModel The ViewModel providing the scene data and state.
 * @param title The title for this discovery view.
 * @param onBackClick Callback when the back button is clicked.
 * @param onSceneClick Callback when a scene is clicked.
 * @param onPerformerClick Callback when a performer is clicked.
 * @param onStudioClick Callback when a studio is clicked.
 */
@Composable
fun SceneDiscoveryScreen(
    viewModel: SceneDiscoveryViewModel = hiltViewModel(),
    title: String,
    onBackClick: () -> Unit,
    onSceneClick: (String, Double?) -> Unit,
    onPerformerClick: (String) -> Unit = {},
    onStudioClick: (String) -> Unit = {},
) {
    SceneListScaffold(
        title = title,
        scenes = viewModel.scenes.collectAsLazyPagingItems(),
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
        onBackClick = onBackClick
    )
}
