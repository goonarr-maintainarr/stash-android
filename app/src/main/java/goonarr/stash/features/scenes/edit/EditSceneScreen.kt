package goonarr.stash.features.scenes.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import goonarr.stash.LocalStashDynamicColors
import goonarr.stash.StashTokens
import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.SceneMarker
import goonarr.stash.core.model.StashID
import goonarr.stash.core.model.Tag
import goonarr.stash.features.components.OutlinedCard
import goonarr.stash.features.components.RatingBar
import goonarr.stash.features.components.SharedEditBackground
import goonarr.stash.features.components.SharedEditScaffold
import goonarr.stash.features.components.SharedEditUiState
import goonarr.stash.features.components.StaggeredEditList
import goonarr.stash.features.components.ThumbnailSection
import goonarr.stash.rememberSceneColors
import goonarr.stash.util.HeroAccentColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSceneScreen(
    sceneId: String,
    onBackClick: () -> Unit,
    onSaveSuccess: () -> Unit,
    viewModel: EditSceneViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var heroColor by remember { mutableStateOf<Color?>(null) }
    val screenshotUrl by viewModel.screenshotUrl.collectAsState()

    LaunchedEffect(sceneId) {
        viewModel.loadScene(sceneId)
    }

    LaunchedEffect(uiState) {
        if (uiState is SharedEditUiState.Saved) {
            onSaveSuccess()
        }
    }

    LaunchedEffect(screenshotUrl) {
        if (screenshotUrl != null) {
            HeroAccentColor.extract(context, screenshotUrl)?.let { heroColor = it }
        }
    }

    val sceneColors = rememberSceneColors(heroColor)

    Box(modifier = Modifier.fillMaxSize()) {
        SharedEditBackground(screenshotUrl)

        CompositionLocalProvider(LocalStashDynamicColors provides sceneColors) {
            SharedEditScaffold(
                title = "Edit Scene",
                onBackClick = onBackClick,
                onSave = { viewModel.save() },
                onRetry = { viewModel.clearError() },
                uiState = uiState,
                dynamicColors = sceneColors
            ) {
                val title by viewModel.title.collectAsState()
                val details by viewModel.details.collectAsState()
                val director by viewModel.director.collectAsState()
                val code by viewModel.code.collectAsState()
                val url by viewModel.url.collectAsState()
                val rating by viewModel.rating.collectAsState()

                val currentPerformers by viewModel.currentPerformers.collectAsState()
                val currentTags by viewModel.currentTags.collectAsState()
                val stashIds by viewModel.stashIds.collectAsState()
                val screenshotUrl by viewModel.screenshotUrl.collectAsState()

                val oHistory by viewModel.oHistory.collectAsState()
                val playHistory by viewModel.playHistory.collectAsState()
                val currentMarkers by viewModel.currentMarkers.collectAsState()

                val performerSearchText by viewModel.performerSearchText.collectAsState()
                val performerSearchResults by viewModel.performerSearchResults.collectAsState()
                val isSearchingPerformers by viewModel.isSearchingPerformers.collectAsState()

                val tagSearchText by viewModel.tagSearchText.collectAsState()
                val tagSearchResults by viewModel.tagSearchResults.collectAsState()
                val isSearchingTags by viewModel.isSearchingTags.collectAsState()

                EditSceneContent(
                    title = title,
                    onTitleChange = viewModel::updateTitle,
                    details = details,
                    onDetailsChange = viewModel::updateDetails,
                    director = director,
                    onDirectorChange = viewModel::updateDirector,
                    code = code,
                    onCodeChange = viewModel::updateCode,
                    url = url,
                    onUrlChange = viewModel::updateUrl,
                    rating = rating,
                    onRatingChange = { viewModel.rating.value = it },

                    currentPerformers = currentPerformers,
                    onRemovePerformer = viewModel::removePerformer,
                    performerSearchText = performerSearchText,
                    onPerformerSearchTextChange = viewModel::updatePerformerSearch,
                    performerSearchResults = performerSearchResults,
                    isSearchingPerformers = isSearchingPerformers,
                    onAddPerformer = viewModel::addPerformer,

                    currentTags = currentTags,
                    onRemoveTag = viewModel::removeTag,
                    tagSearchText = tagSearchText,
                    onTagSearchTextChange = viewModel::updateTagSearch,
                    tagSearchResults = tagSearchResults,
                    isSearchingTags = isSearchingTags,
                    onAddTag = viewModel::addTag,

                    stashIds = stashIds,
                    onRemoveStashId = viewModel::removeStashId,

                    oHistory = oHistory,
                    playHistory = playHistory,
                    onRemoveOHistory = viewModel::removeOHistory,
                    onRemovePlayHistory = viewModel::removePlayHistory,

                    currentMarkers = currentMarkers,
                    onDeleteMarker = viewModel::deleteMarker,

                    screenshotUrl = screenshotUrl,
                    onDeleteCover = viewModel::deleteCover,

                    getStreamUrl = viewModel::getStreamUrl
                )
            }
        }
    }
}

@Composable
internal fun EditSceneContent(
    title: String,
    onTitleChange: (String) -> Unit,
    details: String,
    onDetailsChange: (String) -> Unit,
    director: String,
    onDirectorChange: (String) -> Unit,
    code: String,
    onCodeChange: (String) -> Unit,
    url: String,
    onUrlChange: (String) -> Unit,
    rating: Int?,
    onRatingChange: (Int?) -> Unit,

    currentPerformers: List<Performer>,
    onRemovePerformer: (String) -> Unit,
    performerSearchText: String,
    onPerformerSearchTextChange: (String) -> Unit,
    performerSearchResults: List<Performer>,
    isSearchingPerformers: Boolean,
    onAddPerformer: (Performer) -> Unit,

    currentTags: List<Tag>,
    onRemoveTag: (String) -> Unit,
    tagSearchText: String,
    onTagSearchTextChange: (String) -> Unit,
    tagSearchResults: List<Tag>,
    isSearchingTags: Boolean,
    onAddTag: (Tag) -> Unit,

    stashIds: List<StashID>,
    onRemoveStashId: (String) -> Unit,

    oHistory: List<String>,
    playHistory: List<String>,
    onRemoveOHistory: (String) -> Unit,
    onRemovePlayHistory: (String) -> Unit,

    currentMarkers: List<SceneMarker>,
    onDeleteMarker: (String) -> Unit,

    screenshotUrl: Any?,
    onDeleteCover: () -> Unit,

    getStreamUrl: suspend (String?) -> String?
) {
    val scrollState = rememberScrollState()

    StaggeredEditList(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(StashTokens.Spacing.ContentHorizontal),
        children = listOf(
            {
                if (screenshotUrl != null) {
                    ThumbnailSection(
                        imageUrl = screenshotUrl,
                        onDeleteImage = onDeleteCover
                    )
                }
            },
            {
                DetailsSection(
                    title = title,
                    onTitleChange = onTitleChange,
                    director = director,
                    onDirectorChange = onDirectorChange,
                    code = code,
                    onCodeChange = onCodeChange,
                    url = url,
                    onUrlChange = onUrlChange
                )
            },
            {
                Column(verticalArrangement = Arrangement.spacedBy(StashTokens.Spacing.ContentHorizontal)) {
                    OutlinedCard {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
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
            },
            {
                DescriptionSection(
                    details = details,
                    onDetailsChange = onDetailsChange
                )
            },
            {
                PerformersSection(
                    currentPerformers = currentPerformers,
                    onRemovePerformer = onRemovePerformer,
                    searchText = performerSearchText,
                    onSearchTextChange = onPerformerSearchTextChange,
                    searchResults = performerSearchResults,
                    isSearching = isSearchingPerformers,
                    onAddPerformer = onAddPerformer
                )
            },
            {
                TagsSection(
                    currentTags = currentTags,
                    onRemoveTag = onRemoveTag,
                    searchText = tagSearchText,
                    onSearchTextChange = onTagSearchTextChange,
                    searchResults = tagSearchResults,
                    isSearching = isSearchingTags,
                    onAddTag = onAddTag
                )
            },
            {
                StashIdsSection(
                    stashIds = stashIds,
                    onRemove = onRemoveStashId
                )
            },
            {
                HistorySection(
                    title = "O HISTORY",
                    items = oHistory,
                    onRemove = onRemoveOHistory
                )
            },
            {
                HistorySection(
                    title = "PLAY HISTORY",
                    items = playHistory,
                    onRemove = onRemovePlayHistory
                )
            },
            {
                MarkersSection(
                    currentMarkers = currentMarkers,
                    onDeleteMarker = onDeleteMarker,
                    getStreamUrl = getStreamUrl
                )
            },
            {
                Spacer(modifier = Modifier.navigationBarsPadding())
                Spacer(modifier = Modifier.height(80.dp))
            }
        )
    )
}
