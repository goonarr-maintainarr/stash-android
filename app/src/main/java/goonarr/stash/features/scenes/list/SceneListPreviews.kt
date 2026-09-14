package goonarr.stash.features.scenes.list

import android.content.res.Configuration
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import goonarr.stash.StashTheme
import goonarr.stash.core.model.Scene
import goonarr.stash.features.scenes.SceneLayoutType
import goonarr.stash.features.scenes.SceneSortType
import goonarr.stash.util.MockData
import kotlinx.coroutines.flow.flowOf

@Preview(showBackground = true, heightDp = 1200, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SceneListFullPreview() {
    val scenes = MockData.scenes
    val pagingDataFlow = flowOf(PagingData.from(scenes))
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SceneListContent(
                scenes = pagingDataFlow.collectAsLazyPagingItems(),
                layoutType = SceneLayoutType.LIST,
                listState = rememberLazyListState(),
                compactState = rememberLazyListState(),
                gridState = rememberLazyGridState(),
                onSceneClick = { _, _ -> },
                onPerformerClick = {}
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 1200, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SceneListGridPreview() {
    val scenes = MockData.scenes
    val pagingDataFlow = flowOf(PagingData.from(scenes))
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SceneListContent(
                scenes = pagingDataFlow.collectAsLazyPagingItems(),
                layoutType = SceneLayoutType.GRID,
                listState = rememberLazyListState(),
                compactState = rememberLazyListState(),
                gridState = rememberLazyGridState(),
                onSceneClick = { _, _ -> },
                onPerformerClick = {}
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 1200, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SceneListCompactPreview() {
    val scenes = MockData.scenes
    val pagingDataFlow = flowOf(PagingData.from(scenes))
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SceneListContent(
                scenes = pagingDataFlow.collectAsLazyPagingItems(),
                layoutType = SceneLayoutType.COMPACT,
                listState = rememberLazyListState(),
                compactState = rememberLazyListState(),
                gridState = rememberLazyGridState(),
                onSceneClick = { _, _ -> },
                onPerformerClick = {}
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SceneListLoadingListPreview() {
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SceneListLoadingState(layoutType = SceneLayoutType.LIST, contentTopPadding = 16.dp)
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SceneListLoadingGridPreview() {
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SceneListLoadingState(layoutType = SceneLayoutType.GRID, contentTopPadding = 16.dp)
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SceneListLoadingCompactPreview() {
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SceneListLoadingState(layoutType = SceneLayoutType.COMPACT, contentTopPadding = 16.dp)
        }
    }
}

@Preview(showBackground = true, heightDp = 1200, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SceneDiscoveryScreenPreview() {
    val scenes = MockData.scenes
    val pagingDataFlow = flowOf(PagingData.from(scenes))
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SceneListScaffold(
                title = "Recently Added",
                scenes = pagingDataFlow.collectAsLazyPagingItems(),
                listState = rememberLazyListState(),
                compactState = rememberLazyListState(),
                gridState = rememberLazyGridState(),
                searchQueryFlow = flowOf(""),
                sortTypeFlow = flowOf(SceneSortType.DATE),
                sortDirectionFlow = flowOf("DESC"),
                layoutTypeFlow = flowOf(SceneLayoutType.LIST),
                totalCountFlow = flowOf(142),
                onUpdateSearchQuery = {},
                onUpdateSortType = {},
                onUpdateSortDirection = {},
                onUpdateLayoutType = {},
                onPerformerClick = {},
                onStudioClick = {},
                onBackClick = {},
                onSceneClick = { _, _ -> },
                isTransparentTopBar = false
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SceneDiscoveryLoadingPreview() {
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SceneListScaffold(
                title = "Top Rated",
                scenes = flowOf(PagingData.empty<Scene>()).collectAsLazyPagingItems(),
                listState = rememberLazyListState(),
                compactState = rememberLazyListState(),
                gridState = rememberLazyGridState(),
                searchQueryFlow = flowOf(""),
                sortTypeFlow = flowOf(SceneSortType.RATING),
                sortDirectionFlow = flowOf("DESC"),
                layoutTypeFlow = flowOf(SceneLayoutType.GRID),
                totalCountFlow = flowOf(null),
                onUpdateSearchQuery = {},
                onUpdateSortType = {},
                onUpdateSortDirection = {},
                onUpdateLayoutType = {},
                onPerformerClick = {},
                onStudioClick = {},
                onBackClick = {},
                onSceneClick = { _, _ -> },
                isTransparentTopBar = false
            )
        }
    }
}
