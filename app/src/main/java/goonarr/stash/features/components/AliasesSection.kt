package goonarr.stash.features.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import goonarr.stash.StashTheme
import goonarr.stash.StashTokens

@Composable
fun AliasesSection(
    aliases: List<String>?,
    containerColor: Color? = null,
    borderColor: Color? = null
) {
    if (aliases.isNullOrEmpty()) return

    val finalCardColor = containerColor ?: StashTheme.colors.primary.copy(alpha = StashTokens.Alpha.CardBackground)
    val finalBorderColor = borderColor ?: StashTheme.colors.primary.copy(alpha = StashTokens.Alpha.CardBorder)

    DetailCard(containerColor = finalCardColor, borderColor = finalBorderColor) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "ALSO KNOWN AS",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = aliases.joinToString(", "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
