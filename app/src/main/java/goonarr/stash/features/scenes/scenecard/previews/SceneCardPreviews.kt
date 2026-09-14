package goonarr.stash.features.scenes.scenecard.previews

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import goonarr.stash.StashTheme
import goonarr.stash.features.scenes.scenecard.SceneCard
import goonarr.stash.features.scenes.scenecard.SceneCompactRow
import goonarr.stash.features.scenes.scenecard.SceneGridCard
import goonarr.stash.features.scenes.scenecard.components.SceneInteractiveThumbnail
import goonarr.stash.features.scenes.scenecard.components.SmartImage
import goonarr.stash.util.MockData

/**
 * Previews for the main [SceneCard] in various configurations.
 * - Full list layout
 * - Compact list layout
 * - Expanded description state
 */
@Preview(
    showBackground = true, heightDp = 2000, uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SceneCardPreview() {
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Full Layout (Scene List)", style = MaterialTheme.typography.labelLarge)
                SceneCard(scene = MockData.scene, onClick = { _ -> }, isCompact = false)

                Spacer(modifier = Modifier.height(8.dp))

                Text("Compact Layout (Home Carousel)", style = MaterialTheme.typography.labelLarge)
                SceneCard(scene = MockData.scenes[2], onClick = { _ -> }, isCompact = true)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Expanded Full Layout (with Tags)",
                    style = MaterialTheme.typography.labelLarge
                )
                SceneCard(
                    scene = MockData.scenes[0],
                    onClick = { _ -> },
                    isCompact = false,
                    initiallyExpanded = true
                )
            }
        }
    }
}

/**
 * Previews for the [SceneCard] loading (shimmer) state.
 */
@Preview(
    showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SceneCardLoadingPreview() {
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Full Shimmer (Scene List)", style = MaterialTheme.typography.labelLarge)
                SceneCard(
                    scene = MockData.scene, onClick = { _ -> }, isLoading = true, isCompact = false
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Compact Shimmer (Home Carousel)", style = MaterialTheme.typography.labelLarge)
                SceneCard(
                    scene = MockData.scene, onClick = { _ -> }, isLoading = true, isCompact = true
                )
            }
        }
    }
}

/**
 * Previews for the [SceneGridCard] in a grid layout.
 * Shows two cards side-by-side to verify spacing.
 */
@Preview(
    showBackground = true, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SceneGridCardPreview() {
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Grid Card", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SceneGridCard(
                        scene = MockData.scene,
                        onClick = { _ -> },
                        modifier = Modifier.weight(1f)
                    )
                    SceneGridCard(
                        scene = MockData.scenes[1],
                        onClick = { _ -> },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Grid Card Shimmer", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    SceneGridCard(
                        scene = MockData.scene,
                        onClick = { _ -> },
                        isLoading = true,
                        modifier = Modifier.weight(1f)
                    )
                    SceneGridCard(
                        scene = MockData.scene,
                        onClick = { _ -> },
                        isLoading = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Previews for the [SceneCompactRow].
 * Shows multiple rows and shimmer state.
 */
@Preview(
    showBackground = true, heightDp = 600, uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SceneCompactRowPreview() {
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Compact Row", style = MaterialTheme.typography.labelLarge)
                SceneCompactRow(
                    scene = MockData.scene,
                    onClick = { _ -> }
                )

                SceneCompactRow(
                    scene = MockData.scenes[1],
                    onClick = { _ -> }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Compact Row Shimmer", style = MaterialTheme.typography.labelLarge)
                SceneCompactRow(
                    scene = MockData.scene,
                    onClick = { _ -> },
                    isLoading = true
                )
            }
        }
    }
}

/**
 * Previews for the [SmartImage] component.
 * Demonstrates handling of null, URL, and Data URI sources.
 */
@Preview(
    showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SmartImagePreview() {
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("SmartImage States", style = MaterialTheme.typography.titleMedium)

                // Null image
                Text("Null (empty placeholder)", style = MaterialTheme.typography.labelMedium)
                SmartImage(
                    model = null,
                    contentDescription = "Null image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )

                // URL image (will show placeholder in preview)
                Text(
                    "URL (Coil - shows loading in preview)",
                    style = MaterialTheme.typography.labelMedium
                )
                SmartImage(
                    model = "https://example.com/image.jpg",
                    contentDescription = "URL image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )

                // Data URI (would decode if valid base64)
                Text("Data URI (Base64 decode)", style = MaterialTheme.typography.labelMedium)
                SmartImage(
                    model = "data:image/jpeg;base64,invalidbase64",
                    contentDescription = "Data URI image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
            }
        }
    }
}

/**
 * Preview for the interactive thumbnail in a list context (16:9 full width).
 */
@Preview(
    name = "Video Thumbnail (List)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SceneInteractiveThumbnailListPreview() {
    StashTheme(darkTheme = true) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.padding(16.dp)
        ) {
            SceneInteractiveThumbnail(
                scene = MockData.scene,
                onClick = { _ -> },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                isPlayingPreview = false,
                onPlayingPreviewChange = {},
                isScrubbing = false,
                onScrubbingChange = {},
                hasScrubbed = false,
                onHasScrubbedChange = {},
                scrubProgress = 0f,
                onScrubProgressChange = {}
            )
        }
    }
}

/**
 * Preview for the interactive thumbnail in a grid context (constrained width).
 */
@Preview(
    name = "Video Thumbnail (Grid)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SceneInteractiveThumbnailGridPreview() {
    StashTheme(darkTheme = true) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(modifier = Modifier.width(180.dp)) {
                SceneInteractiveThumbnail(
                    scene = MockData.scene,
                    onClick = { _ -> },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    isPlayingPreview = false,
                    onPlayingPreviewChange = {},
                    isScrubbing = false,
                    onScrubbingChange = {},
                    hasScrubbed = false,
                    onHasScrubbedChange = {},
                    scrubProgress = 0f,
                    onScrubProgressChange = {}
                )
            }
        }
    }
}

/**
 * Preview for the interactive thumbnail in a compact context (small width).
 */
@Preview(
    name = "Video Thumbnail (Compact)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SceneInteractiveThumbnailCompactPreview() {
    StashTheme(darkTheme = true) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(modifier = Modifier.width(120.dp)) {
                SceneInteractiveThumbnail(
                    scene = MockData.scene,
                    onClick = { _ -> },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    isPlayingPreview = false,
                    onPlayingPreviewChange = {},
                    isScrubbing = false,
                    onScrubbingChange = {},
                    hasScrubbed = false,
                    onHasScrubbedChange = {},
                    scrubProgress = 0f,
                    onScrubProgressChange = {}
                )
            }
        }
    }
}
