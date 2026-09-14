package goonarr.stash.core.model.scraper

data class ScrapedScene(
    val title: String?,
    val details: String?,
    val date: String?,
    val studio: ScrapedStudio?,
    val performers: List<ScrapedPerformer>?,
    val tags: List<ScrapedTag>?,
    val image: String?,
    val remoteSiteId: String?,
    val director: String?,
    val code: String?,
    val url: String?,
    val urls: List<String>?
)

data class ScrapedStudio(
    val storedId: String? = null,
    val name: String,
    val urls: List<String>? = null,
    val parent: ScrapedStudio? = null,
    val image: String? = null,
    val remoteSiteId: String? = null,
    val details: String? = null,
    val tags: List<ScrapedTag>? = null
)

/**
 * Scraped performer data from external sources (StashBox, scrapers).
 * All fields are optional as scrapers may return partial data.
 */
data class ScrapedPerformer(
    val storedId: String? = null,
    val name: String,
    val disambiguation: String? = null,
    val gender: String? = null,
    val urls: List<String>? = null,
    val birthdate: String? = null,
    val ethnicity: String? = null,
    val country: String? = null,
    val eyeColor: String? = null,
    val hairColor: String? = null,
    val height: String? = null,
    val weight: String? = null,
    val measurements: String? = null,
    val fakeTits: String? = null,
    val penisLength: String? = null,
    val circumcised: String? = null,
    val careerLength: String? = null,
    val tattoos: String? = null,
    val piercings: String? = null,
    val aliases: String? = null,
    val images: List<String>? = null,
    val details: String? = null,
    val deathDate: String? = null,
    val remoteSiteId: String? = null,
    val tags: List<ScrapedTag>? = null
)

data class ScrapedTag(
    val name: String
)
