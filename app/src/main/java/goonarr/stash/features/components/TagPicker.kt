package goonarr.stash.features.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import goonarr.stash.StashTheme
import goonarr.stash.StashTokens
import goonarr.stash.core.model.Tag
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedbackType

@Composable
fun TagPicker(
    selectedTags: List<Tag>,
    onRemoveTag: (String) -> Unit,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    searchResults: List<Tag>,
    onAddTag: (Tag) -> Unit,
    isSearching: Boolean,
    modifier: Modifier = Modifier,
    placeholder: String = "Add Tag...",
    // Default color, can be overridden
    color: Color = StashTheme.colors.primary
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(StashTokens.Spacing.ContentHorizontal)
    ) {
        // Selected Tags List
        if (selectedTags.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(StashTokens.Spacing.ContentVertical)) {
                selectedTags.forEach { tag ->
                    TagEditItem(
                        tag = tag,
                        onRemove = { onRemoveTag(tag.id) },
                        color = color
                    )
                }
            }
        }

        // Search Results
        if (searchResults.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(StashTokens.Radius.Card),
                color = color.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
            ) {
                Column {
                    searchResults.forEach { tag ->
                        TagSearchItem(
                            tag = tag,
                            onAdd = { onAddTag(tag) },
                            color = color
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(StashTokens.Spacing.ContentVertical))
        }

        // Search Field
        SearchField(
            value = searchText,
            onValueChange = onSearchTextChange,
            placeholder = placeholder,
            isSearching = isSearching,
            color = color
        )
    }
}

@Composable
fun TagSearchItem(
    tag: Tag,
    onAdd: () -> Unit,
    color: Color = StashTheme.colors.tertiary
) {
    val haptic = LocalStashHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.perform(StashHapticFeedbackType.Selection)
                onAdd()
            }
            .background(color.copy(alpha = 0.1f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = tag.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.AddCircle,
            contentDescription = "Add",
            tint = color
        )
    }
}

@Composable
fun TagEditItem(
    tag: Tag,
    onRemove: () -> Unit,
    color: Color = StashTheme.colors.tertiary
) {
    val haptic = LocalStashHapticFeedback.current

    Surface(
        color = color.copy(alpha = 0.3f),
        shape = RoundedCornerShape(StashTokens.Radius.Medium),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Tag,
                contentDescription = null,
                tint = color
            )
            Text(
                text = tag.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    haptic.perform(StashHapticFeedbackType.Medium)
                    onRemove()
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
