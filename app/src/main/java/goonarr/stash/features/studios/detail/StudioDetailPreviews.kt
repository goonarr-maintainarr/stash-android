package goonarr.stash.features.studios.detail

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import goonarr.stash.StashTheme
import goonarr.stash.rememberSceneColors
import goonarr.stash.util.MockData

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StudioDetailOverviewPreview() {
    StashTheme(darkTheme = true) {
        val studioColors = rememberSceneColors(goonarr.stash.StashBlue)
        androidx.compose.runtime.CompositionLocalProvider(goonarr.stash.LocalStashDynamicColors provides studioColors) {
            Surface(color = MaterialTheme.colorScheme.background) {
                StudioDetailContent(
                    studio = MockData.studio,
                    scenes = emptyList(),
                    performers = emptyList(),
                    selectedTab = StudioDetailTab.OVERVIEW,
                    onTabSelected = {},
                    onSceneClick = { _, _ -> },
                    onTagClick = {},
                    onPerformerClick = {},
                    onStudioClick = {},
                    loadMoreScenes = {},
                    loadMorePerformers = {},
                    hasMoreScenes = false,
                    isLoadingMoreScenes = false,
                    hasMorePerformers = false,
                    isLoadingMorePerformers = false,

                    onRatingChanged = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StudioDetailScenesPreview() {
    StashTheme(darkTheme = true) {
        val studioColors = rememberSceneColors(goonarr.stash.StashBlue)
        androidx.compose.runtime.CompositionLocalProvider(goonarr.stash.LocalStashDynamicColors provides studioColors) {
            Surface(color = MaterialTheme.colorScheme.background) {
                StudioDetailContent(
                    studio = MockData.studio,
                    scenes = MockData.scenes,
                    performers = emptyList(),
                    selectedTab = StudioDetailTab.SCENES,
                    onTabSelected = {},
                    onSceneClick = { _, _ -> },
                    onTagClick = {},
                    onPerformerClick = {},
                    onStudioClick = {},
                    loadMoreScenes = {},
                    loadMorePerformers = {},
                    hasMoreScenes = true,
                    isLoadingMoreScenes = false,
                    hasMorePerformers = false,
                    isLoadingMorePerformers = false,

                    onRatingChanged = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StudioDetailPerformersPreview() {
    StashTheme(darkTheme = true) {
        val studioColors = rememberSceneColors(goonarr.stash.StashBlue)
        androidx.compose.runtime.CompositionLocalProvider(goonarr.stash.LocalStashDynamicColors provides studioColors) {
            Surface(color = MaterialTheme.colorScheme.background) {
                StudioDetailContent(
                    studio = MockData.studio,
                    scenes = emptyList(),
                    performers = MockData.performers,
                    selectedTab = StudioDetailTab.PERFORMERS,
                    onTabSelected = {},
                    onSceneClick = { _, _ -> },
                    onTagClick = {},
                    onPerformerClick = {},
                    onStudioClick = {},
                    loadMoreScenes = {},
                    loadMorePerformers = {},
                    hasMoreScenes = false,
                    isLoadingMoreScenes = false,
                    hasMorePerformers = false,
                    isLoadingMorePerformers = false,

                    onRatingChanged = {}
                )
            }
        }
    }
}
