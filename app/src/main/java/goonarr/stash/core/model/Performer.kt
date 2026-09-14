package goonarr.stash.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Performer(
    val id: String,
    val name: String? = null,
    val disambiguation: String? = null,
    val urls: List<String>? = null,
    val gender: String? = null,
    val birthdate: String? = null,
    val deathDate: String? = null,
    val ethnicity: String? = null,
    val country: String? = null,
    val eyeColor: String? = null,
    val hairColor: String? = null,
    val heightCm: Int? = null,
    val weight: Int? = null,
    val measurements: String? = null,
    val fakeTits: String? = null,
    val penisLength: Double? = null,
    val circumcised: String? = null,
    val careerLength: String? = null,
    val tattoos: String? = null,
    val piercings: String? = null,
    val aliasList: List<String>? = null,
    val favorite: Boolean? = null,
    val imagePath: String? = null,
    val details: String? = null,
    val sceneCount: Int? = null,
    val imageCount: Int? = null,
    val galleryCount: Int? = null,
    val groupCount: Int? = null,
    val oCounter: Int? = null,
    val rating100: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val stashIds: List<StashID>? = null,
    val tags: List<PerformerTag>? = null,
    val sortOrder: Int = 0
)

@Serializable
data class PerformerTag(
    val id: String,
    val name: String
)
