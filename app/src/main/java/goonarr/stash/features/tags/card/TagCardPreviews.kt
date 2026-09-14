package goonarr.stash.features.tags.card

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import goonarr.stash.StashTheme
import goonarr.stash.util.MockData

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun TagRowPreview() {
    StashTheme {
        TagRow(
            tag = MockData.tag,
            onClick = {}
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun FavoriteTagRowPreview() {
    StashTheme {
        TagRow(
            tag = MockData.tag.copy(favorite = true),
            onClick = {}
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun FollowedTagRowPreview() {
    StashTheme {
        TagRow(
            tag = MockData.tag,
            onClick = {},
            onUnfollow = {}
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun TagCardShimmerPreview() {
    StashTheme {
        TagCardShimmer()
    }
}
