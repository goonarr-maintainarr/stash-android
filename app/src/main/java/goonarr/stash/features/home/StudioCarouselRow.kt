package goonarr.stash.features.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import goonarr.stash.StashBlue
import goonarr.stash.StashTheme
import goonarr.stash.core.model.Studio
import goonarr.stash.features.studios.list.StudioCard
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.MockData
import goonarr.stash.util.StashHapticFeedbackType

@Composable
fun StudioCarouselRow(
    modifier: Modifier = Modifier,
    title: String,
    count: Int? = null,
    studios: List<Studio>,
    onStudioClick: (String) -> Unit,
    onSeeAllClick: () -> Unit
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (count != null) {
                    Text(
                        text = " • $count",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }
            }
            val haptic = LocalStashHapticFeedback.current
            TextButton(onClick = {
                haptic.perform(StashHapticFeedbackType.Selection)
                onSeeAllClick()
            }) {
                Text(
                    text = "View All",
                    color = StashBlue
                )
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(studios, key = { it.id }) { studio ->
                StudioCard(
                    studio = studio,
                    imageUrl = studio.imagePath,
                    onClick = { onStudioClick(studio.id) },
                    modifier = Modifier.width(200.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StudioCarouselRowPreview() {
    StashTheme {
        StudioCarouselRow(
            title = "Top Studios",
            count = 42,
            studios = MockData.studios.take(5),
            onStudioClick = {},
            onSeeAllClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun StudioCarouselRowNoCountPreview() {
    StashTheme {
        StudioCarouselRow(
            title = "Featured Studios",
            count = null,
            studios = MockData.studios.take(3),
            onStudioClick = {},
            onSeeAllClick = {}
        )
    }
}
