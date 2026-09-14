package goonarr.stash.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.PerformerTag
import goonarr.stash.core.model.StashID

@Entity(tableName = "performers")
data class PerformerEntity(
    @PrimaryKey val id: String,
    val name: String?,
    val imagePath: String?,
    val sceneCount: Int?,
    val oCounter: Int?,

    // Details
    val disambiguation: String?,
    val gender: String?,
    val birthdate: String?,
    val deathDate: String?,
    val country: String?,
    val ethnicity: String?,
    val eyeColor: String?,
    val hairColor: String?,
    val heightCm: Int?,
    // Assuming weight is Int based on simpler models, or String? checking domain
    val weight: Int?,
    val measurements: String?,
    val fakeTits: String?,
    val careerLength: String?,
    val tattoos: String?,
    val piercings: String?,
    val aliasList: List<String>?,
    val favorite: Boolean?,

    val details: String?,
    val rating100: Int?,
    val createdAt: String?,
    val updatedAt: String?,
    val stashIds: List<StashID>?,
    val urls: List<String>?,
    val tags: List<PerformerTag>?,
    val sortOrder: Int = 0
)

fun PerformerEntity.toDomain(): Performer {
    return Performer(
        id = id,
        name = name,
        imagePath = imagePath,
        sceneCount = sceneCount,
        oCounter = oCounter,
        disambiguation = disambiguation,
        gender = gender,
        birthdate = birthdate,
        deathDate = deathDate,
        country = country,
        ethnicity = ethnicity,
        eyeColor = eyeColor,
        hairColor = hairColor,
        heightCm = heightCm,
        weight = weight,
        measurements = measurements,
        fakeTits = fakeTits,
        careerLength = careerLength,
        tattoos = tattoos,
        piercings = piercings,
        aliasList = aliasList,
        favorite = favorite,
        details = details,
        rating100 = rating100,
        createdAt = createdAt,
        updatedAt = updatedAt,
        stashIds = stashIds,
        urls = urls,
        tags = tags,
        sortOrder = sortOrder
    )
}

fun Performer.toEntity(sortOrder: Int = 0): PerformerEntity {
    return PerformerEntity(
        id = id,
        name = name,
        imagePath = imagePath,
        sceneCount = sceneCount,
        oCounter = oCounter,
        disambiguation = disambiguation,
        gender = gender,
        birthdate = birthdate,
        deathDate = deathDate,
        country = country,
        ethnicity = ethnicity,
        eyeColor = eyeColor,
        hairColor = hairColor,
        heightCm = heightCm,
        weight = weight,
        measurements = measurements,
        fakeTits = fakeTits,
        careerLength = careerLength,
        tattoos = tattoos,
        piercings = piercings,
        aliasList = aliasList,
        favorite = favorite,
        details = details,
        rating100 = rating100,
        createdAt = createdAt,
        updatedAt = updatedAt,
        stashIds = stashIds,
        urls = urls,
        tags = tags,
        sortOrder = sortOrder
    )
}
