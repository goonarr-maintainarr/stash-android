package goonarr.stash.features.scenes.detail.components.header

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import goonarr.stash.StashBlue
import goonarr.stash.StashTheme
import goonarr.stash.core.model.Scene
import goonarr.stash.features.components.OCounterButton
import goonarr.stash.features.components.RatingBar
import goonarr.stash.util.DateFormatters
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType
import timber.log.Timber

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SceneHeader(
    scene: Scene,
    onRateScene: (Int) -> Unit,
    onIncrementOCounter: () -> Unit,
    onStudioClick: (String) -> Unit = {},
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    val haptic = LocalStashHapticFeedback.current
    val dotColor = StashTheme.colors.tertiary

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = scene.title ?: "No Title",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            scene.studio?.let { studio ->
                val context = LocalContext.current
                val isImageMissing = studio.imagePath.isNullOrBlank() || studio.imagePath.contains("default=true")

                // Studio Chip: Image if available, Text otherwise.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {
                            if (studio.id.isNotBlank()) {
                                haptic.perform(StashHapticFeedbackType.Light)
                                Timber.d("🏢 Navigation: SceneDetail -> StudioDetail for id: ${studio.id}")
                                onStudioClick(studio.id)
                            } else {
                                Timber.w("⚠️ Attempted to navigate to studio with blank ID")
                            }
                        }
                        .padding(vertical = 2.dp, horizontal = 4.dp)
                ) {
                    if (!isImageMissing) {
                        val imageRequest = remember(studio.imagePath) {
                            ImageRequest.Builder(context)
                                .data(studio.imagePath)
                                .crossfade(true)
                                .build()
                        }

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

                        SubcomposeAsyncImage(
                            model = imageRequest,
                            contentDescription = studio.name ?: "Studio",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .height(24.dp)
                                .widthIn(max = 120.dp)
                                .then(sharedElementModifier),
                            error = {
                                Text(
                                    modifier = Modifier.widthIn(max = 200.dp),
                                    text = studio.name ?: "Studio",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = StashBlue,
                                    maxLines = 1,
                                )
                            },
                            success = {
                                SubcomposeAsyncImageContent()
                            }
                        )
                    } else {
                        Text(
                            modifier = Modifier.widthIn(max = 200.dp),
                            text = studio.name ?: "Studio",
                            style = MaterialTheme.typography.bodyMedium,
                            color = StashBlue,
                            maxLines = 1,
                        )
                    }
                }

                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodyMedium,
                    color = dotColor
                )
            }

            val formattedDate = DateFormatters.formatDateString(scene.date)
            if (formattedDate.isNotEmpty()) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodyMedium,
                    color = dotColor
                )
            }

            val durationSecs = scene.files?.firstOrNull()?.duration ?: 0.0
            if (durationSecs > 0) {
                Text(
                    text = DateFormatters.formatDuration(durationSecs),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Rating and O-Counter
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            RatingBar(
                rating = (scene.rating100 ?: 0) / 20,
                onRatingChanged = onRateScene
            )

            OCounterButton(
                count = scene.oCounter ?: 0,
                onClick = onIncrementOCounter
            )
        }
    }
}
