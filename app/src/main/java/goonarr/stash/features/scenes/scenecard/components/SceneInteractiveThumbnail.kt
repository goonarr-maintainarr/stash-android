package goonarr.stash.features.scenes.scenecard.components

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import dagger.hilt.android.EntryPointAccessors
import goonarr.stash.LocalBlurNsfw
import goonarr.stash.R
import goonarr.stash.StashBlue
import goonarr.stash.core.di.StashEntryPoint
import goonarr.stash.core.model.Scene
import goonarr.stash.features.scenes.utils.SpriteManager
import goonarr.stash.util.DateFormatters
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType
import kotlinx.coroutines.launch

/**
 * A highly interactive thumbnail component for scenes.
 *
 * Capabilities:
 * - **Static Thumbnail**: Displays the scene's screenshot (blurred if NSFW).
 * - **Video Preview**: Plays a short video clip on long-press (if available).
 * - **Sprite Scrubbing**: Allows scrubbing through the video timeline using sprite sheets (if available).
 * - **Progress Overlay**: Shows a progress bar indicating resume time or scrub position.
 * - **Duration Label**: Displays the video duration or current scrub time.
 *
 * @param scene The [Scene] model to display.
 * @param onClick Callback invoked when the user taps the thumbnail.
 * @param modifier Modifier to apply to the layout.
 * @param allowScrubbing Whether to enable the sprite scrubbing gesture (default: true).
 * @param isPlayingPreview Whether the video preview is currently playing.
 * @param onPlayingPreviewChange Callback to update the preview playing state.
 */
@OptIn(UnstableApi::class)
@Composable
fun SceneInteractiveThumbnail(
    scene: Scene,
    onClick: (Double?) -> Unit,
    modifier: Modifier = Modifier,
    allowScrubbing: Boolean = true,
    isPlayingPreview: Boolean = false,
    onPlayingPreviewChange: (Boolean) -> Unit = {},
    // Hoisted State
    isScrubbing: Boolean,
    onScrubbingChange: (Boolean) -> Unit,
    hasScrubbed: Boolean,
    onHasScrubbedChange: (Boolean) -> Unit,
    scrubProgress: Float,
    onScrubProgressChange: (Float) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val stashHaptic = LocalStashHapticFeedback.current

    // State
    // isScrubbing, hasScrubbed, scrubProgress are hoisted
    var scrubBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var thumbnailSize by remember { mutableStateOf(IntSize.Zero) }
    var isInitialized by remember { mutableStateOf(false) }
    var lastScrubMinute by remember { mutableStateOf(-1) }

    // Dependencies
    val isInPreview = LocalInspectionMode.current
    val entryPoint = if (isInPreview) {
        null
    } else {
        remember {
            EntryPointAccessors.fromApplication(context, StashEntryPoint::class.java)
        }
    }
    val spriteManager = if (isInPreview || entryPoint == null) {
        null
    } else {
        remember {
            SpriteManager(context, entryPoint.httpClient(), entryPoint.vttParser())
        }
    }
    val urlFormatter = if (isInPreview || entryPoint == null) null else remember { entryPoint.urlFormatter() }

    val duration = scene.files?.firstOrNull()?.duration ?: 1.0

    // Check capabilities
    val canScrub = !isInPreview && allowScrubbing && scene.paths?.vtt != null && scene.paths.sprite != null && duration > 0
    val canPreview = !isInPreview && scene.paths?.preview != null

    // Video preview state

    var previewUrl by remember { mutableStateOf<String?>(null) }

    // Resolve preview URL when needed
    LaunchedEffect(isPlayingPreview) {
        if (isPlayingPreview && previewUrl == null && urlFormatter != null) {
            previewUrl = urlFormatter.resolveUrl(scene.paths?.preview)
        }
    }

    Box(
        modifier = modifier.onSizeChanged { thumbnailSize = it }
    ) {
        // 1. Video Preview Player
        if (isPlayingPreview && previewUrl != null) {
            val exoPlayer = remember(previewUrl) {
                ExoPlayer.Builder(context).build().apply {
                    setMediaItem(MediaItem.fromUri(previewUrl!!))
                    repeatMode = ExoPlayer.REPEAT_MODE_ALL
                    volume = 0f // Muted
                    prepare()
                    playWhenReady = true
                }
            }

            DisposableEffect(exoPlayer) {
                onDispose {
                    exoPlayer.release()
                }
            }

            AndroidView(
                factory = { ctx ->
                    (LayoutInflater.from(ctx).inflate(R.layout.view_player_texture, null) as PlayerView).apply {
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    }
                },
                update = { playerView ->
                    playerView.player = exoPlayer
                },
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        stashHaptic.perform(StashHapticFeedbackType.Selection)
                        onPlayingPreviewChange(false)
                    }
            )
        } else if (scrubBitmap != null) {
            // 2. Sprite Scrub Preview
            Image(
                bitmap = scrubBitmap!!.asImageBitmap(),
                contentDescription = "Scrub preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (LocalBlurNsfw.current) Modifier.blur(20.dp) else Modifier)
            )
        } else {
            // 3. Static Thumbnail
            SmartImage(
                model = scene.paths?.screenshot,
                contentDescription = scene.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (LocalBlurNsfw.current) Modifier.blur(20.dp) else Modifier)
            )
        }

        // 4. Preview Zone (Top 75%) - tap to navigate, long-press to play preview
        if (!isPlayingPreview && !isScrubbing) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(if (canScrub) 0.75f else 1f)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                stashHaptic.perform(StashHapticFeedbackType.Selection)
                                onClick(if (hasScrubbed) scrubProgress * duration else null)
                            },
                            onLongPress = {
                                if (canPreview) {
                                    stashHaptic.perform(StashHapticFeedbackType.Medium)
                                    onPlayingPreviewChange(true)
                                }
                            }
                        )
                    }
            )
        }

        // 5. Progress Bar
        val displayProgress = if (isScrubbing || hasScrubbed) {
            scrubProgress
        } else {
            ((scene.resumeTime ?: 0.0) / duration).toFloat()
        }
        if (displayProgress > 0f || isScrubbing || hasScrubbed) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(displayProgress.coerceIn(0f, 1f))
                    .height(if (isScrubbing || hasScrubbed) 6.dp else 4.dp)
                    .background(StashBlue)
            )
        }

        // 6. Time Pill
        if (duration > 0) {
            val displayTime = if (isScrubbing || hasScrubbed) scrubProgress * duration else duration
            Text(
                text = DateFormatters.formatDuration(displayTime),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }

        // 7. Scrub Zone (Bottom 25%)
        if (canScrub) {
            val view = LocalView.current
            val consumeScrollConnection = remember {
                object : NestedScrollConnection {
                    override fun onPreScroll(
                        available: Offset,
                        source: NestedScrollSource
                    ): Offset {
                        return available
                    }
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.25f)
                    .nestedScroll(consumeScrollConnection)
                    .pointerInteropFilter { motionEvent ->
                        when (motionEvent.action) {
                            MotionEvent.ACTION_DOWN -> {
                                view.parent?.requestDisallowInterceptTouchEvent(true)
                            }
                            MotionEvent.ACTION_UP,
                            MotionEvent.ACTION_CANCEL -> {
                                view.parent?.requestDisallowInterceptTouchEvent(false)
                            }
                        }
                        false
                    }
                    .pointerInput(scene.id) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            val longPress = awaitLongPressOrCancellation(down.id)

                            if (longPress != null) {
                                longPress.consume()
                                stashHaptic.perform(StashHapticFeedbackType.Medium)
                                stashHaptic.perform(StashHapticFeedbackType.Medium)
                                onScrubbingChange(true)
                                onHasScrubbedChange(true)
                                isInitialized = false
                                lastScrubMinute = -1
                                val initialProgress = (longPress.position.x / (thumbnailSize.width * 0.9f)).coerceIn(0f, 1f)
                                onScrubProgressChange(initialProgress)

                                scope.launch {
                                    val vttUrl = urlFormatter!!.resolveUrl(scene.paths.vtt)
                                    val spriteUrl = urlFormatter.resolveUrl(scene.paths.sprite)
                                    spriteManager!!.initialize(vttUrl, spriteUrl)
                                    isInitialized = true
                                    // Use local variable, not stale state parameter
                                    scrubBitmap = spriteManager.getThumbnail(initialProgress * duration)
                                }

                                do {
                                    val event = awaitPointerEvent()
                                    event.changes.forEach { change ->
                                        if (change.pressed) {
                                            change.consume()
                                            val newProgress = (change.position.x / (thumbnailSize.width * 0.9f)).coerceIn(0f, 1f)
                                            onScrubProgressChange(newProgress)

                                            val currentSecond = newProgress * duration
                                            val currentMinute = (currentSecond / 60).toInt()
                                            if (currentMinute != lastScrubMinute && lastScrubMinute != -1) {
                                                stashHaptic.perform(StashHapticFeedbackType.Light)
                                            }
                                            lastScrubMinute = currentMinute

                                            if (isInitialized) {
                                                scrubBitmap = spriteManager!!.getThumbnail(newProgress * duration)
                                            }
                                        }
                                    }
                                } while (event.changes.any { it.pressed })

                                onScrubbingChange(false)
                                // scrubBitmap = null // Persist image
                                isInitialized = false
                            }
                        }
                    }
            ) {
                Spacer(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
