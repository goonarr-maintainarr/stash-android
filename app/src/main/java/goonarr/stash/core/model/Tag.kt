package goonarr.stash.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val id: String,
    val name: String,
    val description: String? = null,
    val favorite: Boolean? = null,
    val imagePath: String? = null,
    val aliases: List<String>? = null,
    val ignoreAutoTag: Boolean? = null,

    // Counts
    val sceneCount: Int? = null,
    val sceneMarkerCount: Int? = null,
    val imageCount: Int? = null,
    val galleryCount: Int? = null,
    val performerCount: Int? = null,
    val parentCount: Int? = null,
    val childCount: Int? = null,
    val parents: List<Tag>? = null,
    val children: List<Tag>? = null,

    // Timestamps
    val createdAt: String? = null,
    val updatedAt: String? = null,

    val stashIds: List<StashID>? = null
)
