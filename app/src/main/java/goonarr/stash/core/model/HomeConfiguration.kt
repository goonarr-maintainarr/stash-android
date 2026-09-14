package goonarr.stash.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class HomeSectionType {
    RANDOM,
    RECENTLY_RELEASED,
    RECENTLY_ADDED,
    TOP_RATED,
    MOST_WATCHED,
    RECENTLY_WATCHED,
    TAG
}

object HomeSectionIds {
    const val RANDOM = "RANDOM"
    const val RECENTLY_RELEASED = "RECENTLY_RELEASED"
    const val RECENTLY_ADDED = "RECENTLY_ADDED"
    const val TOP_RATED = "TOP_RATED"
    const val MOST_WATCHED = "MOST_WATCHED"
    const val RECENTLY_WATCHED = "RECENTLY_WATCHED"
    const val STASHDB_FAVORITES = "STASHDB_FAVORITES"

    fun getTagSectionId(tagId: String) = "TAG_$tagId"
    fun isTagSection(id: String) = id.startsWith("TAG_")
    fun getTagIdFromSectionId(id: String) = id.removePrefix("TAG_")
}
