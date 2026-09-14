package goonarr.stash.features.scenes.detail.components.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import goonarr.stash.StashTheme
import goonarr.stash.StashTokens
import goonarr.stash.features.components.DetailCard
import goonarr.stash.util.DateFormatters

private val DividerAlpha = StashTokens.Alpha.Divider

@Composable
fun HistoryList(
    title: String,
    dates: List<String>,
    totalDuration: Double? = null,
    iconContent: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelLarge,
                    color = StashTheme.colors.primary
                )
                iconContent()
                Text(
                    text = "${dates.size}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Show total watch duration if available
                if (totalDuration != null) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelLarge,
                        color = StashTheme.colors.primary
                    )
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = DateFormatters.formatDuration(totalDuration),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        val cardColor = StashTheme.colors.primary.copy(alpha = 0.15f)
        val borderColor = StashTheme.colors.primary.copy(alpha = 0.5f)

        DetailCard(containerColor = cardColor, borderColor = borderColor) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dates.sortedDescending().forEachIndexed { index, dateString ->
                    if (index > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = DividerAlpha))
                    }
                    Text(
                        text = DateFormatters.formatTimestamp(dateString),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
