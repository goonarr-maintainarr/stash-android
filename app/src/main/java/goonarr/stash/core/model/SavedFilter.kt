package goonarr.stash.core.model

import kotlinx.serialization.Serializable

/**
 * Represents a saved filter from Stash.
 * Users can create complex filter combinations and save them for quick access.
 */
@Serializable
data class SavedFilter(
    val id: String,
    val mode: FilterMode,
    val name: String,
    val findFilter: FindFilter? = null,
    val objectFilter: Map<String, FilterCriterion>? = null,
    val uiOptions: Map<String, String>? = null
)

/**
 * Filter mode determines what type of content is being filtered.
 */
enum class FilterMode {
    SCENES,
    PERFORMERS,
    STUDIOS,
    TAGS,
    GALLERIES,
    IMAGES,
    SCENE_MARKERS,
    MOVIES,
    GROUPS
}

/**
 * Pagination and search parameters for a filter.
 */
@Serializable
data class FindFilter(
    val q: String? = null,
    val page: Int = 1,
    val perPage: Int = 25,
    val sort: String = "date",
    val direction: String = "DESC"
)

/**
 * Represents a single filter criterion (e.g., tags, performers, studios).
 */
@Serializable
data class FilterCriterion(
    val value: List<String>? = null,
    val modifier: FilterModifier = FilterModifier.INCLUDES,
    val depth: Int? = null,
    val excludes: List<String>? = null
)

/**
 * How a filter criterion should be applied.
 */
enum class FilterModifier {
    EQUALS,
    NOT_EQUALS,
    GREATER_THAN,
    LESS_THAN,
    IS_NULL,
    NOT_NULL,
    INCLUDES_ALL,
    INCLUDES,
    EXCLUDES,
    MATCHES_REGEX,
    NOT_MATCHES_REGEX,
    BETWEEN,
    NOT_BETWEEN
}
