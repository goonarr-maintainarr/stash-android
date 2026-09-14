package goonarr.stash.features.settings.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import goonarr.stash.StashTheme
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType

/**
 * A toggle row with title and description, matching iOS DescriptionToggleRow.
 *
 * @param title The primary title of the row.
 * @param description A secondary label providing more context.
 * @param isOn Whether the toggle is currently in the 'on' state.
 * @param onToggle Callback triggered when the toggle state is changed.
 * @param modifier Optional [Modifier] for the row.
 */
@Composable
fun DescriptionToggleRow(
    title: String,
    description: String,
    isOn: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalStashHapticFeedback.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                haptic.perform(StashHapticFeedbackType.Light)
                onToggle(!isOn)
            }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(end = 16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = isOn,
            onCheckedChange = {
                haptic.perform(StashHapticFeedbackType.Light)
                onToggle(it)
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DescriptionToggleRowPreview_On() {
    StashTheme {
        DescriptionToggleRow(
            title = "Generate Covers",
            description = "Generate cover images (screenshots) for scenes",
            isOn = true,
            onToggle = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DescriptionToggleRowPreview_Off() {
    StashTheme {
        DescriptionToggleRow(
            title = "Generate Previews",
            description = "Generate preview videos (short clips from the scene)",
            isOn = false,
            onToggle = {}
        )
    }
}
