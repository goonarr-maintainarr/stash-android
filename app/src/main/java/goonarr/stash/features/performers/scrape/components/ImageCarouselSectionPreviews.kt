package goonarr.stash.features.performers.scrape.components

import android.content.res.Configuration
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
private fun ImageCarouselPreviewContainer(
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

@Preview(name = "Not Using Image", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ImageCarouselSectionNotUsedPreview() {
    ImageCarouselPreviewContainer {
        ImageCarouselSection(
            currentImageUrl = "https://example.com/current.jpg",
            scrapedImages = MockData.mockScrapedPerformers.first().images ?: emptyList(),
            selectedImageIndex = 0,
            useImage = false,
            onImageSelected = {},
            onUseImageChanged = {}
        )
    }
}

@Preview(name = "Using Scraped Image", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ImageCarouselSectionUsedPreview() {
    ImageCarouselPreviewContainer {
        ImageCarouselSection(
            currentImageUrl = "https://example.com/current.jpg",
            scrapedImages = MockData.mockScrapedPerformers.first().images ?: emptyList(),
            selectedImageIndex = 1,
            useImage = true,
            onImageSelected = {},
            onUseImageChanged = {}
        )
    }
}

@Preview(name = "No Current Image", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ImageCarouselSectionNoCurrentPreview() {
    ImageCarouselPreviewContainer {
        ImageCarouselSection(
            currentImageUrl = null,
            scrapedImages = MockData.mockScrapedPerformers.first().images ?: emptyList(),
            selectedImageIndex = 0,
            useImage = true,
            onImageSelected = {},
            onUseImageChanged = {}
        )
    }
}

@Preview(name = "Many Images", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ImageCarouselSectionManyPreview() {
    val manyImages = List(10) { "https://example.com/img_$it.jpg" }
    ImageCarouselPreviewContainer {
        ImageCarouselSection(
            currentImageUrl = "https://example.com/current.jpg",
            scrapedImages = manyImages,
            selectedImageIndex = 3,
            useImage = true,
            onImageSelected = {},
            onUseImageChanged = {}
        )
    }
}
