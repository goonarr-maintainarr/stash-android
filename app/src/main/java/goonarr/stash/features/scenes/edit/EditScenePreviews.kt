package goonarr.stash.features.scenes.edit

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import goonarr.stash.LocalStashDynamicColors
import goonarr.stash.R
import goonarr.stash.StashTheme
import goonarr.stash.StashTokens
import goonarr.stash.core.model.StashID
import goonarr.stash.features.components.EditSection
import goonarr.stash.features.components.SharedEditScaffold
import goonarr.stash.features.components.SharedEditUiState
import goonarr.stash.features.components.TagPicker
import goonarr.stash.rememberSceneColors
import goonarr.stash.util.HeroAccentColor
import goonarr.stash.util.MockData

@Composable
fun EditScenePreviewContainer(content: @Composable () -> Unit) {
    val context = LocalContext.current
    var heroColor by remember { mutableStateOf<Color?>(null) }

    LaunchedEffect(Unit) {
        HeroAccentColor.extract(context, R.drawable.mock_hero_background)?.let {
            heroColor = it
        }
    }

    StashTheme(darkTheme = true) {
        val sceneColors = rememberSceneColors(heroColor = heroColor)
        CompositionLocalProvider(LocalStashDynamicColors provides sceneColors) {
            Surface(color = MaterialTheme.colorScheme.background) {
                content()
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 2000, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EditSceneScreenPreview() {
    val mockScene = MockData.scene
    val mockPerformers = MockData.performers
    val mockTags = MockData.tags

    EditScenePreviewContainer {
        SharedEditScaffold(
            title = "Edit Scene",
            dynamicColors = LocalStashDynamicColors.current,
            onBackClick = {},
            onSave = {},
            onRetry = {},
            uiState = SharedEditUiState.Success
        ) {
            EditSceneContent(
                title = mockScene.title ?: "",
                onTitleChange = {},
                details = mockScene.details ?: "",
                onDetailsChange = {},
                director = mockScene.director ?: "",
                onDirectorChange = {},
                code = mockScene.code ?: "",
                onCodeChange = {},
                url = mockScene.url ?: "",
                onUrlChange = {},
                rating = mockScene.rating100,
                onRatingChange = {},
                currentPerformers = mockScene.performers ?: emptyList(),
                onRemovePerformer = {},
                performerSearchText = "",
                onPerformerSearchTextChange = {},
                performerSearchResults = mockPerformers,
                isSearchingPerformers = false,
                onAddPerformer = {},
                currentTags = mockScene.tags ?: emptyList(),
                onRemoveTag = {},
                tagSearchText = "",
                onTagSearchTextChange = {},
                tagSearchResults = mockTags.take(5),
                isSearchingTags = false,
                onAddTag = {},
                stashIds = mockScene.stashIds ?: emptyList(),
                onRemoveStashId = {},
                oHistory = mockScene.oHistory ?: emptyList(),
                playHistory = mockScene.playHistory ?: emptyList(),
                onRemoveOHistory = {},
                onRemovePlayHistory = {},
                currentMarkers = mockScene.sceneMarkers ?: emptyList(),
                onDeleteMarker = {},
                screenshotUrl = R.drawable.mock_hero_background,
                onDeleteCover = {},
                getStreamUrl = { null }
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TagsSectionPreview() {
    EditScenePreviewContainer {
        Box(modifier = Modifier.padding(StashTokens.Spacing.CardPadding)) {
            EditSection(
                title = "TAGS",
                count = 3,
                dotColor = StashTheme.colors.tertiary,
                iconContent = {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.Label,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            ) {
                TagPicker(
                    selectedTags = MockData.tags.take(3),
                    onRemoveTag = {},
                    searchText = "Searching...",
                    onSearchTextChange = {},
                    searchResults = MockData.tags.drop(3).take(2),
                    isSearching = true,
                    onAddTag = {},
                    color = StashTheme.colors.tertiary
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MarkersSectionPreview() {
    EditScenePreviewContainer {
        Box(modifier = Modifier.padding(StashTokens.Spacing.CardPadding)) {
            MarkersSection(
                currentMarkers = MockData.scene.sceneMarkers ?: emptyList(),
                onDeleteMarker = {},
                getStreamUrl = { null }
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PerformerEditItemPreview() {
    EditScenePreviewContainer {
        Box(modifier = Modifier.padding(StashTokens.Spacing.CardPadding)) {
            PerformerEditItem(
                performer = MockData.performers.first(),
                onRemove = {}
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PerformerSearchItemPreview() {
    EditScenePreviewContainer {
        Box(modifier = Modifier.padding(StashTokens.Spacing.CardPadding)) {
            PerformerSearchItem(
                performer = MockData.performers.first(),
                onAdd = {},
                imagePath = R.drawable.mock_hero_background
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HistoryEditItemPreview() {
    EditScenePreviewContainer {
        Box(modifier = Modifier.padding(StashTokens.Spacing.CardPadding)) {
            HistoryEditItem(
                timestamp = "2025-12-23T10:05:00Z",
                onRemove = {}
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MarkerEditItemPreview() {
    EditScenePreviewContainer {
        Box(modifier = Modifier.padding(StashTokens.Spacing.CardPadding)) {
            MarkerEditItem(
                marker = MockData.scene.sceneMarkers?.first()!!,
                onDelete = {},
                getStreamUrl = { null }
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StashIdEditItemPreview() {
    EditScenePreviewContainer {
        Box(modifier = Modifier.padding(StashTokens.Spacing.CardPadding)) {
            StashIdEditItem(
                stashId = StashID(stashId = "12345678-1234-1234-1234-1234567890ab", endpoint = "https://stashdb.org/graphql"),
                onRemove = {}
            )
        }
    }
}
