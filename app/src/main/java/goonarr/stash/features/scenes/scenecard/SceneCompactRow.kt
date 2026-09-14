package goonarr.stash.features.scenes.scenecard

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import goonarr.stash.R
import goonarr.stash.StashBlue
import goonarr.stash.core.model.Scene
import goonarr.stash.features.components.bounceClickable
import goonarr.stash.features.scenes.scenecard.components.SceneInteractiveThumbnail
import goonarr.stash.features.scenes.scenecard.shimmers.SceneCompactRowShimmer
import goonarr.stash.rememberSceneColors
import goonarr.stash.util.DateFormatters
import goonarr.stash.util.HeroAccentColor
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType
import timber.log.Timber

/**
 * Compact row-style scene card: Thumbnail on left (~40%), info stacked on right.
 */
@Composable
fun SceneCompactRow(
    scene: Scene,
    onClick: (Double?) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    onPerformerClick: (String) -> Unit = {},
    onStudioClick: (String) -> Unit = {}
) {
    if (isLoading) {
        SceneCompactRowShimmer(modifier)
    } else {
        SceneCompactRowContent(scene, onClick, modifier, onPerformerClick, onStudioClick)
    }
}

@Composable
private fun SceneCompactRowContent(
    scene: Scene,
    onClick: (Double?) -> Unit,
    modifier: Modifier = Modifier,
    onPerformerClick: (String) -> Unit = {},
    onStudioClick: (String) -> Unit = {}
) {
    val stashHaptic = LocalStashHapticFeedback.current
    var isDescriptionExpanded by remember { mutableStateOf(false) }

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

    var isPlayingPreview by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isInPreview = LocalInspectionMode.current

    // Animation state
    val thumbnailWeight by animateFloatAsState(
        targetValue = if (isPlayingPreview) 1f else 0.4f,
        label = "thumbnailWeight"
    )

    // Hero color extraction
    var heroColor by remember { mutableStateOf<Color?>(null) }
    LaunchedEffect(scene.paths?.screenshot) {
        if (!isInPreview) {
            heroColor = HeroAccentColor.extract(context, scene.paths?.screenshot)
        }
    }

    // Animate height when playing preview (expand to ~2 rows height)
    val targetHeight = if (isPlayingPreview) 240.dp else 120.dp
    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        label = "rowHeight"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .bounceClickable(shape = RoundedCornerShape(12.dp), onClick = onContentClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isDescriptionExpanded) Dp.Unspecified else animatedHeight)
        ) {
            // Thumbnail (~40% -> 100% width)
            SceneInteractiveThumbnail(
                scene = scene,
                onClick = onClick,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(thumbnailWeight),
                isPlayingPreview = isPlayingPreview,
                onPlayingPreviewChange = { isPlayingPreview = it },
                isScrubbing = isScrubbing,
                onScrubbingChange = { isScrubbing = it },
                hasScrubbed = hasScrubbed,
                onHasScrubbedChange = { hasScrubbed = it },
                scrubProgress = scrubProgress,
                onScrubProgressChange = { scrubProgress = it }
            )

            // Info Section (collapses when playing)
            if (thumbnailWeight < 0.95f) {
                Column(
                    modifier = Modifier
                        .weight(1f - thumbnailWeight)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top: Title, Studio • Date, Description
                    Column {
                        // Title (2 lines)
                        Text(
                            text = scene.title ?: "No Title",
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 2,
                            minLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Studio • Date (on same line)
                        val studioId = scene.studio?.id
                        val studioName = scene.studio?.name ?: "Unknown Studio"
                        val formattedDate = DateFormatters.formatDateString(scene.date)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = studioName,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (studioId != null) StashBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                modifier = if (studioId != null) {
                                    Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable {
                                            stashHaptic.perform(StashHapticFeedbackType.Light)
                                            if (studioId.isNotBlank()) {
                                                Timber.d("🏢 Navigation: Home -> StudioDetail for id: $studioId")
                                                onStudioClick(studioId)
                                            }
                                        }
                                        .padding(vertical = 2.dp)
                                } else {
                                    Modifier
                                }
                            )
                            if (formattedDate.isNotEmpty()) {
                                val sceneColors = rememberSceneColors(heroColor)
                                Text(
                                    text = " • ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = sceneColors.complementary,
                                )
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }

                        // Description (expandable on long-press)
                        // Always reserve 2 lines of space even if empty
                        val details = scene.details
                        Text(
                            text = details ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 2,
                            minLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = if (!details.isNullOrBlank()) {
                                Modifier.pointerInput(Unit) {
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
                            } else {
                                Modifier
                            }
                        )
                    }

                    // Bottom: Performers + Icons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Performers
                        val performers = scene.performers ?: emptyList()
                        Row(modifier = Modifier.weight(1f)) {
                            performers.take(2).forEachIndexed { index, performer ->
                                Text(
                                    text = performer.name ?: "Unknown",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StashBlue,
                                    maxLines = 1,
                                    modifier = Modifier.clickable {
                                        stashHaptic.perform(StashHapticFeedbackType.Light)
                                        onPerformerClick(performer.id)
                                    }
                                )
                                if (index < minOf(performers.size - 1, 1)) {
                                    Text(
                                        text = ", ",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = StashBlue
                                    )
                                }
                            }
                            if (performers.size > 2) {
                                Text(
                                    text = " +${performers.size - 2}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Icons: O-Counter, Markers, Rating
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val oCount = scene.oCounter ?: 0
                            if (oCount > 0) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_sweat_drops),
                                        contentDescription = "O-Counter",
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(text = "$oCount", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            val markerCount = scene.sceneMarkers?.size ?: 0
                            if (markerCount > 0) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_map_marker),
                                        contentDescription = "Markers",
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "$markerCount",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }

                            val rating100 = scene.rating100
                            if (rating100 != null && rating100 > 0) {
                                val ratingValue = rating100 / 20.0
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_star_filled),
                                        contentDescription = "Rating",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = String.format("%.1g", ratingValue),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFFFD700)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
