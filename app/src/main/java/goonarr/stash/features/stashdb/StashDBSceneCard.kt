package goonarr.stash.features.stashdb

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import goonarr.stash.StashBlue
import goonarr.stash.core.model.StashDBScene
import goonarr.stash.features.components.bounceClickable
import goonarr.stash.util.DateFormatters

/**
 * Card component for displaying StashDB scenes.
 * Displays scene image, title, studio, date, and performers.
 */
@Composable
fun StashDBSceneCard(
    scene: StashDBScene,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .bounceClickable(
                shape = RoundedCornerShape(8.dp),
                onClick = onClick
            ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Scene thumbnail
            val imageUrl = scene.images?.firstOrNull()?.url
            if (imageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = scene.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                )
            } else {
                // Placeholder for missing image
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                )
            }

            Column(modifier = Modifier.padding(12.dp)) {
                // Title
                Text(
                    text = scene.title ?: "No Title",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Studio and date row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val studioName = scene.studio?.name ?: "Unknown Studio"
                    Text(
                        text = studioName,
                        style = MaterialTheme.typography.bodySmall,
                        color = StashBlue
                    )

                    scene.releaseDate?.let { date ->
                        val formattedDate = DateFormatters.formatDateString(date)
                        if (formattedDate.isNotEmpty()) {
                            Text(
                                text = " • ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Single-line description
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = scene.details?.take(100) ?: " ",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Performers row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f)) {
                        val performers = scene.performers?.take(3) ?: emptyList()
                        if (performers.isEmpty()) {
                            Text(
                                text = " ",
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1
                            )
                        } else {
                            performers.forEachIndexed { index, performer ->
                                Text(
                                    text = performer.performer?.name ?: "Unknown",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = StashBlue,
                                    maxLines = 1
                                )
                                if (index < performers.size - 1) {
                                    Text(
                                        text = ", ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            val allPerformers = scene.performers ?: emptyList()
                            if (allPerformers.size > 3) {
                                Text(
                                    text = " +${allPerformers.size - 3}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // External link indicator
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "StashDB Link",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
