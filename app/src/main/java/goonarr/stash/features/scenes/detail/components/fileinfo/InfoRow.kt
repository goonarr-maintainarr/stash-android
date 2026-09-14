package goonarr.stash.features.scenes.detail.components.fileinfo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import goonarr.stash.StashBlue

@Composable
fun InfoRow(
    label: String,
    value: String,
    isLink: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            overflow = TextOverflow.Ellipsis,
            color = if (isLink || onClick != null) StashBlue else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .padding(start = 16.dp)
                .then(
                    if (onClick != null) {
                        Modifier.clickable { onClick() }
                    } else if (isLink) {
                        Modifier.clickable { /* TODO: Open URL */ }
                    } else {
                        Modifier
                    }
                )
        )
    }
}
