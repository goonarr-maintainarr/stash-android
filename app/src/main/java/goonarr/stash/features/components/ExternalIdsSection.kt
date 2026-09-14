package goonarr.stash.features.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import goonarr.stash.StashTheme
import goonarr.stash.StashTokens
import goonarr.stash.core.model.StashID
import goonarr.stash.features.scenes.detail.components.fileinfo.InfoRow
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType
import goonarr.stash.util.StringFormatters

private const val DividerAlpha = 0.12f

@Composable
fun ExternalIdsSection(
    stashIds: List<StashID>,
    modifier: Modifier = Modifier,
    containerColor: Color? = null
) {
    if (stashIds.isEmpty()) return

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "EXTERNAL IDS",
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
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${stashIds.size}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        val finalCardColor = containerColor ?: StashTheme.colors.primary.copy(alpha = StashTokens.Alpha.CardBackground)
        val borderColor = if (containerColor != null) null else StashTheme.colors.primary.copy(alpha = StashTokens.Alpha.CardBorder)

        // If DetailCard needs non-null, we must provide one. Checking file next will confirm.
        // Assuming borderColor might need to be Color, but let's see.
        DetailCard(containerColor = finalCardColor, borderColor = borderColor ?: Color.Transparent) {
            val uriHandler = LocalUriHandler.current
            val haptic = LocalStashHapticFeedback.current

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                stashIds.forEachIndexed { index, id ->
                    if (index > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = DividerAlpha))
                    }

                    val externalUrl = id.getUrl()
                    InfoRow(
                        label = StringFormatters.formatEndpoint(id.endpoint),
                        value = id.stashId,
                        onClick = externalUrl?.let { url ->
                            {
                                haptic.perform(StashHapticFeedbackType.Light)
                                uriHandler.openUri(url)
                            }
                        }
                    )
                }
            }
        }
    }
}
