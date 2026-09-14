package goonarr.stash.features.scenes.scenecard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import goonarr.stash.StashBlue
import goonarr.stash.core.model.Scene
import goonarr.stash.features.components.bounceClickable
import goonarr.stash.features.scenes.scenecard.components.SceneInteractiveThumbnail
import goonarr.stash.features.scenes.scenecard.shimmers.SceneGridCardShimmer
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType

/**
 * Grid-style scene card: Thumbnail with runtime, title (1 line), performer names.
 */
@Composable
fun SceneGridCard(
    scene: Scene,
    onClick: (Double?) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    onPerformerClick: (String) -> Unit = {}
) {
    if (isLoading) {
        SceneGridCardShimmer(modifier)
    } else {
        SceneGridCardContent(scene, onClick, modifier, onPerformerClick)
    }
}

@Composable
private fun SceneGridCardContent(
    scene: Scene,
    onClick: (Double?) -> Unit,
    modifier: Modifier = Modifier,
    onPerformerClick: (String) -> Unit = {}
) {
    val stashHaptic = LocalStashHapticFeedback.current

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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .bounceClickable(
                shape = RoundedCornerShape(8.dp),
                onClick = onContentClick
            ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Thumbnail with Runtime Pill
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

            Column(modifier = Modifier.padding(8.dp)) {
                // Title (max 1 line)
                Text(
                    text = scene.title ?: "No Title",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Performers (clickable, blue)
                val performers = scene.performers ?: emptyList()
                if (performers.isNotEmpty()) {
                    Row {
                        performers.forEachIndexed { index, performer ->
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
                            if (index < performers.size - 1) {
                                Text(
                                    text = ", ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = StashBlue
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
