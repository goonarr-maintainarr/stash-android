package goonarr.stash.features.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import goonarr.stash.R
import goonarr.stash.StashBlue
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType

@Composable
fun OCounterButton(
    modifier: Modifier = Modifier,
    count: Int,
    onClick: () -> Unit,
) {
    val haptic = LocalStashHapticFeedback.current
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                haptic.perform(StashHapticFeedbackType.Medium)
                onClick()
            },
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_sweat_drops),
                contentDescription = "O Counter",
                tint = StashBlue,
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = "$count",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
