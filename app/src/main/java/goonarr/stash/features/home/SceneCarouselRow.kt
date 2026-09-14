package goonarr.stash.features.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import goonarr.stash.StashBlue
import goonarr.stash.StashTheme
import goonarr.stash.core.model.Scene
import goonarr.stash.features.scenes.scenecard.SceneCard
import goonarr.stash.features.scenes.scenecard.shimmers.SceneCardShimmer
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.MockData
import goonarr.stash.util.StashHapticFeedbackType

@Composable
fun SceneCarouselRow(
    modifier: Modifier = Modifier,
    title: String,
    count: Int? = null,
    scenes: List<Scene>,
    sort: String? = null,
    direction: String? = "DESC",
    onSceneClick: (String, Double?) -> Unit,
    onPerformerClick: (String) -> Unit = {},
    onStudioClick: (String) -> Unit = {},
    onSeeAllClick: (String, String, List<String>) -> Unit
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (count != null) {
                    Text(
                        text = " • $count",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }
            val haptic = LocalStashHapticFeedback.current
            TextButton(onClick = {
                haptic.perform(StashHapticFeedbackType.Selection)
                onSeeAllClick(sort ?: "", direction ?: "DESC", scenes.map { it.id })
            }) {
                Text(
                    text = "View All",
                    color = StashBlue
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(scenes, key = { it.id }) { scene ->
                SceneCard(
                    scene = scene,
                    onClick = { time -> onSceneClick(scene.id, time) },
                    onPerformerClick = onPerformerClick,
                    onStudioClick = onStudioClick,
                    isCarousel = true,
                    modifier = Modifier.width(390.dp)
                )
            }
        }
    }
}

@Composable
fun SceneCarouselRowShimmer(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(28.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            // See All placeholder aligned to the right
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(24.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
            userScrollEnabled = false
        ) {
            items(5) {
                SceneCardShimmer(
                    isCarousel = true,
                    modifier = Modifier.width(390.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SceneCarouselRowPreview() {
    StashTheme {
        SceneCarouselRow(
            title = "Recently Released",
            count = 14,
            scenes = MockData.scenes.take(5),
            sort = "date",
            direction = "DESC",
            onSceneClick = { _, _ -> },
            onPerformerClick = {},
            onSeeAllClick = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SceneCarouselRowShimmerPreview() {
    StashTheme {
        SceneCarouselRowShimmer()
    }
}
