package goonarr.stash.features.scenes.detail.components.markers

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import coil.compose.AsyncImage
import coil.request.ImageRequest
import goonarr.stash.LocalBlurNsfw
import goonarr.stash.StashTheme
import goonarr.stash.core.model.SceneMarker
import goonarr.stash.features.components.bounceClickable
import goonarr.stash.features.player.VideoPlayer
import goonarr.stash.util.DateFormatters
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType
import timber.log.Timber

@SuppressLint("ConfigurationScreenWidthHeight")
@UnstableApi
@Composable
fun SceneMarkerCard(
    marker: SceneMarker,
    onSeek: (Double) -> Unit,
    getStreamUrl: suspend (String?) -> String?,
    modifier: Modifier = Modifier
) {
    var streamUrl by remember { mutableStateOf<String?>(null) }
    var isVisible by remember { mutableStateOf(false) }
    var shouldPlay by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val haptic = LocalStashHapticFeedback.current

    // Resolve stream URL
    LaunchedEffect(marker.stream) {
        val resolved = getStreamUrl(marker.stream)
        Timber.d("Marker ${marker.id}: stream=${marker.stream}, resolved=$resolved")
        streamUrl = resolved
    }

    // Debounce playback
    LaunchedEffect(isVisible) {
        shouldPlay = isVisible
    }

    val dynamicColors = StashTheme.colors

    Column(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInWindow()
                val size = coordinates.size
                val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

                isVisible = position.y < screenHeightPx && position.y + size.height > 0
            }
            .bounceClickable(
                shape = RoundedCornerShape(8.dp),
                spotColor = dynamicColors.complementary,
                ambientColor = dynamicColors.complementary,
                onClick = {
                    haptic.perform(StashHapticFeedbackType.Selection)
                    onSeek(marker.seconds)
                }
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            val isBlurred = LocalBlurNsfw.current

            if (streamUrl != null && shouldPlay && !isBlurred) {
                // Video Player (Only if NOT blurred and SHOULD play)
                VideoPlayer(
                    url = streamUrl,
                    isMuted = true,
                    useController = false,
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
                    onPlayerCreated = { player ->
                        player.repeatMode = Player.REPEAT_MODE_ONE
                        player.play()
                    }
                )
            } else {
                // Thumbnail (Blurred if NSFW is on)
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(marker.preview)
                        .crossfade(true)
                        .build(),
                    contentDescription = marker.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .then(if (isBlurred) Modifier.blur(20.dp) else Modifier)
                )
            }

            // Timestamp badge
            Text(
                text = DateFormatters.formatDuration(marker.seconds),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )

            // Title Overlay
            Text(
                text = marker.title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
                    .widthIn(max = 220.dp) // Don't overlap with timestamp
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}
