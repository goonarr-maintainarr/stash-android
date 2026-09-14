package goonarr.stash.features.performers.detail

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import goonarr.stash.StashTheme
import goonarr.stash.StashTokens
import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.Scene
import goonarr.stash.core.model.Tag
import goonarr.stash.features.components.AliasesSection
import goonarr.stash.features.components.CollapsibleLinks
import goonarr.stash.features.components.DetailCard
import goonarr.stash.features.components.ExternalIdsSection
import goonarr.stash.features.components.TagsSection
import goonarr.stash.features.components.rememberShimmerBrush
import goonarr.stash.features.performers.components.PerformerBanner
import goonarr.stash.features.performers.formatHeight
import goonarr.stash.features.performers.formatWeight
import goonarr.stash.features.scenes.detail.components.fileinfo.InfoRow
import goonarr.stash.features.scenes.scenecard.SceneCard
import goonarr.stash.util.DateFormatters
import goonarr.stash.util.HeroAccentColor
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.MockData
import goonarr.stash.util.StashHapticFeedbackType
import timber.log.Timber

/**
 * Screen displaying detailed information about a performer.
 * Includes their biography, physical attributes, career info, and a list of scenes they appear in.
 *
 * @param performerId The unique identifier of the performer to display.
 * @param refreshTrigger Optional trigger to force a refresh (e.g., after editing).
 * @param onRefreshConsumed Callback when a refresh has been processed.
 * @param onBackClick Callback to navigate back.
 * @param onSceneClick Callback when a scene is selected.
 * @param onPerformerClick Callback when another performer is selected (e.g., from a scene's performers).
 * @param onStudioClick Callback when a studio is selected.
 * @param onEditClick Callback to navigate to the performer edit screen.
 * @param onScrapeClick Callback to navigate to the performer scrape screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformerDetailScreen(
    viewModel: PerformerDetailViewModel = hiltViewModel(),
    refreshTrigger: Boolean? = null,
    performerId: String,
    onRefreshConsumed: () -> Unit = {},
    onBackClick: () -> Unit,
    onSceneClick: (String, Double?) -> Unit,
    onTagClick: (String) -> Unit,
    onPerformerClick: (String) -> Unit = {},
    onStudioClick: (String) -> Unit = {},
    onEditClick: (String) -> Unit,
    onScrapeClick: (String) -> Unit = {},
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    var heroColor by remember { mutableStateOf<Color?>(null) }
    var showDeleteSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current
    val haptic = LocalStashHapticFeedback.current

    LaunchedEffect(performerId) {
        viewModel.loadPerformer(performerId)
    }

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger == true) {
            Timber.d("🔄 Refreshing performer details triggered by navigation result")
            viewModel.refresh(performerId)
            onRefreshConsumed()
        }
    }

    val performer = (uiState as? PerformerDetailUiState.Success)?.performer
    val stablePath = performer?.imagePath?.substringBefore("?")
    LaunchedEffect(stablePath) {
        if (performer != null && stablePath != null) {
            Timber.d("👤 Performer image changed, extracting color for: $stablePath")
            val color = HeroAccentColor.extract(context, stablePath)
            if (color != null) {
                Timber.d("✨ Extracted hero color for performer: $color")
                heroColor = color
            } else {
                Timber.d("❌ Failed to extract hero color for performer")
            }
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is PerformerDetailUiState.Deleted) {
            onBackClick()
        }
    }

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
            DeletePerformerSheetContent(
                onDelete = {
                    showDeleteSheet = false
                    viewModel.deletePerformer()
                },
                onCancel = { showDeleteSheet = false }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Blurred Background
        if (uiState is PerformerDetailUiState.Success) {
            val successState = uiState as PerformerDetailUiState.Success
            val currentPerformer = successState.performer

            if (currentPerformer.imagePath != null) {
                val context = LocalContext.current
                val stablePath = currentPerformer.imagePath.substringBefore("?")
                val imageRequest = remember(stablePath) {
                    ImageRequest.Builder(context)
                        .data(stablePath)
                        .crossfade(true)
                        .build()
                }

                AsyncImage(
                    model = imageRequest,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(20.dp)
                )

                // Dark Overlay logic (Black with 0.7 opacity)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                )
            }
        }

        // Content Layer - Edge-to-Edge
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is PerformerDetailUiState.Loading -> {
                    PerformerDetailShimmer()
                }

                is PerformerDetailUiState.Success -> {
                    PerformerDetailContent(
                        performer = state.performer,
                        scenes = state.scenes,
                        hasMoreScenes = state.hasMoreScenes,
                        isLoadingMore = state.isLoadingMore,
                        onSceneClick = onSceneClick,
                        onTagClick = onTagClick,
                        onPerformerClick = onPerformerClick,
                        onStudioClick = onStudioClick,
                        onLoadMore = { viewModel.loadMoreScenes(performerId) },
                        heroColor = heroColor,
                        onRatingChanged = { stars -> viewModel.setRatingStars(stars) },
                        onFavoriteClick = { viewModel.toggleFavorite() },
                        animatedVisibilityScope = animatedVisibilityScope,
                        sharedTransitionScope = sharedTransitionScope
                    )
                }

                is PerformerDetailUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }

                is PerformerDetailUiState.Deleted -> {
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
        }

        // Top App Bar Layer - Overlay
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
                // Hero image is behind
                navigationIconContentColor = Color.White
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
                        shape = RoundedCornerShape(StashTokens.Radius.Card),
                        shadowElevation = 8.dp
                    ) {
                        DropdownMenuItem(
                            text = { Text("Scrape") },
                            onClick = {
                                haptic.perform(StashHapticFeedbackType.Medium)
                                showMenu = false
                                onScrapeClick(performerId)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = heroColor ?: MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                haptic.perform(StashHapticFeedbackType.Medium)
                                showMenu = false
                                onEditClick(performerId)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = heroColor ?: MaterialTheme.colorScheme.primary
                                )
                            }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
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
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PerformerDetailContent(
    modifier: Modifier = Modifier,
    performer: Performer,
    scenes: List<Scene> = emptyList(),
    hasMoreScenes: Boolean = false,
    isLoadingMore: Boolean = false,
    onSceneClick: (String, Double?) -> Unit = { _, _ -> },
    onTagClick: (String) -> Unit = {},
    onPerformerClick: (String) -> Unit = {},
    onStudioClick: (String) -> Unit = {},
    onLoadMore: () -> Unit = {},
    heroColor: Color? = null,
    onRatingChanged: (Int) -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItemsCount = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex =
                listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            hasMoreScenes && !isLoadingMore && lastVisibleItemIndex >= totalItemsCount - 5
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            onLoadMore()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        heroColor?.copy(alpha = 0.25f) ?: Color.Transparent,
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = 1200f
                )
            ),
        state = listState,
        contentPadding = PaddingValues(
            // TopBar height approx
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 64.dp,
            bottom = StashTokens.Spacing.ContentHorizontal + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        )
    ) {
        // 1. Hero Image
        item {
            val sharedElementModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                with(sharedTransitionScope) {
                    Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState(key = "performer_image_${performer.id}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ ->
                            tween(durationMillis = 300)
                        }
                    )
                }
            } else {
                Modifier
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = StashTokens.Spacing.ContentHorizontal)
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .then(sharedElementModifier)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                PerformerBanner(
                    performer = performer,
                    isInteractive = true,
                    showDetails = true,
                    showCountry = true,
                    onFavoriteClick = onFavoriteClick,
                    onRatingChange = onRatingChanged,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 2. Stats & Bio Section (Padded)
        item {
            Column(modifier = Modifier.padding(StashTokens.Spacing.ContentHorizontal)) {
                PerformerCareerInfoSection(performer, heroColor)
                Spacer(modifier = Modifier.height(StashTokens.Spacing.ContentHorizontal))
                PerformerPhysicalInfoSection(performer, heroColor)

                AliasesSection(aliases = performer.aliasList)

                BodyModificationsSection(
                    tattoos = performer.tattoos,
                    piercings = performer.piercings
                )

                if (!performer.urls.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CollapsibleLinks(
                        urls = performer.urls,
                        title = "Links",
                        modifier = Modifier.fillMaxWidth(),
                        showBackground = true
                    )
                }

                if (!performer.stashIds.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    ExternalIdsSection(
                        stashIds = performer.stashIds,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Dates
                if (performer.createdAt != null || performer.updatedAt != null) {
                    val cardColor = StashTheme.colors.primary.copy(alpha = StashTokens.Alpha.CardBackground)
                    val borderColor = StashTheme.colors.primary.copy(alpha = StashTokens.Alpha.CardBorder)

                    DetailCard(containerColor = cardColor, borderColor = borderColor) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            var hasPreviousItem = false
                            performer.createdAt?.takeIf { it.isNotEmpty() }?.let {
                                InfoRow(label = "Created", value = DateFormatters.formatTimestamp(it))
                                hasPreviousItem = true
                            }
                            performer.updatedAt?.takeIf { it.isNotEmpty() }?.let {
                                if (hasPreviousItem) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = StashTokens.Alpha.Divider))
                                }
                                InfoRow(label = "Updated", value = DateFormatters.formatTimestamp(it))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Biography
                if (!performer.details.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Biography",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Text(
                        text = performer.details,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 3. Tags Section (Edge-to-Edge Carousel)
        if (!performer.tags.isNullOrEmpty()) {
            item {
                TagsSection(
                    tags = performer.tags?.map { Tag(id = it.id, name = it.name) } ?: emptyList(),
                    onTagClick = onTagClick,
                    modifier = Modifier.padding(horizontal = StashTokens.Spacing.ContentHorizontal)
                )
                Spacer(modifier = Modifier.height(StashTokens.Spacing.ContentHorizontal))
            }
        }

        // 4. Scenes Section Header
        if (scenes.isNotEmpty()) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Scenes",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text("•", style = MaterialTheme.typography.titleLarge)
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "${performer.sceneCount ?: scenes.size}",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }

        // 5. Scenes List
        items(scenes, key = { it.id }) { scene ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SceneCard(
                    scene = scene,
                    onClick = { time -> onSceneClick(scene.id, time) },
                    onPerformerClick = onPerformerClick,
                    onStudioClick = onStudioClick,
                )
            }
        }

        // 6. Loading Indicator
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp
                    )
                }
            }
        }
    }
}

@Composable
fun PerformerCareerInfoSection(performer: Performer, accentColor: Color?) {
    val items = listOfNotNull(
        performer.birthdate?.let { Attribute("Born", it, Icons.Default.CalendarToday) },
        performer.careerLength?.let { Attribute("Career", it, Icons.Default.Work) }
    )

    if (items.isEmpty()) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items.forEach { attribute ->
            Box(modifier = Modifier.weight(1f)) {
                AttributeRow(attribute, accentColor)
            }
        }
        // If only 1 item, fill the empty space to keep sizing consistent if we wanted,
        // but here we probably want it to stretch or just be 50% width.
        // Let's add a spacer if there's only 1 item to effectively make it a 2-column grid row
        if (items.size == 1) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun PerformerPhysicalInfoSection(performer: Performer, accentColor: Color?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Physical",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val attributes = listOfNotNull(
            performer.gender?.let {
                Attribute(
                    "Gender",
                    it.replaceFirstChar { char -> char.uppercase() },
                    Icons.Default.Person
                )
            },
            performer.ethnicity?.let { Attribute("Ethnicity", it, Icons.Default.Public) },
            performer.heightCm?.let {
                // Straighten as ruler
                Attribute(
                    "Height",
                    formatHeight(it),
                    Icons.Default.Straighten
                )
            },
            performer.weight?.let { Attribute("Weight", formatWeight(it), Icons.Default.MonitorWeight) },
            performer.measurements?.takeIf { it.isNotEmpty() }
                ?.let { Attribute("Measurements", it, Icons.Default.AspectRatio) },
            performer.eyeColor?.takeIf { it.isNotEmpty() }
                ?.let { Attribute("Eyes", it.replaceFirstChar { char -> char.uppercase() }, Icons.Default.Visibility) },
            performer.hairColor?.takeIf { it.isNotEmpty() }
                ?.let { Attribute("Hair", it.replaceFirstChar { char -> char.uppercase() }, Icons.Default.Face) },
            performer.fakeTits?.takeIf { it.isNotEmpty() }?.let { Attribute("Augmented", it, Icons.Default.Favorite) },
        )

        // rudimentary 2-column grid
        attributes.chunked(2).forEach { rowAttributes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowAttributes.forEach { attribute ->
                    Box(modifier = Modifier.weight(1f)) {
                        AttributeRow(attribute, accentColor)
                    }
                }
                // If odd number, fill the empty space to keep alignment
                if (rowAttributes.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

data class Attribute(val label: String, val value: String, val icon: ImageVector)

@Composable
fun AttributeRow(attribute: Attribute, accentColor: Color?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = attribute.icon,
            contentDescription = null,
            // iOS used blue, primary is likely closest
            tint = accentColor ?: MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = attribute.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
            Text(
                text = attribute.value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// PerformerAliasesSection removed (replaced by reusable AliasesSection)

@Composable
fun BodyModificationsSection(tattoos: String?, piercings: String?) {
    if (tattoos.isNullOrEmpty() && piercings.isNullOrEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Body Modifications",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!tattoos.isNullOrEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Tattoos",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = tattoos,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (!piercings.isNullOrEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Piercings",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = piercings,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun PerformerDetailShimmer(modifier: Modifier = Modifier) {
    val brush = rememberShimmerBrush()
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Hero Image Shimmer
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(500.dp) // Approximate height since actual image uses intrinsic sizing
                .clip(RoundedCornerShape(12.dp))
                .background(brush)
        ) {
            // Hero Overlay Shimmer
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Name
                Box(
                    modifier = Modifier
                        .height(32.dp)
                        .fillMaxWidth(0.7f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.3f))
                )

                // Stats Row
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(60.dp, 16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.3f))
                    )
                    Box(
                        modifier = Modifier
                            .size(40.dp, 16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.3f))
                    )
                    Box(
                        modifier = Modifier
                            .size(40.dp, 16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.3f))
                    )
                }

                // Country
                Box(
                    modifier = Modifier
                        .size(80.dp, 16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.3f))
                )
            }
        }

        // 1. Stats & Bio (Padded)
        Column(modifier = Modifier.padding(16.dp)) {
            // Career Info Shimmer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(brush)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Physical Info Shimmer (Grid)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )

                repeat(4) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(brush)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(brush)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Aliases Shimmer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body Mods Shimmer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .width(60.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.3f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                }
            }

            // Bio Shimmer
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Spacer(modifier = Modifier.height(12.dp))
            repeat(4) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // 2. Tags Shimmer (Edge-to-Edge)
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .width(80.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(brush)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Spacer(modifier = Modifier.width(16.dp))
            repeat(5) {
                Box(
                    modifier = Modifier
                        .size(80.dp, 32.dp)
                        .clip(CircleShape)
                        .background(brush)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
        }

        // 3. Scenes Shimmer (Padded)
        Column(modifier = Modifier.padding(16.dp)) {
            // Scenes Shimmer
            Spacer(modifier = Modifier.height(24.dp))
            // Header
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(100.dp, 28.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .size(24.dp, 24.dp)
                        .clip(CircleShape)
                        .background(brush)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // List
            repeat(3) {
                SceneCard(
                    scene = MockData.scene.copy(id = "loading_$it"),
                    onClick = {},
                    isLoading = true
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun DeletePerformerSheetContent(
    onDelete: () -> Unit,
    onCancel: () -> Unit,
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
            text = "Delete Performer",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Are you sure you want to delete this performer? This action cannot be undone.",
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
                    text = "Delete Performer",
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
