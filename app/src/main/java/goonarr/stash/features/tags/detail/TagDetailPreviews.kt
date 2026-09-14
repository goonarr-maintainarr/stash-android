package goonarr.stash.features.tags.detail

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import goonarr.stash.StashTheme
import goonarr.stash.util.MockData

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    heightDp = 800
)
@Composable
fun TagDetailPreview() {
    val tag = MockData.tag.copy(
        name = "Outdoor",
        sceneCount = 153,
        sceneMarkerCount = 12,
        performerCount = 8,
        // Mock counts for new stats
        description = "Scenes filmed outdoors in natural light. This tag covers a wide range of locations.",
        aliases = listOf("Nature", "Outside")
    )

    val scenes = MockData.scenes
    val markers = MockData.scene.sceneMarkers ?: emptyList()
    val performers = MockData.performers
    val studios = MockData.studios

    StashTheme {
        TagDetailContent(
            tag = tag,
            isFavorite = true,
            isFollowed = true,
            scenes = scenes,
            markers = markers,
            selectedTab = TagDetailTab.SCENES,
            onTabSelected = {},
            onToggleFavorite = {},
            onToggleFollow = {},
            onSceneClick = { _, _ -> },
            onPerformerClick = {},
            onStudioClick = {},
            onEditClick = {},
            onDeleteTag = {},
            onBackClick = {},
            onGetStreamUrl = { it },
            onTagClick = {},
            containerColor = MaterialTheme.colorScheme.background,
            loadMoreScenes = {},
            loadMoreMarkers = {},
            hasMoreScenes = true,
            isLoadingMoreScenes = false,
            hasMoreMarkers = false,
            isLoadingMoreMarkers = false,
            loadMorePerformers = {},
            loadMoreStudios = {},
            performers = performers,
            hasMorePerformers = false,
            isLoadingMorePerformers = false,
            studios = studios,
            hasMoreStudios = false,
            isLoadingMoreStudios = false,
            studioCount = studios.size
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    heightDp = 800
)
@Composable
fun TagDetailMarkersPreview() {
    val tag = MockData.previewTag
    val markers = MockData.scene.sceneMarkers ?: emptyList()

    StashTheme {
        TagDetailContent(
            tag = tag,
            isFavorite = false,
            isFollowed = false,
            scenes = emptyList(),
            markers = markers,
            selectedTab = TagDetailTab.MARKERS,
            onTabSelected = {},
            onToggleFavorite = {},
            onToggleFollow = {},
            onSceneClick = { _, _ -> },
            onPerformerClick = {},
            onStudioClick = {},
            onEditClick = {},
            onDeleteTag = {},
            onBackClick = {},
            onGetStreamUrl = { it },
            onTagClick = {},
            containerColor = MaterialTheme.colorScheme.background,
            loadMoreScenes = {},
            loadMoreMarkers = {},
            hasMoreScenes = false,
            isLoadingMoreScenes = false,
            hasMoreMarkers = true,
            isLoadingMoreMarkers = false,
            loadMorePerformers = {},
            loadMoreStudios = {},
            performers = emptyList(),
            hasMorePerformers = false,
            isLoadingMorePerformers = false,
            studios = emptyList(),
            hasMoreStudios = false,
            isLoadingMoreStudios = false,
            studioCount = 5
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    heightDp = 800
)
@Composable
fun TagDetailPerformersPreview() {
    val tag = MockData.previewTag
    val performers = MockData.performers

    StashTheme {
        TagDetailContent(
            tag = tag,
            isFavorite = false,
            isFollowed = false,
            scenes = emptyList(),
            markers = emptyList(),
            selectedTab = TagDetailTab.PERFORMERS,
            onTabSelected = {},
            onToggleFavorite = {},
            onToggleFollow = {},
            onSceneClick = { _, _ -> },
            onPerformerClick = {},
            onStudioClick = {},
            onEditClick = {},
            onDeleteTag = {},
            onBackClick = {},
            onGetStreamUrl = { it },
            onTagClick = {},
            containerColor = MaterialTheme.colorScheme.background,
            loadMoreScenes = {},
            loadMoreMarkers = {},
            hasMoreScenes = false,
            isLoadingMoreScenes = false,
            hasMoreMarkers = false,
            isLoadingMoreMarkers = false,
            loadMorePerformers = {},
            loadMoreStudios = {},
            performers = performers,
            hasMorePerformers = true,
            isLoadingMorePerformers = false,
            studios = emptyList(),
            hasMoreStudios = false,
            isLoadingMoreStudios = false,
            studioCount = 5
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    heightDp = 800
)
@Composable
fun TagDetailStudiosPreview() {
    val tag = MockData.previewTag
    val studios = MockData.studios

    StashTheme {
        TagDetailContent(
            tag = tag,
            isFavorite = false,
            isFollowed = false,
            scenes = emptyList(),
            markers = emptyList(),
            selectedTab = TagDetailTab.STUDIOS,
            onTabSelected = {},
            onToggleFavorite = {},
            onToggleFollow = {},
            onSceneClick = { _, _ -> },
            onPerformerClick = {},
            onStudioClick = {},
            onEditClick = {},
            onDeleteTag = {},
            onBackClick = {},
            onGetStreamUrl = { it },
            onTagClick = {},
            containerColor = MaterialTheme.colorScheme.background,
            loadMoreScenes = {},
            loadMoreMarkers = {},
            hasMoreScenes = false,
            isLoadingMoreScenes = false,
            hasMoreMarkers = false,
            isLoadingMoreMarkers = false,
            loadMorePerformers = {},
            loadMoreStudios = {},
            performers = emptyList(),
            hasMorePerformers = false,
            isLoadingMorePerformers = false,
            studios = studios,
            hasMoreStudios = true,
            isLoadingMoreStudios = false,
            studioCount = studios.size
        )
    }
}
