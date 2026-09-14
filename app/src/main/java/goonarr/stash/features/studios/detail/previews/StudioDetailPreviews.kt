package goonarr.stash.features.studios.detail.previews

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import goonarr.stash.LocalStashDynamicColors
import goonarr.stash.StashBlue
import goonarr.stash.StashTheme
import goonarr.stash.features.studios.detail.DeleteStudioSheetContent
import goonarr.stash.features.studios.detail.StudioDetailContent
import goonarr.stash.features.studios.detail.StudioDetailTab
import goonarr.stash.rememberSceneColors
import goonarr.stash.util.MockData

/**
 * Previews for the [StudioDetailContent] in various tab states.
 */
@Preview(
    name = "Studio Overview Tab",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun StudioDetailOverviewPreview() {
    StashTheme(darkTheme = true) {
        Surface(color = Color.Black) {
            StudioDetailContent(
                studio = MockData.fullStudio,
                scenes = MockData.scenes,
                performers = MockData.performers,
                selectedTab = StudioDetailTab.OVERVIEW,
                onTabSelected = {},
                onSceneClick = { _, _ -> },
                onPerformerClick = {},
                onStudioClick = {},
                loadMoreScenes = {},
                loadMorePerformers = {},
                hasMoreScenes = false,
                isLoadingMoreScenes = false,
                hasMorePerformers = false,
                isLoadingMorePerformers = false,
                onRatingChanged = {},
                onTagClick = TODO(),
                onFavoriteClick = TODO()
            )
        }
    }
}

@Preview(
    name = "Studio Scenes Tab",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun StudioDetailScenesPreview() {
    StashTheme(darkTheme = true) {
        Surface(color = Color.Black) {
            StudioDetailContent(
                studio = MockData.fullStudio,
                scenes = MockData.scenes,
                performers = MockData.performers,
                selectedTab = StudioDetailTab.SCENES,
                onTabSelected = {},
                onSceneClick = { _, _ -> },
                onPerformerClick = {},
                onStudioClick = {},
                loadMoreScenes = {},
                loadMorePerformers = {},
                hasMoreScenes = true,
                isLoadingMoreScenes = false,
                hasMorePerformers = false,
                isLoadingMorePerformers = false,
                onRatingChanged = {},
                onTagClick = TODO(),
                onFavoriteClick = TODO()
            )
        }
    }
}

@Preview(
    name = "Studio Performers Tab",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun StudioDetailPerformersPreview() {
    StashTheme(darkTheme = true) {
        Surface(color = Color.Black) {
            StudioDetailContent(
                studio = MockData.fullStudio,
                scenes = MockData.scenes,
                performers = MockData.performers,
                selectedTab = StudioDetailTab.PERFORMERS,
                onTabSelected = {},
                onSceneClick = { _, _ -> },
                onPerformerClick = {},
                onStudioClick = {},
                loadMoreScenes = {},
                loadMorePerformers = {},
                hasMoreScenes = false,
                isLoadingMoreScenes = false,
                hasMorePerformers = true,
                isLoadingMorePerformers = false,

                onRatingChanged = {},
                onTagClick = TODO(),
                onFavoriteClick = TODO()
            )
        }
    }
}

@Preview(name = "Delete Studio Sheet", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DeleteStudioSheetPreview() {
    StashTheme(darkTheme = true) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.large
        ) {
            DeleteStudioSheetContent(
                studioName = "Nebula Productions",
                onDelete = {},
                onCancel = {}
            )
        }
    }
}

@Preview(name = "Studio Detail - Loading", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StudioDetailLoadingPreview() {
    StashTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = StashBlue)
            }
        }
    }
}

@Preview(name = "Studio Detail - Error", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StudioDetailErrorPreview() {
    StashTheme(darkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Error: Failed to fetch studio details",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Preview(name = "Studio Detail - Empty", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StudioDetailEmptyPreview() {
    val dynamicColors = rememberSceneColors(StashBlue)
    StashTheme(darkTheme = true) {
        CompositionLocalProvider(LocalStashDynamicColors provides dynamicColors) {
            Surface(modifier = Modifier.fillMaxSize(), color = dynamicColors.primary.copy(alpha = 0.05f)) {
                StudioDetailContent(
                    studio = MockData.fullStudio.copy(name = "Empty Studio", details = null),
                    scenes = emptyList(),
                    performers = emptyList(),
                    selectedTab = StudioDetailTab.OVERVIEW,
                    onTabSelected = {},
                    onSceneClick = { _, _ -> },
                    onPerformerClick = {},
                    onStudioClick = {},
                    loadMoreScenes = {},
                    loadMorePerformers = {},
                    hasMoreScenes = false,
                    isLoadingMoreScenes = false,
                    hasMorePerformers = false,
                    isLoadingMorePerformers = false,
                    onRatingChanged = {},
                    onTagClick = TODO(),
                    onFavoriteClick = TODO()
                )
            }
        }
    }
}
