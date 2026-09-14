package goonarr.stash.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import goonarr.stash.core.model.StashID
import goonarr.stash.core.model.Studio

@Entity(tableName = "studios")
data class StudioEntity(
    @PrimaryKey val id: String,
    val name: String,
    val imagePath: String?,
    val sceneCount: Int?,
    val imageCount: Int?,
    val galleryCount: Int?,
    val performerCount: Int?,
    val groupCount: Int?,
    val details: String?,
    val rating100: Int?,
    val favorite: Boolean?,
    val createdAt: String?,
    val updatedAt: String?,
    val oCounter: Int?,
    val ignoreAutoTag: Boolean?,
    val aliases: List<String>?,
    val urls: List<String>?,
    val stashIds: List<StashID>?,
    // Future enhancement for relation
    val parentStudioId: String? = null
)

fun StudioEntity.toDomain(): Studio {
    return Studio(
        id = id,
        name = name,
        imagePath = imagePath,
        sceneCount = sceneCount,
        imageCount = imageCount,
        galleryCount = galleryCount,
        performerCount = performerCount,
        groupCount = groupCount,
        details = details,
        rating100 = rating100,
        favorite = favorite,
        createdAt = createdAt,
        updatedAt = updatedAt,
        oCounter = oCounter,
        ignoreAutoTag = ignoreAutoTag,
        aliases = aliases,
        urls = urls,
        stashIds = stashIds
    )
}

fun Studio.toEntity(): StudioEntity {
    return StudioEntity(
        id = id,
        name = name,
        imagePath = imagePath,
        sceneCount = sceneCount,
        imageCount = imageCount,
        galleryCount = galleryCount,
        performerCount = performerCount,
        groupCount = groupCount,
        details = details,
        rating100 = rating100,
        favorite = favorite,
        createdAt = createdAt,
        updatedAt = updatedAt,
        oCounter = oCounter,
        ignoreAutoTag = ignoreAutoTag,
        aliases = aliases,
        urls = urls,
        stashIds = stashIds,
        parentStudioId = parentStudio?.id
    )
}
