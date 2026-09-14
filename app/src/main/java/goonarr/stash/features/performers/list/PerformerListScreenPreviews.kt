package goonarr.stash.features.performers.list

import android.content.res.Configuration
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import goonarr.stash.StashTheme
import goonarr.stash.features.performers.PerformerViewType
import goonarr.stash.util.MockData
import kotlinx.coroutines.flow.flowOf

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PerformerListScreenGridLargePreview() {
    val performers = MockData.performers
    val pagingDataFlow = flowOf(PagingData.from(performers))
    StashTheme(darkTheme = true) {
        SharedTransitionLayout {
            androidx.compose.animation.AnimatedVisibility(visible = true) {
                PerformerListContent(
                    pagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    viewType = PerformerViewType.GRID_LARGE,
                    listState = rememberLazyListState(),
                    gridLargeState = rememberLazyGridState(),
                    gridSmallState = rememberLazyGridState(),
                    onPerformerClick = {},
                    animatedVisibilityScope = this,
                    sharedTransitionScope = this@SharedTransitionLayout
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PerformerListScreenGridSmallPreview() {
    val performers = MockData.performers
    val pagingDataFlow = flowOf(PagingData.from(performers))
    StashTheme(darkTheme = true) {
        SharedTransitionLayout {
            androidx.compose.animation.AnimatedVisibility(visible = true) {
                PerformerListContent(
                    pagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    viewType = PerformerViewType.GRID_SMALL,
                    listState = rememberLazyListState(),
                    gridLargeState = rememberLazyGridState(),
                    gridSmallState = rememberLazyGridState(),
                    onPerformerClick = {},
                    animatedVisibilityScope = this,
                    sharedTransitionScope = this@SharedTransitionLayout
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PerformerListScreenListPreview() {
    val performers = MockData.performers
    val pagingDataFlow = flowOf(PagingData.from(performers))
    StashTheme(darkTheme = true) {
        SharedTransitionLayout {
            androidx.compose.animation.AnimatedVisibility(visible = true) {
                PerformerListContent(
                    pagingItems = pagingDataFlow.collectAsLazyPagingItems(),
                    viewType = PerformerViewType.LIST,
                    listState = rememberLazyListState(),
                    gridLargeState = rememberLazyGridState(),
                    gridSmallState = rememberLazyGridState(),
                    onPerformerClick = {},
                    animatedVisibilityScope = this,
                    sharedTransitionScope = this@SharedTransitionLayout
                )
            }
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun PerformerListScreenLoadingPreview() {
    StashTheme(darkTheme = true) {
        PerformerListLoadingState()
    }
}
