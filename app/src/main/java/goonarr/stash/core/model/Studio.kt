package goonarr.stash.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Studio(
    val id: String,
    val name: String,
    val urls: List<String>? = null,
    val parentStudio: SimpleStudio? = null,
    val childStudios: List<SimpleStudio>? = null,
    val aliases: List<String>? = null,
    val tags: List<Tag>? = null,
    val ignoreAutoTag: Boolean? = null,
    val imagePath: String? = null,
    val sceneCount: Int? = null,
    val imageCount: Int? = null,
    val galleryCount: Int? = null,
    val performerCount: Int? = null,
    val groupCount: Int? = null,
    val stashIds: List<StashID>? = null,
    val rating100: Int? = null,
    val favorite: Boolean? = null,
    val details: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val oCounter: Int? = null,
    val url: String? = null
)

@Serializable
data class SimpleStudio(
    val id: String,
    val name: String,
    val imagePath: String? = null
)
