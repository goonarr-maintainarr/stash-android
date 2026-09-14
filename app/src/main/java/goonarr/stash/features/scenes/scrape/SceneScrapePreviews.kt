package goonarr.stash.features.scenes.scrape

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import goonarr.stash.StashTheme
import goonarr.stash.core.model.scraper.ParseMode
import goonarr.stash.core.model.scraper.TaggerConfig
import goonarr.stash.util.MockData

@Composable
private fun ScrapeScenePreviewContainer(content: @Composable () -> Unit) {
    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TaggerConfigDialogPreview() {
    ScrapeScenePreviewContainer {
        TaggerConfigDialog(
            config = TaggerConfig(
                mode = ParseMode.FILENAME
            ),
            onConfigChanged = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SceneScrapeScreenPreview() {
    val uiState = SceneScrapeUiState(
        stashBoxes = MockData.mockStashBoxes,
        selectedStashBox = MockData.mockStashBoxes.first(),
        currentScene = MockData.scene,
        query = "Mock Scene Title"
    )

    ScrapeScenePreviewContainer {
        SceneScrapeContent(
            uiState = uiState,
            onBackClick = {},
            onSettingsClick = {},
            onStashBoxSelect = {},
            onQueryChange = {},
            onScrape = {},
            onScrapeByFragment = {},
            onResultClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SceneScrapeScrapingPreview() {
    val uiState = SceneScrapeUiState(
        isScraping = true,
        selectedStashBox = MockData.mockStashBoxes.first(),
        currentScene = MockData.scene
    )

    ScrapeScenePreviewContainer {
        SceneScrapeContent(
            uiState = uiState,
            onBackClick = {},
            onSettingsClick = {},
            onStashBoxSelect = {},
            onQueryChange = {},
            onScrape = {},
            onScrapeByFragment = {},
            onResultClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SceneScrapeResultsPreview() {
    val uiState = SceneScrapeUiState(
        scrapedResults = MockData.mockScrapedResults,
        selectedStashBox = MockData.mockStashBoxes.first(),
        currentScene = MockData.scene
    )

    ScrapeScenePreviewContainer {
        SceneScrapeContent(
            uiState = uiState,
            onBackClick = {},
            onSettingsClick = {},
            onStashBoxSelect = {},
            onQueryChange = {},
            onScrape = {},
            onScrapeByFragment = {},
            onResultClick = {}
        )
    }
}
