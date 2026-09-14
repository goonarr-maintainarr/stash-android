package goonarr.stash.features.home.configure

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import goonarr.stash.StashTheme
import goonarr.stash.core.model.FilterMode
import goonarr.stash.core.model.HomeSectionIds
import goonarr.stash.core.model.SavedFilter

@Composable
private fun ConfigureHomeItemPreview(item: HomeConfigItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.DragHandle,
            contentDescription = "Drag to reorder",
            modifier = Modifier.padding(end = 16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = item.title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge
        )

        Switch(
            checked = !item.isHidden,
            onCheckedChange = {}
        )
    }
}

@Composable
fun ConfigureHomeItemShimmer() {
    val brush = goonarr.stash.features.components.rememberShimmerBrush()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        // Arrows shimmer
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(48.dp)
                .background(brush)
        )

        // Title shimmer
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
                .height(24.dp)
                .background(brush)
        )

        // Switch shimmer
        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .width(50.dp)
                .height(30.dp)
                .background(brush)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ConfigureHomeScreenPreview() {
    val items = listOf(
        HomeConfigItem(HomeSectionIds.RANDOM, "Random", isHidden = false),
        HomeConfigItem(HomeSectionIds.RECENTLY_RELEASED, "Recently Released", isHidden = true),
        HomeConfigItem("TAG_1", "Tag: Example", isHidden = false)
    )

    StashTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Change Rows") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Text(
                    "Drag to reorder, use switch to toggle visibility",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Static preview - actual screen uses ReorderableLazyColumn
                items.forEach { item ->
                    ConfigureHomeItemPreview(item)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConfigureHomeItemShimmerPreview() {
    StashTheme {
        Surface {
            ConfigureHomeItemShimmer()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ConfigureHomeScreenWithSavedFiltersPreview() {
    val items = listOf(
        HomeConfigItem(HomeSectionIds.RANDOM, "Random", isHidden = false),
        HomeConfigItem(HomeSectionIds.RECENTLY_RELEASED, "Recently Released", isHidden = true),
        HomeConfigItem("TAG_1", "Tag: BBC", isHidden = false),
        HomeConfigItem("SAVED_FILTER_1", "Anal Creampies", isHidden = false)
    )

    val savedFilterSections = listOf(
        SavedFilterSection(
            mode = FilterMode.SCENES,
            title = "Scenes",
            filters = listOf(
                SavedFilterItem(
                    filter = SavedFilter(
                        id = "1",
                        mode = FilterMode.SCENES,
                        name = "Anal Creampies"
                    ),
                    isEnabled = true
                ),
                SavedFilterItem(
                    filter = SavedFilter(
                        id = "2",
                        mode = FilterMode.SCENES,
                        name = "Big Boobs"
                    ),
                    isEnabled = false
                ),
                SavedFilterItem(
                    filter = SavedFilter(
                        id = "3",
                        mode = FilterMode.SCENES,
                        name = "Gangbang"
                    ),
                    isEnabled = false
                )
            )
        ),
        SavedFilterSection(
            mode = FilterMode.PERFORMERS,
            title = "Performers",
            filters = listOf(
                SavedFilterItem(
                    filter = SavedFilter(
                        id = "10",
                        mode = FilterMode.PERFORMERS,
                        name = "Redhead"
                    ),
                    isEnabled = false
                ),
                SavedFilterItem(
                    filter = SavedFilter(
                        id = "11",
                        mode = FilterMode.PERFORMERS,
                        name = "Tallest"
                    ),
                    isEnabled = true
                )
            )
        ),
        SavedFilterSection(
            mode = FilterMode.TAGS,
            title = "Tags",
            filters = listOf(
                SavedFilterItem(
                    filter = SavedFilter(
                        id = "20",
                        mode = FilterMode.TAGS,
                        name = "Newest Tags"
                    ),
                    isEnabled = false
                )
            )
        )
    )

    StashTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Change Rows") },
                    navigationIcon = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Text(
                    "Drag to reorder, use switch to toggle visibility",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Draggable items
                items.forEach { item ->
                    ConfigureHomeItemPreview(item)
                }

                // Saved Filters Section
                Spacer(modifier = Modifier.height(32.dp))

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Text(
                        "Saved Filters",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Toggle to show/hide in home screen",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                savedFilterSections.forEach { section ->
                    Text(
                        text = section.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    section.filters.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(vertical = 12.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.filter.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Switch(
                                checked = item.isEnabled,
                                onCheckedChange = {}
                            )
                        }
                    }
                }
            }
        }
    }
}
