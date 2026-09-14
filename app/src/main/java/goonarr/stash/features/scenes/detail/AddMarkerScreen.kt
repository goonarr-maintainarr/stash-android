package goonarr.stash.features.scenes.detail

import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import goonarr.stash.LocalStashDynamicColors
import goonarr.stash.StashTheme
import goonarr.stash.StashTokens
import goonarr.stash.core.model.Scene
import goonarr.stash.core.model.Tag
import goonarr.stash.features.components.SearchField
import goonarr.stash.features.components.StaggeredEntry
import goonarr.stash.features.components.TagEditItem
import goonarr.stash.features.components.TagSearchItem
import goonarr.stash.features.player.LocalGlobalPlayerState
import goonarr.stash.features.player.VideoScrubber
import goonarr.stash.features.scenes.utils.SpriteManager
import goonarr.stash.rememberSceneColors
import goonarr.stash.util.DateFormatters
import goonarr.stash.util.HeroAccentColor
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.MockData
import goonarr.stash.util.StashHapticFeedbackType
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Full-screen host for the "Add Marker" flow.
 *
 * This screen provides a [Scaffold] with a [TopAppBar] and handles the different
 * UI states (Loading, Success, Error) by delegating the main content to [AddMarkerContent].
 *
 * @param onBackClick Navigation callback to return to the previous screen.
 * @param viewModel The state holder for marker creation, loaded via Hilt.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMarkerScreen(
    onBackClick: () -> Unit,
    viewModel: AddMarkerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val playerState = LocalGlobalPlayerState.current
    val context = LocalContext.current
    val screenshotUrl by viewModel.screenshotUrl.collectAsState()
    var heroColor by remember { mutableStateOf<Color?>(null) }

    LaunchedEffect(screenshotUrl) {
        screenshotUrl?.let { url ->
            HeroAccentColor.extract(context, url)?.let { heroColor = it }
        }
    }

    val sceneColors = rememberSceneColors(heroColor)
    var markerTitle by remember { mutableStateOf("") }
    var selectedTagId by remember { mutableStateOf<String?>(null) }
    val selectedAdditionalTagIds = remember { mutableStateListOf<String>() }
    var tagSearchText by remember { mutableStateOf("") }
    var startSeconds by remember { mutableDoubleStateOf(viewModel.initialSeconds) }
    var endSeconds by remember { mutableStateOf<Double?>(null) }

    CompositionLocalProvider(LocalStashDynamicColors provides sceneColors) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Blurred Background
            if (screenshotUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(screenshotUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(20.dp)
                )

                // Dark Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                )
            }

            val dynamicColors = StashTheme.colors
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Add Marker") },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            Button(
                                onClick = {
                                    if (selectedTagId != null && startSeconds.isFinite() &&
                                        (endSeconds == null || endSeconds!!.isFinite())
                                    ) {
                                        viewModel.createMarker(
                                            title = markerTitle.ifBlank { "Marker" },
                                            seconds = startSeconds,
                                            endSeconds = endSeconds,
                                            primaryTagId = selectedTagId!!,
                                            additionalTagIds = selectedAdditionalTagIds.toList(),
                                            onSuccess = onBackClick
                                        )
                                    }
                                },
                                enabled = selectedTagId != null,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = sceneColors.primary,
                                    disabledContainerColor = Color.Transparent,
                                    disabledContentColor = sceneColors.primary.copy(alpha = 0.3f)
                                )
                            ) {
                                Text(
                                    text = "Create",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = sceneColors.complementary,
                            navigationIconContentColor = sceneColors.complementary
                        )
                    )
                },
                containerColor = Color.Transparent
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding())
                        .background(Color.Transparent)
                ) {
                    when (val state = uiState) {
                        is SceneDetailUiState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = sceneColors.primary
                            )
                        }

                        is SceneDetailUiState.Success -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                AddMarkerContent(
                                    scene = state.scene,
                                    allTags = state.allTags,
                                    markerTitle = markerTitle,
                                    onMarkerTitleChange = { markerTitle = it },
                                    selectedTagId = selectedTagId,
                                    onSelectedTagIdChange = { selectedTagId = it },
                                    selectedAdditionalTagIds = selectedAdditionalTagIds,
                                    tagSearchText = tagSearchText,
                                    onTagSearchTextChange = { tagSearchText = it },
                                    startSeconds = startSeconds,
                                    onStartSecondsChange = { startSeconds = it },
                                    endSeconds = endSeconds,
                                    onEndSecondsChange = { endSeconds = it },
                                    externalPlayer = playerState.player,
                                    spriteManager = viewModel.spriteManager,
                                    getStreamUrl = { viewModel.getStreamUrl(it) }
                                )
                            }
                        }

                        is SceneDetailUiState.Error -> {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(16.dp)
                            )
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}

/**
 * # AddMarkerContent
 *
 * The primary UI component for selecting time ranges and tagging scene markers.
 *
 * ### Features:
 * - **Unified Time Scrubber**: A single [VideoScrubber] handles both start and end times.
 * - **Interactive Time Boxes**: Toggle between [ActiveTimeMode.START] and [ActiveTimeMode.END]
 *   by clicking the corresponding [TimeBox].
 * - **Smart Preview**: Automatically loops video playback within the selected range when enabled.
 * - **Hierarchical Tagging**: Supports a required primary tag and optional additional tags.
 *
 * @param scene The scene being tagged.
 * @param allTags Comprehensive list of all available tags for primary selection.
 * @param initialSeconds The baseline time where the user started the "Add Marker" action.
 * @param heroColor Theme accent color (optional).
 * @param externalPlayer Shared player instance to avoid OOM by reusing decoders.
 * @param spriteManager Optional sprite manager for thumbnail previews.
 * @param onDismiss Navigation callback for cancellation.
 * @param onCreate Submission callback for marker creation.
 * @param getStreamUrl Resolver for the scene's media stream.
 */
@OptIn(ExperimentalMaterial3Api::class, UnstableApi::class)
@Composable
fun AddMarkerContent(
    scene: Scene,
    allTags: List<Tag>,
    markerTitle: String,
    onMarkerTitleChange: (String) -> Unit,
    selectedTagId: String?,
    onSelectedTagIdChange: (String?) -> Unit,
    selectedAdditionalTagIds: MutableList<String>,
    tagSearchText: String,
    onTagSearchTextChange: (String) -> Unit,
    startSeconds: Double,
    onStartSecondsChange: (Double) -> Unit,
    endSeconds: Double?,
    onEndSecondsChange: (Double?) -> Unit,
    externalPlayer: ExoPlayer? = null,
    spriteManager: SpriteManager? = null,
    getStreamUrl: suspend (String?) -> String?
) {
    val context = LocalContext.current
    val haptic = LocalStashHapticFeedback.current
    val isInspectionMode = LocalInspectionMode.current
    var duration by remember { mutableDoubleStateOf(0.0) }

    // Visibility state for staggered entry
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        contentVisible = true
    }

    // Mini Player state
    var streamUrl by remember { mutableStateOf<String?>(null) }
    var localPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    val miniPlayer = externalPlayer ?: localPlayer
    var activeMode by remember { mutableStateOf(ActiveTimeMode.START) }
    var isLooping by remember { mutableStateOf(false) }
    var currentRuntime by remember { mutableDoubleStateOf(startSeconds) }
    val loopEnd = endSeconds ?: (startSeconds + 5.0).coerceAtMost(duration)

    // Pulsing animation for the loop button
    val infiniteTransition = rememberInfiniteTransition(label = "LoopPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    // Sync state with player playback and handle looping
    LaunchedEffect(miniPlayer?.isPlaying, activeMode, isLooping, startSeconds, endSeconds) {
        val player = miniPlayer ?: return@LaunchedEffect
        while (isActive) {
            if (player.isPlaying) {
                val currentPos = player.currentPosition / 1000.0
                // Only update if value is valid
                if (currentPos.isFinite()) {
                    // Update current runtime for button display
                    currentRuntime = currentPos

                    // Update UI selection if we're not looping (otherwise we're in "preview" mode)
                    if (!isLooping) {
                        if (activeMode == ActiveTimeMode.START) {
                            onStartSecondsChange(currentPos)
                        } else {
                            onEndSecondsChange(currentPos)
                        }
                    }

                    // Handle Looping
                    if (isLooping) {
                        if (currentPos >= loopEnd || currentPos < startSeconds) {
                            player.seekTo((startSeconds * 1000).toLong())
                        }
                    }
                }
            }
            delay(50) // Update roughly at 20fps for smooth UI
        }
    }

    // Helper for quick seeking
    val seekBy: (Double) -> Unit = { delta ->
        val current = if (activeMode == ActiveTimeMode.START) startSeconds else (endSeconds ?: startSeconds)
        val target = (current + delta).coerceIn(0.0, duration)
        if (target.isFinite()) {
            if (activeMode == ActiveTimeMode.START) {
                onStartSecondsChange(target)
            } else {
                onEndSecondsChange(target)
            }
            isLooping = false // Stop looping on manual seek
            miniPlayer?.seekTo((target * 1000).toLong())
            miniPlayer?.playWhenReady = false
        }
    }

    // Resolve stream URL for mini player
    LaunchedEffect(scene) {
        if (streamUrl == null) {
            val file = scene.files?.firstOrNull()
            duration = file?.duration ?: 0.0
            streamUrl = getStreamUrl(scene.paths?.stream)
        }
    }

    // Manage mini player lifecycle
    DisposableEffect(streamUrl, externalPlayer) {
        if (externalPlayer == null && streamUrl != null) {
            // Create local player only if no external player exists
            val player = ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(streamUrl!!))
                prepare()
                seekTo((startSeconds * 1000).toLong())
                playWhenReady = false
                volume = 0f
            }
            localPlayer = player
            onDispose {
                player.release()
                localPlayer = null
            }
        } else if (externalPlayer != null) {
            // External player already has correct scene loaded from scene detail
            externalPlayer.playWhenReady = false
            externalPlayer.seekTo((startSeconds * 1000).toLong())
            onDispose { }
        } else {
            onDispose { }
        }
    }

    val availableTagsOnScene = scene.tags ?: emptyList()
    val dynamicColors = StashTheme.colors
    val contentColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(StashTokens.Spacing.ContentHorizontal)
    ) {
        // Mini Video Player (Always Visible)
        if (streamUrl != null || externalPlayer != null || isInspectionMode) {
            StaggeredEntry(visible = contentVisible, index = 1) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(StashTokens.Radius.Medium))
                        .background(Color.Black)
                ) {
                    if (isInspectionMode) {
                        AsyncImage(
                            model = scene.paths?.screenshot,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else if (miniPlayer != null) {
                        AndroidView(
                            factory = { context ->
                                PlayerView(context).apply {
                                    player = miniPlayer
                                    useController = false
                                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                }
                            },
                            update = { playerView ->
                                playerView.player = miniPlayer
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = contentColor)
                        }
                    }
                }
            }
        }

        // Quick Seek Buttons
        StaggeredEntry(visible = contentVisible, index = 2) {
            Box(modifier = Modifier.padding(horizontal = StashTokens.Spacing.ContentHorizontal)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(StashTokens.Spacing.ContentVertical)
                ) {
                    listOf(-3.0, 5.0, 15.0, 30.0).forEach { delta ->
                        val label = if (delta > 0) "+${delta.toInt()}" else delta.toInt().toString()
                        Button(
                            onClick = {
                                haptic.perform(StashHapticFeedbackType.Light)
                                seekBy(delta)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = dynamicColors.complementary,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(StashTokens.Radius.Medium)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Unified Time Selection Section
        StaggeredEntry(visible = contentVisible, index = 3) {
            Box(modifier = Modifier.padding(horizontal = StashTokens.Spacing.ContentHorizontal)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Start Time Box
                    TimeBox(
                        label = "START",
                        seconds = startSeconds,
                        isActive = activeMode == ActiveTimeMode.START,
                        contentColor = contentColor,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            haptic.perform(StashHapticFeedbackType.Selection)
                            activeMode = ActiveTimeMode.START
                        },
                        onUseCurrent = {
                            haptic.perform(StashHapticFeedbackType.Light)
                            activeMode = ActiveTimeMode.START
                            onStartSecondsChange(startSeconds) // This stays as is but we seek the player
                            isLooping = false
                            miniPlayer?.seekTo((startSeconds * 1000).toLong())
                            miniPlayer?.playWhenReady = false
                        }
                    )

                    // End Time Box
                    TimeBox(
                        label = "END",
                        seconds = endSeconds ?: startSeconds,
                        isActive = activeMode == ActiveTimeMode.END,
                        hasValue = endSeconds != null,
                        contentColor = contentColor,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            haptic.perform(StashHapticFeedbackType.Selection)
                            if (endSeconds == null) {
                                onEndSecondsChange((startSeconds + 5.0).coerceAtMost(duration))
                            }
                            activeMode = ActiveTimeMode.END
                        },
                        onUseCurrent = {
                            haptic.perform(StashHapticFeedbackType.Light)
                            activeMode = ActiveTimeMode.END
                            onEndSecondsChange(startSeconds) // Use startSeconds as "Current" anchor if not playing
                            isLooping = false
                            miniPlayer?.seekTo((startSeconds * 1000).toLong())
                            miniPlayer?.playWhenReady = false
                        },
                        onClear = {
                            haptic.perform(StashHapticFeedbackType.Light)
                            onEndSecondsChange(null)
                            activeMode = ActiveTimeMode.START
                        }
                    )
                }
            }
        }

        // Unified Scrubber
        StaggeredEntry(visible = contentVisible, index = 4) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = StashTokens.Spacing.ContentHorizontal)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp)
                            .height(56.dp)
                            .background(
                                dynamicColors.primary.copy(alpha = 0.1f),
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = dynamicColors.primary.copy(alpha = 0.2f),
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    ) {
                        VideoScrubber(
                            player = miniPlayer,
                            durationMs = (duration * 1000).toLong(),
                            tintColor = dynamicColors.primary,
                            spriteManager = spriteManager,
                            modifier = Modifier.fillMaxSize(),
                            onScrubbing = { s, _ ->
                                val targetSec = s.toDouble()
                                if (activeMode == ActiveTimeMode.START) {
                                    onStartSecondsChange(targetSec)
                                } else {
                                    onEndSecondsChange(targetSec)
                                }
                            }
                        )
                    }
                }
            }
        }

//        Spacer(modifier = Modifier.height(StashTokens.Spacing.ContentVertical))

        StaggeredEntry(visible = contentVisible, index = 5) {
            Button(
                onClick = {
                    haptic.perform(if (isLooping) StashHapticFeedbackType.Light else StashHapticFeedbackType.Medium)
                    isLooping = !isLooping
                    if (isLooping) {
                        miniPlayer?.seekTo((startSeconds * 1000).toLong())
                        miniPlayer?.playWhenReady = true
                    } else {
                        miniPlayer?.playWhenReady = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = StashTokens.Spacing.ContentHorizontal)
                    .height(44.dp)
                    .graphicsLayer {
                        scaleX = if (isLooping) pulseScale else 1f
                        scaleY = if (isLooping) pulseScale else 1f
                        alpha = if (isLooping) pulseAlpha else 1f
                    },
                colors = ButtonDefaults.buttonColors(
                    containerColor = dynamicColors.primary,
                    contentColor = if (isLooping) Color.Black else dynamicColors.primary
                ),
                shape = RoundedCornerShape(StashTokens.Radius.Medium)
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = if (isLooping) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = dynamicColors.complementary
                )
                Spacer(modifier = Modifier.width(8.dp))
                val labelPrefix = if (isLooping) "Previewing" else "Preview"
                val loopDuration = DateFormatters.formatLoopDuration(loopEnd - startSeconds)
                val displayTime = if (isLooping) currentRuntime else startSeconds
                val startTime = DateFormatters.formatDuration(displayTime)
                Text(
                    text = "$labelPrefix $loopDuration Marker ($startTime)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = dynamicColors.complementary
                )
            }
        }

        // Marker Title Field
        StaggeredEntry(visible = contentVisible, index = 6) {
            Box(modifier = Modifier.padding(horizontal = StashTokens.Spacing.ContentHorizontal)) {
                OutlinedTextField(
                    value = markerTitle,
                    onValueChange = onMarkerTitleChange,
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(StashTokens.Radius.Card),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = dynamicColors.primary.copy(alpha = 0.2f),
                        unfocusedContainerColor = dynamicColors.primary.copy(alpha = 0.1f),
                        focusedBorderColor = dynamicColors.primary,
                        unfocusedBorderColor = dynamicColors.primary.copy(alpha = 0.5f),
                        focusedLabelColor = dynamicColors.primary,
                        unfocusedLabelColor = dynamicColors.primary.copy(alpha = 0.5f),
                        cursorColor = dynamicColors.primary
                    )
                )
            }
        }

        // Primary Tag (Required)
        StaggeredEntry(visible = contentVisible, index = 7) {
            Box(modifier = Modifier.padding(horizontal = StashTokens.Spacing.ContentHorizontal)) {
                Column {
                    Text(
                        text = "Primary Tag (Required)",
                        style = MaterialTheme.typography.labelMedium,
                        color = dynamicColors.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(StashTokens.Spacing.ContentVertical))

                    if (selectedTagId != null) {
                        // Show Selected Primary Tag
                        val selectedTag = allTags.find { it.id == selectedTagId }
                        if (selectedTag != null) {
                            TagEditItem(
                                tag = selectedTag,
                                onRemove = {
                                    haptic.perform(StashHapticFeedbackType.Light)
                                    onSelectedTagIdChange(null)
                                    onTagSearchTextChange("")
                                }
                            )
                        }
                    } else {
                        val filteredPrimaryTags = remember(allTags, tagSearchText) {
                            if (tagSearchText.isEmpty()) {
                                emptyList()
                            } else {
                                allTags.filter { it.name.contains(tagSearchText, ignoreCase = true) }
                            }
                        }

                        if (filteredPrimaryTags.isNotEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(StashTokens.Radius.Card),
                                color = dynamicColors.tertiary.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, dynamicColors.tertiary.copy(alpha = 0.3f))
                            ) {
                                Column {
                                    filteredPrimaryTags.forEach { tag ->
                                        if (!selectedAdditionalTagIds.contains(tag.id)) {
                                            TagSearchItem(tag = tag, onAdd = {
                                                haptic.perform(StashHapticFeedbackType.Selection)
                                                onSelectedTagIdChange(tag.id)
                                                onTagSearchTextChange("")
                                            })
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(StashTokens.Spacing.ContentVertical))
                        }

                        // Show Search + Suggestions for Primary
                        SearchField(
                            value = tagSearchText,
                            onValueChange = onTagSearchTextChange,
                            placeholder = "Add Primary Tag...",
                            isSearching = false,
                            color = dynamicColors.tertiary
                        )
                    }
                }
            }
        }

        // Additional Tags Section
        StaggeredEntry(visible = contentVisible, index = 8) {
            Box(modifier = Modifier.padding(horizontal = StashTokens.Spacing.ContentHorizontal)) {
                Column {
                    Text(
                        text = "Additional Tags (Optional)",
                        style = MaterialTheme.typography.labelMedium,
                        color = dynamicColors.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(StashTokens.Spacing.ContentVertical))

                    // List selected additional
                    if (selectedAdditionalTagIds.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(StashTokens.Spacing.ContentVertical)) {
                            selectedAdditionalTagIds.forEach { id ->
                                val tag = availableTagsOnScene.find { it.id == id } ?: allTags.find { it.id == id }
                                if (tag != null) {
                                    TagEditItem(tag = tag, onRemove = {
                                        haptic.perform(StashHapticFeedbackType.Light)
                                        selectedAdditionalTagIds.remove(id)
                                    })
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(StashTokens.Spacing.ContentVertical))
                    }

                    // Search for additional (Only visible if Primary is selected, so we don't conflict with Primary search)
                    if (selectedTagId != null) {
                        val filteredAdditionalTags = remember(allTags, tagSearchText, selectedTagId, selectedAdditionalTagIds.size) {
                            val baseList = allTags.filter { it.id != selectedTagId && !selectedAdditionalTagIds.contains(it.id) }
                            if (tagSearchText.isNotEmpty()) {
                                baseList.filter { it.name.contains(tagSearchText, ignoreCase = true) }
                            } else {
                                emptyList()
                            }
                        }

                        if (filteredAdditionalTags.isNotEmpty()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(StashTokens.Radius.Card),
                                color = dynamicColors.tertiary.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, dynamicColors.tertiary.copy(alpha = 0.3f))
                            ) {
                                Column {
                                    filteredAdditionalTags.forEach { tag ->
                                        TagSearchItem(tag = tag, onAdd = {
                                            haptic.perform(StashHapticFeedbackType.Selection)
                                            selectedAdditionalTagIds.add(tag.id)
                                            onTagSearchTextChange("")
                                        })
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(StashTokens.Spacing.ContentVertical))
                        }

                        SearchField(
                            value = tagSearchText,
                            onValueChange = onTagSearchTextChange,
                            placeholder = "Add additional tag...",
                            isSearching = false,
                            color = dynamicColors.primary
                        )
                    } else {
                        // Hint text explaining needed primary selection?
                        if (selectedAdditionalTagIds.isEmpty()) {
                            Text(
                                text = "Select a primary tag to add more.",
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

/**
 * Enumeration of available time selection modes.
 */
private enum class ActiveTimeMode { START, END }

/**
 * A stylized box displaying a timestamp and providing selection/action callbacks.
 *
 * @param label The category label (e.g., "START", "END").
 * @param seconds The timestamp to display.
 * @param isActive Whether this box is currently focused for scrubbing.
 * @param contentColor The base foreground color.
 * @param modifier Row modifiers.
 * @param hasValue Whether a valid timestamp exists for this category.
 * @param onClick Selection callback.
 * @param onUseCurrent Callback to snap to the player's current position.
 * @param onClear Callback to remove the timestamp (only for optional categories).
 */
@Composable
private fun TimeBox(
    label: String,
    seconds: Double,
    isActive: Boolean,
    contentColor: Color,
    modifier: Modifier = Modifier,
    hasValue: Boolean = true,
    onClick: () -> Unit,
    onUseCurrent: () -> Unit,
    onClear: (() -> Unit)? = null
) {
    val borderColor = if (isActive) StashTheme.colors.complementary else contentColor.copy(alpha = 0.2f)
    val backgroundColor = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(StashTokens.Radius.Medium))
            .clickable { onClick() }
            .padding(StashTokens.Spacing.ContentVertical)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            if (onClear != null && hasValue) {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(16.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = contentColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Text(
            text = if (hasValue) DateFormatters.formatDuration(seconds) else "--:--",
            style = MaterialTheme.typography.titleMedium,
            color = if (hasValue) contentColor else contentColor.copy(alpha = 0.3f),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        TextButton(
            onClick = onUseCurrent,
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.height(24.dp)
        ) {
            Text(
                "Use Current",
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}

// region Previews

@Composable
private fun AddMarkerPreviewContainer(content: @Composable () -> Unit) {
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AddMarkerContentPreview() {
    val selectedAdditionalTagIds = remember { mutableStateListOf<String>() }
    AddMarkerPreviewContainer {
        AddMarkerContent(
            scene = MockData.scene,
            allTags = MockData.tags,
            markerTitle = "",
            onMarkerTitleChange = {},
            selectedTagId = null,
            onSelectedTagIdChange = {},
            selectedAdditionalTagIds = selectedAdditionalTagIds,
            tagSearchText = "",
            onTagSearchTextChange = {},
            startSeconds = 30.0,
            onStartSecondsChange = {},
            endSeconds = null,
            onEndSecondsChange = {},
            getStreamUrl = { null }
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Defined Range")
@Composable
fun AddMarkerContentDefinedRangePreview() {
    val selectedAdditionalTagIds = remember { mutableStateListOf<String>() }
    AddMarkerPreviewContainer {
        AddMarkerContent(
            scene = MockData.scene,
            allTags = MockData.tags,
            markerTitle = "Pre-filled",
            onMarkerTitleChange = {},
            selectedTagId = "tag1",
            onSelectedTagIdChange = {},
            selectedAdditionalTagIds = selectedAdditionalTagIds,
            tagSearchText = "",
            onTagSearchTextChange = {},
            startSeconds = 120.0,
            onStartSecondsChange = {},
            endSeconds = 135.0,
            onEndSecondsChange = {},
            getStreamUrl = { null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AddMarkerScreenLoadingPreview() {
    AddMarkerPreviewContainer {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Add Marker") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Empty Tags")
@Composable
fun AddMarkerContentEmptyTagsPreview() {
    val selectedAdditionalTagIds = remember { mutableStateListOf<String>() }
    AddMarkerPreviewContainer {
        AddMarkerContent(
            scene = MockData.scene.copy(tags = emptyList()),
            allTags = emptyList(),
            markerTitle = "",
            onMarkerTitleChange = {},
            selectedTagId = null,
            onSelectedTagIdChange = {},
            selectedAdditionalTagIds = selectedAdditionalTagIds,
            tagSearchText = "",
            onTagSearchTextChange = {},
            startSeconds = 30.0,
            onStartSecondsChange = {},
            endSeconds = null,
            onEndSecondsChange = {},
            getStreamUrl = { null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Error State")
@Composable
fun AddMarkerScreenErrorPreview() {
    AddMarkerPreviewContainer {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Add Marker") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Failed to load scene details",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
// endregion

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Full Screen Success")
@Composable
fun AddMarkerScreenSuccessPreview() {
    val sceneColors = rememberSceneColors(Color.Blue)

    AddMarkerPreviewContainer {
        CompositionLocalProvider(LocalStashDynamicColors provides sceneColors) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Add Marker") },
                        navigationIcon = {
                            IconButton(onClick = {}) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        },
                        actions = {
                            Button(
                                onClick = {},
                                enabled = true,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = sceneColors.primary,
                                    disabledContainerColor = Color.Transparent,
                                    disabledContentColor = sceneColors.primary.copy(alpha = 0.3f)
                                )
                            ) {
                                Text(
                                    text = "Create",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = sceneColors.complementary,
                            navigationIconContentColor = sceneColors.complementary
                        )
                    )
                },
                containerColor = Color.Transparent
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding())
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    AddMarkerContent(
                        scene = MockData.scene,
                        allTags = MockData.tags,
                        markerTitle = "Funny Moment",
                        onMarkerTitleChange = {},
                        selectedTagId = "1",
                        onSelectedTagIdChange = {},
                        selectedAdditionalTagIds = remember { mutableStateListOf() },
                        tagSearchText = "",
                        onTagSearchTextChange = {},
                        startSeconds = 65.0,
                        onStartSecondsChange = {},
                        endSeconds = null,
                        onEndSecondsChange = {},
                        getStreamUrl = { null }
                    )
                }
            }
        }
    }
}
