package goonarr.stash.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import goonarr.stash.core.model.StashID
import goonarr.stash.core.model.Tag

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val favorite: Boolean?,
    val imagePath: String?,
    val aliases: List<String>?,
    val ignoreAutoTag: Boolean?,

    // Counts
    val sceneCount: Int?,
    val sceneMarkerCount: Int?,
    val imageCount: Int?,
    val galleryCount: Int?,
    val performerCount: Int?,
    val parentCount: Int?,
    val childCount: Int?,

    // Timestamps
    val createdAt: String?,
    val updatedAt: String?,

    val stashIds: List<StashID>?,

    val sortOrder: Int = 0,
    val parents: List<Tag>? = null,
    val children: List<Tag>? = null
)

fun TagEntity.toDomain(): Tag {
    return Tag(
        id = id,
        name = name,
        description = description,
        favorite = favorite,
        imagePath = imagePath,
        aliases = aliases,
        ignoreAutoTag = ignoreAutoTag,
        sceneCount = sceneCount,
        sceneMarkerCount = sceneMarkerCount,
        imageCount = imageCount,
        galleryCount = galleryCount,
        performerCount = performerCount,
        parentCount = parentCount,
        childCount = childCount,
        createdAt = createdAt,
        updatedAt = updatedAt,
        stashIds = stashIds,
        parents = parents,
        children = children
    )
}

fun Tag.toEntity(sortOrder: Int = 0): TagEntity {
    return TagEntity(
        id = id,
        name = name,
        description = description,
        favorite = favorite,
        imagePath = imagePath,
        aliases = aliases,
        ignoreAutoTag = ignoreAutoTag,
        sceneCount = sceneCount,
        sceneMarkerCount = sceneMarkerCount,
        imageCount = imageCount,
        galleryCount = galleryCount,
        performerCount = performerCount,
        parentCount = parentCount,
        childCount = childCount,
        createdAt = createdAt,
        updatedAt = updatedAt,
        stashIds = stashIds,
        sortOrder = sortOrder,
        parents = parents,
        children = children
    )
}
