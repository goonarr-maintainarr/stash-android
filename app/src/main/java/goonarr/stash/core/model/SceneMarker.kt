package goonarr.stash.core.model

import kotlinx.serialization.Serializable

@Serializable
data class SceneMarker(
    val id: String,
    val title: String,
    val seconds: Double,
    val endSeconds: Double? = null,
    val primaryTag: Tag? = null,
    val tags: List<Tag>? = null,
    val stream: String? = null,
    val preview: String? = null,
    val scene: Scene? = null
)
