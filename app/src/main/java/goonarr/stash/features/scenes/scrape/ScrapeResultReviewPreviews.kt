package goonarr.stash.features.scenes.scrape

import android.content.res.Configuration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import goonarr.stash.StashTheme
import goonarr.stash.core.model.Scene
import goonarr.stash.core.model.scraper.ScrapedPerformer
import goonarr.stash.core.model.scraper.ScrapedScene
import goonarr.stash.core.model.scraper.ScrapedStudio
import goonarr.stash.core.model.scraper.ScrapedTag
import goonarr.stash.core.model.scraper.TaggerConfig

@Preview(showBackground = true, heightDp = 2000, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ScrapeResultReviewScreenPreview() {
    val scrapedScene = ScrapedScene(
        title = "Scraped Title - Updated From StashDB",
        details = "This is a scraped description that is longer than the original one. " +
            "It includes additional details from the external database.",
        date = "2023-10-27",
        studio = ScrapedStudio(name = "Scraped Studio Name"),
        performers = listOf(
            ScrapedPerformer(name = "Performer One", gender = "Female", images = listOf("https://example.com/p1.jpg")),
            ScrapedPerformer(name = "Performer Two", gender = "Male", images = listOf("https://example.com/p2.jpg"))
        ),
        tags = listOf(ScrapedTag(name = "Tag A"), ScrapedTag(name = "Tag B"), ScrapedTag(name = "Tag C")),
        image = "https://example.com/cover.jpg",
        remoteSiteId = "stashdb-12345",
        director = "Scraped Director",
        code = "SCR-123",
        url = "https://stashdb.org/scenes/12345",
        urls = null
    )

    val currentScene = Scene(
        id = "1",
        title = "Original Title",
        details = "Original details.",
        date = "2023-01-01",
        code = "ORG-001"
    )

    StashTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ScrapeResultReviewScreen(
                scrapedScene = scrapedScene,
                currentScene = currentScene,
                taggerConfig = TaggerConfig(),
                onApply = {},
                onBack = {},
                viewModel = null
            )
        }
    }
}
