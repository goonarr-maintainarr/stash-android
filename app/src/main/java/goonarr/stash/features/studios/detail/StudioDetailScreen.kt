package goonarr.stash.features.studios.detail

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import coil.request.ImageRequest
import goonarr.stash.LocalBlurNsfw
import goonarr.stash.LocalStashDynamicColors
import goonarr.stash.R
import goonarr.stash.StashBlue
import goonarr.stash.StashTheme
import goonarr.stash.StashTokens
import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.Scene
import goonarr.stash.core.model.Studio
import goonarr.stash.features.components.AliasesSection
import goonarr.stash.features.components.CollapsibleLinks
import goonarr.stash.features.components.DetailCard
import goonarr.stash.features.components.ExternalIdsSection
import goonarr.stash.features.components.RatingBar
import goonarr.stash.features.components.TagsSection
import goonarr.stash.features.performers.PerformerCompactCard
import goonarr.stash.features.scenes.detail.components.fileinfo.InfoRow
import goonarr.stash.features.scenes.scenecard.SceneCard
import goonarr.stash.rememberSceneColors
import goonarr.stash.util.DateFormatters
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType

/**
 * Displays detailed information about a studio including overview, scenes, and performers.
 *
 * Features a 3-dot menu in the top bar with Edit and Delete options.
 *
 * @param studioId The ID of the studio to display.
 * @param viewModel The ViewModel managing studio state.
 * @param onBackClick Callback when back button is pressed.
 * @param onSceneClick Callback when a scene is tapped, with scene ID and optional timestamp.
 * @param onPerformerClick Callback when a performer is tapped, with performer ID.
 * @param onStudioClick Callback when a sub-studio is tapped, with studio ID.
 * @param onEditClick Callback when edit is selected from menu, with studio ID.
 * @param onDeleteSuccess Callback when studio is successfully deleted.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioDetailScreen(
    studioId: String,
    viewModel: StudioDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onSceneClick: (String, Double?) -> Unit,
    onTagClick: (String) -> Unit,
    onPerformerClick: (String) -> Unit,
    onStudioClick: (String) -> Unit,
    onEditClick: (String) -> Unit = {},
    onScrapeClick: (String) -> Unit = {},
    onDeleteSuccess: () -> Unit = {},
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    val state by viewModel.state.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    val context = LocalContext.current
    val haptic = LocalStashHapticFeedback.current
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    LaunchedEffect(studioId) {
        viewModel.loadStudio(studioId)
    }

    // Navigate away when delete succeeds
    val deleteSuccess by viewModel.deleteSuccess.collectAsState()
    LaunchedEffect(deleteSuccess) {
        if (deleteSuccess) {
            onDeleteSuccess()
        }
    }

    // Refresh when returning from Edit screen
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh(studioId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val studioColors = rememberSceneColors(StashBlue)

    CompositionLocalProvider(LocalStashDynamicColors provides studioColors) {
        if (showDeleteSheet) {
            ModalBottomSheet(
                onDismissRequest = { showDeleteSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .size(32.dp, 4.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                CircleShape
                            )
                    )
                }
            ) {
                DeleteStudioSheetContent(
                    studioName = (state as? StudioDetailState.Content)?.studio?.name ?: "this studio",
                    onDelete = {
                        showDeleteSheet = false
                        viewModel.deleteStudio()
                    },
                    onCancel = { showDeleteSheet = false }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                TopAppBar(
                    title = {
                        if (state is StudioDetailState.Content) {
                            val studio = (state as StudioDetailState.Content).studio
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = studio.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
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
                    actions = {
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
                                shape = RoundedCornerShape(StashTokens.Radius.Card),
                                shadowElevation = 8.dp
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Scrape") },
                                    onClick = {
                                        haptic.perform(StashHapticFeedbackType.Medium)
                                        showMenu = false
                                        onScrapeClick(studioId)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            // Using Search icon for scrape
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = StashBlue
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Edit") },
                                    onClick = {
                                        haptic.perform(StashHapticFeedbackType.Medium)
                                        showMenu = false
                                        onEditClick(studioId)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null,
                                            tint = StashBlue
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
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White
                    )
                )

                when (val currentState = state) {
                    is StudioDetailState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    is StudioDetailState.Content -> {
                        StudioDetailContent(
                            studio = currentState.studio,
                            scenes = currentState.scenes,
                            performers = currentState.performers,
                            selectedTab = selectedTab,
                            onTabSelected = viewModel::selectTab,
                            onSceneClick = onSceneClick,
                            onTagClick = onTagClick,
                            onPerformerClick = onPerformerClick,
                            onStudioClick = onStudioClick,
                            loadMoreScenes = viewModel::loadMoreScenes,
                            loadMorePerformers = viewModel::loadMorePerformers,
                            hasMoreScenes = currentState.hasMoreScenes,
                            isLoadingMoreScenes = currentState.isLoadingMoreScenes,
                            hasMorePerformers = currentState.hasMorePerformers,
                            isLoadingMorePerformers = currentState.isLoadingMorePerformers,

                            onRatingChanged = viewModel::updateRating,
                            onFavoriteClick = viewModel::toggleFavorite,
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope
                        )
                    }

                    is StudioDetailState.NotFound -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Studio Not Found", color = Color.White)
                        }
                    }

                    is StudioDetailState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Error: ${currentState.message}", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudioDetailContent(
    studio: Studio,
    scenes: List<Scene>,
    performers: List<Performer>,
    selectedTab: StudioDetailTab,
    onTabSelected: (StudioDetailTab) -> Unit,
    onSceneClick: (String, Double?) -> Unit,
    onTagClick: (String) -> Unit,
    onPerformerClick: (String) -> Unit,
    onStudioClick: (String) -> Unit,
    loadMoreScenes: () -> Unit,
    loadMorePerformers: () -> Unit,
    hasMoreScenes: Boolean,
    isLoadingMoreScenes: Boolean,
    hasMorePerformers: Boolean,
    isLoadingMorePerformers: Boolean,
    onRatingChanged: (Int) -> Unit,
    onFavoriteClick: () -> Unit = {},
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    val haptic = LocalStashHapticFeedback.current

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryTabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = Color.Transparent,
            contentColor = StashBlue,
            indicator = { /* Default indicator is fine */ },
            divider = { HorizontalDivider(color = Color.White.copy(alpha = 0.1f)) }
        ) {
            StudioDetailTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = {
                        haptic.perform(StashHapticFeedbackType.Medium)
                        onTabSelected(tab)
                    },
                    text = {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.titleSmall,
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
                StudioDetailTab.OVERVIEW -> StudioOverviewTab(
                    studio = studio,
                    onTagClick = onTagClick,
                    onStudioClick = onStudioClick,
                    onRatingChanged = onRatingChanged,
                    onFavoriteClick = onFavoriteClick,
                    animatedVisibilityScope = animatedVisibilityScope,
                    sharedTransitionScope = sharedTransitionScope
                )
                StudioDetailTab.SCENES -> StudioScenesTab(
                    scenes = scenes,
                    hasMore = hasMoreScenes,
                    isLoadingMore = isLoadingMoreScenes,
                    onSceneClick = onSceneClick,
                    onPerformerClick = onPerformerClick,
                    onStudioClick = onStudioClick,
                    onLoadMore = loadMoreScenes
                )

                StudioDetailTab.PERFORMERS -> StudioPerformersTab(
                    performers = performers,
                    hasMore = hasMorePerformers,
                    isLoadingMore = isLoadingMorePerformers,
                    onPerformerClick = onPerformerClick,
                    onLoadMore = loadMorePerformers
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun StudioOverviewTab(
    studio: Studio,
    onTagClick: (String) -> Unit,
    onStudioClick: (String) -> Unit,
    onRatingChanged: (Int) -> Unit,
    onFavoriteClick: () -> Unit = {},
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    val sharedElementModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        with(sharedTransitionScope) {
            Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(key = "studio_image_${studio.id}"),
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
        contentPadding = PaddingValues(
            start = StashTokens.Spacing.ContentHorizontal,
            end = StashTokens.Spacing.ContentHorizontal,
            top = StashTokens.Spacing.ContentHorizontal,
            bottom = StashTokens.Spacing.ContentHorizontal + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Hero Card (Large Image)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .then(sharedElementModifier)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                if (studio.imagePath != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(studio.imagePath)
                            .crossfade(true)
                            .build(),
                        contentDescription = studio.name,
                        contentScale = ContentScale.Fit,
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
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }

                // Favorite Heart Icon at top-left
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = if (studio.favorite == true) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (studio.favorite == true) "Remove from favorites" else "Add to favorites",
                        tint = if (studio.favorite == true) Color(0xFFE53935) else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Rating
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Studio Rating",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.5f)
                )
                RatingBar(
                    rating = (studio.rating100 ?: 0) / 20,
                    onRatingChanged = onRatingChanged
                )
            }
        }

        // O-Counter
        val oCount = studio.oCounter ?: 0
        if (oCount > 0) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "O-Counter",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sweat_drops),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "$oCount",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Parent Studio
        if (studio.parentStudio != null) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Parent Studio",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .clickable { onStudioClick(studio.parentStudio.id) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (studio.parentStudio.imagePath != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(studio.parentStudio.imagePath)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = studio.parentStudio.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = studio.parentStudio.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Chevron to indicate navigation
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            // Placeholder
                            contentDescription = null,
                            tint = Color.Transparent
                        )
                    }
                }
            }
        }

        // Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StudioStatItem(
                    icon = Icons.Default.Movie,
                    label = "Scenes",
                    value = "${studio.sceneCount ?: 0}",
                    modifier = Modifier.weight(1f)
                )
                StudioStatItem(
                    icon = Icons.Default.People,
                    label = "Performers",
                    value = "${studio.performerCount ?: 0}",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Description
        if (!studio.details.isNullOrEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            modifier = Modifier.size(20.dp),
                            contentDescription = null,
                            tint = StashBlue
                        )
                        Text("About", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = studio.details,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Tags
        if (!studio.tags.isNullOrEmpty()) {
            item {
                TagsSection(

                    tags = studio.tags,
                    onTagClick = onTagClick
                )
            }
        }

        // Aliases
        item {
            AliasesSection(
                aliases = studio.aliases,
                containerColor = StashTheme.colors.tertiary.copy(alpha = StashTokens.Alpha.CardBackground),
                borderColor = StashTheme.colors.tertiary.copy(alpha = StashTokens.Alpha.CardBorder)
            )
        }

        // URLs
        if (!studio.urls.isNullOrEmpty()) {
            item {
                CollapsibleLinks(
                    urls = studio.urls,
                    title = "Links",
                    modifier = Modifier.fillMaxWidth(),
                    showBackground = true
                )
            }
        }

        // External/Stash IDs
        if (!studio.stashIds.isNullOrEmpty()) {
            item {
                ExternalIdsSection(stashIds = studio.stashIds)
            }
        }

        // Dates
        if (studio.createdAt != null || studio.updatedAt != null) {
            item {
                val cardColor = StashTheme.colors.primary.copy(alpha = StashTokens.Alpha.CardBackground)
                val borderColor = StashTheme.colors.primary.copy(alpha = StashTokens.Alpha.CardBorder)

                DetailCard(containerColor = cardColor, borderColor = borderColor) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        var hasPreviousItem = false
                        studio.createdAt?.takeIf { it.isNotEmpty() }?.let {
                            InfoRow(label = "Created", value = DateFormatters.formatTimestamp(it))
                            hasPreviousItem = true
                        }
                        studio.updatedAt?.takeIf { it.isNotEmpty() }?.let {
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

        // Parent/Child Studios could be added here if needed
    }
}

@Composable
fun StudioStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            tint = accentColor ?: StashBlue,
            modifier = Modifier.size(24.dp)
        )
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
            Text(value, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StudioScenesTab(
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
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            hasMore && !isLoadingMore && lastVisibleItemIndex >= totalItemsCount - 5
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(
            start = StashTokens.Spacing.ContentHorizontal,
            end = StashTokens.Spacing.ContentHorizontal,
            top = StashTokens.Spacing.ContentHorizontal,
            bottom = StashTokens.Spacing.ContentHorizontal + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        ),
        verticalArrangement = Arrangement.spacedBy(StashTokens.Spacing.ContentHorizontal)
    ) {
        items(scenes, key = { it.id }) { scene ->
            SceneCard(
                scene = scene,
                onClick = { time -> onSceneClick(scene.id, time) },
                onPerformerClick = onPerformerClick,
                onStudioClick = onStudioClick
            )
        }

        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

@Composable
fun StudioPerformersTab(
    performers: List<Performer>,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onPerformerClick: (String) -> Unit,
    onLoadMore: () -> Unit
) {
    val gridState = rememberLazyGridState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItemsCount = gridState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            hasMore && !isLoadingMore && lastVisibleItemIndex >= totalItemsCount - 6
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (performers.isEmpty() && !isLoadingMore) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No performers found", color = Color.White.copy(alpha = 0.5f))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = StashTokens.Spacing.ContentHorizontal,
                    end = StashTokens.Spacing.ContentHorizontal,
                    top = StashTokens.Spacing.ContentHorizontal,
                    bottom = StashTokens.Spacing.ContentHorizontal + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                ),
                horizontalArrangement = Arrangement.spacedBy(StashTokens.Spacing.ContentVertical),
                verticalArrangement = Arrangement.spacedBy(StashTokens.Spacing.ContentVertical)
            ) {
                items(performers, key = { it.id }) { performer ->
                    PerformerCompactCard(
                        performer = performer,
                        onClick = { onPerformerClick(performer.id) }
                    )
                }

                if (isLoadingMore) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(3) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun DeleteStudioSheetContent(
    studioName: String,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 12.dp, bottom = 32.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Delete Studio",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Are you sure you want to delete $studioName? This action cannot be undone.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        val haptic = LocalStashHapticFeedback.current

        Surface(
            onClick = {
                haptic.perform(StashHapticFeedbackType.Medium)
                onDelete()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Delete Studio",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        TextButton(
            onClick = {
                haptic.perform(StashHapticFeedbackType.Light)
                onCancel()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Cancel",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
