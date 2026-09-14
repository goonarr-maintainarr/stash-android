package goonarr.stash.features.studios.list

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import goonarr.stash.LocalStashDynamicColors
import goonarr.stash.StashBlue
import goonarr.stash.StashTheme
import goonarr.stash.rememberSceneColors
import goonarr.stash.util.MockData

@Preview(name = "Studio Card - Light", showBackground = true)
@Preview(name = "Studio Card - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StudioCardPreview() {
    StashTheme(darkTheme = true) {
        val studioColors = rememberSceneColors(StashBlue)
        CompositionLocalProvider(LocalStashDynamicColors provides studioColors) {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.padding(16.dp)
            ) {
                StudioCard(
                    studio = MockData.studio,
                    imageUrl = MockData.studio.imagePath,
                    onClick = {}
                )
            }
        }
    }
}

@Preview(name = "Studio Card - No Image", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StudioCardNoImagePreview() {
    StashTheme(darkTheme = true) {
        val studioColors = rememberSceneColors(StashBlue)
        CompositionLocalProvider(LocalStashDynamicColors provides studioColors) {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.padding(16.dp)
            ) {
                StudioCard(
                    studio = MockData.studio.copy(imagePath = null),
                    imageUrl = null,
                    onClick = {}
                )
            }
        }
    }
}

@Preview(name = "Studio Card - Favorite", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StudioCardFavoritePreview() {
    StashTheme(darkTheme = true) {
        val studioColors = rememberSceneColors(StashBlue)
        CompositionLocalProvider(LocalStashDynamicColors provides studioColors) {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.padding(16.dp)
            ) {
                StudioCard(
                    studio = MockData.studio.copy(favorite = true),
                    imageUrl = MockData.studio.imagePath,
                    onClick = {}
                )
            }
        }
    }
}

@Preview(name = "Studio Card - Shimmer", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StudioCardShimmerPreview() {
    StashTheme(darkTheme = true) {
        val studioColors = rememberSceneColors(StashBlue)
        CompositionLocalProvider(LocalStashDynamicColors provides studioColors) {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.padding(16.dp)
            ) {
                StudioCardShimmer()
            }
        }
    }
}
