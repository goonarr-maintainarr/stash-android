package goonarr.stash.features.tags.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import goonarr.stash.LocalStashDynamicColors
import goonarr.stash.StashBlue
import goonarr.stash.StashTheme
import goonarr.stash.StashTokens
import goonarr.stash.core.model.Tag
import goonarr.stash.core.network.StashBoxTag
import goonarr.stash.features.components.EditSection
import goonarr.stash.features.components.EditSwitchRow
import goonarr.stash.features.components.EditTextField
import goonarr.stash.features.components.SharedEditScaffold
import goonarr.stash.features.components.SharedEditUiState
import goonarr.stash.features.components.StaggeredEditList
import goonarr.stash.features.components.StringListEditor
import goonarr.stash.features.components.TagPicker
import goonarr.stash.features.components.ThumbnailSection
import goonarr.stash.rememberSceneColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTagScreen(
    tagId: String,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: EditTagViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(tagId) {
        viewModel.loadTag(tagId)
    }

    LaunchedEffect(uiState) {
        if (uiState is SharedEditUiState.Saved) {
            onSaveSuccess()
        }
    }

    // Use StashBlue as the primary color for the edit screen, consistent with other edit screens
    val dynamicColors = rememberSceneColors(StashBlue)

    CompositionLocalProvider(LocalStashDynamicColors provides dynamicColors) {
        SharedEditScaffold(
            title = "Edit Tag",
            onBackClick = onBackClick,
            onSave = { viewModel.save() },
            onRetry = { viewModel.clearError() },
            uiState = uiState,
            dynamicColors = dynamicColors
        ) {
            val name by viewModel.name.collectAsState()
            val details by viewModel.details.collectAsState()
            val aliases by viewModel.aliases.collectAsState()
            val favorite by viewModel.favorite.collectAsState()
            val ignoreAutoTag by viewModel.ignoreAutoTag.collectAsState()
            val imageUrl by viewModel.imageUrl.collectAsState()

            val parentTags by viewModel.parentTags.collectAsState()
            val parentTagSearchText by viewModel.parentTagSearchText.collectAsState()
            val parentTagSearchResults by viewModel.parentTagSearchResults.collectAsState()

            val subTags by viewModel.subTags.collectAsState()
            val subTagSearchText by viewModel.subTagSearchText.collectAsState()
            val subTagSearchResults by viewModel.subTagSearchResults.collectAsState()

            val isSearching by viewModel.isSearching.collectAsState()

            val stashBoxSearchText by viewModel.stashBoxSearchText.collectAsState()
            val stashBoxSearchResults by viewModel.stashBoxSearchResults.collectAsState()
            val isSearchingStashBox by viewModel.isSearchingStashBox.collectAsState()

            EditTagContent(
                name = name,
                onNameChange = { viewModel.name.value = it },
                details = details,
                onDetailsChange = { viewModel.details.value = it },
                aliases = aliases,
                onAliasesChange = { viewModel.aliases.value = it },
                favorite = favorite,
                onFavoriteChange = { viewModel.favorite.value = it },
                ignoreAutoTag = ignoreAutoTag,
                onIgnoreAutoTagChange = { viewModel.ignoreAutoTag.value = it },
                imageUrl = imageUrl,
                onDeleteImage = { viewModel.deleteImage() },
                // Parent Tags
                parentTags = parentTags,
                onRemoveParentTag = viewModel::removeParentTag,
                parentTagSearchText = parentTagSearchText,
                onParentTagSearchTextChange = viewModel::searchParentTags,
                parentTagSearchResults = parentTagSearchResults,
                onAddParentTag = viewModel::addParentTag,
                // Sub Tags
                subTags = subTags,
                onRemoveSubTag = viewModel::removeSubTag,
                subTagSearchText = subTagSearchText,
                onSubTagSearchTextChange = viewModel::searchSubTags,
                subTagSearchResults = subTagSearchResults,
                onAddSubTag = viewModel::addSubTag,
                isSearching = isSearching,
                // StashBox
                stashBoxSearchText = stashBoxSearchText,
                onStashBoxSearchTextChange = viewModel::searchStashBox,
                stashBoxSearchResults = stashBoxSearchResults,
                onStashBoxTagSelected = viewModel::onStashBoxTagSelected,
                isSearchingStashBox = isSearchingStashBox
            )
        }
    }
}

@Composable
internal fun EditTagContent(
    name: String,
    onNameChange: (String) -> Unit,
    details: String,
    onDetailsChange: (String) -> Unit,
    aliases: List<String>,
    onAliasesChange: (List<String>) -> Unit,
    favorite: Boolean,
    onFavoriteChange: (Boolean) -> Unit,
    ignoreAutoTag: Boolean,
    onIgnoreAutoTagChange: (Boolean) -> Unit,
    imageUrl: String?,
    onDeleteImage: () -> Unit,
    // Parent Tags
    parentTags: List<Tag>,
    onRemoveParentTag: (String) -> Unit,
    parentTagSearchText: String,
    onParentTagSearchTextChange: (String) -> Unit,
    parentTagSearchResults: List<Tag>,
    onAddParentTag: (Tag) -> Unit,
    // Sub Tags
    subTags: List<Tag>,
    onRemoveSubTag: (String) -> Unit,
    subTagSearchText: String,
    onSubTagSearchTextChange: (String) -> Unit,
    subTagSearchResults: List<Tag>,
    onAddSubTag: (Tag) -> Unit,
    isSearching: Boolean,
    // StashBox
    stashBoxSearchText: String,
    onStashBoxSearchTextChange: (String) -> Unit,
    stashBoxSearchResults: List<StashBoxTag>,
    onStashBoxTagSelected: (StashBoxTag) -> Unit,
    isSearchingStashBox: Boolean
) {
    val scrollState = rememberScrollState()
    var showStashBoxSearch by remember { mutableStateOf(false) }

    if (showStashBoxSearch) {
        StashBoxSearchDialog(
            onDismiss = { showStashBoxSearch = false },
            searchText = stashBoxSearchText,
            onSearchTextChange = onStashBoxSearchTextChange,
            searchResults = stashBoxSearchResults,
            onTagSelected = {
                onStashBoxTagSelected(it)
                showStashBoxSearch = false
            },
            isSearching = isSearchingStashBox
        )
    }

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
                EditSection(title = "DESCRIPTION") {
                    EditTextField(
                        value = details,
                        onValueChange = onDetailsChange,
                        label = "Description",
                        singleLine = false,
                        minLines = 5
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
                EditSection(title = "PARENT TAGS") {
                    TagPicker(
                        selectedTags = parentTags,
                        onRemoveTag = onRemoveParentTag,
                        searchText = parentTagSearchText,
                        onSearchTextChange = onParentTagSearchTextChange,
                        searchResults = parentTagSearchResults,
                        onAddTag = onAddParentTag,
                        isSearching = isSearching,
                        placeholder = "Add Parent Tag..."
                    )
                }
            },
            {
                EditSection(title = "SUB TAGS") {
                    TagPicker(
                        selectedTags = subTags,
                        onRemoveTag = onRemoveSubTag,
                        searchText = subTagSearchText,
                        onSearchTextChange = onSubTagSearchTextChange,
                        searchResults = subTagSearchResults,
                        onAddTag = onAddSubTag,
                        isSearching = isSearching,
                        placeholder = "Add Sub Tag..."
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
                EditSection(title = "STASHBOX") {
                    Button(
                        onClick = { showStashBoxSearch = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = StashTheme.colors.primary)
                    ) {
                        Text("Search StashBox")
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
