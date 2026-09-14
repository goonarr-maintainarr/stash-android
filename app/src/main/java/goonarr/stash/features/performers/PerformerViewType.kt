package goonarr.stash.features.performers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.ui.graphics.vector.ImageVector
import goonarr.stash.features.components.LayoutOption

/**
 * View types for the Performer List screen.
 *
 * - LIST: Full-width cards (~350dp height), focuses on one performer at a time
 * - GRID_LARGE: 2-column grid with full metadata (current default)
 * - GRID_SMALL: 3-column grid with only name, for fast scrolling
 */
enum class PerformerViewType(
    override val displayName: String,
    override val icon: ImageVector
) : LayoutOption {
    LIST("List", Icons.Default.ViewList),
    GRID_LARGE("Grid (Large)", Icons.Default.ViewModule),
    GRID_SMALL("Grid (Small)", Icons.Default.GridView);

    companion object {
        fun fromString(value: String): PerformerViewType {
            return entries.find { it.name == value } ?: GRID_LARGE
        }
    }
}
