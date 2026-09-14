package goonarr.stash.features.scenes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.ui.graphics.vector.ImageVector
import goonarr.stash.features.components.LayoutOption

/**
 * Layout types for the Scene List screen.
 */
enum class SceneLayoutType(
    override val displayName: String,
    override val icon: ImageVector
) : LayoutOption {
    LIST("List", Icons.Default.ViewAgenda),
    GRID("Grid", Icons.Default.CalendarViewMonth),
    COMPACT("Compact", Icons.Default.ViewList);

    companion object {
        fun fromString(value: String): SceneLayoutType {
            return entries.find { it.name == value } ?: LIST
        }
    }
}
