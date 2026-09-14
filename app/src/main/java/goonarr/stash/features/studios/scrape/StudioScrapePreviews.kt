package goonarr.stash.features.studios.scrape

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
private fun StudioScrapePreviewContainer(
    heroColor: Color? = null,
    content: @Composable () -> Unit
) {
    val studioColors = rememberSceneColors(heroColor)
    StashTheme(darkTheme = true) {
        CompositionLocalProvider(LocalStashDynamicColors provides studioColors) {
            Surface(color = MaterialTheme.colorScheme.background) {
                content()
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StudioScrapeScreenPreview() {
    val uiState = StudioScrapeUiState(
        stashBoxes = MockData.mockStashBoxes,
        selectedStashBox = MockData.mockStashBoxes.first(),
        currentStudio = MockData.studio,
        query = "Nebula Productions"
    )

    StudioScrapePreviewContainer {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
            )
            StudioScrapeContent(
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
fun StudioScrapeResultsPreview() {
    val uiState = StudioScrapeUiState(
        scrapeResults = MockData.mockScrapedStudios,
        selectedStashBox = MockData.mockStashBoxes.first(),
        currentStudio = MockData.studio,
        query = "Nebula Productions"
    )

    StudioScrapePreviewContainer {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
            )
            StudioScrapeContent(
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

@Preview(showBackground = true, heightDp = 1000, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StudioScrapeResultReviewScreenPreview() {
    val scrapedStudio = MockData.mockScrapedStudios.first()
    val currentStudio = MockData.fullStudio
    val selection = StudioScrapeSelection(
        useName = true,
        useDetails = true,
        useImage = true,
        useTags = true
    )

    StudioScrapePreviewContainer {
        StudioScrapeResultReviewScreen(
            scrapedStudio = scrapedStudio,
            currentStudio = currentStudio,
            selection = selection,
            onSelectionChanged = {},
            isLoading = false,
            onApply = {},
            onBack = {},
            scraperName = "StashDB"
        )
    }
}
