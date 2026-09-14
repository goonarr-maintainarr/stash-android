package goonarr.stash.features.tags.detail

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import goonarr.stash.LocalBlurNsfw
import goonarr.stash.LocalStashDynamicColors
import goonarr.stash.StashTheme
import goonarr.stash.StashTokens
import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.Scene
import goonarr.stash.core.model.SceneMarker
import goonarr.stash.core.model.Studio
import goonarr.stash.core.model.Tag
import goonarr.stash.features.components.AliasesSection
import goonarr.stash.features.components.DetailCard
import goonarr.stash.features.components.ExternalIdsSection
import goonarr.stash.features.components.TagsSection
import goonarr.stash.features.performers.PerformerCard
import goonarr.stash.features.scenes.detail.components.fileinfo.InfoRow
import goonarr.stash.features.scenes.detail.components.markers.SceneMarkerCard
import goonarr.stash.features.scenes.scenecard.SceneCard
import goonarr.stash.features.studios.list.StudioCard
import goonarr.stash.util.DateFormatters
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
@Composable
fun TagDetailScreen(
    tagId: String,
    onBackClick: () -> Unit,
    onDeleteSuccess: () -> Unit = onBackClick,
    onSceneClick: (String, Double?) -> Unit,
    onPerformerClick: (String) -> Unit = {},
    onStudioClick: (String) -> Unit = {},
    onEditClick: (String) -> Unit = {},
    onTagClick: (String) -> Unit = {},
    viewModel: TagDetailViewModel = hiltViewModel(),
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    val state by viewModel.state.collectAsState()
    val isFollowed by viewModel.isFollowed.collectAsState(initial = false)
    val selectedTab by viewModel.selectedTab.collectAsState()

    val deleteSuccess by viewModel.deleteSuccess.collectAsState()

    LaunchedEffect(tagId) {
        viewModel.loadTag(tagId)
    }

    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) {
            onDeleteSuccess()
        }
    }

    val themeColors = LocalStashDynamicColors.current
    val haptic = LocalStashHapticFeedback.current

    CompositionLocalProvider(LocalStashDynamicColors provides themeColors) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (val currentState = state) {
                is TagDetailState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is TagDetailState.Content -> {
                    TagDetailContent(
                        tag = currentState.tag,
                        isFavorite = currentState.tag.favorite == true,
                        isFollowed = isFollowed,
                        scenes = currentState.scenes,
                        markers = currentState.markers,
                        selectedTab = selectedTab,
                        onTabSelected = viewModel::selectTab,
                        onToggleFavorite = {
                            haptic.perform(StashHapticFeedbackType.Selection)
                            viewModel.toggleFavorite()
                        },
                        onToggleFollow = {
                            haptic.perform(StashHapticFeedbackType.Selection)
                            viewModel.toggleFollow()
                        },
                        onSceneClick = onSceneClick,
                        onPerformerClick = onPerformerClick,
                        onStudioClick = onStudioClick,
                        onEditClick = onEditClick,
                        onTagClick = onTagClick,
                        onDeleteTag = viewModel::deleteTag,
                        onBackClick = onBackClick,
                        onGetStreamUrl = viewModel::getStreamUrl,
                        containerColor = MaterialTheme.colorScheme.background,
                        loadMoreScenes = viewModel::loadMoreScenes,
                        loadMoreMarkers = viewModel::loadMoreMarkers,
                        loadMorePerformers = viewModel::loadMorePerformers,
                        loadMoreStudios = viewModel::loadMoreStudios,
                        hasMoreScenes = currentState.hasMoreScenes,
                        isLoadingMoreScenes = currentState.isLoadingMoreScenes,
                        hasMoreMarkers = currentState.hasMoreMarkers,
                        isLoadingMoreMarkers = currentState.isLoadingMoreMarkers,
                        performers = currentState.performers,
                        hasMorePerformers = currentState.hasMorePerformers,
                        isLoadingMorePerformers = currentState.isLoadingMorePerformers,
                        studios = currentState.studios,
                        hasMoreStudios = currentState.hasMoreStudios,
                        isLoadingMoreStudios = currentState.isLoadingMoreStudios,
                        studioCount = currentState.studioCount,
                        animatedVisibilityScope = animatedVisibilityScope,
                        sharedTransitionScope = sharedTransitionScope
                    )
                }
                is TagDetailState.NotFound -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Tag Not Found", color = Color.White)
                    }
                }
                is TagDetailState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${currentState.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalCoroutinesApi::class)
@Composable
fun TagDetailContent(
    tag: Tag?,
    isFavorite: Boolean,
    isFollowed: Boolean,
    scenes: List<Scene>,
    markers: List<SceneMarker>,
    selectedTab: TagDetailTab,
    onTabSelected: (TagDetailTab) -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleFollow: () -> Unit,
    onSceneClick: (String, Double?) -> Unit,
    onPerformerClick: (String) -> Unit,
    onStudioClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onTagClick: (String) -> Unit,
    onDeleteTag: () -> Unit,
    onBackClick: () -> Unit,
    onGetStreamUrl: suspend (String?) -> String?,
    containerColor: Color = MaterialTheme.colorScheme.background,
    loadMoreScenes: () -> Unit,
    loadMoreMarkers: () -> Unit,
    loadMorePerformers: () -> Unit,
    loadMoreStudios: () -> Unit,
    hasMoreScenes: Boolean,
    isLoadingMoreScenes: Boolean,
    hasMoreMarkers: Boolean,
    isLoadingMoreMarkers: Boolean,
    performers: List<Performer> = emptyList(),
    hasMorePerformers: Boolean = false,
    isLoadingMorePerformers: Boolean = false,
    studios: List<Studio> = emptyList(),
    hasMoreStudios: Boolean = false,
    isLoadingMoreStudios: Boolean = false,
    studioCount: Int = 0,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    val haptic = LocalStashHapticFeedback.current
    val themeColors = LocalStashDynamicColors.current
    var showDeleteSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (showDeleteSheet) {
        ModalBottomSheet(
            onDismissRequest = { showDeleteSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = StashTokens.Spacing.ContentVertical + 4.dp)
                        .size(32.dp, 4.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = StashTokens.Alpha.ButtonBackground),
                            CircleShape
                        )
                )
            }
        ) {
            DeleteTagSheetContent(
                tagName = tag?.name ?: "this tag",
                onDelete = {
                    showDeleteSheet = false
                    onDeleteTag()
                },
                onCancel = { showDeleteSheet = false }
            )
        }
    }

    Scaffold(
        modifier = Modifier,
        topBar = {
            TopAppBar(
                title = {
                    if (tag != null) {
                        Text(tag.name, color = Color.White)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                actions = {
                    var showMenu by remember { mutableStateOf(false) }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = Color.White
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            containerColor = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(StashTokens.Radius.Card)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    haptic.perform(StashHapticFeedbackType.Medium)
                                    showMenu = false
                                    tag?.id?.let { onEditClick(it) }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = themeColors.primary
                                    )
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(
                                    horizontal = StashTokens.Spacing.ContentHorizontal,
                                    vertical = StashTokens.Radius.Small
                                ),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    haptic.perform(StashHapticFeedbackType.Heavy)
                                    showMenu = false
                                    showDeleteSheet = true
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }
                }
            )
        },
        containerColor = containerColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = { HorizontalDivider(color = Color.White.copy(alpha = 0.1f)) },
                edgePadding = 0.dp
            ) {
                TagDetailTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = {
                            haptic.perform(StashHapticFeedbackType.Selection)
                            onTabSelected(tab)
                        },
                        text = {
                            Text(
                                tab.title,
                                color = if (selectedTab == tab) Color.White else Color.White.copy(alpha = 0.6f),
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selectedContentColor = Color.White,
                        unselectedContentColor = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    TagDetailTab.OVERVIEW -> {
                        TagOverviewTab(
                            tag = tag,
                            studioCount = studioCount,
                            isFavorite = isFavorite,
                            isFollowed = isFollowed,
                            onToggleFavorite = onToggleFavorite,
                            onToggleFollow = onToggleFollow,
                            onTagClick = onTagClick,
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope
                        )
                    }

                    TagDetailTab.SCENES -> {
                        TagScenesTab(
                            scenes = scenes,
                            hasMore = hasMoreScenes,
                            isLoadingMore = isLoadingMoreScenes,
                            onSceneClick = onSceneClick,
                            onPerformerClick = onPerformerClick,
                            onStudioClick = onStudioClick,
                            onLoadMore = loadMoreScenes
                        )
                    }

                    TagDetailTab.MARKERS -> {
                        TagMarkersTab(
                            markers = markers,
                            hasMore = hasMoreMarkers,
                            isLoadingMore = isLoadingMoreMarkers,
                            onSceneClick = onSceneClick,
                            onGetStreamUrl = onGetStreamUrl,
                            onLoadMore = loadMoreMarkers
                        )
                    }

                    TagDetailTab.PERFORMERS -> {
                        TagPerformersTab(
                            performers = performers,
                            hasMore = hasMorePerformers,
                            isLoadingMore = isLoadingMorePerformers,
                            onPerformerClick = onPerformerClick,
                            onLoadMore = loadMorePerformers
                        )
                    }

                    TagDetailTab.STUDIOS -> {
                        TagStudiosTab(
                            studios = studios,
                            hasMore = hasMoreStudios,
                            isLoadingMore = isLoadingMoreStudios,
                            onStudioClick = onStudioClick,
                            onLoadMore = loadMoreStudios
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TagScenesTab(
    scenes: List<Scene>,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onSceneClick: (String, Double?) -> Unit,
    onPerformerClick: (String) -> Unit,
    onStudioClick: (String) -> Unit,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            hasMore && (lastVisibleItemIndex >= totalItems - 5)
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !isLoadingMore) {
            onLoadMore()
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + StashTokens.Spacing.ContentHorizontal
        )
    ) {
        if (scenes.isEmpty() && !isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(StashTokens.Spacing.SectionSpacing),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No scenes found", color = Color.White.copy(alpha = StashTokens.Alpha.TextSecondary))
                }
            }
        } else {
            items(scenes, key = { it.id }) { scene ->
                SceneCard(
                    scene = scene,
                    onClick = { onSceneClick(scene.id, null) },
                    onPerformerClick = onPerformerClick,
                    onStudioClick = onStudioClick,
                    modifier = Modifier.padding(
                        horizontal = StashTokens.Spacing.ContentHorizontal,
                        vertical = StashTokens.Spacing.ContentVertical
                    )
                )
            }
        }

        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun TagMarkersTab(
    markers: List<SceneMarker>,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onSceneClick: (String, Double?) -> Unit,
    onLoadMore: () -> Unit,
    onGetStreamUrl: suspend (String?) -> String?
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            hasMore && (lastVisibleItemIndex >= totalItems - 5)
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !isLoadingMore) {
            onLoadMore()
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + StashTokens.Spacing.ContentHorizontal
        )
    ) {
        if (markers.isEmpty() && !isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(StashTokens.Spacing.SectionSpacing),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No markers found", color = Color.White.copy(alpha = StashTokens.Alpha.TextSecondary))
                }
            }
        } else {
            items(markers, key = { it.id }) { marker ->
                SceneMarkerCard(
                    marker = marker,
                    onSeek = { seconds -> marker.scene?.id?.let { onSceneClick(it, seconds) } },
                    getStreamUrl = onGetStreamUrl,
                    modifier = Modifier.padding(
                        horizontal = StashTokens.Spacing.ContentHorizontal,
                        vertical = StashTokens.Spacing.ContentVertical
                    )
                )
            }
        }

        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun TagPerformersTab(
    performers: List<Performer>,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onPerformerClick: (String) -> Unit,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyGridState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            hasMore && (lastVisibleItemIndex >= totalItems - 5)
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !isLoadingMore) {
            onLoadMore()
        }
    }

    LazyVerticalGrid(
        state = listState,
        columns = GridCells.Adaptive(minSize = 120.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = StashTokens.Spacing.ContentHorizontal,
            end = StashTokens.Spacing.ContentHorizontal,
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + StashTokens.Spacing.ContentHorizontal,
            top = StashTokens.Spacing.ContentVertical
        ),
        verticalArrangement = Arrangement.spacedBy(StashTokens.Spacing.ContentVertical),
        horizontalArrangement = Arrangement.spacedBy(StashTokens.Spacing.ContentHorizontal)
    ) {
        if (performers.isEmpty() && !isLoadingMore) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(StashTokens.Spacing.SectionSpacing),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No performers found", color = Color.White.copy(alpha = StashTokens.Alpha.TextSecondary))
                }
            }
        } else {
            items(performers, key = { it.id }) { performer ->
                PerformerCard(
                    performer = performer,
                    onClick = { onPerformerClick(performer.id) }
                )
            }
        }

        if (isLoadingMore) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@Composable
fun TagStudiosTab(
    studios: List<Studio>,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onStudioClick: (String) -> Unit,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyGridState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            hasMore && (lastVisibleItemIndex >= totalItems - 5)
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !isLoadingMore) {
            onLoadMore()
        }
    }

    LazyVerticalGrid(
        state = listState,
        columns = GridCells.Adaptive(minSize = 120.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = StashTokens.Spacing.ContentHorizontal,
            end = StashTokens.Spacing.ContentHorizontal,
            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + StashTokens.Spacing.ContentHorizontal,
            top = StashTokens.Spacing.ContentVertical
        ),
        verticalArrangement = Arrangement.spacedBy(StashTokens.Spacing.ContentVertical),
        horizontalArrangement = Arrangement.spacedBy(StashTokens.Spacing.ContentHorizontal)
    ) {
        if (studios.isEmpty() && !isLoadingMore) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(StashTokens.Spacing.SectionSpacing),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No studios found", color = Color.White.copy(alpha = StashTokens.Alpha.TextSecondary))
                }
            }
        } else {
            items(studios, key = { it.id }) { studio ->
                StudioCard(
                    studio = studio,
                    imageUrl = studio.imagePath,
                    onClick = { onStudioClick(studio.id) }
                )
            }
        }

        if (isLoadingMore) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TagOverviewTab(
    tag: Tag?,
    studioCount: Int,
    isFavorite: Boolean,
    isFollowed: Boolean,
    onToggleFavorite: () -> Unit,
    onToggleFollow: () -> Unit,
    onTagClick: (String) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    if (tag == null) return

    val themeColors = LocalStashDynamicColors.current
    val sharedElementModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(key = "tag_name_${tag.id}"),
                animatedVisibilityScope = animatedVisibilityScope,
                boundsTransform = { _, _ ->
                    tween(durationMillis = 300)
                }
            )
        }
    } else {
        Modifier
    }

    val sharedImageModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(key = "tag_image_${tag.id}"),
                animatedVisibilityScope = animatedVisibilityScope,
                boundsTransform = { _, _ ->
                    tween(durationMillis = 300)
                }
            )
        }
    } else {
        Modifier
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Hero Image
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .then(sharedImageModifier)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                if (!tag.imagePath.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(tag.imagePath)
                            .crossfade(true)
                            .build(),
                        contentDescription = tag.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.05f))
                            .then(if (LocalBlurNsfw.current) Modifier.blur(20.dp) else Modifier)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tag.name.firstOrNull()?.toString() ?: "",
                            style = MaterialTheme.typography.displayLarge,
                            color = Color.White.copy(alpha = 0.1f)
                        )
                    }
                }

                // Favorite Button (Moved back to image)
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isFavorite) "Unfavorite" else "Favorite",
                        tint = if (isFavorite) Color(0xFFE53935) else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Header and Add to Home Switch
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = tag.name,
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = sharedElementModifier
                )

                // Add to Home Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add to Home Screen",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Switch(
                        checked = isFollowed,
                        onCheckedChange = { onToggleFollow() }
                    )
                }
            }
        }

        // Stats Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TagStatItem(
                        icon = Icons.Default.VideoLibrary,
                        label = "Scenes",
                        value = "${tag.sceneCount ?: 0}",
                        modifier = Modifier.weight(1f),
                        accentColor = MaterialTheme.colorScheme.primary
                    )
                    TagStatItem(
                        icon = Icons.Default.LocationOn,
                        label = "Markers",
                        value = "${tag.sceneMarkerCount ?: 0}",
                        modifier = Modifier.weight(1f),
                        accentColor = MaterialTheme.colorScheme.secondary
                    )
                }

                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TagStatItem(
                        icon = Icons.Default.People,
                        label = "Performers",
                        value = "${tag.performerCount ?: 0}",
                        modifier = Modifier.weight(1f),
                        accentColor = MaterialTheme.colorScheme.tertiary
                    )
                    TagStatItem(
                        icon = Icons.Default.Business,
                        label = "Studios",
                        value = "$studioCount",
                        modifier = Modifier.weight(1f),
                        accentColor = Color.White
                    )
                }
            }
        }

        // Description / About
        if (!tag.description.isNullOrEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            modifier = Modifier.size(20.dp),
                            contentDescription = null,
                            tint = themeColors.primary
                        )
                        Text(
                            text = "About",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = tag.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Aliases
        item {
            AliasesSection(aliases = tag.aliases)
        }

        // Parent Tags
        if (!tag.parents.isNullOrEmpty()) {
            item {
                TagsSection(
                    tags = tag.parents,
                    title = "PARENT TAGS",
                    onTagClick = onTagClick
                )
            }
        }

        // Subtags
        if (!tag.children.isNullOrEmpty()) {
            item {
                TagsSection(
                    tags = tag.children,
                    title = "SUBTAGS",
                    onTagClick = onTagClick
                )
            }
        }

        // External IDs
        if (!tag.stashIds.isNullOrEmpty()) {
            item {
                ExternalIdsSection(stashIds = tag.stashIds)
            }
        }

        // Dates
        if (tag.createdAt != null || tag.updatedAt != null) {
            item {
                val cardColor = StashTheme.colors.tertiary.copy(alpha = StashTokens.Alpha.CardBackground)
                val borderColor = StashTheme.colors.tertiary.copy(alpha = StashTokens.Alpha.CardBorder)

                DetailCard(containerColor = cardColor, borderColor = borderColor) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        var hasPreviousItem = false
                        tag.createdAt?.takeIf { it.isNotEmpty() }?.let {
                            InfoRow(label = "Created", value = DateFormatters.formatTimestamp(it))
                            hasPreviousItem = true
                        }
                        tag.updatedAt?.takeIf { it.isNotEmpty() }?.let {
                            if (hasPreviousItem) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = StashTokens.Alpha.Divider
                                    )
                                )
                            }
                            InfoRow(label = "Updated", value = DateFormatters.formatTimestamp(it))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TagStatItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accentColor: Color? = null
) {
    Row(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor ?: Color.White,
            modifier = Modifier.size(24.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DeleteTagSheetContent(
    tagName: String,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Delete Tag?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Are you sure you want to delete \"$tagName\"? This action cannot be undone.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = onDelete,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        }
    }
}
