package goonarr.stash.features.performers.detail

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import goonarr.stash.StashTheme
import goonarr.stash.util.MockData

@Preview(name = "Performers Detail", heightDp = 2000)
@Composable
private fun PerformerDetailContentPreview() {
    StashTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            PerformerDetailContent(
                performer = MockData.fullPerformer.copy(
                    favorite = true,
                    rating100 = 100
                ),
                scenes = MockData.scenes,
                onTagClick = {}
            )
        }
    }
}

@Preview(name = "Performers Detail Dark", uiMode = Configuration.UI_MODE_NIGHT_YES, heightDp = 2000)
@Composable
private fun PerformerDetailContentDarkPreview() {
    StashTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            PerformerDetailContent(
                performer = MockData.fullPerformer.copy(
                    favorite = false,
                    rating100 = 80
                ),
                scenes = MockData.scenes,
                onTagClick = {}
            )
        }
    }
}

@Preview(name = "Performers Detail Loading", heightDp = 2000)
@Composable
private fun PerformerDetailLoadingPreview() {
    StashTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            PerformerDetailShimmer()
        }
    }
}

@Preview(name = "Delete Performer Sheet")
@Composable
private fun DeletePerformerSheetPreview() {
    StashTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface
        ) {
            DeletePerformerSheetContent(
                onDelete = {},
                onCancel = {}
            )
        }
    }
}

@Preview(name = "Delete Performer Sheet Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DeletePerformerSheetDarkPreview() {
    StashTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface
        ) {
            DeletePerformerSheetContent(
                onDelete = {},
                onCancel = {}
            )
        }
    }
}
