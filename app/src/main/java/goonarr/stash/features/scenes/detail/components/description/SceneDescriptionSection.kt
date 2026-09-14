package goonarr.stash.features.scenes.detail.components.description

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import goonarr.stash.StashBlue
import goonarr.stash.StashTheme
import goonarr.stash.StashTokens
import goonarr.stash.features.components.DetailCard
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType

@Composable
fun SceneDescriptionSection(description: String?) {
    if (description.isNullOrBlank()) return

    var isExpanded by remember { mutableStateOf(false) }
    var isTruncated by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.padding(horizontal = StashTokens.Spacing.ContentHorizontal),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "DESCRIPTION",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )

        val cardColor = StashTheme.colors.primary.copy(alpha = 0.15f)
        val borderColor = StashTheme.colors.primary.copy(alpha = 0.5f)
        val haptic = LocalStashHapticFeedback.current

        DetailCard(containerColor = cardColor, borderColor = borderColor) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .then(
                        if (isTruncated) {
                            Modifier.clickable {
                                haptic.perform(StashHapticFeedbackType.Light)
                                isExpanded = !isExpanded
                            }
                        } else {
                            Modifier
                        }
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { textLayoutResult ->
                        if (!isExpanded) {
                            isTruncated = textLayoutResult.hasVisualOverflow
                        }
                    }
                )

                if (!isExpanded && isTruncated) {
                    Text(
                        text = "Read more",
                        style = MaterialTheme.typography.bodySmall,
                        color = StashBlue,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}
