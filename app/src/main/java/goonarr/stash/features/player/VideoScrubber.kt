package goonarr.stash.features.player

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.window.Popup
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import goonarr.stash.StashTheme
import goonarr.stash.core.model.SceneMarker
import goonarr.stash.features.scenes.utils.SpriteManager
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType
import kotlin.math.abs
import kotlin.math.ln
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * A pill-shaped video play bar with integrated play/pause and mute buttons.
 *
 * Design:
 * ┌────────────────────────────────────────────────────┐
 * │  ▶  │───●───|────|──────|───────────────│ 🔊  │
 * └────────────────────────────────────────────────────┘
 *                      Marker Name
 *
 * @param player The ExoPlayer instance to control.
 * @param durationMs The video duration in milliseconds.
 * @param tintColor The accent color for controls (extracted hero color).
 * @param markers Optional list of scene markers to show as tick marks.
 * @param spriteManager Optional sprite manager for thumbnail previews.
 */
@Composable
fun VideoScrubber(
    player: ExoPlayer?,
    durationMs: Long,
    modifier: Modifier = Modifier,
    tintColor: Color = Color.Red,
    markers: List<SceneMarker> = emptyList(),
    spriteManager: SpriteManager? = null,
    onScrubbing: ((currentSeconds: Float, isScrubbing: Boolean) -> Unit)? = null,
    /**
     * 0.0f = Full Size (Normal)
     * 1.0f = Compact Mode (MiniPlayer)
     */
    compactFactor: Float = 0f,
    manualPositionMs: Long? = null
) {
    if (durationMs <= 0) return

    val isManualMode = player == null
    var isPlaying by remember { mutableStateOf(player?.isPlaying == true) }
    var isMuted by remember { mutableStateOf(player?.volume == 0f) }
    var currentPosition by remember { mutableFloatStateOf(0f) }
    var isScrubbing by remember { mutableStateOf(false) }
    var sliderSize by remember { mutableStateOf(IntSize.Zero) }
    var scrubBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var hoveredMarkerName by remember { mutableStateOf<String?>(null) }
    var lastTriggeredMarker by remember { mutableStateOf<SceneMarker?>(null) }
    var verticalDragOffset by remember { mutableFloatStateOf(0f) }
    var accumulatedScrubDelta by remember { mutableFloatStateOf(0f) }
    val durationSeconds = durationMs / 1000f
    val density = LocalDensity.current
    val haptic = LocalStashHapticFeedback.current

    // Helper for interpolation
    fun Dp.lerp(other: Dp, t: Float): Dp {
        return lerp(this, other, t)
    }

    // Base Dimensions
    val basePillHeight = 56.dp.lerp(40.dp, compactFactor)
    val baseButtonSize = 44.dp.lerp(32.dp, compactFactor)
    val baseTrackHeight = 6.dp.lerp(4.dp, compactFactor)
    val maxTrackHeight = 42.dp.lerp(24.dp, compactFactor)

    val iconSizePlay = 24.dp.lerp(20.dp, compactFactor)
    val iconSizeMute = 20.dp.lerp(18.dp, compactFactor)

    // Calculate Precision Factor (0.0 -> 1.0) based on drag
    // Max vertical drag for full precision: 300dp
    val maxDragDist = 300.dp
    val verticalDistDp = with(density) { abs(verticalDragOffset).toDp() }
    val precisionFactor = (verticalDistDp / maxDragDist).coerceIn(0f, 1f)

    // Constant dimensions (no animation needed for these specifically, we animate the factor)
    val pillHeight = basePillHeight

    // Track height (thickness) scales with precision during scrubbing
    val visualExpansionFactor = ln(1f + 10f * precisionFactor) / ln(11f)
    val targetTrackHeight =
        if (isScrubbing) baseTrackHeight + ((maxTrackHeight - baseTrackHeight) * visualExpansionFactor) else baseTrackHeight
    val trackHeight by animateDpAsState(
        targetValue = targetTrackHeight,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "trackHeight"
    )

    val defaultThumbSize = 12.dp.lerp(10.dp, compactFactor)
    val minThumbSize = 8.dp.lerp(6.dp, compactFactor)
    val targetThumbSize =
        if (isScrubbing) defaultThumbSize - ((defaultThumbSize - minThumbSize) * precisionFactor) else defaultThumbSize
    val thumbSize by animateDpAsState(
        targetValue = targetThumbSize,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "thumbSize"
    )

    val markerTolerance = 1.5f // seconds

    // Listen to player state changes
    DisposableEffect(player) {
        if (player != null) {
            val listener = object : Player.Listener {
                override fun onIsPlayingChanged(playing: Boolean) {
                    isPlaying = playing
                }
            }
            player.addListener(listener)
            onDispose {
                player.removeListener(listener)
            }
        } else {
            onDispose { }
        }
    }

    var isExiting by remember { mutableStateOf(false) }
    val thumbScale = remember { Animatable(1f) }

    // Update position periodically when not scrubbing
    LaunchedEffect(player, isScrubbing, manualPositionMs) {
        while (isActive) {
            if (!isScrubbing) {
                if (manualPositionMs != null) {
                    currentPosition = manualPositionMs.toFloat() / 1000f
                } else if (player != null) {
                    currentPosition = player.currentPosition / 1000f
                }

                // Update hovered marker based on current position
                val nearMarker =
                    markers.find { abs(currentPosition - it.seconds) < markerTolerance }
                hoveredMarkerName = nearMarker?.title
            }
            delay(250)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // Pill Container - main content
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(pillHeight),
            shape = RoundedCornerShape(50),
            color = tintColor.copy(alpha = 0.35f),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play/Pause Button (Left End)
                // Use Dynamic Theme Colors
                val dynamicColors = StashTheme.colors
                val complementaryColor = dynamicColors.complementary
                val triadicColor = dynamicColors.tertiary

                // Button color: tintColor when playing, complementary when paused
                val buttonColor by animateColorAsState(
                    targetValue = if (isPlaying) tintColor else complementaryColor,
                    animationSpec = tween(durationMillis = 300),
                    label = "buttonColor"
                )

                val playPauseScale = remember { Animatable(1f) }
                val scope = rememberCoroutineScope()

                Surface(
                    modifier = Modifier
                        .size(baseButtonSize)
                        .padding(2.dp)
                        .graphicsLayer {
                            scaleX = playPauseScale.value
                            scaleY = playPauseScale.value
                        },
                    shape = CircleShape,
                    color = buttonColor.copy(alpha = 0.4f),
                    onClick = {
                        if (!isManualMode && player != null) {
                            haptic.perform(StashHapticFeedbackType.Medium)
                            scope.launch {
                                playPauseScale.animateTo(1.25f, tween(100))
                                playPauseScale.animateTo(1.25f, tween(100))
                                playPauseScale.animateTo(
                                    1f,
                                    spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                )
                            }
                            if (player.isPlaying) player.pause() else player.play()
                        }
                    },
                    enabled = !isManualMode
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AnimatedContent(
                            targetState = isPlaying,
                            transitionSpec = {
                                (
                                    scaleIn(animationSpec = tween(300)) + fadeIn(
                                        animationSpec = tween(
                                            300
                                        )
                                    )
                                    )
                                    .togetherWith(
                                        scaleOut(animationSpec = tween(300)) + fadeOut(
                                            animationSpec = tween(300)
                                        )
                                    )
                            },
                            label = "playPauseIcon"
                        ) { playing ->
                            Icon(
                                imageVector = if (playing) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playing) "Pause" else "Play",
                                tint = buttonColor,
                                modifier = Modifier.size(iconSizePlay)
                            )
                        }
                    }
                }

                // Seek Bar Track
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 8.dp)
                        .onSizeChanged { sliderSize = it }
                        .pointerInput(durationSeconds) {
                            detectTapGestures { offset ->
                                val newPosition = (offset.x / size.width) * durationSeconds
                                currentPosition = newPosition.coerceIn(0f, durationSeconds)
                                player?.seekTo((currentPosition * 1000).toLong())
                            }
                        }
                        .pointerInput(durationSeconds) {
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    isScrubbing = true
                                    verticalDragOffset = 0f
                                    scope.launch { thumbScale.snapTo(1f) }
                                    isExiting = false

                                    // Snap to initial touch position (Absolute Seek)
                                    // Initial tap is always absolute (no zoom yet)
                                    val startPosition = (offset.x / size.width) * durationSeconds
                                    currentPosition = startPosition.coerceIn(0f, durationSeconds)

                                    onScrubbing?.invoke(currentPosition, true)
                                },
                                onDragEnd = {
                                    player?.seekTo((currentPosition * 1000).toLong())

                                    // Start exit animation
                                    isScrubbing = false
                                    isExiting = true
                                    scope.launch {
                                        thumbScale.animateTo(
                                            1.1f,
                                            spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                        )
                                        thumbScale.animateTo(0f, tween(200))
                                        isExiting = false
                                        scrubBitmap = null
                                        lastTriggeredMarker = null
                                    }

                                    onScrubbing?.invoke(currentPosition, false)
                                    verticalDragOffset = 0f
                                },
                                onDragCancel = {
                                    isScrubbing = false
                                    isExiting = false
                                    onScrubbing?.invoke(currentPosition, false)
                                    scrubBitmap = null
                                    lastTriggeredMarker = null
                                    verticalDragOffset = 0f
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()

                                    // Calculate vertical distance from the center of the track
                                    verticalDragOffset = change.position.y

                                    // Accordion Zoom Logic
                                    // Calculate current precision factor based on vertical drag
                                    val verticalDistDp =
                                        with(density) { abs(verticalDragOffset).toDp() }
                                    val currentPrecision =
                                        (verticalDistDp / 300.dp).coerceIn(0f, 1f)

                                    // visibleDuration shrinks as we drag down (precisionFactor increases)
                                    // At factor 0: visibleDuration = durationSeconds (Full View)
                                    // At factor 1: visibleDuration = durationSeconds / 30 (30x Zoom)
                                    val minVisibleDuration = durationSeconds / 30f // Max zoom level
                                    val visibleDuration =
                                        durationSeconds - ((durationSeconds - minVisibleDuration) * currentPrecision)

                                    // Apply delta based on VISIBLE duration
                                    // Visual movement remains 1:1 with finger, but time moves slower
                                    val timeDelta = (dragAmount / size.width) * visibleDuration
                                    val previousPosition = currentPosition
                                    var newPosition =
                                        (currentPosition + timeDelta).coerceIn(0f, durationSeconds)

                                    // Gear Ratio Haptics (Continuous feedback based on scrubbing speed)
                                    // Interval scales from 30s (at full view) to 1s (at max zoom)
                                    accumulatedScrubDelta += abs(timeDelta)
                                    val zoomRatio =
                                        visibleDuration / durationSeconds // 1.0 (zoomed out) to ~0.03 (zoomed in)
                                    val tickInterval = 30f * zoomRatio // Scale interval with zoom
                                    if (accumulatedScrubDelta >= tickInterval) {
                                        // Heavier clicks for coarse scrubbing (zoomed out)
                                        // Lighter clicks for fine scrubbing (zoomed in)
                                        val feedbackType =
                                            if (zoomRatio > 0.5f) StashHapticFeedbackType.Medium else StashHapticFeedbackType.Selection
                                        haptic.perform(feedbackType)
                                        accumulatedScrubDelta = 0f
                                    }

                                    // Check for marker crossing (detect if we crossed over a marker)
                                    val crossedMarker = markers.find { marker ->
                                        val markerTime = marker.seconds
                                        // Check if marker is between previous and new position (works both directions)
                                        val minPos = minOf(previousPosition, newPosition)
                                        val maxPos = maxOf(previousPosition, newPosition)
                                        markerTime in minPos..maxPos
                                    }

                                    // Magnetic snap: when scrubbing slowly near a marker, snap to it
                                    val snapThreshold =
                                        3.5f * zoomRatio // Scale snap threshold with zoom!
                                    val snapStrength = 0.75f // How strong the snap effect is (0-1)
                                    if (zoomRatio < 0.5f) { // Only snap when zoomed in somewhat
                                        val nearestMarker =
                                            markers.minByOrNull { abs(newPosition - it.seconds) }
                                        if (nearestMarker != null && abs(newPosition - nearestMarker.seconds) < snapThreshold) {
                                            // Leap toward the marker
                                            newPosition =
                                                (newPosition + (nearestMarker.seconds - newPosition) * snapStrength).toFloat()
                                        }
                                    }

                                    currentPosition = newPosition

                                    // Real-time seeking for immediate feedback
                                    player?.seekTo((currentPosition * 1000).toLong())

                                    scrubBitmap =
                                        spriteManager?.getThumbnail(currentPosition.toDouble())
                                    onScrubbing?.invoke(currentPosition, true)

                                    // Trigger haptic and show name if we crossed a marker
                                    if (crossedMarker != null && crossedMarker != lastTriggeredMarker) {
                                        haptic.perform(StashHapticFeedbackType.Heavy)
                                        hoveredMarkerName = crossedMarker.title
                                    } else if (crossedMarker == null) {
                                        // Check if we're inside or near a marker
                                        val nearMarker = markers.find { marker ->
                                            val start = marker.seconds
                                            val end = marker.endSeconds ?: start
                                            // Allow 0.5s buffer before/after
                                            currentPosition >= start - 0.5f && currentPosition <= end + 0.5f
                                        }

                                        if (nearMarker != null) {
                                            hoveredMarkerName = nearMarker.title
                                        } else {
                                            hoveredMarkerName = null
                                            lastTriggeredMarker = null
                                        }
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.CenterStart
                ) {
                    // Track Background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(trackHeight)
                            .clip(RoundedCornerShape(2.dp))
                            .background(tintColor.copy(alpha = 0.2f))
                    )

                    // Calculate Viewport Window based on Zoom
                    // At precision 0: Window is 0.duration
                    // At precision 1: Window is centered on currentPosition, width = visibleDuration
                    val minVisibleDuration = durationSeconds / 30f
                    val visibleDuration =
                        durationSeconds - ((durationSeconds - minVisibleDuration) * precisionFactor)

                    // Center the window based on the thumb's relative visual position
                    // This prevents the thumb from "sliding" to the center of the screen when zooming
                    // If thumb is at 20% of screen, we want it to STAY at 20% of screen
                    val safeDuration = if (durationSeconds > 0) durationSeconds else 1f
                    val relativeVisualPos = (currentPosition / safeDuration).coerceIn(0f, 1f)
                    val windowStart =
                        (currentPosition - (visibleDuration * relativeVisualPos)).coerceIn(
                            0f,
                            (durationSeconds - visibleDuration).coerceAtLeast(0f)
                        )
                    // We don't really need windowEnd for logic, just width

                    // Track Progress
                    // Normalize currentPosition within the visible window [windowStart, windowStart + visibleDuration]
                    // Value 0.0 -> Left Edge, 1.0 -> Right Edge
                    val progressInWindow =
                        ((currentPosition - windowStart) / visibleDuration).coerceIn(0f, 1f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressInWindow)
                            .height(trackHeight)
                            .clip(RoundedCornerShape(2.dp))
                            .background(tintColor)
                    )

                    // Marker Tick Marks
                    if (markers.isNotEmpty() && sliderSize.width > 0) {
                        markers.forEach { marker ->
                            val markerStart = marker.seconds
                            val markerEnd = marker.endSeconds ?: markerStart
                            val windowEnd = windowStart + visibleDuration

                            // Check if any part of the marker is visible in the current window
                            if (markerEnd >= windowStart && markerStart <= windowEnd) {
                                val startProgress =
                                    (markerStart - windowStart) / visibleDuration
                                val endProgress =
                                    (markerEnd - windowStart) / visibleDuration

                                val startPx = startProgress * sliderSize.width
                                val endPx = endProgress * sliderSize.width
                                val rawWidthPx = (endPx - startPx).coerceAtLeast(0.0)

                                val minWidthPx = with(density) { 6.dp.toPx() }
                                val isDot = rawWidthPx < minWidthPx

                                // If it's a dot (point marker or very short range), center it on startPx
                                // If it's a bar (range), start it at startPx
                                val finalXPx = if (isDot) startPx - (minWidthPx / 2) else startPx
                                val finalWidthPx = if (isDot) minWidthPx else rawWidthPx

                                val finalXDp = with(density) { finalXPx.toFloat().toDp() }
                                val finalWidthDp = with(density) { finalWidthPx.toFloat().toDp() }

                                Box(
                                    modifier = Modifier
                                        .offset(x = finalXDp)
                                        .width(finalWidthDp)
                                        .height(6.dp)
                                        .clip(if (isDot) CircleShape else RoundedCornerShape(2.dp))
                                        .background(complementaryColor.copy(alpha = 0.9f))
                                )
                            }
                        }
                    }

                    // Thumb (playhead)
                    Box(
                        modifier = Modifier
                            .offset(x = with(density) { (progressInWindow * sliderSize.width).toDp() - (thumbSize / 2) })
                            .size(thumbSize)
                            .clip(CircleShape)
                            .background(triadicColor)
                    )

                    // Marker Name Text (Centered Overlay)
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (hoveredMarkerName != null) {
                            Text(
                                text = hoveredMarkerName ?: "",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = complementaryColor
                                ),
                                maxLines = 1,
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Mute Button (Right End)
                // Color: tintColor when unmuted (volume up), complementary when muted (volume off)
                val muteButtonColor by animateColorAsState(
                    targetValue = if (!isMuted) tintColor else complementaryColor,
                    animationSpec = tween(durationMillis = 300),
                    label = "muteButtonColor"
                )

                val muteScale = remember { Animatable(1f) }

                Surface(
                    modifier = Modifier
                        .size(baseButtonSize)
                        .padding(2.dp)
                        .graphicsLayer {
                            scaleX = muteScale.value
                            scaleY = muteScale.value
                        },
                    shape = CircleShape,
                    color = muteButtonColor.copy(alpha = 0.4f),
                    onClick = {
                        if (!isManualMode && player != null) {
                            haptic.perform(StashHapticFeedbackType.Medium)
                            scope.launch {
                                muteScale.animateTo(1.25f, tween(100))
                                muteScale.animateTo(
                                    1f,
                                    spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                )
                            }
                            if (player.volume > 0f) {
                                player.volume = 0f
                                isMuted = true
                            } else {
                                player.volume = 1f
                                isMuted = false
                            }
                        }
                    },
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AnimatedContent(
                            targetState = isMuted,
                            transitionSpec = {
                                (
                                    scaleIn(animationSpec = tween(300)) + fadeIn(
                                        animationSpec = tween(
                                            300
                                        )
                                    )
                                    )
                                    .togetherWith(
                                        scaleOut(animationSpec = tween(300)) + fadeOut(
                                            animationSpec = tween(300)
                                        )
                                    )
                            },
                            label = "muteIcon"
                        ) { muted ->
                            Icon(
                                imageVector = if (muted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                contentDescription = if (muted) "Unmute" else "Mute",
                                tint = muteButtonColor,
                                modifier = Modifier.size(iconSizeMute)
                            )
                        }
                    }
                }
            }
        }

        // Sprite Preview Popup (overlay, outside main layout)
        scrubBitmap?.let { bitmap ->
            if ((isScrubbing || isExiting) && sliderSize.width > 0) {
                val progress = currentPosition / durationSeconds
                val offsetX = (progress * sliderSize.width)
                val previewWidth = 224.dp
                val halfWidthPx = with(density) { (previewWidth / 2).toPx() }
                // Allow extra movement to the right (add some extra room beyond track width)
                val extraRightPadding = with(density) { 40.dp.toPx() }
                val maxX = sliderSize.width.toFloat() - halfWidthPx * 2 + extraRightPadding
                val startX = (offsetX - halfWidthPx).coerceIn(0f, maxX.coerceAtLeast(0f))

                val previewHeight = (previewWidth * (9f / 16f))
                val previewHeightPx = with(density) { previewHeight.toPx() }
                val yOffsetPx = -(previewHeightPx + 20).toInt()

                Popup(
                    alignment = Alignment.TopStart,
                    offset = IntOffset(x = startX.toInt().coerceAtLeast(0), y = yOffsetPx)
                ) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = thumbScale.value
                                scaleY = thumbScale.value
                                alpha = thumbScale.value.coerceIn(0f, 1f)
                            }
                            .width(previewWidth)
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )

                        // Marker name overlay on preview
                        hoveredMarkerName?.let { markerName ->
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = markerName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = tintColor,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
