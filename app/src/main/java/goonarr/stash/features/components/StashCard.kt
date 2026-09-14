package goonarr.stash.features.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import goonarr.stash.StashTheme
import goonarr.stash.StashTokens

/**
 * A reusable card component styled with the Stash dynamic theme colors.
 * Uses the hero color for background tint and border.
 */
@Composable
fun StashCard(
    modifier: Modifier = Modifier,
    containerColor: Color = StashTheme.colors.cardBackground,
    borderColor: Color? = StashTheme.colors.cardBorder,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(StashTokens.Radius.Card),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        border = borderColor?.let { BorderStroke(1.dp, it) }
    ) {
        content()
    }
}
