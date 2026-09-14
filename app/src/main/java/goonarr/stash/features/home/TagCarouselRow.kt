package goonarr.stash.features.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import goonarr.stash.StashBlue
import goonarr.stash.StashTheme
import goonarr.stash.StashTokens
import goonarr.stash.core.model.Tag
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.MockData
import goonarr.stash.util.StashHapticFeedbackType

@Composable
fun TagCarouselRow(
    modifier: Modifier = Modifier,
    title: String,
    count: Int? = null,
    tags: List<Tag>,
    onTagClick: (String) -> Unit,
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
            items(tags, key = { it.id }) { tag ->
                TagCardCompact(
                    tag = tag,
                    onClick = { onTagClick(tag.id) },
                    modifier = Modifier.width(200.dp)
                )
            }
        }
    }
}

/**
 * Compact tag card for carousel display.
 */
@Composable
internal fun TagCardCompact(
    tag: Tag,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(StashTokens.Radius.Card))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        if (!tag.imagePath.isNullOrEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(tag.imagePath)
                    .crossfade(true)
                    .build(),
                contentDescription = tag.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Overlay with tag name
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    RoundedCornerShape(bottomStart = StashTokens.Radius.Card, bottomEnd = StashTokens.Radius.Card)
                )
                .padding(8.dp)
        ) {
            Text(
                text = tag.name,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TagCarouselRowPreview() {
    StashTheme {
        TagCarouselRow(
            title = "Popular Tags",
            count = 128,
            tags = MockData.tags.take(5),
            onTagClick = {},
            onSeeAllClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TagCarouselRowNoCountPreview() {
    StashTheme {
        TagCarouselRow(
            title = "Featured Tags",
            count = null,
            tags = MockData.tags.take(3),
            onTagClick = {},
            onSeeAllClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TagCardCompactPreview() {
    StashTheme {
        TagCardCompact(
            tag = MockData.tags.first(),
            onClick = {},
            modifier = Modifier.width(200.dp)
        )
    }
}
