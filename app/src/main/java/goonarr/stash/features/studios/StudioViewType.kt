package goonarr.stash.features.studios

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.ui.graphics.vector.ImageVector
import goonarr.stash.features.components.LayoutOption

/**
 * View types for the Studio List screen.
 */
enum class StudioViewType(
    override val displayName: String,
    override val icon: ImageVector
) : LayoutOption {
    GRID("Grid", Icons.Default.ViewModule),
    LIST("List", Icons.AutoMirrored.Filled.ViewList);

    companion object {
        fun fromString(value: String): StudioViewType {
            return entries.find { it.name == value } ?: GRID
        }
    }
}
