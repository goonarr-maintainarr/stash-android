package goonarr.stash.features.tags.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import goonarr.stash.StashTheme
import goonarr.stash.StashTokens
import goonarr.stash.core.network.StashBoxTag

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StashBoxSearchDialog(
    onDismiss: () -> Unit,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    searchResults: List<StashBoxTag>,
    onTagSelected: (StashBoxTag) -> Unit,
    isSearching: Boolean
) {
    val dynamicColors = StashTheme.colors

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp)
        ) {
            Column(modifier = Modifier.padding(StashTokens.Spacing.ContentHorizontal)) {
                Text(
                    text = "Search StashDB Tags",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = StashTokens.Spacing.ContentHorizontal)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = onSearchTextChange,
                        label = { Text("Tag Name") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = dynamicColors.primary.copy(alpha = 0.1f),
                            unfocusedContainerColor = dynamicColors.primary.copy(alpha = 0.05f),
                            focusedBorderColor = dynamicColors.primary,
                            unfocusedBorderColor = dynamicColors.primary.copy(alpha = 0.5f),
                            cursorColor = dynamicColors.primary,
                            focusedLabelColor = dynamicColors.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSearchTextChange(searchText) },
                        colors = ButtonDefaults.buttonColors(containerColor = dynamicColors.primary)
                    ) {
                        Text("Search")
                    }
                }

                Spacer(modifier = Modifier.height(StashTokens.Spacing.ContentHorizontal))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(min = 100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(color = dynamicColors.primary)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(searchResults.size) { index ->
                                val tag = searchResults[index]
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onTagSelected(tag) }
                                        .padding(vertical = 12.dp)
                                ) {
                                    Text(
                                        text = tag.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!tag.description.isNullOrBlank()) {
                                        Text(
                                            text = tag.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    if (tag.aliases.isNotEmpty()) {
                                        Text(
                                            text = "Aliases: ${tag.aliases.joinToString(", ")}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                if (index < searchResults.lastIndex) {
                                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(StashTokens.Spacing.ContentHorizontal))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = dynamicColors.primary)
                    }
                }
            }
        }
    }
}
