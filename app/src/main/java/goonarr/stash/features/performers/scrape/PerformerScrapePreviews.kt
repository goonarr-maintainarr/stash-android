package goonarr.stash.features.performers.scrape

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import goonarr.stash.LocalStashDynamicColors
import goonarr.stash.StashTheme
import goonarr.stash.rememberSceneColors
import goonarr.stash.util.MockData

@Composable
private fun PerformerScrapePreviewContainer(
    heroColor: Color? = null,
    content: @Composable () -> Unit
) {
    val performerColors = rememberSceneColors(heroColor)
    StashTheme(darkTheme = true) {
        CompositionLocalProvider(LocalStashDynamicColors provides performerColors) {
            Surface(color = MaterialTheme.colorScheme.background) {
                content()
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PerformerScrapeScreenPreview() {
    val uiState = PerformerScrapeUiState(
        stashBoxes = MockData.mockStashBoxes,
        selectedStashBox = MockData.mockStashBoxes.first(),
        currentPerformer = MockData.performer,
        query = "Mock Performer"
    )

    PerformerScrapePreviewContainer {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
            )
            PerformerScrapeContent(
                uiState = uiState,
                lazyListState = rememberLazyListState(),
                onBackClick = {},
                onStashBoxSelect = {},
                onQueryChange = {},
                onScrape = {},
                onResultClick = {}
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PerformerScrapeResultsPreview() {
    val uiState = PerformerScrapeUiState(
        scrapeResults = MockData.mockScrapedPerformers,
        selectedStashBox = MockData.mockStashBoxes.first(),
        currentPerformer = MockData.performer
    )

    PerformerScrapePreviewContainer {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
            )
            PerformerScrapeContent(
                uiState = uiState,
                lazyListState = rememberLazyListState(),
                onBackClick = {},
                onStashBoxSelect = {},
                onQueryChange = {},
                onScrape = {},
                onResultClick = {}
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 1500, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PerformerScrapeResultReviewScreenPreview() {
    val scrapedPerformer = MockData.mockScrapedPerformers.first()
    val currentPerformer = MockData.fullPerformer
    val selection = PerformerScrapeSelection(
        useName = true,
        useDisambiguation = true,
        useImage = true,
        selectedImageIndex = 0
    )

    PerformerScrapePreviewContainer {
        PerformerScrapeResultReviewScreen(
            scrapedPerformer = scrapedPerformer,
            currentPerformer = currentPerformer,
            selection = selection,
            onSelectionChanged = {},
            isLoading = false,
            onApply = {},
            onBack = {}
        )
    }
}
