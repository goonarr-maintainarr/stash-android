package goonarr.stash.features.studios.scrape.components

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
import goonarr.stash.core.model.scraper.ScrapedStudio
import goonarr.stash.features.components.bounceClickable

@Composable
fun StudioResultCard(
    studio: ScrapedStudio,
    onClick: () -> Unit
) {
    val studioColors = StashTheme.colors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClickable(
                minElevation = 0.dp,
                maxElevation = 0.dp,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = studioColors.primary.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Studio Image
            if (!studio.image.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(studio.image)
                        .crossfade(true)
                        .build(),
                    contentDescription = studio.name,
                    alignment = Alignment.Center,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(StashTokens.Radius.Medium))
                        .background(
                            Color.White.copy(alpha = 0.1f)
                        )
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(StashTokens.Radius.Medium))
                        .background(
                            Color.Gray.copy(alpha = 0.3f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = studio.name.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleLarge,
                        color = studioColors.complementary
                    )
                }
            }

            // Studio Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = studio.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (!studio.urls.isNullOrEmpty()) {
                    Text(
                        text = studio.urls.first(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                if (studio.parent != null) {
                    Text(
                        text = "Parent: ${studio.parent!!.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
