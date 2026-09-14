package goonarr.stash.features.studios.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import goonarr.stash.LocalStashDynamicColors
import goonarr.stash.StashBlue
import goonarr.stash.StashTokens
import goonarr.stash.core.model.Tag
import goonarr.stash.features.components.EditSection
import goonarr.stash.features.components.EditSwitchRow
import goonarr.stash.features.components.EditTextField
import goonarr.stash.features.components.OutlinedCard
import goonarr.stash.features.components.RatingBar
import goonarr.stash.features.components.SharedEditScaffold
import goonarr.stash.features.components.SharedEditUiState
import goonarr.stash.features.components.StaggeredEditList
import goonarr.stash.features.components.StringListEditor
import goonarr.stash.features.components.TagPicker
import goonarr.stash.features.components.ThumbnailSection
import goonarr.stash.rememberSceneColors
import timber.log.Timber

/**
 * Screen for editing studio information.
 *
 * Displays a form with all editable studio fields including name, URLs, details,
 * aliases, and settings. Uses StashTheme primary blue as the background color
 * without hero image extraction.
 *
 * @param studioId The ID of the studio to edit.
 * @param onBackClick Callback when back/cancel button is pressed.
 * @param onSaveSuccess Callback when changes are successfully saved.
 * @param viewModel The ViewModel managing edit state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditStudioScreen(
    studioId: String,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: EditStudioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(studioId) {
        Timber.d("📝 EditStudioScreen: Loading studio $studioId")
        viewModel.loadStudio(studioId)
    }

    LaunchedEffect(uiState) {
        if (uiState is SharedEditUiState.Saved) {
            Timber.d("✅ EditStudioScreen: Save successful, navigating back")
            onSaveSuccess()
        }
    }

    // Use StashBlue as the primary color for the edit screen
    val dynamicColors = rememberSceneColors(StashBlue)

    CompositionLocalProvider(LocalStashDynamicColors provides dynamicColors) {
        SharedEditScaffold(
            title = "Edit Studio",
            onBackClick = onBackClick,
            onSave = { viewModel.save() },
            onRetry = { viewModel.clearError() },
            uiState = uiState,
            dynamicColors = dynamicColors
        ) {
            val name by viewModel.name.collectAsState()
            val details by viewModel.details.collectAsState()
            val rating by viewModel.rating.collectAsState()
            val urls by viewModel.urls.collectAsState()
            val aliases by viewModel.aliases.collectAsState()
            val favorite by viewModel.favorite.collectAsState()
            val ignoreAutoTag by viewModel.ignoreAutoTag.collectAsState()
            val imageUrl by viewModel.imageUrl.collectAsState()

            val tagSearchText by viewModel.tagSearchText.collectAsState()
            val tagSearchResults by viewModel.tagSearchResults.collectAsState()
            val isSearchingTags by viewModel.isSearchingTags.collectAsState()
            val tags by viewModel.tags.collectAsState()

            EditStudioContent(
                name = name,
                onNameChange = { viewModel.name.value = it },
                details = details,
                onDetailsChange = { viewModel.details.value = it },
                rating = rating,
                onRatingChange = { viewModel.rating.value = it },
                urls = urls,
                onUrlsChange = { viewModel.urls.value = it },
                aliases = aliases,
                onAliasesChange = { viewModel.aliases.value = it },
                tags = tags,
                onRemoveTag = { viewModel.removeTag(it) },
                tagSearchText = tagSearchText,
                onTagSearchTextChange = { viewModel.tagSearchText.value = it },
                tagSearchResults = tagSearchResults,
                onAddTag = { viewModel.addTag(it) },
                isSearchingTags = isSearchingTags,
                favorite = favorite,
                onFavoriteChange = { viewModel.favorite.value = it },
                ignoreAutoTag = ignoreAutoTag,
                onIgnoreAutoTagChange = { viewModel.ignoreAutoTag.value = it },
                imageUrl = imageUrl,
                onDeleteImage = { viewModel.deleteImage() }
            )
        }
    }
}

/**
 * Main content of the edit studio form.
 *
 * Displays form sections for basic info, details, URLs, aliases, and settings.
 * Uses staggered entry animations for visual appeal.
 */
@Composable
internal fun EditStudioContent(
    name: String,
    onNameChange: (String) -> Unit,
    details: String,
    onDetailsChange: (String) -> Unit,
    rating: Int?,
    onRatingChange: (Int?) -> Unit,
    urls: List<String>,
    onUrlsChange: (List<String>) -> Unit,
    aliases: List<String>,
    onAliasesChange: (List<String>) -> Unit,
    tags: List<Tag>,
    onRemoveTag: (String) -> Unit,
    tagSearchText: String,
    onTagSearchTextChange: (String) -> Unit,
    tagSearchResults: List<Tag>,
    onAddTag: (Tag) -> Unit,
    isSearchingTags: Boolean,
    favorite: Boolean,
    onFavoriteChange: (Boolean) -> Unit,
    ignoreAutoTag: Boolean,
    onIgnoreAutoTagChange: (Boolean) -> Unit,
    imageUrl: String?,
    onDeleteImage: () -> Unit
) {
    val scrollState = rememberScrollState()

    StaggeredEditList(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(StashTokens.Spacing.ContentHorizontal),
        children = listOf(
            {
                if (imageUrl != null) {
                    ThumbnailSection(
                        imageUrl = imageUrl,
                        onDeleteImage = onDeleteImage
                    )
                }
            },
            {
                EditSection(title = "BASIC INFO") {
                    Column(verticalArrangement = Arrangement.spacedBy(StashTokens.Spacing.ContentHorizontal)) {
                        EditTextField(
                            value = name,
                            onValueChange = onNameChange,
                            label = "Name"
                        )
                    }
                }
            },
            {
                EditSection(title = "DETAILS") {
                    Column(verticalArrangement = Arrangement.spacedBy(StashTokens.Spacing.ContentHorizontal)) {
                        EditTextField(
                            value = details,
                            onValueChange = onDetailsChange,
                            label = "Description",
                            singleLine = false,
                            minLines = 5
                        )

                        OutlinedCard {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Rating", style = MaterialTheme.typography.bodyLarge)
                                RatingBar(
                                    rating = (rating ?: 0) / 20,
                                    onRatingChanged = { stars ->
                                        onRatingChange(if (stars == 0) null else stars * 20)
                                    }
                                )
                            }
                        }
                    }
                }
            },
            {
                EditSection(title = "URLS") {
                    StringListEditor(
                        values = urls,
                        onValueChange = onUrlsChange,
                        label = "URL",
                        addItemLabel = "Add URL"
                    )
                }
            },
            {
                EditSection(title = "TAGS") {
                    TagPicker(
                        selectedTags = tags,
                        onRemoveTag = onRemoveTag,
                        searchText = tagSearchText,
                        onSearchTextChange = onTagSearchTextChange,
                        searchResults = tagSearchResults,
                        onAddTag = onAddTag,
                        isSearching = isSearchingTags,
                        placeholder = "Add Tag..."
                    )
                }
            },
            {
                EditSection(title = "ALIASES") {
                    StringListEditor(
                        values = aliases,
                        onValueChange = onAliasesChange,
                        label = "Alias",
                        addItemLabel = "Add Alias"
                    )
                }
            },
            {
                EditSection(title = "SETTINGS") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        EditSwitchRow(
                            label = "Favorite",
                            checked = favorite,
                            onCheckedChange = onFavoriteChange,
                            inCard = false
                        )

                        EditSwitchRow(
                            label = "Ignore Auto Tag",
                            checked = ignoreAutoTag,
                            onCheckedChange = onIgnoreAutoTagChange,
                            inCard = false
                        )
                    }
                }
            },
            {
                Spacer(modifier = Modifier.navigationBarsPadding())
                Spacer(modifier = Modifier.height(80.dp))
            }
        )
    )
}
