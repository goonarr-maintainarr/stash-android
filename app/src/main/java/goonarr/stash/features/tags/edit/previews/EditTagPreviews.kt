package goonarr.stash.features.tags.edit.previews

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import goonarr.stash.LocalStashDynamicColors
import goonarr.stash.StashBlue
import goonarr.stash.StashTheme
import goonarr.stash.core.model.Tag
import goonarr.stash.features.components.SharedEditScaffold
import goonarr.stash.features.components.SharedEditUiState
import goonarr.stash.features.tags.edit.EditTagContent
import goonarr.stash.rememberSceneColors

@Preview(
    name = "Edit Tag - Success",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun EditTagScreenSuccessPreview() {
    val dynamicColors = rememberSceneColors(StashBlue)
    StashTheme(darkTheme = true) {
        CompositionLocalProvider(LocalStashDynamicColors provides dynamicColors) {
            SharedEditScaffold(
                title = "Edit Tag",
                dynamicColors = dynamicColors,
                onBackClick = {},
                onSave = {},
                onRetry = {},
                uiState = SharedEditUiState.Success
            ) {
                EditTagContent(
                    name = "Outdoor",
                    onNameChange = {},
                    details = "Scenes filmed outdoors.",
                    onDetailsChange = {},
                    aliases = listOf("Outside", "Nature"),
                    onAliasesChange = {},
                    favorite = true,
                    onFavoriteChange = {},
                    ignoreAutoTag = false,
                    onIgnoreAutoTagChange = {},
                    imageUrl = "http://example.com/image.jpg",
                    onDeleteImage = {},
                    parentTags = listOf(
                        Tag(id = "1", name = "Location", imagePath = null)
                    ),
                    subTags = listOf(
                        Tag(id = "2", name = "Beach", imagePath = null),
                        Tag(id = "3", name = "Forest", imagePath = null)
                    ),
                    parentTagSearchText = "",
                    onParentTagSearchTextChange = {},
                    parentTagSearchResults = emptyList(),
                    onAddParentTag = {},
                    onRemoveParentTag = {},
                    subTagSearchText = "",
                    onSubTagSearchTextChange = {},
                    subTagSearchResults = emptyList(),
                    onAddSubTag = {},
                    onRemoveSubTag = {},
                    isSearching = false,
                    stashBoxSearchText = "",
                    onStashBoxSearchTextChange = {},
                    stashBoxSearchResults = emptyList(),
                    onStashBoxTagSelected = {},
                    isSearchingStashBox = false,
                )
            }
        }
    }
}

@Preview(
    name = "Edit Tag - Loading",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun EditTagScreenLoadingPreview() {
    val dynamicColors = rememberSceneColors(StashBlue)
    StashTheme(darkTheme = true) {
        CompositionLocalProvider(LocalStashDynamicColors provides dynamicColors) {
            SharedEditScaffold(
                title = "Edit Tag",
                dynamicColors = dynamicColors,
                onBackClick = {},
                onSave = {},
                onRetry = {},
                uiState = SharedEditUiState.Loading
            ) {
                // Content hidden
            }
        }
    }
}

@Preview(
    name = "Edit Tag - Error",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun EditTagScreenErrorPreview() {
    val dynamicColors = rememberSceneColors(StashBlue)
    StashTheme(darkTheme = true) {
        CompositionLocalProvider(LocalStashDynamicColors provides dynamicColors) {
            SharedEditScaffold(
                title = "Edit Tag",
                dynamicColors = dynamicColors,
                onBackClick = {},
                onSave = {},
                onRetry = {},
                uiState = SharedEditUiState.Error("Failed to load tag details.")
            ) {
                // Content hidden
            }
        }
    }
}
