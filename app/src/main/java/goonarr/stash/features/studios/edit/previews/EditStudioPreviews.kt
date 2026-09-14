package goonarr.stash.features.studios.edit.previews

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import goonarr.stash.LocalStashDynamicColors
import goonarr.stash.StashBlue
import goonarr.stash.StashTheme
import goonarr.stash.features.components.SharedEditScaffold
import goonarr.stash.features.components.SharedEditUiState
import goonarr.stash.features.studios.edit.EditStudioContent
import goonarr.stash.features.studios.edit.EditStudioScreen
import goonarr.stash.rememberSceneColors
import goonarr.stash.util.MockData

/**
 * Previews for the [EditStudioScreen] in Success state.
 */
@Preview(
    name = "Edit Studio - Success",
    heightDp = 1400,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun EditStudioScreenSuccessPreview() {
    val dynamicColors = rememberSceneColors(StashBlue)
    StashTheme(darkTheme = true) {
        CompositionLocalProvider(LocalStashDynamicColors provides dynamicColors) {
            SharedEditScaffold(
                title = "Edit Studio",
                dynamicColors = dynamicColors,
                onBackClick = {},
                onSave = {},
                onRetry = {},
                uiState = SharedEditUiState.Success
            ) {
                EditStudioContent(
                    name = MockData.fullStudio.name,
                    onNameChange = {},
                    details = MockData.fullStudio.details ?: "",
                    onDetailsChange = {},
                    rating = MockData.fullStudio.rating100,
                    onRatingChange = {},
                    urls = MockData.fullStudio.urls ?: emptyList(),
                    onUrlsChange = {},
                    aliases = MockData.fullStudio.aliases ?: emptyList(),
                    onAliasesChange = {},
                    favorite = MockData.fullStudio.favorite == true,
                    onFavoriteChange = {},
                    ignoreAutoTag = MockData.fullStudio.ignoreAutoTag == true,
                    onIgnoreAutoTagChange = {},
                    imageUrl = MockData.fullStudio.imagePath,
                    onDeleteImage = {},
                    tags = MockData.tags.take(3),
                    onRemoveTag = {},
                    tagSearchText = "",
                    onTagSearchTextChange = {},
                    tagSearchResults = emptyList(),
                    onAddTag = {},
                    isSearchingTags = false
                )
            }
        }
    }
}

/**
 * Previews for the [EditStudioScreen] in Loading state.
 */
@Preview(
    name = "Edit Studio - Loading",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun EditStudioScreenLoadingPreview() {
    val dynamicColors = rememberSceneColors(StashBlue)
    StashTheme(darkTheme = true) {
        CompositionLocalProvider(LocalStashDynamicColors provides dynamicColors) {
            SharedEditScaffold(
                title = "Edit Studio",
                dynamicColors = dynamicColors,
                onBackClick = {},
                onSave = {},
                onRetry = {},
                uiState = SharedEditUiState.Loading
            ) {
                // Content not shown in Loading state
            }
        }
    }
}

/**
 * Previews for the [EditStudioScreen] in Saving state.
 */
@Preview(
    name = "Edit Studio - Saving",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun EditStudioScreenSavingPreview() {
    val dynamicColors = rememberSceneColors(StashBlue)
    StashTheme(darkTheme = true) {
        CompositionLocalProvider(LocalStashDynamicColors provides dynamicColors) {
            SharedEditScaffold(
                title = "Edit Studio",
                dynamicColors = dynamicColors,
                onBackClick = {},
                onSave = {},
                onRetry = {},
                uiState = SharedEditUiState.Saving
            ) {
                EditStudioContent(
                    name = MockData.fullStudio.name,
                    onNameChange = {},
                    details = MockData.fullStudio.details ?: "",
                    onDetailsChange = {},
                    rating = MockData.fullStudio.rating100,
                    onRatingChange = {},
                    urls = MockData.fullStudio.urls ?: emptyList(),
                    onUrlsChange = {},
                    aliases = MockData.fullStudio.aliases ?: emptyList(),
                    onAliasesChange = {},
                    favorite = MockData.fullStudio.favorite == true,
                    onFavoriteChange = {},
                    ignoreAutoTag = MockData.fullStudio.ignoreAutoTag == true,
                    onIgnoreAutoTagChange = {},
                    imageUrl = MockData.fullStudio.imagePath,
                    onDeleteImage = {},
                    tags = MockData.tags.take(3),
                    onRemoveTag = {},
                    tagSearchText = "",
                    onTagSearchTextChange = {},
                    tagSearchResults = emptyList(),
                    onAddTag = {},
                    isSearchingTags = false
                )
            }
        }
    }
}

/**
 * Previews for the [EditStudioScreen] in Error state.
 */
@Preview(
    name = "Edit Studio - Error",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun EditStudioScreenErrorPreview() {
    val dynamicColors = rememberSceneColors(StashBlue)
    StashTheme(darkTheme = true) {
        CompositionLocalProvider(LocalStashDynamicColors provides dynamicColors) {
            SharedEditScaffold(
                title = "Edit Studio",
                dynamicColors = dynamicColors,
                onBackClick = {},
                onSave = {},
                onRetry = {},
                uiState = SharedEditUiState.Error("Server returned a 500 error while saving.")
            ) {
                // Content not shown in Error state
            }
        }
    }
}
