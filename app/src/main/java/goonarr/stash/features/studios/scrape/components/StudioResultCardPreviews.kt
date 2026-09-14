package goonarr.stash.features.studios.scrape.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import goonarr.stash.StashTheme
import goonarr.stash.util.MockData

@Composable
private fun StudioResultCardPreviewContainer(
    content: @Composable () -> Unit
) {
    StashTheme(darkTheme = true) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

@Preview(name = "Basic Studio", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StudioResultCardBasicPreview() {
    StudioResultCardPreviewContainer {
        StudioResultCard(
            studio = MockData.mockScrapedStudios.first(),
            onClick = {}
        )
    }
}

@Preview(name = "No Image Studio", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StudioResultCardNoImagePreview() {
    StudioResultCardPreviewContainer {
        StudioResultCard(
            studio = MockData.mockScrapedStudios[1],
            onClick = {}
        )
    }
}

@Preview(name = "List View", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StudioResultCardListPreview() {
    StudioResultCardPreviewContainer {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            MockData.mockScrapedStudios.forEach { studio ->
                StudioResultCard(
                    studio = studio,
                    onClick = {}
                )
            }
        }
    }
}
