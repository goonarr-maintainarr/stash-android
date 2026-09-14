package goonarr.stash.features.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import goonarr.stash.StashTheme
import kotlinx.coroutines.flow.flowOf

private enum class PreviewSortOption(
    override val displayName: String,
    override val icon: ImageVector? = null,
    override val iconRes: Int? = null
) : SortOption {
    NAME("Name", Icons.Default.ArrowUpward),
    DATE("Date", Icons.Default.ArrowDownward),
    RATING("Rating", Icons.Default.Check),
    LONG_NAME("Rating (High to Low with extra text)", Icons.Default.Check)
}

private enum class PreviewLayoutOption(
    override val displayName: String,
    override val icon: ImageVector
) : LayoutOption {
    GRID("Grid", Icons.Default.Check),
    LIST("List", Icons.AutoMirrored.Filled.ViewList)
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Normal State", showBackground = true, backgroundColor = 0xFF0D1426)
@Composable
fun SortableListTopAppBarNormalPreview() {
    StashTheme {
        SortableListTopAppBar<PreviewSortOption, PreviewLayoutOption>(
            title = "Studios",
            count = 142,
            sortOptions = PreviewSortOption.entries.toList(),
            currentSortType = flowOf(PreviewSortOption.NAME),
            currentSortDirection = flowOf("ASC"),
            defaultSortType = PreviewSortOption.NAME,
            onUpdateSortType = {},
            onUpdateSortDirection = {},
            searchQuery = "",
            onSearchQueryChange = {},
            layoutOptions = PreviewLayoutOption.entries.toList(),
            currentLayoutType = flowOf(PreviewLayoutOption.GRID),
            defaultLayoutType = PreviewLayoutOption.GRID,
            onUpdateLayoutType = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Searching State", showBackground = true, backgroundColor = 0xFF0D1426)
@Composable
fun SortableListTopAppBarSearchingPreview() {
    StashTheme {
        SortableListTopAppBar<PreviewSortOption, PreviewLayoutOption>(
            title = "Scenes",
            count = 1024,
            sortOptions = PreviewSortOption.entries.toList(),
            currentSortType = flowOf(PreviewSortOption.DATE),
            currentSortDirection = flowOf("DESC"),
            defaultSortType = PreviewSortOption.NAME,
            onUpdateSortType = {},
            onUpdateSortDirection = {},
            searchQuery = "action",
            onSearchQueryChange = {},
            layoutOptions = PreviewLayoutOption.entries.toList(),
            currentLayoutType = flowOf(PreviewLayoutOption.LIST),
            defaultLayoutType = PreviewLayoutOption.GRID,
            onUpdateLayoutType = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "With Back Click", showBackground = true, backgroundColor = 0xFF0D1426)
@Composable
fun SortableListTopAppBarBackPreview() {
    StashTheme {
        SortableListTopAppBar<PreviewSortOption, PreviewLayoutOption>(
            title = "Performers",
            count = 56,
            sortOptions = PreviewSortOption.entries.toList(),
            currentSortType = flowOf(PreviewSortOption.NAME),
            currentSortDirection = flowOf("DESC"),
            defaultSortType = PreviewSortOption.NAME,
            onUpdateSortType = {},
            onUpdateSortDirection = {},
            searchQuery = "",
            onSearchQueryChange = {},
            onBackClick = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "With Actions", showBackground = true, backgroundColor = 0xFF0D1426)
@Composable
fun SortableListTopAppBarActionsPreview() {
    StashTheme {
        SortableListTopAppBar<PreviewSortOption, PreviewLayoutOption>(
            title = "Studios",
            count = 142,
            sortOptions = PreviewSortOption.entries.toList(),
            currentSortType = flowOf(PreviewSortOption.NAME),
            currentSortDirection = flowOf("ASC"),
            defaultSortType = PreviewSortOption.NAME,
            onUpdateSortType = {},
            onUpdateSortDirection = {},
            searchQuery = "",
            onSearchQueryChange = {},
            actions = {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "No Count", showBackground = true, backgroundColor = 0xFF0D1426)
@Composable
fun SortableListTopAppBarNoCountPreview() {
    StashTheme {
        SortableListTopAppBar<PreviewSortOption, PreviewLayoutOption>(
            title = "All Tags",
            count = null,
            sortOptions = PreviewSortOption.entries.toList(),
            currentSortType = flowOf(PreviewSortOption.NAME),
            currentSortDirection = flowOf("ASC"),
            defaultSortType = PreviewSortOption.NAME,
            onUpdateSortType = {},
            onUpdateSortDirection = {},
            searchQuery = "",
            onSearchQueryChange = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Long Title", showBackground = true, backgroundColor = 0xFF0D1426)
@Composable
fun SortableListTopAppBarLongTitlePreview() {
    StashTheme {
        SortableListTopAppBar<PreviewSortOption, PreviewLayoutOption>(
            title = "Very Long Title for a Studio that Probably Doesn't Fit",
            count = 9999,
            sortOptions = PreviewSortOption.entries.toList(),
            currentSortType = flowOf(PreviewSortOption.NAME),
            currentSortDirection = flowOf("ASC"),
            defaultSortType = PreviewSortOption.NAME,
            onUpdateSortType = {},
            onUpdateSortDirection = {},
            searchQuery = "",
            onSearchQueryChange = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Dark Theme", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun SortableListTopAppBarDarkModePreview() {
    StashTheme(darkTheme = true) {
        SortableListTopAppBar<PreviewSortOption, PreviewLayoutOption>(
            title = "Dark Mode",
            count = 42,
            sortOptions = PreviewSortOption.entries.toList(),
            currentSortType = flowOf(PreviewSortOption.NAME),
            currentSortDirection = flowOf("ASC"),
            defaultSortType = PreviewSortOption.NAME,
            onUpdateSortType = {},
            onUpdateSortDirection = {},
            searchQuery = "focused",
            onSearchQueryChange = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Long Filter Label", showBackground = true, backgroundColor = 0xFF0D1426)
@Composable
fun SortableListTopAppBarLongFilterPreview() {
    StashTheme {
        SortableListTopAppBar<PreviewSortOption, PreviewLayoutOption>(
            title = "Studios",
            count = 142,
            sortOptions = PreviewSortOption.entries.toList(),
            currentSortType = flowOf(PreviewSortOption.LONG_NAME),
            currentSortDirection = flowOf("ASC"),
            defaultSortType = PreviewSortOption.NAME,
            onUpdateSortType = {},
            onUpdateSortDirection = {},
            searchQuery = "",
            onSearchQueryChange = {}
        )
    }
}
