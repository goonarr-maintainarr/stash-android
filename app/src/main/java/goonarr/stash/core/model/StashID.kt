package goonarr.stash.core.model

import kotlinx.serialization.Serializable

@Serializable
data class StashID(
    @kotlinx.serialization.SerialName("stash_id")
    val stashId: String,
    val endpoint: String
) {
    val id: String get() = stashId

    /**
     * Returns the external URL for this stash ID based on the endpoint and entity type.
     * @param type The entity type (e.g., "scenes", "tags", "performers", "studios")
     */
    fun getUrl(type: String = "scenes"): String? {
        val path = when (type) {
            "scenes" -> "scenes"
            "tags" -> "tags"
            "performers" -> "performers"
            "studios" -> "studios"
            else -> type
        }

        return when {
            endpoint.contains("stashdb.org") -> "https://stashdb.org/$path/$stashId"
            endpoint.contains("theporndb.net") || endpoint.contains("metadataapi.net") ->
                "https://theporndb.net/$path/$stashId"
            else -> null
        }
    }
}
