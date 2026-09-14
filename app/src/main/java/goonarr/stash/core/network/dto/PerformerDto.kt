package goonarr.stash.core.network.dto

import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.PerformerTag
import goonarr.stash.core.model.StashID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PerformerDto(
    val id: String,
    val name: String? = null,
    val disambiguation: String? = null,
    val urls: List<String>? = null,
    val gender: String? = null,
    val birthdate: String? = null,
    @SerialName("death_date") val deathDate: String? = null,
    val ethnicity: String? = null,
    val country: String? = null,
    @SerialName("eye_color") val eyeColor: String? = null,
    @SerialName("hair_color") val hairColor: String? = null,
    @SerialName("height_cm") val heightCm: Int? = null,
    val weight: Int? = null,
    val measurements: String? = null,
    @SerialName("fake_tits") val fakeTits: String? = null,
    @SerialName("penis_length") val penisLength: Double? = null,
    val circumcised: String? = null,
    @SerialName("career_length") val careerLength: String? = null,
    val tattoos: String? = null,
    val piercings: String? = null,
    @SerialName("alias_list") val aliasList: List<String>? = null,
    val favorite: Boolean? = null,
    @SerialName("image_path") val imagePath: String? = null,
    val details: String? = null,
    @SerialName("scene_count") val sceneCount: Int? = null,
    @SerialName("image_count") val imageCount: Int? = null,
    @SerialName("gallery_count") val galleryCount: Int? = null,
    @SerialName("group_count") val groupCount: Int? = null,
    @SerialName("o_counter") val oCounter: Int? = null,
    val rating100: Int? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("stash_ids") val stashIds: List<StashIDDto>? = null,
    val tags: List<PerformerTagDto>? = null
)

@Serializable
data class PerformerTagDto(
    val id: String,
    val name: String
)

@Serializable
data class StashIDDto(
    val endpoint: String,
    @SerialName("stash_id") val stashId: String
)

fun PerformerDto.toDomain(): Performer = Performer(
    id = id,
    name = name,
    disambiguation = disambiguation,
    urls = urls,
    gender = gender,
    birthdate = birthdate,
    deathDate = deathDate,
    ethnicity = ethnicity,
    country = country,
    eyeColor = eyeColor,
    hairColor = hairColor,
    heightCm = heightCm,
    weight = weight,
    measurements = measurements,
    fakeTits = fakeTits,
    penisLength = penisLength,
    circumcised = circumcised,
    careerLength = careerLength,
    tattoos = tattoos,
    piercings = piercings,
    aliasList = aliasList,
    favorite = favorite,
    imagePath = imagePath,
    details = details,
    sceneCount = sceneCount,
    imageCount = imageCount,
    galleryCount = galleryCount,
    groupCount = groupCount,
    oCounter = oCounter,
    rating100 = rating100,
    createdAt = createdAt,
    updatedAt = updatedAt,
    stashIds = stashIds?.map { it.toDomain() },
    tags = tags?.map { it.toDomain() }
)

fun PerformerTagDto.toDomain(): PerformerTag = PerformerTag(
    id = id,
    name = name
)

fun StashIDDto.toDomain(): StashID = StashID(
    endpoint = endpoint,
    stashId = stashId
)
