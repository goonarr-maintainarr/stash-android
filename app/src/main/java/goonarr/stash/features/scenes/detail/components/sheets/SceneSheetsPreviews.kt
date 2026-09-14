package goonarr.stash.features.scenes.detail.components.sheets

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import goonarr.stash.LocalStashDynamicColors
import goonarr.stash.StashBlue
import goonarr.stash.StashTheme
import goonarr.stash.rememberSceneColors
import goonarr.stash.util.MockData

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
