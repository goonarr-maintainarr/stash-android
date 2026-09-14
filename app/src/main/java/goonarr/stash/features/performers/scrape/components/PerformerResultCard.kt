package goonarr.stash.features.performers.scrape.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import goonarr.stash.StashTheme
import goonarr.stash.StashTokens
import goonarr.stash.core.model.scraper.ScrapedPerformer
import goonarr.stash.features.components.bounceClickable

@Composable
fun PerformerResultCard(
    performer: ScrapedPerformer,
    onClick: () -> Unit
) {
    val performerColors = StashTheme.colors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClickable(
                minElevation = 0.dp,
                maxElevation = 0.dp,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = performerColors.primary.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Performer Image
            if (!performer.images.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(performer.images.first())
                        .crossfade(true)
                        .build(),
                    contentDescription = performer.name,
                    alignment = Alignment.TopCenter,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(128.dp)
                        .clip(RoundedCornerShape(StashTokens.Radius.Medium))
                        .background(
                            Color.Gray.copy(alpha = 0.3f)
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(StashTokens.Radius.Medium))
                        .background(
                            Color.Gray.copy(alpha = 0.3f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = performer.name.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleLarge,
                        color = performerColors.complementary
                    )
                }
            }

            // Performer Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = performer.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!performer.disambiguation.isNullOrBlank()) {
                    Text(
                        text = performer.disambiguation,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (!performer.country.isNullOrBlank()) {
                    Text(
                        text = performer.country,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
