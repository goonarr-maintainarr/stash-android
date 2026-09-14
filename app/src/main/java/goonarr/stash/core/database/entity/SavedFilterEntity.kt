package goonarr.stash.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import goonarr.stash.core.model.FilterCriterion
import goonarr.stash.core.model.FilterMode
import goonarr.stash.core.model.FindFilter
import goonarr.stash.core.model.SavedFilter

@Entity(tableName = "saved_filters")
data class SavedFilterEntity(
    @PrimaryKey val id: String,
    val mode: FilterMode,
    val name: String,
    val findFilter: FindFilter? = null,
    val objectFilter: Map<String, FilterCriterion>? = null,
    val uiOptions: Map<String, String>? = null,
    val sortOrder: Int = 0
)

fun SavedFilterEntity.toDomain(): SavedFilter {
    return SavedFilter(
        id = id,
        mode = mode,
        name = name,
        findFilter = findFilter,
        objectFilter = objectFilter,
        uiOptions = uiOptions
    )
}

fun SavedFilter.toEntity(sortOrder: Int = 0): SavedFilterEntity {
    return SavedFilterEntity(
        id = id,
        mode = mode,
        name = name,
        findFilter = findFilter,
        objectFilter = objectFilter,
        uiOptions = uiOptions,
        sortOrder = sortOrder
    )
}
