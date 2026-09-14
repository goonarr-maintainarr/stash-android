package goonarr.stash.features.home

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import goonarr.stash.StashTheme
import goonarr.stash.util.MockData

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    heightDp = 2000
)
@Composable
fun HomeScreenPreview() {
    val mockScenes = MockData.scenes
    val successState = HomeUiState.Success(
        sections = listOf(
            HomeSectionData.SceneSection(
                id = "1",
                title = "Random",
                scenes = mockScenes.shuffled().take(5),
                sort = "random"
            ),
            HomeSectionData.SceneSection(
                id = "2",
                title = "Recently Released",
                scenes = mockScenes.take(5),
                sort = "date"
            ),
            HomeSectionData.SceneSection(
                id = "3",
                title = "Tag: Test",
                scenes = mockScenes.take(5),
                sort = "tag_id:test"
            )
        )
    )

    StashTheme {
        HomeScreenContent(
            uiState = successState,
            onRefresh = {},
            onPerformerClick = {},
            onTagsClick = {},
            onSeeAllClick = { _, _, _, _, _ -> },
            onConfigureClick = {},
            onStatsClick = {},
            onBlurToggle = {},
            onSceneClick = { _, _ -> },
            onStudioClick = {}
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    heightDp = 2000
)
@Composable
fun HomeScreenLoadingPreview() {
    StashTheme {
        HomeScreenContent(
            uiState = HomeUiState.Loading,
            onRefresh = {},
            onPerformerClick = {},
            onTagsClick = {},
            onSeeAllClick = { _, _, _, _, _ -> },
            onConfigureClick = {},
            onStatsClick = {},
            onBlurToggle = {},
            onSceneClick = { _, _ -> },
            onStudioClick = {}
        )
    }
}
