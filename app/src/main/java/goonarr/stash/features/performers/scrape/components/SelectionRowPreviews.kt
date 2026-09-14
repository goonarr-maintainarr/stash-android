package goonarr.stash.features.performers.scrape.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import goonarr.stash.StashTheme

@Composable
private fun SelectionRowPreviewContainer(
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

@Preview(name = "Selection Row - Selected", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SelectionRowSelectedPreview() {
    SelectionRowPreviewContainer {
        SelectionRow(
            label = "Name",
            currentValue = "Old Name",
            scrapedValue = "New Scraped Name",
            isSelected = true,
            onSelectionChanged = {}
        )
    }
}

@Preview(name = "Selection Row - Unselected", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SelectionRowUnselectedPreview() {
    SelectionRowPreviewContainer {
        SelectionRow(
            label = "Birth Date",
            currentValue = "1990-01-01",
            scrapedValue = "1992-05-15",
            isSelected = false,
            onSelectionChanged = {}
        )
    }
}

@Preview(name = "Selection Row - List", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SelectionRowListPreview() {
    SelectionRowPreviewContainer {
        Column {
            SelectionRow(
                label = "Name",
                currentValue = "Jane Doe",
                scrapedValue = "Jane D. Smith",
                isSelected = true,
                onSelectionChanged = {}
            )
            SelectionRow(
                label = "Country",
                currentValue = null,
                scrapedValue = "USA",
                isSelected = false,
                onSelectionChanged = {}
            )
        }
    }
}
