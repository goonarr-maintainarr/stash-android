package goonarr.stash.features.performers

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.ui.graphics.vector.ImageVector
import goonarr.stash.R
import goonarr.stash.features.components.SortOption

enum class PerformerSortType(
    val apiValue: String,
    override val displayName: String,
    override val icon: ImageVector? = null,
    @DrawableRes override val iconRes: Int? = null
) : SortOption {
    NAME(
        apiValue = "name",
        displayName = "Name",
        icon = Icons.Default.TextFormat
    ),
    CREATED_AT(
        apiValue = "created_at",
        displayName = "Created",
        // approximating calendar.badge.plus
        icon = Icons.Default.DateRange
    ),
    UPDATED_AT(
        apiValue = "updated_at",
        displayName = "Updated",
        // approximating calendar.badge.clock
        icon = Icons.Default.History
    ),
    SCENE_COUNT(
        apiValue = "scene_count",
        displayName = "Scene Count",
        icon = Icons.Default.Movie
    ),
    O_COUNTER(
        apiValue = "o_counter",
        displayName = "O-Counter",
        // Assuming this exists from previous SceneSortType work
        iconRes = R.drawable.ic_sweat_drops
    )
}
