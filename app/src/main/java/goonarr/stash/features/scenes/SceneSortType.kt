package goonarr.stash.features.scenes

import androidx.annotation.DrawableRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.ui.graphics.vector.ImageVector
import goonarr.stash.R
import goonarr.stash.features.components.SortOption

enum class SceneSortType(
    val apiValue: String,
    override val displayName: String,
    override val icon: ImageVector? = null,
    @DrawableRes override val iconRes: Int? = null
) : SortOption {
    RANDOM(
        apiValue = "random",
        displayName = "Random",
        icon = Icons.Default.Shuffle
    ),
    CREATED_AT(
        apiValue = "created_at",
        displayName = "Created At",
        icon = Icons.Default.DateRange
    ),
    UPDATED_AT(
        apiValue = "updated_at",
        displayName = "Updated At",
        icon = Icons.Default.History
    ),
    DATE(
        apiValue = "date",
        displayName = "Date",
        icon = Icons.Default.CalendarToday
    ),
    RATING(
        apiValue = "rating",
        displayName = "Rating",
        icon = Icons.Default.Star
    ),
    O_COUNTER(
        apiValue = "o_counter",
        displayName = "O Count",
        iconRes = R.drawable.ic_sweat_drops
    ),
    LAST_PLAYED_AT(
        apiValue = "last_played_at",
        displayName = "Last Played",
        icon = Icons.Default.History
    ),
    TITLE(
        apiValue = "title",
        displayName = "Title",
        icon = Icons.Default.TextFormat
    );

    companion object {
        fun fromApiValue(value: String): SceneSortType {
            return entries.find { it.apiValue == value } ?: DATE
        }
    }
}
