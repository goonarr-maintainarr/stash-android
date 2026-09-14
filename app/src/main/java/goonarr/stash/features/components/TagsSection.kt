package goonarr.stash.features.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import goonarr.stash.StashTheme
import goonarr.stash.core.model.Tag
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun TagsSection(
    modifier: Modifier = Modifier,
    tags: List<Tag>,
    title: String = "TAGS",
    onTagClick: (String) -> Unit,
    horizontalPadding: Dp = 0.dp,
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    if (tags.isEmpty()) return

    val haptic = LocalStashHapticFeedback.current

    val tagColor = StashTheme.colors.tertiary
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = horizontalPadding),
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
                    color = tagColor
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Label,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${tags.size}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        LazyHorizontalStaggeredGrid(
            rows = StaggeredGridCells.Fixed(2),
            modifier = Modifier.height(80.dp),
            contentPadding = PaddingValues(horizontal = horizontalPadding),
            horizontalItemSpacing = 8.dp,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tags, key = { it.id }) { tag ->
                val sharedElementModifier = if (sharedTransitionScope != null && animatedVisibilityScope != null) {
                    with(sharedTransitionScope) {
                        Modifier.sharedElement(
                            sharedContentState = rememberSharedContentState(key = "tag_name_${tag.id}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = { _, _ ->
                                tween(durationMillis = 300)
                            }
                        )
                    }
                } else {
                    Modifier
                }

                SuggestionChip(
                    onClick = {
                        haptic.perform(StashHapticFeedbackType.Medium)
                        onTagClick(tag.id)
                    },
                    label = {
                        Text(
                            text = tag.name,
                            modifier = sharedElementModifier
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = tagColor.copy(alpha = 0.3f),
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(1.dp, tagColor.copy(alpha = 0.5f))
                )
            }
        }
    }
}
