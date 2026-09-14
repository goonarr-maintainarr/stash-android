package goonarr.stash.features.scenes.scenecard

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import goonarr.stash.R
import goonarr.stash.StashBlue
import goonarr.stash.core.model.Scene
import goonarr.stash.features.components.bounceClickable
import goonarr.stash.features.scenes.scenecard.components.SceneInteractiveThumbnail
import goonarr.stash.features.scenes.scenecard.shimmers.SceneCardShimmer
import goonarr.stash.rememberSceneColors
import goonarr.stash.util.DateFormatters
import goonarr.stash.util.HeroAccentColor
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType
import timber.log.Timber

/**
 * Main scene card composable that orchestrates the display of a scene.
 * Handles loading state and delegates to [SceneCardContent].
 *
 * @param scene The [Scene] model to display.
 * @param onClick Callback invoked when the card is clicked.
 * @param modifier Modifier to apply to the card.
 * @param isLoading Whether to display a shimmer loading state.
 * @param isCompact If true, displays a compact version (1 line title, no description). Default is false.
 * @param isCarousel If true, optimizes the layout for a horizontal carousel (1 line title, 1 line description). Default is false.
 * @param initiallyExpanded If true, the description is initially expanded (if applicable). Default is false.
 * @param onPerformerClick Callback invoked when a performer name is clicked.
 * @param onStudioClick Callback invoked when the studio name is clicked.
 */
@Composable
fun SceneCard(
    scene: Scene,
    onClick: (Double?) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    // Compact: 1 line title, no description. Full: 4 line title, with description
    isCompact: Boolean = false,
    // Carousel: 1 line title, 1 line description (or reserved space)
    isCarousel: Boolean = false,
    initiallyExpanded: Boolean = false,
    onPerformerClick: (String) -> Unit = {},
    onStudioClick: (String) -> Unit = {},
) {
    if (isLoading) {
        SceneCardShimmer(modifier, isCompact)
    } else {
        SceneCardContent(
            scene,
            onClick,
            modifier,
            isCompact,
            isCarousel,
            initiallyExpanded,
            onPerformerClick,
            onStudioClick
        )
    }
}

/**
 * Internal content composable for [SceneCard].
 * Displays the interactive thumbnail, title, metadata, description, and footer icons.
 */
@SuppressLint("DefaultLocale")
@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SceneCardContent(
    scene: Scene,
    onClick: (Double?) -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    isCarousel: Boolean = false,
    initiallyExpanded: Boolean = false,
    onPerformerClick: (String) -> Unit = {},
    onStudioClick: (String) -> Unit = {},
) {
    // State for expandable description
    var isDescriptionExpanded by remember { mutableStateOf(initiallyExpanded) }

    // Scrubbing State
    var isScrubbing by remember { mutableStateOf(false) }
    var hasScrubbed by remember { mutableStateOf(false) }
    var scrubProgress by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }

    val duration = scene.files?.firstOrNull()?.duration ?: 0.0

    // Helper to invoke onClick with the correct time based on scrub state
    val onContentClick: () -> Unit = {
        val time = if (hasScrubbed) scrubProgress * duration else null
        onClick(time)
    }

    // Haptics
    val stashHaptic = LocalStashHapticFeedback.current

    // Hero color extraction
    val context = LocalContext.current
    var heroColor by remember { mutableStateOf<Color?>(null) }
    val isInPreview = LocalInspectionMode.current
    LaunchedEffect(scene.paths?.screenshot) {
        if (!isInPreview) {
            heroColor = HeroAccentColor.extract(context, scene.paths?.screenshot)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .bounceClickable(
                shape = RoundedCornerShape(8.dp),
                onClick = onContentClick
            ),
        // Entire card clickable with bounce
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Thumbnail with Scrubber Overlay
            var isPlayingPreview by remember { mutableStateOf(false) }
            SceneInteractiveThumbnail(
                scene = scene,
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                isPlayingPreview = isPlayingPreview,
                onPlayingPreviewChange = { isPlayingPreview = it },
                isScrubbing = isScrubbing,
                onScrubbingChange = { isScrubbing = it },
                hasScrubbed = hasScrubbed,
                onHasScrubbedChange = { hasScrubbed = it },
                scrubProgress = scrubProgress,
                onScrubProgressChange = { scrubProgress = it }
            )

            Column(modifier = Modifier.padding(12.dp)) {
                // Title
                Text(
                    text = scene.title ?: "No Title",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = if (isCompact || isCarousel) 1 else 4,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Studio, date, duration row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val studioId = scene.studio?.id
                    val studioName = scene.studio?.name ?: "Unknown Studio"

                    // Studio name - clickable
                    Text(
                        text = studioName,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (studioId != null) StashBlue else MaterialTheme.typography.bodySmall.color,
                        modifier = if (studioId != null) {
                            Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    stashHaptic.perform(StashHapticFeedbackType.Light)
                                    if (studioId.isNotBlank()) {
                                        Timber.d("🏢 Navigation: Home -> StudioDetail for id: $studioId")
                                        onStudioClick(studioId)
                                    } else {
                                        Timber.w("⚠️ Attempted to navigate to studio with blank ID")
                                    }
                                }
//                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        } else {
                            Modifier
                        }
                    )

                    val formattedDate = DateFormatters.formatDateString(scene.date)
                    if (formattedDate.isNotEmpty()) {
                        val sceneColors = rememberSceneColors(heroColor)
                        Text(
                            text = " • ",
                            style = MaterialTheme.typography.bodySmall,
                            color = sceneColors.tertiary
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Description (expandable on long-press) - only show if not compact and not carousel
                if (!isCompact && !isCarousel && !scene.details.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    val description = scene.details
                    val truncatedDescription =
                        if (description.length > 200 && !isDescriptionExpanded) {
                            description.take(200) + "..."
                        } else {
                            description
                        }

                    Text(
                        text = truncatedDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    stashHaptic.perform(StashHapticFeedbackType.Selection)
                                    onContentClick()
                                },
                                onLongPress = {
                                    isDescriptionExpanded = !isDescriptionExpanded
                                    stashHaptic.perform(StashHapticFeedbackType.Medium)
                                }
                            )
                        }
                    )
                }

                // Carousel mode: always show 1 line for description (or reserve space)
                if (isCarousel) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = scene.details?.take(100) ?: " ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Performers and Icons
                val performers = scene.performers ?: emptyList()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f)
                    ) {
                        if (performers.isEmpty()) {
                            // Placeholder to maintain consistent card height
                            Text(
                                text = " ",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1
                            )
                        } else {
                            val sceneColors = rememberSceneColors(heroColor)
                            performers.forEachIndexed { index, performer ->
                                val performerId = performer.id
                                Text(
                                    text = performer.name ?: "Unknown",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = StashBlue,
                                    maxLines = 1,
                                    modifier = Modifier.clickable {
                                        stashHaptic.perform(StashHapticFeedbackType.Light)
                                        onPerformerClick(performerId)
                                    }
                                )
                                if (index < performers.size - 1) {
                                    Text(
                                        text = ", ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = sceneColors.tertiary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // O-Counter (only if > 0)
                    val oCount = scene.oCounter ?: 0
                    if (oCount > 0) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sweat_drops),
                            contentDescription = "O-Counter",
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$oCount",
                            style = MaterialTheme.typography.labelMedium,
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    // Markers (only if > 0)
                    val markerCount = scene.sceneMarkers?.size ?: 0
                    if (markerCount > 0) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_map_marker),
                            contentDescription = "Markers",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$markerCount",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    // Rating (gold star with number, 0-100 converted to 0-5)
                    val rating100 = scene.rating100
                    if (rating100 != null && rating100 > 0) {
                        val ratingValue = rating100 / 20.0
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_star_filled),
                                contentDescription = "Rating",
                                // Gold
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%.1g", ratingValue),
                                style = MaterialTheme.typography.labelMedium,
                                // Gold
                                color = Color(0xFFFFD700)
                            )
                        }
                    }
                }
            }
        }
    }
}
