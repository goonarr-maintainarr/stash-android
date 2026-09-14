package goonarr.stash.features.scenes.detail

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.TextureView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import goonarr.stash.LocalBlurNsfw
import goonarr.stash.LocalStashDynamicColors
import goonarr.stash.StashBlue
import goonarr.stash.StashTokens
import goonarr.stash.core.model.Scene
import goonarr.stash.features.components.ExternalIdsSection
import goonarr.stash.features.components.StaggeredEntry
import goonarr.stash.features.components.TagsSection
import goonarr.stash.features.player.LocalGlobalPlayerState
import goonarr.stash.features.player.VideoPlayer
import goonarr.stash.features.player.VideoScrubber
import goonarr.stash.features.scenes.detail.components.description.SceneDescriptionSection
import goonarr.stash.features.scenes.detail.components.fileinfo.FileInfoSection
import goonarr.stash.features.scenes.detail.components.header.SceneHeader
import goonarr.stash.features.scenes.detail.components.history.SceneHistorySection
import goonarr.stash.features.scenes.detail.components.markers.SceneMarkersSection
import goonarr.stash.features.scenes.detail.components.metadata.SceneAdditionalMetadataSection
import goonarr.stash.features.scenes.detail.components.performers.PerformersSection
import goonarr.stash.features.scenes.detail.components.sheets.DeleteSceneSheetContent
import goonarr.stash.features.scenes.detail.components.sheets.GenerateScreenshotSheetContent
import goonarr.stash.features.scenes.detail.components.shimmer.SceneDetailShimmer
import goonarr.stash.features.scenes.utils.SpriteManager
import goonarr.stash.rememberSceneColors
import goonarr.stash.util.DateFormatters
import goonarr.stash.util.HeroAccentColor
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@SuppressLint("UnsafeOptInUsageError")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SceneDetailScreen(
    sceneId: String,
    startTimestamp: Double? = null,
    onBackClick: () -> Unit,
    onPerformerClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onScrapeClick: (String) -> Unit,
    onAddMarkerClick: (String, Double) -> Unit,
    onTagClick: (String) -> Unit,
    onStudioClick: (String) -> Unit = {},
    viewModel: SceneDetailViewModel = hiltViewModel(),
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    var heroColor by remember { mutableStateOf<Color?>(null) }
    val context = LocalContext.current
    val playerState = LocalGlobalPlayerState.current
    val haptic = LocalStashHapticFeedback.current

    LaunchedEffect(sceneId) {
        viewModel.loadScene(sceneId)
    }

    LaunchedEffect(uiState) {
        if (uiState is SceneDetailUiState.Success) {
            val scene = (uiState as SceneDetailUiState.Success).scene
            Timber.d("🎬 Scene success, extracting color for: ${scene.paths?.screenshot}")
            val color = HeroAccentColor.extract(context, scene.paths?.screenshot)
            if (color != null) {
                Timber.d("✨ Extracted hero color for scene: $color")
                heroColor = color
            } else {
                Timber.d("❌ Failed to extract hero color for scene")
            }
        } else if (uiState is SceneDetailUiState.Deleted) {
            onBackClick()
        }
    }

    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState()
    var showDeleteSheet by remember { mutableStateOf(false) }
    var deleteFile by remember { mutableStateOf(true) }
    var deleteGenerated by remember { mutableStateOf(true) }
    // State for managing the "Generate Screenshot" modal and its preview
    var showGenerateSheet by remember { mutableStateOf(false) }
    // Reference to the PlayerView to allow frame capture from the underlying TextureView
    var playerView by remember { mutableStateOf<PlayerView?>(null) }
    // The captured frame bitmap to display as a preview in the modal
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    // Extracted hero color from the captured bitmap for live modal styling
    var previewHeroColor by remember { mutableStateOf<Color?>(null) }
    val scope = rememberCoroutineScope()

    // Lift the triadic color scheme to the root to ensure modals inherit the same colors
    val dynamicColors = rememberSceneColors(heroColor)

    CompositionLocalProvider(LocalStashDynamicColors provides dynamicColors) {
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                    )
                }
            ) {
                DeleteSceneSheetContent(
                    deleteFile = deleteFile,
                    onDeleteFileChange = { deleteFile = it },
                    deleteGenerated = deleteGenerated,
                    onDeleteGeneratedChange = { deleteGenerated = it },
                    onDeleteConfirm = {
                        showDeleteSheet = false
                        viewModel.deleteScene(deleteFile, deleteGenerated)
                    },
                    onCancel = { showDeleteSheet = false }
                )
            }
        }

        if (showGenerateSheet) {
            ModalBottomSheet(
                onDismissRequest = { showGenerateSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = {
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .size(32.dp, 4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                shape = CircleShape
                            )
                    )
                }
            ) {
                val currentSeconds = playerState.player?.currentPosition?.div(1000.0) ?: 0.0
                val screenshotColors = rememberSceneColors(previewHeroColor ?: heroColor)

                CompositionLocalProvider(LocalStashDynamicColors provides screenshotColors) {
                    GenerateScreenshotSheetContent(
                        previewBitmap = capturedBitmap,
                        onGenerate = {
                            showGenerateSheet = false
                            viewModel.generateScreenshot(sceneId, currentSeconds)
                        },
                        onCancel = { showGenerateSheet = false }
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Blurred Background
            if (uiState is SceneDetailUiState.Success) {
                val scene = (uiState as SceneDetailUiState.Success).scene
                val screenshot = scene.paths?.screenshot

                if (screenshot != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(screenshot)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(20.dp)
                    )

                    // Dark Overlay logic from iOS (Black with 0.7 opacity)
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
                    is SceneDetailUiState.Loading -> {
                        SceneDetailShimmer()
                    }

                    is SceneDetailUiState.Success -> {
                        SceneDetailContent(
                            scene = state.scene,
                            onPerformerClick = onPerformerClick,
                            onTagClick = onTagClick,
                            onStudioClick = onStudioClick,
                            heroColor = heroColor,
                            spriteManager = viewModel.spriteManager,
                            getStreamUrl = viewModel::getStreamUrl,
                            onRateScene = viewModel::rateScene,
                            onIncrementOCounter = viewModel::incrementOCounter,
                            onPlayerViewCreated = { playerView = it },
                            scrollState = scrollState,
                            startTimestamp = startTimestamp,
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope
                        )
                    }

                    is SceneDetailUiState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = state.message, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    is SceneDetailUiState.Deleted -> {
                        // Do nothing, navigation is handled by LaunchedEffect
                    }
                }
            }

            // Top App Bar Layer - Overlay
            val iconColor = dynamicColors.complementary
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = iconColor
                        )
                    }
                },
                actions = {
                    // PiP / Minimize Button
                    if (playerState.currentSceneId == sceneId && !playerState.isMinimized) {
                        val haptic = LocalStashHapticFeedback.current
                        IconButton(
                            onClick = {
                                haptic.perform(StashHapticFeedbackType.Medium)
                                playerState.minimize()
                                onBackClick() // Navigates back, leaving player active
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureInPicture,
                                contentDescription = "Minimize Player",
                                tint = iconColor
                            )
                        }
                    }

                    var showMenu by remember { mutableStateOf(false) }
                    val haptic = LocalStashHapticFeedback.current

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = iconColor
                            )
                        }

                        val menuColor = heroColor ?: StashBlue
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            containerColor = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(StashTokens.Radius.Card),
                            shadowElevation = 8.dp
                        ) {
                            DropdownMenuItem(
                                text = { Text("Scrape", color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    haptic.perform(StashHapticFeedbackType.Medium)
                                    showMenu = false
                                    onScrapeClick(sceneId)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = menuColor
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit", color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    haptic.perform(StashHapticFeedbackType.Medium)
                                    showMenu = false
                                    onEditClick(sceneId)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = menuColor
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Add Marker", color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    haptic.perform(StashHapticFeedbackType.Medium)
                                    showMenu = false
                                    val seconds = playerState.player?.currentPosition?.div(1000.0) ?: 0.0
                                    playerState.player?.pause()
                                    onAddMarkerClick(sceneId, seconds)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = menuColor
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Generate Screenshot", color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    haptic.perform(StashHapticFeedbackType.Medium)
                                    showMenu = false
                                    // Capture the current frame from the internal TextureView
                                    // This requires the VideoPlayer to be configured with surface_type="texture_view"
                                    val bitmap = (playerView?.videoSurfaceView as? TextureView)?.bitmap
                                    capturedBitmap = bitmap
                                    // Extract live accent color from the captured frame
                                    if (bitmap != null) {
                                        scope.launch {
                                            previewHeroColor = HeroAccentColor.extractFromBitmap(bitmap)
                                        }
                                    } else {
                                        previewHeroColor = null
                                    }
                                    showGenerateSheet = true
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = null,
                                        tint = menuColor
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Rescan Scene", color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    haptic.perform(StashHapticFeedbackType.Medium)
                                    showMenu = false
                                    val path = (uiState as? SceneDetailUiState.Success)?.scene?.files?.firstOrNull()?.path
                                    if (path != null) {
                                        viewModel.rescanScene(path)
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = menuColor
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Generate", color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    haptic.perform(StashHapticFeedbackType.Medium)
                                    showMenu = false
                                    val sceneId = (uiState as? SceneDetailUiState.Success)?.scene?.id
                                    if (sceneId != null) {
                                        viewModel.generate(sceneId)
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = menuColor
                                    )
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Generate Default Thumbnail", color = MaterialTheme.colorScheme.onSurface) },
                                onClick = {
                                    haptic.perform(StashHapticFeedbackType.Medium)
                                    showMenu = false
                                    val sceneId = (uiState as? SceneDetailUiState.Success)?.scene?.id
                                    if (sceneId != null) {
                                        viewModel.generateDefaultThumbnail(sceneId)
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = menuColor
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
                    containerColor = (
                        heroColor?.copy(alpha = 0.1f)
                            ?.compositeOver(
                                background = MaterialTheme.colorScheme.surface
                            )
                            ?: MaterialTheme.colorScheme.surface
                        ).copy(
                        alpha = (scrollState.value.toFloat() / with(LocalDensity.current) { 200.dp.toPx() }).coerceIn(0f, 1f)
                    ),
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SceneDetailContent(
    scene: Scene,
    onPerformerClick: (String) -> Unit,
    onTagClick: (String) -> Unit,
    onStudioClick: (String) -> Unit = {},
    heroColor: Color? = null,
    spriteManager: SpriteManager? = null,
    getStreamUrl: suspend (String?) -> String?,
    onRateScene: (Int) -> Unit,
    onIncrementOCounter: () -> Unit,
    onPlayerViewCreated: (PlayerView) -> Unit,
    scrollState: ScrollState,
    startTimestamp: Double? = null,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    val playerState = LocalGlobalPlayerState.current
    val density = LocalDensity.current

    // Scrubbing state for time overlay
    var scrubPosition by remember { mutableFloatStateOf(0f) }
    var isScrubbing by remember { mutableStateOf(false) }
    var showTimeOverlay by remember { mutableStateOf(false) }
    val isPlaying = playerState.isPlaying

    // Dynamic Layout: Staggered expansion
    val playBarThresholdPx = with(density) { StashTokens.Radius.Card.toPx() }
    val videoThresholdPx = with(density) { (60.dp + StashTokens.Radius.Card).toPx() } // PlayBar + Spacer

    val isPlayBarExpanded by remember { derivedStateOf { scrollState.value > playBarThresholdPx } }
    val isVideoExpanded by remember { derivedStateOf { scrollState.value > videoThresholdPx } }

    val videoPadding by animateDpAsState(
        targetValue = if (isVideoExpanded) 0.dp else StashTokens.Radius.Small,
        label = "videoPadding"
    )
    val videoElevation by animateDpAsState(
        targetValue = if (isVideoExpanded) 24.dp else StashTokens.Radius.Medium,
        label = "videoElevation"
    )

    val playBarPadding by animateDpAsState(
        targetValue = if (isPlayBarExpanded) 2.dp else 6.dp,
        label = "playBarPadding"
    )
    val playBarElevation by animateDpAsState(
        targetValue = if (isPlayBarExpanded) 8.dp else 0.dp,
        label = "playBarElevation"
    )

    // Update scrubPosition when paused to show current time
    LaunchedEffect(isPlaying, playerState.player) {
        if (!isPlaying && playerState.player != null) {
            scrubPosition = playerState.player!!.currentPosition / 1000f
            showTimeOverlay = true
        }
    }

    // Fade out time overlay 2 seconds after scrubbing stops, but only if playing
    LaunchedEffect(isScrubbing, isPlaying) {
        if (!isScrubbing && showTimeOverlay && isPlaying) {
            delay(2000)
            showTimeOverlay = false
        } else if (!isPlaying) {
            // Keep showing when paused
            showTimeOverlay = true
        }
    }
    // Visibility state for staggered entry
    var contentVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        contentVisible = true
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        // Calculate video height based on 16:9 aspect ratio
        val videoHeight = screenWidth * (9f / 16f)
        val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 64.dp

        // Height of the sticky area (Video + Padding)
        val stickyHeaderHeight = videoHeight + topPadding

        // Play Bar Height (approx) + Padding
        val playBarHeight = 60.dp

        // Scrollable Content
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(StashTokens.Spacing.ContentHorizontal)
            ) {
                // Spacer for Sticky Video Area (Video + 4dp + PlayBar)
                Spacer(modifier = Modifier.height(stickyHeaderHeight + playBarHeight))

                StaggeredEntry(visible = contentVisible, index = 2) {
                    Box(modifier = Modifier.padding(horizontal = StashTokens.Spacing.ContentHorizontal)) {
                        SceneHeader(
                            scene = scene,
                            onRateScene = onRateScene,
                            onIncrementOCounter = onIncrementOCounter,
                            onStudioClick = onStudioClick,
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope
                        )
                    }
                }

                StaggeredEntry(visible = contentVisible, index = 3) {
                    SceneDescriptionSection(scene.details)
                }

                StaggeredEntry(visible = contentVisible, index = 4) {
                    TagsSection(
                        modifier = Modifier.fillMaxWidth(),
                        tags = scene.tags ?: emptyList(),
                        onTagClick = onTagClick,
                        horizontalPadding = StashTokens.Spacing.ContentHorizontal,
                        animatedVisibilityScope = animatedVisibilityScope,
                        sharedTransitionScope = sharedTransitionScope
                    )
                }

                StaggeredEntry(visible = contentVisible, index = 5) {
                    SceneMarkersSection(
                        markers = scene.sceneMarkers ?: emptyList(),
                        onSeek = { seconds ->
                            val wasPlaying = playerState.player?.isPlaying == true
                            playerState.player?.seekTo((seconds * 1000).toLong())
                            playerState.player?.prepare()
                            if (wasPlaying) {
                                playerState.player?.play()
                            }
                        },
                        getStreamUrl = getStreamUrl
                    )
                }

                StaggeredEntry(visible = contentVisible, index = 6) {
                    PerformersSection(
                        performers = scene.performers ?: emptyList(),
                        onPerformerClick = onPerformerClick,
                        animatedVisibilityScope = animatedVisibilityScope,
                        sharedTransitionScope = sharedTransitionScope
                    )
                }

                StaggeredEntry(visible = contentVisible, index = 7) {
                    SceneHistorySection(scene)
                }

                StaggeredEntry(visible = contentVisible, index = 8) {
                    SceneAdditionalMetadataSection(scene)
                }

                StaggeredEntry(visible = contentVisible, index = 9) {
                    ExternalIdsSection(
                        stashIds = scene.stashIds ?: emptyList(),
                        modifier = Modifier.padding(horizontal = StashTokens.Spacing.ContentHorizontal)
                    )
                }

                StaggeredEntry(visible = contentVisible, index = 10) {
                    scene.files?.firstOrNull()?.let { file ->
                        FileInfoSection(file)
                    }
                }

                val bottomPadding =
                    WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding() + 24.dp
                Spacer(modifier = Modifier.height(bottomPadding))
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(top = topPadding)
                    .zIndex(5f)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // VIDEO PLAYER CONTENT
                    var mainStreamUrl by remember { mutableStateOf<String?>(null) }
                    val durationMs =
                        ((scene.files?.firstOrNull()?.duration ?: 0.0) * 1000).toLong()

                    LaunchedEffect(scene.paths?.stream, heroColor) {
                        val url = getStreamUrl(scene.paths?.stream)
                        mainStreamUrl = url
                        val isBusy =
                            playerState.isPlaying && playerState.currentSceneId != scene.id
                        if (url != null && !isBusy) {
                            playerState.play(
                                url = url,
                                sceneId = scene.id,
                                title = scene.title,
                                image = scene.paths?.screenshot,
                                autoPlay = false,
                                markers = scene.sceneMarkers ?: emptyList(),
                                spriteManager = spriteManager,
                                tintColor = heroColor,
                                startTimeSeconds = startTimestamp ?: scene.resumeTime
                            )
                        }
                    }

                    // Video Layout
                    StaggeredEntry(visible = contentVisible, index = 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = videoPadding)
                                .shadow(
                                    elevation = videoElevation,
                                    shape = RoundedCornerShape(8.dp),
                                    spotColor = heroColor ?: Color.Black,
                                    ambientColor = heroColor ?: Color.Black
                                )
                                .clip(RoundedCornerShape(8.dp))
                                .aspectRatio(16f / 9f)
                                .background(Color.Black)
                                .onGloballyPositioned { coordinates ->
                                    val position =
                                        coordinates.parentLayoutCoordinates?.localToWindow(
                                            Offset.Zero
                                        )
                                    val size = coordinates.size.toSize()
                                    if (position != null) {
                                        playerState.sourceBounds = Rect(
                                            offset = position,
                                            size = size
                                        )
                                    }
                                }
                        ) {
                            val isPlayingThis = playerState.currentSceneId == scene.id
                            if (mainStreamUrl != null && isPlayingThis) {
                                VideoPlayer(
                                    url = mainStreamUrl,
                                    isMuted = false,
                                    useController = false,
                                    autoPlay = false,
                                    externalPlayer = playerState.player,
                                    onPlayerViewCreated = onPlayerViewCreated
                                )
                            } else {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(scene.paths?.screenshot).crossfade(true).build(),
                                    contentDescription = scene.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .then(if (LocalBlurNsfw.current) Modifier.blur(20.dp) else Modifier)
                                )
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.3f))
                                )
                                if (mainStreamUrl != null) {
                                    IconButton(
                                        onClick = {
                                            mainStreamUrl?.let { url ->
                                                playerState.play(
                                                    url = url,
                                                    sceneId = scene.id,
                                                    title = scene.title,
                                                    image = scene.paths?.screenshot,
                                                    markers = scene.sceneMarkers ?: emptyList(),
                                                    spriteManager = spriteManager,
                                                    tintColor = heroColor
                                                )
                                            }
                                        },
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(64.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            "Play",
                                            Modifier.size(64.dp),
                                            Color.White
                                        )
                                    }
                                }
                            }

                            // Runtime Overlay
                            this@Column.AnimatedVisibility(
                                visible = showTimeOverlay,
                                enter = fadeIn(),
                                exit = fadeOut(),
                                modifier = Modifier.align(Alignment.BottomEnd)
                            ) {
                                Text(
                                    text = DateFormatters.formatDuration(scrubPosition.toDouble()),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .background(
                                            Color.Black.copy(alpha = 0.7f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    // Spacer between Video and PlayBar
                    Spacer(modifier = Modifier.height(12.dp))

                    // Play Bar
                    if (playerState.player != null && durationMs > 0) {
                        StaggeredEntry(visible = contentVisible, index = 1) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = playBarPadding)
                                    .shadow(
                                        elevation = playBarElevation,
                                        shape = CircleShape,
                                        spotColor = heroColor ?: Color.Black,
                                        ambientColor = heroColor ?: Color.Black
                                    )
                            ) {
                                VideoScrubber(
                                    player = playerState.player,
                                    durationMs = durationMs,
                                    tintColor = heroColor ?: Color.Red,
                                    markers = scene.sceneMarkers ?: emptyList(),
                                    spriteManager = spriteManager,
                                    onScrubbing = { position, scrubbing ->
                                        scrubPosition = position
                                        isScrubbing = scrubbing
                                        if (scrubbing) showTimeOverlay = true
                                    }
                                )
                            }
                        }
                    }
                } // Close Column
            } // Close Content Box
        } // Close Sticky Header Box
    } // Close BoxWithConstraints
}
