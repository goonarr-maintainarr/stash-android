package goonarr.stash.features.studios

/**
 * Enumeration of available sort types for Studios.
 */
import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.ui.graphics.vector.ImageVector
import goonarr.stash.R
import goonarr.stash.features.components.SortOption

/**
 * Enumeration of available sort types for Studios.
 */
enum class StudioSortType(
    val value: String,
    override val displayName: String,
    override val icon: ImageVector? = null,
    @DrawableRes override val iconRes: Int? = null
) : SortOption {
    NAME("name", "Name", icon = Icons.Default.TextFormat),
    CREATED_AT("created_at", "Date Created", icon = Icons.Default.DateRange),
    UPDATED_AT("updated_at", "Date Updated", icon = Icons.Default.History),
    SCENE_COUNT("scene_count", "Scene Count", icon = Icons.Default.VideoLibrary),
    FAVORITE("favorite", "Favorite", icon = Icons.Default.Favorite),
    O_COUNTER("o_counter", "O-Counter", iconRes = R.drawable.ic_sweat_drops),
    RATING("rating", "Rating", icon = Icons.Default.Star);

    override fun toString(): String = displayName
}
