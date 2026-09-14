package goonarr.stash.features.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import goonarr.stash.LocalStashDynamicColors
import goonarr.stash.rememberSceneColors
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun MiniPlayer(playerState: GlobalPlayerState, onExpand: () -> Unit) {
    // Only show if minimised AND we have a video
    // We wrap this in AnimatedVisibility to animate entry/exit
    val visible = playerState.isMinimized && playerState.currentVideoUrl != null

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it } + fadeIn(),
        exit = slideOutVertically { it } + fadeOut(),
        label = "MiniPlayerContainer"
    ) {
        MiniPlayerContent(playerState, onExpand, visible)
    }
}

@Composable
private fun MiniPlayerContent(playerState: GlobalPlayerState, onExpand: () -> Unit, visible: Boolean) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val scope = rememberCoroutineScope()

    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    // Use Animatable for smooth physics-based movement and transition
    // 0.0 -> Minimizing (Start), 1.0 -> Minimized (Floating)
    val transitionAnim = remember { androidx.compose.animation.core.Animatable(0f) }

    // Initialize transition
    LaunchedEffect(visible) {
        if (visible) {
            // Reset to 0 (Source) if just appearing
            transitionAnim.snapTo(0f)
            // Animate to 1 (Floating)
            transitionAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(stiffness = Spring.StiffnessLow)
            )
        } else {
            // Logic for expanding could go here if we weren't doing simple exit
        }
    }

    // Calculate current interpolated rect
    val sourceRect = playerState.sourceBounds ?: Rect(Offset.Zero, Size(screenWidthPx, screenWidthPx * 9f / 16f))

    // Default Floating Position (Bottom Right)
    LaunchedEffect(Unit) {
        if (playerState.floatingPosition == null) {
            val initialWidth = playerState.floatingWidth
            val widthPx = with(density) { initialWidth.toPx() }
            val heightPx = widthPx * (9f / 16f) + with(density) { 60.dp.toPx() }

            val startPos = Offset(
                x = screenWidthPx - widthPx - 40f,
                y = screenHeightPx - heightPx - 250f
            )
            playerState.floatingPosition = startPos
        }
    }

    val currentFloatingPos = playerState.floatingPosition ?: Offset.Zero
    val floatingWidthPx = with(density) { playerState.floatingWidth.toPx() }

    // We need to animate the WHOLE container (video + scrubber).
    // For the video part:
    // Source: sourceRect
    // Target: Rect(currentFloatingPos, Size(floatingWidthPx, floatingHeightPx))

    // Controls Visibility State (Auto-hide)
    var areControlsVisible by remember { mutableStateOf(false) }
    var hideControlsJob by remember { mutableStateOf<Job?>(null) }

    val haptic = LocalHapticFeedback.current
    val snapScale = remember { Animatable(1f) }

    fun showControls() {
        areControlsVisible = true
        hideControlsJob?.cancel()
        hideControlsJob = scope.launch {
            kotlinx.coroutines.delay(3000)
            areControlsVisible = false
        }
    }

    // Initial show
    LaunchedEffect(Unit) {
        showControls()
    }

    // Interpolation Factor check
    val t = transitionAnim.value

    // 1. Calculate Current Bounds
    val targetWidth = floatingWidthPx
    val targetVideoHeight = targetWidth * (9f / 16f)

    // Lerp Video Rect
    val currentVideoWidth = lerp(sourceRect.width, targetWidth, t)
    // Height is just video height now, no extra chrome below
    val currentVideoHeight = lerp(sourceRect.height, targetVideoHeight, t)
    val currentX = lerp(sourceRect.left, currentFloatingPos.x, t)
    val currentY = lerp(sourceRect.top, currentFloatingPos.y, t)

    // Dynamic Theme Provider
    val sceneColors = rememberSceneColors(playerState.currentTintColor)

    CompositionLocalProvider(LocalStashDynamicColors provides sceneColors) {
        Box(
            modifier = Modifier
                .offset { IntOffset(currentX.roundToInt(), currentY.roundToInt()) }
                .graphicsLayer {
                    scaleX = snapScale.value
                    scaleY = snapScale.value
                }
                .width(with(density) { currentVideoWidth.toDp() })
                .pointerInput(Unit) {
                    // Manual gesture implementation loop
                    while (true) {
                        awaitPointerEventScope {
                            val touchSlop = viewConfiguration.touchSlop
                            val down = awaitFirstDown(requireUnconsumed = false)

                            var zoom = 1f
                            var pan = Offset.Zero
                            var rotation = 0f

                            var pastSlop = false
                            var panAccumulator = Offset.Zero

                            do {
                                val event = awaitPointerEvent()
                                val canceled = event.changes.any { it.isConsumed }
                                if (canceled) break

                                val zoomChange = event.calculateZoom()
                                val panChange = event.calculatePan()

                                if (t >= 0.9f) {
                                    if (!pastSlop) {
                                        panAccumulator += panChange
                                        val panDistance = panAccumulator.getDistance()
                                        // Also consider zoom as a trigger to start immediately (pinch usually implies intent)
                                        // But safe to wait for small movement
                                        if (panDistance > touchSlop || zoomChange != 1f) {
                                            pastSlop = true
                                        }
                                    }

                                    if (pastSlop) {
                                        // Update State
                                        if (panChange != Offset.Zero) {
                                            val currentPos = playerState.floatingPosition ?: Offset.Zero
                                            val newPos = currentPos + panChange
                                            playerState.floatingPosition = newPos
                                        }

                                        if (zoomChange != 1f) {
                                            val currentWidthPx = with(density) { playerState.floatingWidth.toPx() }
                                            val newWidthPx = (currentWidthPx * zoomChange).coerceIn(
                                                minimumValue = with(density) { 200.dp.toPx() },
                                                maximumValue = with(density) { (configuration.screenWidthDp.dp - 20.dp).toPx() }
                                            )
                                            playerState.floatingWidth = with(density) { newWidthPx.toDp() }

                                            // Haptic feedback during pinch zoom
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }

                                        event.changes.forEach {
                                            if (it.positionChanged()) it.consume()
                                        }
                                    }
                                }
                            } while (event.changes.any { it.pressed })

                            // Snap Logic
                            if (pastSlop) {
                                scope.launch {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    launch {
                                        snapScale.animateTo(1.05f, tween(100))
                                        snapScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                    }

                                    val currentPos = playerState.floatingPosition ?: Offset.Zero
                                    val currentWidthPx = with(density) { playerState.floatingWidth.toPx() }
                                    val centerX = currentPos.x + (currentWidthPx / 2f)
                                    val screenCenter = screenWidthPx / 2f

                                    val targetX = if (centerX < screenCenter) 20f else screenWidthPx - currentWidthPx - 20f
                                    val heightPx = currentWidthPx * (9f / 16f)
                                    val targetY = currentPos.y.coerceIn(100f, screenHeightPx - heightPx - 100f)

                                    playerState.floatingPosition = Offset(targetX, targetY)
                                }
                            }
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            if (areControlsVisible) {
                                areControlsVisible = false
                                hideControlsJob?.cancel()
                            } else {
                                showControls()
                            }
                        }
                    )
                }
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                modifier = Modifier
                    .wrapContentHeight()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = sceneColors.primary,
                        ambientColor = sceneColors.primary
                    )
            ) {
                Box {
                    // Video Content
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .background(Color.Black)
                    ) {
                        VideoPlayer(
                            url = playerState.currentVideoUrl,
                            // TODO: sync mute state?
                            isMuted = false,
                            useController = false,
                            externalPlayer = playerState.player
                        )
                    }

                    // Overlay Controls (Fade in/out based on visibility AND transition)
                    // We combine the transition 't' (0->1) with the visibility toggle
                    val controlsAlpha by animateFloatAsState(
                        targetValue = if (areControlsVisible) 1f else 0f,
                        label = "controlsAlpha"
                    )
                    // Only show controls when fully docked (t > 0.9)
                    val finalAlpha = if (t > 0.9f) controlsAlpha else 0f

                    if (finalAlpha > 0f) {
                        // Top Gradient
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                                .align(Alignment.TopCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Black.copy(alpha = 0.7f), Color.Transparent)
                                    )
                                )
                                .graphicsLayer { alpha = finalAlpha }
                        )

                        // Bottom Gradient (for scrubber)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                    )
                                )
                                .graphicsLayer { alpha = finalAlpha }
                        )

                        // Header Controls
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .align(Alignment.TopCenter)
                                .graphicsLayer { alpha = finalAlpha },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Expand button on left
                            val iconTint = sceneColors.complementary
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onExpand()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInFull, "Return to Scene",
                                    modifier = Modifier.size(16.dp), tint = iconTint
                                )
                            }

                            // Close button on right
                            IconButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    playerState.dismiss()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close, "Close",
                                    modifier = Modifier.size(16.dp), tint = iconTint
                                )
                            }
                        }

                        // Scrubber (Overlay at Bottom)
                        if (playerState.player != null) {
                            val durationMs = playerState.player?.duration ?: 0L
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                                    .graphicsLayer { alpha = finalAlpha }
                            ) {
                                VideoScrubber(
                                    player = playerState.player,
                                    durationMs = if (durationMs > 0) durationMs else 0L,
                                    modifier = Modifier.fillMaxWidth(),
                                    tintColor = playerState.currentTintColor ?: MaterialTheme.colorScheme.primary,
                                    markers = playerState.currentMarkers,
                                    spriteManager = playerState.currentSpriteManager,
                                    compactFactor = 1f,
                                    onScrubbing = { _, isScrubbing ->
                                        if (isScrubbing) {
                                            // Keep controls visible while dragging
                                            areControlsVisible = true
                                            hideControlsJob?.cancel()
                                        } else {
                                            // Resume auto-hide when drag ends
                                            showControls()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
