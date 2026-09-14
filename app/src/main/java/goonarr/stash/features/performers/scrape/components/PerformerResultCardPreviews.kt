package goonarr.stash.features.performers.scrape.components

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
import goonarr.stash.core.model.scraper.ScrapedPerformer

@Composable
private fun PerformerResultCardPreviewContainer(
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

@Preview(name = "Basic Performer", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PerformerResultCardBasicPreview() {
    PerformerResultCardPreviewContainer {
        PerformerResultCard(
            performer = ScrapedPerformer(
                name = "Jane Doe",
                images = listOf("https://example.com/jane.jpg")
            ),
            onClick = {}
        )
    }
}

@Preview(name = "With Disambiguation", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PerformerResultCardDisambiguationPreview() {
    PerformerResultCardPreviewContainer {
        PerformerResultCard(
            performer = ScrapedPerformer(
                name = "Jane Doe",
                disambiguation = "The Actress",
                images = listOf("https://example.com/jane.jpg"),
                country = "USA"
            ),
            onClick = {}
        )
    }
}

@Preview(name = "No Image", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PerformerResultCardNoImagePreview() {
    PerformerResultCardPreviewContainer {
        PerformerResultCard(
            performer = ScrapedPerformer(
                name = "Jane Doe",
                images = null
            ),
            onClick = {}
        )
    }
}

@Preview(name = "List View", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PerformerResultCardListPreview() {
    PerformerResultCardPreviewContainer {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            PerformerResultCard(
                performer = ScrapedPerformer(
                    name = "Jane Doe",
                    disambiguation = "The Actress",
                    images = listOf("https://example.com/jane.jpg")
                ),
                onClick = {}
            )
            PerformerResultCard(
                performer = ScrapedPerformer(
                    name = "John Smith",
                    images = null,
                    country = "Canada"
                ),
                onClick = {}
            )
        }
    }
}
