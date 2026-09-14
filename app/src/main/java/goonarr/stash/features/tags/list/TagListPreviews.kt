package goonarr.stash.features.tags.list

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import goonarr.stash.StashTheme
import goonarr.stash.util.MockData

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    heightDp = 1500
)
@Composable
fun TagListContentPreview() {
    StashTheme {
        TagListContent(
            tags = MockData.tags,
            followedTags = MockData.tags.take(3),
            favoriteTags = MockData.tags.filter { it.favorite == true },
            followedTagIds = MockData.tags.take(3).map { it.id }.toSet(),
            onTagClick = {},
            onToggleFollow = {}
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    heightDp = 1500
)
@Composable
fun TagListContentSearchPreview() {
    StashTheme {
        TagListContent(
            tags = MockData.tags.take(5),
            followedTags = MockData.tags.take(3),
            favoriteTags = emptyList(),
            followedTagIds = MockData.tags.take(3).map { it.id }.toSet(),
            onTagClick = {},
            onToggleFollow = {},
            isSearchActive = true
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    heightDp = 2000
)
@Composable
fun TagListContentLoadingPreview() {
    StashTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
