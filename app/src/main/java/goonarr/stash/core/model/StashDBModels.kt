package goonarr.stash.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// MARK: - Core Models

@Serializable
data class StashDBPerformer(
    val id: String,
    val name: String,
    val disambiguation: String? = null,
    val gender: String? = null,
    @SerialName("birth_date") val birthDate: String? = null,
    val ethnicity: String? = null,
    val country: String? = null,
    val height: Int? = null,
    @SerialName("hair_color") val hairColor: String? = null,
    @SerialName("eye_color") val eyeColor: String? = null,
    @SerialName("breast_type") val breastType: String? = null,
    @SerialName("career_start_year") val careerStartYear: Int? = null,
    @SerialName("career_end_year") val careerEndYear: Int? = null,
    @SerialName("scene_count") val sceneCount: Int = 0,
    @SerialName("is_favorite") val isFavorite: Boolean = false,
    val images: List<StashDBImage>? = null,
    val urls: List<StashDBURL>? = null
)

@Serializable
data class StashDBScene(
    val id: String,
    val title: String? = null,
    val details: String? = null,
    val date: String? = null,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("production_date") val productionDate: String? = null,
    val duration: Int? = null,
    val director: String? = null,
    val code: String? = null,
    val deleted: Boolean? = null,
    val created: String? = null,
    val updated: String? = null,
    val studio: StashDBStudio? = null,
    val performers: List<StashDBPerformerAppearance>? = null,
    val tags: List<StashDBTag>? = null,
    val images: List<StashDBImage>? = null,
    val urls: List<StashDBURL>? = null
)

@Serializable
data class StashDBStudio(
    val id: String,
    val name: String,
    val aliases: List<String>? = null,
    val deleted: Boolean? = null,
    @SerialName("is_favorite") val isFavorite: Boolean? = null,
    val created: String? = null,
    val updated: String? = null
)

// MARK: - Supporting Models

@Serializable
data class StashDBPerformerAppearance(
    val performer: StashDBPerformerBasic
)

@Serializable
data class StashDBPerformerBasic(
    val id: String,
    val name: String,
    val gender: String? = null
)

@Serializable
data class StashDBTag(
    val id: String,
    val name: String
)

@Serializable
data class StashDBImage(
    val id: String,
    val url: String,
    val width: Int,
    val height: Int
)

@Serializable
data class StashDBURL(
    val url: String,
    val type: String
)

// MARK: - GraphQL Response Wrappers

@Serializable
data class StashDBPerformersResult(
    val queryPerformers: StashDBPerformersData
)

@Serializable
data class StashDBPerformersData(
    val count: Int,
    val performers: List<StashDBPerformer>
)

@Serializable
data class StashDBScenesQueryResult(
    val queryScenes: StashDBScenesData
)

@Serializable
data class StashDBScenesData(
    val count: Int,
    val scenes: List<StashDBScene>
)

@Serializable
data class StashDBStudiosResult(
    val queryStudios: StashDBStudiosData
)

@Serializable
data class StashDBStudiosData(
    val count: Int,
    val studios: List<StashDBStudio>
)
