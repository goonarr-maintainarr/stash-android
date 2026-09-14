package goonarr.stash.features.scenes.detail.components.shimmer

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import goonarr.stash.StashTheme
import goonarr.stash.features.components.rememberShimmerBrush

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
fun ShimmerCardPreview() {
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ShimmerCard(brush = rememberShimmerBrush(), title = "SAMPLE", rows = 3)
        }
    }
}
