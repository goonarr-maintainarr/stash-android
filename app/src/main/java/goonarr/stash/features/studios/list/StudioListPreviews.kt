package goonarr.stash.features.studios.list

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import goonarr.stash.LocalStashDynamicColors
import goonarr.stash.StashBlue
import goonarr.stash.StashTheme
import goonarr.stash.rememberSceneColors
import goonarr.stash.util.MockData

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StudioListContentPreview() {
    StashTheme(darkTheme = true) {
        val studioColors = rememberSceneColors(StashBlue)
        CompositionLocalProvider(LocalStashDynamicColors provides studioColors) {
            Surface(color = MaterialTheme.colorScheme.background) {
                StudioListContent(
                    state = StudioListState.Content(MockData.studios, totalCount = MockData.studios.size),
                    onStudioClick = {},
                    onRefresh = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StudioListLoadingPreview() {
    StashTheme(darkTheme = true) {
        val studioColors = rememberSceneColors(StashBlue)
        CompositionLocalProvider(LocalStashDynamicColors provides studioColors) {
            Surface(color = MaterialTheme.colorScheme.background) {
                StudioListContent(
                    state = StudioListState.Loading,
                    onStudioClick = {},
                    onRefresh = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StudioListEmptyPreview() {
    StashTheme(darkTheme = true) {
        val studioColors = rememberSceneColors(StashBlue)
        CompositionLocalProvider(LocalStashDynamicColors provides studioColors) {
            Surface(color = MaterialTheme.colorScheme.background) {
                StudioListContent(
                    state = StudioListState.Empty,
                    onStudioClick = {},
                    onRefresh = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StudioListErrorPreview() {
    StashTheme(darkTheme = true) {
        val studioColors = rememberSceneColors(StashBlue)
        CompositionLocalProvider(LocalStashDynamicColors provides studioColors) {
            Surface(color = MaterialTheme.colorScheme.background) {
                StudioListContent(
                    state = StudioListState.Error("Failed to fetch studios"),
                    onStudioClick = {},
                    onRefresh = {}
                )
            }
        }
    }
}
