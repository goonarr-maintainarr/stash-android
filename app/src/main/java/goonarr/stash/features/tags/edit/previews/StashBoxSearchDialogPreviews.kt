package goonarr.stash.features.tags.edit.previews

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import goonarr.stash.StashTheme
import goonarr.stash.core.network.StashBoxTag
import goonarr.stash.features.tags.edit.StashBoxSearchDialog

@Preview(
    name = "StashBox Search Dialog - Results",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun StashBoxSearchDialogResultsPreview() {
    StashTheme(darkTheme = true) {
        StashBoxSearchDialog(
            onDismiss = {},
            searchText = "Anal",
            onSearchTextChange = {},
            searchResults = listOf(
                StashBoxTag(
                    id = "1",
                    name = "Anal",
                    description = "Anal sex scenes",
                    aliases = listOf("anal sex", "butt stuff"),
                    category = null
                ),
                StashBoxTag(
                    id = "2",
                    name = "Anal Creampie",
                    description = "Creampie in anal sex",
                    aliases = listOf("anal cream pie"),
                    category = null
                )
            ),
            onTagSelected = {},
            isSearching = false
        )
    }
}

@Preview(
    name = "StashBox Search Dialog - Searching",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun StashBoxSearchDialogSearchingPreview() {
    StashTheme(darkTheme = true) {
        StashBoxSearchDialog(
            onDismiss = {},
            searchText = "Anal",
            onSearchTextChange = {},
            searchResults = emptyList(),
            onTagSelected = {},
            isSearching = true
        )
    }
}

@Preview(
    name = "StashBox Search Dialog - Empty",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun StashBoxSearchDialogEmptyPreview() {
    StashTheme(darkTheme = true) {
        StashBoxSearchDialog(
            onDismiss = {},
            searchText = "Nonexistent",
            onSearchTextChange = {},
            searchResults = emptyList(),
            onTagSelected = {},
            isSearching = false
        )
    }
}
