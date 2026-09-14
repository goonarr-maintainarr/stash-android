package goonarr.stash.core.network.dto

import goonarr.stash.core.model.scraper.ScrapedPerformer
import goonarr.stash.core.model.scraper.ScrapedScene
import goonarr.stash.core.model.scraper.ScrapedStudio
import goonarr.stash.core.model.scraper.ScrapedTag
import goonarr.stash.core.model.scraper.StashBox
import kotlinx.serialization.Serializable

@Serializable
data class StashBoxDto(
    val name: String,
    val endpoint: String,
    val api_key: String?
) {
    fun toDomain() = StashBox(name, endpoint, apiKey = api_key)
}

@Serializable
data class ScrapedSceneDto(
    val title: String?,
    val details: String?,
    val date: String?,
    val studio: ScrapedStudioDto?,
    val performers: List<ScrapedPerformerDto>?,
    val tags: List<ScrapedTagDto>?,
    val image: String?,
    val remote_site_id: String?,
    val director: String?,
    val code: String?,
    val url: String?,
    val urls: List<String>?
) {
    fun toDomain() = ScrapedScene(
        title = title,
        details = details,
        date = date,
        studio = studio?.toDomain(),
        performers = performers?.map { it.toDomain() },
        tags = tags?.map { it.toDomain() },
        image = image,
        remoteSiteId = remote_site_id,
        director = director,
        code = code,
        url = url,
        urls = urls
    )
}

@Serializable
data class ScrapedStudioDto(
    val stored_id: String? = null,
    val name: String,
    val urls: List<String>? = null,
    val parent: ScrapedStudioDto? = null,
    val image: String? = null,
    val remote_site_id: String? = null,
    val details: String? = null,
    val tags: List<ScrapedTagDto>? = null
) {
    fun toDomain(): ScrapedStudio = ScrapedStudio(
        storedId = stored_id,
        name = name,
        urls = urls,
        parent = parent?.toDomain(),
        image = image,
        remoteSiteId = remote_site_id,
        details = details,
        tags = tags?.map { it.toDomain() }
    )
}

@Serializable
data class ScrapedPerformerDto(
    val stored_id: String? = null,
    val name: String,
    val disambiguation: String? = null,
    val gender: String? = null,
    val urls: List<String>? = null,
    val birthdate: String? = null,
    val ethnicity: String? = null,
    val country: String? = null,
    val eye_color: String? = null,
    val hair_color: String? = null,
    val height: String? = null,
    val weight: String? = null,
    val measurements: String? = null,
    val fake_tits: String? = null,
    val penis_length: String? = null,
    val circumcised: String? = null,
    val career_length: String? = null,
    val tattoos: String? = null,
    val piercings: String? = null,
    val aliases: String? = null,
    val images: List<String>? = null,
    val details: String? = null,
    val death_date: String? = null,
    val remote_site_id: String? = null,
    val tags: List<ScrapedTagDto>? = null
) {
    fun toDomain() = ScrapedPerformer(
        storedId = stored_id,
        name = name,
        disambiguation = disambiguation,
        gender = gender,
        urls = urls,
        birthdate = birthdate,
        ethnicity = ethnicity,
        country = country,
        eyeColor = eye_color,
        hairColor = hair_color,
        height = height,
        weight = weight,
        measurements = measurements,
        fakeTits = fake_tits,
        penisLength = penis_length,
        circumcised = circumcised,
        careerLength = career_length,
        tattoos = tattoos,
        piercings = piercings,
        aliases = aliases,
        images = images,
        details = details,
        deathDate = death_date,
        remoteSiteId = remote_site_id,
        tags = tags?.map { it.toDomain() }
    )
}

@Serializable
data class ScrapedTagDto(
    val name: String
) {
    fun toDomain() = ScrapedTag(name)
}

@Serializable
data class ConfigurationResponse(val configuration: ConfigDto)

@Serializable
data class ConfigDto(val general: ConfigGeneralDto)

@Serializable
data class ConfigGeneralDto(val stashBoxes: List<StashBoxDto>)

@Serializable
data class ScrapeSingleSceneResponse(val scrapeSingleScene: List<ScrapedSceneDto>?)

@Serializable
data class ScrapeSinglePerformerResponse(val scrapeSinglePerformer: List<ScrapedPerformerDto>?)

@Serializable
data class ScrapeSingleStudioResponse(val scrapeSingleStudio: List<ScrapedStudioDto>?)
