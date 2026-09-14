package goonarr.stash.features.scenes.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import goonarr.stash.core.model.Scene
import goonarr.stash.features.scenes.SceneLayoutType
import goonarr.stash.features.scenes.scenecard.SceneCard
import goonarr.stash.features.scenes.scenecard.SceneCompactRow
import goonarr.stash.features.scenes.scenecard.SceneGridCard

/**
 * The main content area for the Scene List, supporting different layout types.
 *
 * @param scenes The paginated list of scenes to display.
 * @param layoutType The current layout type (List, Grid, or Compact).
 * @param onSceneClick Callback when a scene is clicked.
 * @param onPerformerClick Callback when a performer is clicked.
 * @param onStudioClick Callback when a studio is clicked.
 * @param contentTopPadding The top padding to account for the top app bar.
 * @param clipToPadding Whether to clip content to the padding.
 */
@Composable
fun SceneListContent(
    scenes: LazyPagingItems<Scene>,
    layoutType: SceneLayoutType,
    listState: LazyListState,
    compactState: LazyListState,
    gridState: LazyGridState,
    onSceneClick: (String, Double?) -> Unit,
    onPerformerClick: (String) -> Unit = {},
    onStudioClick: (String) -> Unit = {},
    contentTopPadding: Dp = 80.dp,
    contentBottomPadding: Dp = 0.dp,
    clipToPadding: Boolean = true,
    scrollableHeader: @Composable () -> Unit = {},
    header: @Composable () -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.background
) {
    if (layoutType == SceneLayoutType.GRID) {
        SceneGrid(
            state = gridState,
            modifier = Modifier.fillMaxSize(),
            contentTopPadding = contentTopPadding,
            contentBottomPadding = contentBottomPadding,
            // Smaller cards for 2-column grid
            minSize = 180.dp,
            clipToPadding = clipToPadding
        ) {
            // Header item for grid (spanning full width)
            item(span = { GridItemSpan(maxLineSpan) }) {
                scrollableHeader()
            }

            stickyHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(containerColor)
                ) {
                    header()
                }
            }

            items(
                count = scenes.itemCount,
                key = scenes.itemKey { it.id }
            ) { index ->
                val scene = scenes[index]
                if (scene != null) {
                    SceneGridCard(
                        scene = scene,
                        onClick = { time -> onSceneClick(scene.id, time) },
                        onPerformerClick = onPerformerClick
                    )
                }
            }
        }
    } else {
        // Use separate states for List and Compact views
        val state = if (layoutType == SceneLayoutType.COMPACT) compactState else listState

        SceneList(
            state = state,
            modifier = Modifier.fillMaxSize(),
            contentTopPadding = contentTopPadding,
            contentBottomPadding = contentBottomPadding,
        ) {
            item {
                scrollableHeader()
            }

            stickyHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(containerColor)
                ) {
                    header()
                }
            }

            items(
                count = scenes.itemCount,
                key = scenes.itemKey { it.id }
            ) { index ->
                val scene = scenes[index]
                if (scene != null) {
                    if (layoutType == SceneLayoutType.COMPACT) {
                        SceneCompactRow(
                            scene = scene,
                            onClick = { time -> onSceneClick(scene.id, time) },
                            onPerformerClick = onPerformerClick,
                            onStudioClick = onStudioClick
                        )
                    } else {
                        SceneCard(
                            scene = scene,
                            onClick = { time -> onSceneClick(scene.id, time) },
                            onPerformerClick = onPerformerClick,
                            onStudioClick = onStudioClick
                        )
                    }
                }
            }
        }
    }
}

/**
 * A loading state for the Scene List, displaying shimmer cards.
 *
 * @param contentTopPadding The top padding to account for the top app bar.
 * @param layoutType The current layout type to determine the card style.
 * @param clipToPadding Whether to clip content to the padding.
 */
@Composable
fun SceneListLoadingState(
    contentTopPadding: Dp = 80.dp,
    contentBottomPadding: Dp = 0.dp,
    layoutType: SceneLayoutType = SceneLayoutType.LIST,
    clipToPadding: Boolean = true,
    scrollableHeader: @Composable () -> Unit = {},
    header: @Composable () -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.background
) {
    val isGrid = layoutType == SceneLayoutType.GRID

    if (isGrid) {
        SceneGrid(
            modifier = Modifier.fillMaxSize(),
            contentTopPadding = contentTopPadding,
            contentBottomPadding = contentBottomPadding,
            minSize = 180.dp,
            clipToPadding = clipToPadding
        ) {
            // Header item for grid (spanning full width)
            item(span = { GridItemSpan(maxLineSpan) }) {
                scrollableHeader()
            }

            stickyHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(containerColor)
                ) {
                    header()
                }
            }

            items(8) {
                SceneGridCard(
                    scene = Scene(
                        id = "",
                        files = emptyList(),
                        paths = null,
                        performers = emptyList(),
                        sceneMarkers = emptyList()
                    ),
                    onClick = {},
                    isLoading = true
                )
            }
        }
    } else {
        SceneList(
            modifier = Modifier.fillMaxSize(),
            contentTopPadding = contentTopPadding,
            contentBottomPadding = contentBottomPadding,
        ) {
            item {
                scrollableHeader()
            }

            stickyHeader {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(containerColor)
                ) {
                    header()
                }
            }

            items(6) {
                val mockScene = Scene(
                    id = "",
                    files = emptyList(),
                    paths = null,
                    performers = emptyList(),
                    sceneMarkers = emptyList()
                )
                if (layoutType == SceneLayoutType.COMPACT) {
                    SceneCompactRow(
                        scene = mockScene,
                        onClick = {},
                        isLoading = true
                    )
                } else {
                    SceneCard(
                        scene = mockScene,
                        onClick = {},
                        isLoading = true,
                        isCompact = false
                    )
                }
            }
        }
    }
}

/**
 * A grid-based layout for scene items.
 */
@Composable
fun SceneGrid(
    modifier: Modifier = Modifier,
    state: LazyGridState = rememberLazyGridState(),
    contentTopPadding: Dp = 80.dp,
    contentBottomPadding: Dp = 0.dp,
    minSize: Dp = 350.dp,
    clipToPadding: Boolean = true,
    content: LazyGridScope.() -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = minSize),
        state = state,
        contentPadding = PaddingValues(
            top = contentTopPadding,
            start = 16.dp,
            end = 16.dp,
            bottom = contentBottomPadding + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize(),
        content = content
    )
}

/**
 * A list-based layout for scene items.
 */
@Composable
fun SceneList(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentTopPadding: Dp = 80.dp,
    contentBottomPadding: Dp = 0.dp,
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        state = state,
        contentPadding = PaddingValues(
            top = contentTopPadding,
            start = 16.dp,
            end = 16.dp,
            bottom = contentBottomPadding + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxSize(),
        content = content
    )
}
