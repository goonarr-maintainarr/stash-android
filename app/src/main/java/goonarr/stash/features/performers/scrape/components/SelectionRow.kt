package goonarr.stash.features.performers.scrape.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import goonarr.stash.StashTheme
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType

@Composable
fun SelectionRow(
    label: String,
    currentValue: String?,
    scrapedValue: String?,
    isSelected: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    val colors = StashTheme.colors
    val haptic = LocalStashHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.perform(StashHapticFeedbackType.Light)
                onSelectionChanged(!isSelected)
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = {
                haptic.perform(StashHapticFeedbackType.Light)
                onSelectionChanged(it)
            },
            colors = CheckboxDefaults.colors(
                checkedColor = colors.primary,
                uncheckedColor = colors.complementary
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = colors.complementary
            )

            if (currentValue != null) {
                Text(
                    text = "Current: $currentValue",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (scrapedValue != null) {
                Text(
                    text = "Scraped: $scrapedValue",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) colors.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }

    HorizontalDivider(
        color = colors.complementary.copy(alpha = 0.1f),
        thickness = 0.5.dp
    )
}
