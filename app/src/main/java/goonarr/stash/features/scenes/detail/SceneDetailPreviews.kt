package goonarr.stash.features.scenes.detail

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import goonarr.stash.LocalStashDynamicColors
import goonarr.stash.StashBlue
import goonarr.stash.StashTheme
import goonarr.stash.features.components.ExternalIdsSection
import goonarr.stash.features.components.TagsSection
import goonarr.stash.features.scenes.detail.components.description.SceneDescriptionSection
import goonarr.stash.features.scenes.detail.components.fileinfo.FileInfoSection
import goonarr.stash.features.scenes.detail.components.header.SceneHeader
import goonarr.stash.features.scenes.detail.components.history.SceneHistorySection
import goonarr.stash.features.scenes.detail.components.markers.SceneMarkersSection
import goonarr.stash.features.scenes.detail.components.metadata.SceneAdditionalMetadataSection
import goonarr.stash.features.scenes.detail.components.performers.PerformersSection
import goonarr.stash.features.scenes.detail.components.sheets.DeleteSceneSheetContent
import goonarr.stash.features.scenes.detail.components.sheets.GenerateScreenshotSheetContent
import goonarr.stash.features.scenes.detail.components.shimmer.SceneDetailShimmer
import goonarr.stash.rememberSceneColors
import goonarr.stash.util.MockData

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SceneHeaderPreview() {
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) {
                SceneHeader(
                    scene = MockData.scene,
                    onRateScene = {},
                    onIncrementOCounter = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TagsSectionPreview() {
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) {
                TagsSection(tags = MockData.scene.tags ?: emptyList(), onTagClick = {})
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SceneMarkersSectionPreview() {
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) {
                SceneMarkersSection(
                    markers = MockData.scene.sceneMarkers ?: emptyList(),
                    onSeek = {},
                    getStreamUrl = { null }
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PerformersSectionPreview() {
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) {
                PerformersSection(
                    performers = MockData.performers,
                    onPerformerClick = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FileInfoSectionPreview() {
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) {
                MockData.scene.files?.firstOrNull()?.let {
                    FileInfoSection(it)
                }
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SceneHistorySectionPreview() {
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) {
                SceneHistorySection(MockData.scene)
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SceneStashIDsSectionPreview() {
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) {
                ExternalIdsSection(stashIds = MockData.scene.stashIds ?: emptyList())
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SceneAdditionalMetadataSectionPreview() {
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Box(modifier = Modifier.padding(16.dp)) {
                SceneAdditionalMetadataSection(MockData.scene)
            }
        }
    }
}

// SceneDetailContentPreview is skipped because it requires GlobalPlayerState which cannot be mocked.

@Preview(showBackground = true, heightDp = 2000, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SceneDetailShimmerPreview() {
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            SceneDetailShimmer()
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SceneDescriptionSectionPreview() {
    StashTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("Short Description (No Truncation)", style = MaterialTheme.typography.titleMedium)
            SceneDescriptionSection(description = "This is a short description.")

            Text("Long Description (Truncated)", style = MaterialTheme.typography.titleMedium)
            SceneDescriptionSection(
                description = "This is a much longer description that should definitely exceed three " +
                    "lines of text in the preview so we can see the 'Read more' button and test the truncation logic correctly. " +
                    "Let's add even more text just to be sure it overflows. Specifically, we want to see if the button appears " +
                    "and if clicking it works as expected."
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DeleteSceneSheetContentLightPreview() {
    StashTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            DeleteSceneSheetContent(
                deleteFile = true,
                onDeleteFileChange = {},
                deleteGenerated = true,
                onDeleteGeneratedChange = {},
                onDeleteConfirm = {},
                onCancel = {}
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DeleteSceneSheetContentDarkPreview() {
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            DeleteSceneSheetContent(
                deleteFile = true,
                onDeleteFileChange = {},
                deleteGenerated = true,
                onDeleteGeneratedChange = {},
                onDeleteConfirm = {},
                onCancel = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GenerateScreenshotSheetContentLightPreview() {
    StashTheme(darkTheme = false) {
        val sceneColors = rememberSceneColors(StashBlue)
        CompositionLocalProvider(LocalStashDynamicColors provides sceneColors) {
            Surface(color = MaterialTheme.colorScheme.surface) {
                GenerateScreenshotSheetContent(
                    previewBitmap = MockData.placeholderBitmap,
                    onGenerate = {},
                    onCancel = {}
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun GenerateScreenshotSheetContentDarkPreview() {
    StashTheme(darkTheme = true) {
        val sceneColors = rememberSceneColors(StashBlue)
        CompositionLocalProvider(LocalStashDynamicColors provides sceneColors) {
            Surface(color = MaterialTheme.colorScheme.surface) {
                GenerateScreenshotSheetContent(
                    previewBitmap = MockData.placeholderBitmap,
                    onGenerate = {},
                    onCancel = {}
                )
            }
        }
    }
}
