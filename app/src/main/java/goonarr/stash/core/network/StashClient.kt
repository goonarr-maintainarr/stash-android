package goonarr.stash.core.network

import android.util.Base64
import goonarr.stash.core.model.GenerationOptions
import goonarr.stash.core.model.Job
import goonarr.stash.core.model.Log
import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.ScanOptions
import goonarr.stash.core.model.Scene
import goonarr.stash.core.model.SceneMarker
import goonarr.stash.core.model.Stats
import goonarr.stash.core.model.Studio
import goonarr.stash.core.model.Tag
import goonarr.stash.core.model.scraper.ScrapedScene
import goonarr.stash.core.model.scraper.StashBox
import goonarr.stash.core.model.scraper.TaggerConfig
import goonarr.stash.core.network.dto.ConfigurationResponse
import goonarr.stash.core.network.dto.PerformerDto
import goonarr.stash.core.network.dto.SceneDto
import goonarr.stash.core.network.dto.SceneMarkerDto
import goonarr.stash.core.network.dto.ScrapeSingleSceneResponse
import goonarr.stash.core.network.dto.StudioDto
import goonarr.stash.core.network.dto.TagDto
import goonarr.stash.core.network.dto.toDomain
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import javax.inject.Inject
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import timber.log.Timber

interface StashClient {
    suspend fun testConnection(url: String, apiKey: String?): Boolean
    suspend fun fetchStats(url: String, apiKey: String?): Stats
    suspend fun fetchJobQueue(url: String, apiKey: String?): List<Job>

    suspend fun fetchLogs(url: String, apiKey: String?): List<Log>
    suspend fun findScenes(
        url: String,
        apiKey: String?,
        searchText: String,
        page: Int,
        perPage: Int = 20,
        sort: String = "created_at",
        direction: String = "DESC",
        performerId: String? = null,
        tagId: String? = null,
        studioId: String? = null,
        updatedAt: String? = null
    ): Pair<List<Scene>, Int>

    suspend fun findScene(url: String, apiKey: String?, id: String): Scene?
    suspend fun findPerformers(
        url: String,
        apiKey: String?,
        searchText: String,
        page: Int,
        perPage: Int = 20,
        sort: String = "name",
        direction: String = "ASC",
        studioId: String? = null,
        tagId: String? = null,
        updatedAt: String? = null
    ): Pair<List<Performer>, Int>

    suspend fun findPerformer(url: String, apiKey: String?, id: String): Performer?
    suspend fun findStudios(
        url: String,
        apiKey: String?,
        searchText: String,
        page: Int,
        perPage: Int = 20,
        sort: String = "name",
        direction: String = "ASC",
        tagId: String? = null,
        updatedAt: String? = null
    ): Pair<List<Studio>, Int>

    suspend fun findStudio(url: String, apiKey: String?, id: String): Studio?
    suspend fun findTags(
        url: String,
        apiKey: String?,
        searchText: String,
        page: Int,
        updatedAt: String? = null
    ): Pair<List<Tag>, Int>

    suspend fun findAllTags(url: String, apiKey: String?): List<Tag>

    suspend fun findTagsByIds(url: String, apiKey: String?, ids: List<String>): List<Tag>
    suspend fun findSceneMarkers(
        url: String,
        apiKey: String?,
        page: Int,
        perPage: Int = 20,
        tagId: String? = null
    ): Pair<List<SceneMarker>, Int>

    suspend fun updatePerformer(
        url: String,
        apiKey: String?,
        id: String,
        name: String? = null,
        disambiguation: String? = null,
        urlParam: String? = null,
        gender: String? = null,
        birthdate: String? = null,
        ethnicity: String? = null,
        country: String? = null,
        eyeColor: String? = null,
        heightCm: Int? = null,
        measurements: String? = null,
        fakeTits: String? = null,
        careerLength: String? = null,
        tattoos: String? = null,
        piercings: String? = null,
        aliases: String? = null,
        details: String? = null,
        deathDate: String? = null,
        hairColor: String? = null,
        weight: Int? = null,
        rating: Int? = null,
        favorite: Boolean? = null,
        urls: List<String>? = null,
        penisLength: Double? = null,
        circumcised: String? = null,
        image: String? = null,
        tagIds: List<String>? = null,
        stashIds: List<Pair<String, String>>? = null
    ): Performer?

    suspend fun updateScene(
        url: String,
        apiKey: String?,
        id: String,
        title: String? = null,
        details: String? = null,
        performerIds: List<String>? = null,
        tagIds: List<String>? = null,
        director: String? = null,
        code: String? = null,
        urlParam: String? = null,
        date: String? = null,
        rating: Int? = null,
        studioId: String? = null,
        coverImage: String? = null,
        stashIds: List<Pair<String, String>>? = null
    ): Scene?

    suspend fun deleteOHistory(url: String, apiKey: String?, id: String, times: List<String>): Boolean
    suspend fun deletePlayHistory(url: String, apiKey: String?, id: String, times: List<String>): Boolean

    /**
     * Downloads an image and converts it to a Data URI string (e.g. data:image/jpeg;base64,...)
     */
    suspend fun downloadImageAsBase64(url: String): String?

    suspend fun incrementOCounter(url: String, apiKey: String?, id: String): Int

    suspend fun incrementPlayCount(url: String, apiKey: String?, id: String): Int

    suspend fun requestScan(url: String, apiKey: String?, options: ScanOptions): Boolean

    suspend fun requestGeneration(
        url: String,
        apiKey: String?,
        options: GenerationOptions
    ): Boolean

    suspend fun stopJob(url: String, apiKey: String?, jobId: String): Boolean
    suspend fun stopAllJobs(url: String, apiKey: String?): Boolean

    suspend fun sceneDestroy(
        url: String,
        apiKey: String?,
        id: String,
        deleteFile: Boolean,
        deleteGenerated: Boolean
    ): Boolean

    suspend fun performerDestroy(url: String, apiKey: String?, id: String): Boolean

    /**
     * Updates a studio.
     *
     * @param url The Stash server URL.
     * @param apiKey The API key for authentication.
     * @param id The Stash studio ID.
     * @param name Optional new name.
     * @param urls Optional new URLs.
     * @param parentId Optional new parent studio ID.
     * @param rating100 Optional new rating (0-100).
     * @param favorite Optional new favorite status.
     * @param details Optional new details/description.
     * @param aliases Optional new aliases.
     * @param tagIds Optional new tag IDs.
     * @param ignoreAutoTag Optional new ignore auto-tagging status.
     * @return The updated studio or null if not found.
     */
    suspend fun updateStudio(
        url: String,
        apiKey: String?,
        id: String,
        name: String? = null,
        urls: List<String>? = null,
        parentId: String? = null,
        rating100: Int? = null,
        favorite: Boolean? = null,
        details: String? = null,
        aliases: List<String>? = null,
        tagIds: List<String>? = null,
        ignoreAutoTag: Boolean? = null,
        image: String? = null
    ): Studio?

    /**
     * Deletes a studio.
     *
     * @param url The Stash server URL.
     * @param apiKey The API key for authentication.
     * @param id The Stash studio ID.
     * @return True if successful, false otherwise.
     */
    suspend fun studioDestroy(url: String, apiKey: String?, id: String): Boolean

    suspend fun updateTag(
        url: String,
        apiKey: String?,
        id: String,
        name: String? = null,
        aliases: List<String>? = null,
        image: String? = null,
        parentIds: List<String>? = null,
        childIds: List<String>? = null,
        favorite: Boolean? = null,
        ignoreAutoTag: Boolean? = null,
        description: String? = null,
        stashIds: List<Pair<String, String>>? = null
    ): Tag?

    suspend fun tagDestroy(url: String, apiKey: String?, id: String): Boolean

    suspend fun createSceneMarker(
        url: String,
        apiKey: String?,
        title: String,
        seconds: Double,
        endSeconds: Double?,
        sceneId: String,
        primaryTagId: String,
        tagIds: List<String>?
    ): SceneMarker?

    suspend fun deleteSceneMarker(url: String, apiKey: String?, id: String): Boolean

    /**
     * Generates a new screenshot for a scene at the specified timestamp.
     * @param url The Stash server URL.
     * @param apiKey The API key for authentication.
     * @param sceneId The ID of the scene.
     * @param timestamp The timestamp (in seconds) where the screenshot should be captured.
     * @return True if the screenshot was successfully generated, false otherwise.
     */
    suspend fun sceneGenerateScreenshot(
        url: String,
        apiKey: String?,
        sceneId: String,
        timestamp: Double
    ): Boolean

    /**
     * Updates the watch history for a scene.
     * @param url The Stash server URL.
     * @param apiKey The API key for authentication.
     * @param id The ID of the scene.
     * @param resumeTime The time in seconds to resume playback from.
     * @param playDuration The duration in seconds that the scene was watched.
     * @return True if the activity was successfully saved, false otherwise.
     */
    suspend fun sceneSaveActivity(
        url: String,
        apiKey: String?,
        id: String,
        resumeTime: Double?,
        playDuration: Double?
    ): Boolean

    suspend fun fetchStashBoxes(url: String, apiKey: String?): List<StashBox>

    suspend fun scrapeScene(
        url: String,
        apiKey: String?,
        stashBox: StashBox,
        sceneTitle: String?,
        sceneCode: String?,
        sceneDetails: String?,
        sceneDirector: String?,
        sceneUrl: String?,
        sceneDate: String?,
        query: String?
    ): List<ScrapedScene>

    suspend fun scrapeSceneByFragment(
        url: String,
        apiKey: String?,
        stashBox: StashBox,
        sceneTitle: String?,
        sceneCode: String?,
        sceneDetails: String?,
        sceneDirector: String?,
        sceneUrl: String?,
        sceneDate: String?
    ): List<ScrapedScene>

    suspend fun scrapePerformer(
        url: String,
        apiKey: String?,
        stashBox: StashBox,
        query: String
    ): List<goonarr.stash.core.model.scraper.ScrapedPerformer>

    suspend fun scrapeStudio(
        url: String,
        apiKey: String?,
        stashBox: StashBox,
        query: String
    ): List<goonarr.stash.core.model.scraper.ScrapedStudio>

    suspend fun createPerformer(
        url: String,
        apiKey: String?,
        name: String,
        urlParam: String? = null,
        gender: String? = null,
        birthdate: String? = null,
        ethnicity: String? = null,
        country: String? = null,
        eyeColor: String? = null,
        heightCm: Int? = null,
        measurements: String? = null,
        fakeTits: String? = null,
        careerLength: String? = null,
        tattoos: String? = null,
        piercings: String? = null,
        aliases: String? = null,
        details: String? = null,
        deathDate: String? = null,
        hairColor: String? = null,
        weight: Int? = null,
        rating: Int? = null,
        image: String? = null
    ): Performer?

    suspend fun fetchTaggerConfig(url: String, apiKey: String?): TaggerConfig?
    suspend fun saveTaggerConfig(
        url: String,
        apiKey: String?,
        config: TaggerConfig
    ): Boolean

    // Sync Pruning - Lightweight ID fetches
    suspend fun fetchAllSceneIds(url: String, apiKey: String?): List<String>
    suspend fun fetchAllPerformerIds(url: String, apiKey: String?): List<String>
    suspend fun fetchAllTagIds(url: String, apiKey: String?): List<String>
    suspend fun fetchAllStudioIds(url: String, apiKey: String?): List<String>

    // Saved Filters
    suspend fun findSavedFilters(url: String, apiKey: String?, mode: String? = null): List<SavedFilterDto>
    suspend fun findSavedFilter(url: String, apiKey: String?, id: String): SavedFilterDto?

    // Configuration
    suspend fun getConfiguration(url: String, apiKey: String?): UIConfiguration?
}

@Serializable
data class PerformerDestroyResponse(val performerDestroy: Boolean)

@Serializable
data class SceneDestroyResponse(val sceneDestroy: Boolean)

@Serializable
data class SceneSaveActivityResponse(val sceneSaveActivity: Boolean)

@Serializable
data class TestResponse(val findScenes: TestScenes)

@Serializable
data class TestScenes(val count: Int)

@Serializable
data class StatsResponse(val stats: Stats)

@Serializable
data class JobQueueResponse(val jobQueue: List<Job>?)

@Serializable
data class LogsResponse(val logs: List<Log>)

@Serializable
data class FindSceneResponse(val findScene: SceneDto?)

@Serializable
data class SceneUpdateResponse(val sceneUpdate: SceneDto?)

@Serializable
data class SceneIncrementOResponse(val sceneIncrementO: Int)

@Serializable
data class SceneIncrementPlayCountResponse(val sceneIncrementPlayCount: Int)

@Serializable
data class SceneDeleteHistoryResponse(val count: Int)

@Serializable
data class SceneMarkerCreateResponse(val sceneMarkerCreate: SceneMarkerDto?)

@Serializable
data class SceneMarkerDestroyResponse(val sceneMarkerDestroy: Boolean)

@Serializable
data class PerformerUpdateResponse(val performerUpdate: PerformerDto?)

@Serializable
data class FindPerformersResponse(val findPerformers: PerformersResult)

@Serializable
data class PerformersResult(val performers: List<PerformerDto>, val count: Int)

@Serializable
data class FindPerformerResponse(val findPerformer: PerformerDto?)

@Serializable
data class FindStudiosResponse(val findStudios: StudiosResult)

@Serializable
data class StudiosResult(val studios: List<StudioDto>, val count: Int)

@Serializable
data class FindStudioResponse(val findStudio: StudioDto?)

@Serializable
data class StudioUpdateResponse(val studioUpdate: StudioDto?)

@Serializable
data class StudioDestroyResponse(val studioDestroy: Boolean)

@Serializable
data class TagUpdateResponse(val tagUpdate: TagDto?)

@Serializable
data class TagDestroyResponse(val tagDestroy: Boolean)

@Serializable
data class FindScenesResponse(val findScenes: ScenesResult)

@Serializable
data class ScenesResult(val scenes: List<SceneDto>, val count: Int)

@Serializable
data class FindTagsResponse(val findTags: TagsResult)

@Serializable
data class TagsResult(val tags: List<TagDto>, val count: Int)

@Serializable
data class FindSceneMarkersResponse(val findSceneMarkers: SceneMarkersResult)

@Serializable
data class SceneMarkersResult(val scene_markers: List<SceneMarkerDto>, val count: Int)

@Serializable
data class ConfigurationUIResponse(val configuration: ConfigurationUI)

@Serializable
data class ConfigurationUI(val ui: JsonElement)

@Serializable
data class ConfigureUISettingResponse(val configureUISetting: JsonElement)

@Serializable
data class FindSavedFiltersResponse(val findSavedFilters: List<SavedFilterDto>)

@Serializable
data class FindSavedFilterResponse(val findSavedFilter: SavedFilterDto?)

@Serializable
data class SavedFilterDto(
    val id: String,
    val mode: String,
    val name: String,
    val find_filter: FindFilterDto?,
    val object_filter: JsonElement?,
    val ui_options: JsonElement?
)

@Serializable
data class FindFilterDto(
    val q: String? = null,
    val page: Int = 1,
    val per_page: Int = 25,
    val sort: String = "date",
    val direction: String = "DESC"
)

// Configuration DTOs
@Serializable
data class GetConfigurationResponse(val configuration: ConfigurationDto)

@Serializable
data class ConfigurationDto(val ui: JsonElement)

@Serializable
data class UIConfiguration(val frontPageContent: List<FrontPageContentItem>? = null)

@Serializable
data class FrontPageContentItem(
    val __typename: String,
    // For CustomFilter
    val mode: String? = null,
    val sortBy: String? = null,
    val direction: String? = null,
    val message: MessageDto? = null,
    val title: String? = null,
    // For SavedFilter
    val savedFilterId: Int? = null
)

@Serializable
data class MessageDto(
    val id: String,
    val values: Map<String, String>? = null
)

class KtorStashClient @Inject constructor(
    private val executor: GraphQLExecutor
) : StashClient {

    override suspend fun testConnection(url: String, apiKey: String?): Boolean {
        return try {
            // We just need to check if it throws, return type ignores structure for now or define a simple wrapper
            // StashQueries.testConnection returns: { findScenes: { scenes: [...], count: ... } }

            executor.execute<TestResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.testConnection
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun fetchStats(url: String, apiKey: String?): Stats {
        val response = executor.execute<StatsResponse>(
            url = url,
            apiKey = apiKey,
            query = StashQueries.stats
        )
        return response.stats
    }

    override suspend fun fetchJobQueue(url: String, apiKey: String?): List<Job> {
        val response = executor.execute<JobQueueResponse>(
            url = url,
            apiKey = apiKey,
            query = StashQueries.jobQueue
        )
        return response.jobQueue ?: emptyList()
    }

    override suspend fun fetchLogs(url: String, apiKey: String?): List<Log> {
        val response = executor.execute<LogsResponse>(
            url = url,
            apiKey = apiKey,
            query = StashQueries.logs
        )
        return response.logs
    }

    override suspend fun findPerformers(
        url: String,
        apiKey: String?,
        searchText: String,
        page: Int,
        perPage: Int,
        sort: String,
        direction: String,
        studioId: String?,
        tagId: String?,
        updatedAt: String?
    ): Pair<List<Performer>, Int> {
        val response = executor.execute<FindPerformersResponse>(
            url = url,
            apiKey = apiKey,
            query = StashQueries.findPerformers(
                searchText = searchText,
                page = page,
                perPage = perPage,
                sort = sort,
                direction = direction,
                studioId = studioId,
                tagId = tagId,
                updatedAt = updatedAt
            )
        )
        return Pair(response.findPerformers.performers.map { it.toDomain() }, response.findPerformers.count)
    }

    override suspend fun findScenes(
        url: String,
        apiKey: String?,
        searchText: String,
        page: Int,
        perPage: Int,
        sort: String,
        direction: String,
        performerId: String?,
        tagId: String?,
        studioId: String?,
        updatedAt: String?
    ): Pair<List<Scene>, Int> {
        val response = executor.execute<FindScenesResponse>(
            url = url,
            apiKey = apiKey,
            query = StashQueries.findScenes(
                searchText = searchText,
                page = page,
                perPage = perPage,
                sort = sort,
                direction = direction,
                performerId = performerId,
                tagId = tagId,
                studioId = studioId,
                updatedAt = updatedAt
            )
        )
        return Pair(response.findScenes.scenes.map { it.toDomain() }, response.findScenes.count)
    }

    override suspend fun findScene(url: String, apiKey: String?, id: String): Scene? {
        val response = executor.execute<FindSceneResponse>(
            url = url,
            apiKey = apiKey,
            query = StashQueries.findScene(id)
        )
        return response.findScene?.toDomain()
    }

    override suspend fun updateScene(
        url: String,
        apiKey: String?,
        id: String,
        title: String?,
        details: String?,
        performerIds: List<String>?,
        tagIds: List<String>?,
        director: String?,
        code: String?,
        urlParam: String?,
        date: String?,
        rating: Int?,
        studioId: String?,
        coverImage: String?,
        stashIds: List<Pair<String, String>>?
    ): Scene? {
        val response = executor.execute<SceneUpdateResponse>(
            url = url,
            apiKey = apiKey,
            query = StashQueries.sceneUpdate(
                id = id,
                title = title,
                details = details,
                performerIds = performerIds,
                tagIds = tagIds,
                director = director,
                code = code,
                url = urlParam,
                date = date,
                rating = rating,
                studioId = studioId,
                coverImage = coverImage,
                stashIds = stashIds
            )
        )
        return response.sceneUpdate?.toDomain()
    }

    override suspend fun scrapeSceneByFragment(
        url: String,
        apiKey: String?,
        stashBox: StashBox,
        sceneTitle: String?,
        sceneCode: String?,
        sceneDetails: String?,
        sceneDirector: String?,
        sceneUrl: String?,
        sceneDate: String?
    ): List<ScrapedScene> {
        return try {
            // Prepare query components from fragment
            val queryComponents = mutableListOf<String>()
            sceneTitle?.let { if (it.isNotEmpty()) queryComponents.add(it) }
            sceneCode?.let { if (it.isNotEmpty()) queryComponents.add(it) }

            val constructedQuery = queryComponents.joinToString(" ").trim()
            // Fallback to "placeholder" if empty, as per iOS/Stash requirements
            val query = constructedQuery.ifEmpty { "placeholder" }

            val response = executor.execute<ScrapeSingleSceneResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.scrapeSingleScene(
                    scraperName = stashBox.name,
                    scraperEndpoint = stashBox.endpoint,
                    // Note: StashBox apiKey
                    scraperApiKey = stashBox.apiKey,
                    sceneTitle = sceneTitle,
                    sceneCode = sceneCode,
                    sceneDetails = sceneDetails,
                    sceneDirector = sceneDirector,
                    sceneUrl = sceneUrl,
                    sceneDate = sceneDate,
                    query = query
                )
            )
            response.scrapeSingleScene?.map { it.toDomain() } ?: emptyList()
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to scrape scene by fragment")
            emptyList()
        }
    }

    override suspend fun deleteOHistory(url: String, apiKey: String?, id: String, times: List<String>): Boolean {
        return try {
            executor.execute<SceneDeleteHistoryResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.sceneDeleteO(id, times)
            )
            true
        } catch (e: Exception) {
            Timber.e(e, "❌ Error deleting O history")
            false
        }
    }

    override suspend fun deletePlayHistory(url: String, apiKey: String?, id: String, times: List<String>): Boolean {
        return try {
            executor.execute<SceneDeleteHistoryResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.sceneDeletePlay(id, times)
            )
            true
        } catch (e: Exception) {
            Timber.e(e, "❌ Error deleting play history")
            false
        }
    }

    override suspend fun incrementOCounter(url: String, apiKey: String?, id: String): Int {
        val response = executor.execute<SceneIncrementOResponse>(
            url = url,
            apiKey = apiKey,
            query = StashQueries.sceneIncrementO(id)
        )
        return response.sceneIncrementO
    }

    override suspend fun incrementPlayCount(url: String, apiKey: String?, id: String): Int {
        Timber.d("📊 StashClient.incrementPlayCount: scene $id")
        val response = executor.execute<SceneIncrementPlayCountResponse>(
            url = url,
            apiKey = apiKey,
            query = StashQueries.sceneIncrementPlayCount(id)
        )
        return response.sceneIncrementPlayCount
    }

    override suspend fun updatePerformer(
        url: String,
        apiKey: String?,
        id: String,
        name: String?,
        disambiguation: String?,
        urlParam: String?,
        gender: String?,
        birthdate: String?,
        ethnicity: String?,
        country: String?,
        eyeColor: String?,
        heightCm: Int?,
        measurements: String?,
        fakeTits: String?,
        careerLength: String?,
        tattoos: String?,
        piercings: String?,
        aliases: String?,
        details: String?,
        deathDate: String?,
        hairColor: String?,
        weight: Int?,
        rating: Int?,
        favorite: Boolean?,
        urls: List<String>?,
        penisLength: Double?,
        circumcised: String?,
        image: String?,
        tagIds: List<String>?,
        stashIds: List<Pair<String, String>>?
    ): Performer? {
        val queryStr = StashQueries.performerUpdate(
            id = id,
            name = name,
            disambiguation = disambiguation,
            url = urlParam,
            gender = gender,
            birthdate = birthdate,
            ethnicity = ethnicity,
            country = country,
            eyeColor = eyeColor,
            heightCm = heightCm,
            measurements = measurements,
            fakeTits = fakeTits,
            careerLength = careerLength,
            tattoos = tattoos,
            piercings = piercings,
            aliases = aliases,
            details = details,
            deathDate = deathDate,
            hairColor = hairColor,
            weight = weight,
            rating = rating,
            favorite = favorite,
            urls = urls,
            penisLength = penisLength,
            circumcised = circumcised,
            image = image,
            tagIds = tagIds,
            stashIds = stashIds
        )
        Timber.d("🚀 GraphQL Query: $queryStr")

        val response = executor.execute<PerformerUpdateResponse>(
            url = url,
            apiKey = apiKey,
            query = queryStr
        )
        return response.performerUpdate?.toDomain()
    }

    override suspend fun findPerformer(url: String, apiKey: String?, id: String): Performer? {
        val response = executor.execute<FindPerformerResponse>(
            url = url,
            apiKey = apiKey,
            query = StashQueries.findPerformer(id)
        )
        return response.findPerformer?.toDomain()
    }

    override suspend fun findStudios(
        url: String,
        apiKey: String?,
        searchText: String,
        page: Int,
        perPage: Int,
        sort: String,
        direction: String,
        tagId: String?,
        updatedAt: String?
    ): Pair<List<Studio>, Int> {
        val response = executor.execute<FindStudiosResponse>(
            url = url,
            apiKey = apiKey,
            query = StashQueries.findStudios(
                searchText = searchText,
                page = page,
                perPage = perPage,
                sort = sort,
                direction = direction,
                tagId = tagId,
                updatedAt = updatedAt
            )
        )
        return Pair(response.findStudios.studios.map { it.toDomain() }, response.findStudios.count)
    }

    override suspend fun findStudio(url: String, apiKey: String?, id: String): Studio? {
        val response = executor.execute<FindStudioResponse>(
            url = url,
            apiKey = apiKey,
            query = StashQueries.findStudio(id)
        )
        return response.findStudio?.toDomain()
    }

    override suspend fun findTags(url: String, apiKey: String?, searchText: String, page: Int, updatedAt: String?): Pair<List<Tag>, Int> {
        val response = executor.execute<FindTagsResponse>(
            url = url,
            apiKey = apiKey,
            query = StashQueries.findTags(searchText = searchText, page = page, updatedAt = updatedAt)
        )
        return Pair(response.findTags.tags.map { it.toDomain() }, response.findTags.count)
    }

    override suspend fun findAllTags(url: String, apiKey: String?): List<Tag> {
        val response = executor.execute<FindTagsResponse>(
            url = url,
            apiKey = apiKey,
            query = StashQueries.findTags(searchText = "", page = 1, perPage = -1)
        )
        return response.findTags.tags.map { it.toDomain() }
    }

    override suspend fun findTagsByIds(url: String, apiKey: String?, ids: List<String>): List<Tag> {
        if (ids.isEmpty()) return emptyList()
        val response = executor.execute<FindTagsResponse>(
            url = url,
            apiKey = apiKey,
            query = StashQueries.findTagsByIds(ids)
        )
        return response.findTags.tags.map { it.toDomain() }
    }

    override suspend fun findSceneMarkers(
        url: String,
        apiKey: String?,
        page: Int,
        perPage: Int,
        tagId: String?
    ): Pair<List<SceneMarker>, Int> {
        val response = executor.execute<FindSceneMarkersResponse>(
            url = url,
            apiKey = apiKey,
            query = StashQueries.findSceneMarkers(
                page = page,
                perPage = perPage,
                tagId = tagId
            )
        )
        return Pair(response.findSceneMarkers.scene_markers.map { it.toDomain() }, response.findSceneMarkers.count)
    }

    @Serializable
    private data class MetadataScanResponse(val metadataScan: String?)

    @Serializable
    private data class MetadataGenerateResponse(val metadataGenerate: String?)

    @Serializable
    private data class StopJobResponse(val stopJob: Boolean?)

    @Serializable
    private data class StopAllJobsResponse(val stopAllJobs: Boolean?)

    override suspend fun requestScan(
        url: String,
        apiKey: String?,
        options: ScanOptions
    ): Boolean {
        return try {
            executor.execute<MetadataScanResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.metadataScan(options)
            )
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to request scan")
            false
        }
    }

    override suspend fun requestGeneration(
        url: String,
        apiKey: String?,
        options: GenerationOptions
    ): Boolean {
        return try {
            executor.execute<MetadataGenerateResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.metadataGenerate(options)
            )
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to request generation")
            false
        }
    }

    override suspend fun stopJob(url: String, apiKey: String?, jobId: String): Boolean {
        return try {
            val response = executor.execute<StopJobResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.stopJob(jobId)
            )
            response.stopJob == true
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop job")
            false
        }
    }

    override suspend fun stopAllJobs(url: String, apiKey: String?): Boolean {
        return try {
            val response = executor.execute<StopAllJobsResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.stopAllJobs()
            )
            response.stopAllJobs == true
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop all jobs")
            false
        }
    }
    override suspend fun sceneDestroy(
        url: String,
        apiKey: String?,
        id: String,
        deleteFile: Boolean,
        deleteGenerated: Boolean
    ): Boolean {
        return try {
            val response = executor.execute<SceneDestroyResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.sceneDestroy(
                    id = id,
                    deleteFile = deleteFile,
                    deleteGenerated = deleteGenerated
                )
            )
            response.sceneDestroy
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete scene")
            false
        }
    }

    override suspend fun sceneSaveActivity(
        url: String,
        apiKey: String?,
        id: String,
        resumeTime: Double?,
        playDuration: Double?
    ): Boolean {
        return try {
            val response = executor.execute<SceneSaveActivityResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.sceneSaveActivity(
                    id = id,
                    resumeTime = resumeTime,
                    playDuration = playDuration
                )
            )
            response.sceneSaveActivity
        } catch (e: Exception) {
            Timber.e(e, "Failed to save scene activity")
            false
        }
    }

    override suspend fun performerDestroy(url: String, apiKey: String?, id: String): Boolean {
        return try {
            val response = executor.execute<PerformerDestroyResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.performerDestroy(id)
            )
            response.performerDestroy
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete performer")
            false
        }
    }

    override suspend fun updateStudio(
        url: String,
        apiKey: String?,
        id: String,
        name: String?,
        urls: List<String>?,
        parentId: String?,
        rating100: Int?,
        favorite: Boolean?,
        details: String?,
        aliases: List<String>?,
        tagIds: List<String>?,
        ignoreAutoTag: Boolean?,
        image: String?
    ): Studio? {
        Timber.d("🌐 StashClient.updateStudio: studio $id")
        val response = executor.execute<StudioUpdateResponse>(
            url = url,
            apiKey = apiKey,
            query = StashQueries.studioUpdate(
                id = id,
                name = name,
                urls = urls,
                parentId = parentId,
                rating100 = rating100,
                favorite = favorite,
                details = details,
                aliases = aliases,
                tagIds = tagIds,
                ignoreAutoTag = ignoreAutoTag,
                image = image
            )
        )
        return response.studioUpdate?.toDomain()
    }

    override suspend fun studioDestroy(url: String, apiKey: String?, id: String): Boolean {
        return try {
            val response = executor.execute<StudioDestroyResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.studioDestroy(id)
            )
            response.studioDestroy
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete studio")
            false
        }
    }

    override suspend fun updateTag(
        url: String,
        apiKey: String?,
        id: String,
        name: String?,
        aliases: List<String>?,
        image: String?,
        parentIds: List<String>?,
        childIds: List<String>?,
        favorite: Boolean?,
        ignoreAutoTag: Boolean?,
        description: String?,
        stashIds: List<Pair<String, String>>?
    ): Tag? {
        Timber.d("🌐 StashClient.updateTag: tag $id")
        val response = executor.execute<TagUpdateResponse>(
            url = url,
            apiKey = apiKey,
            query = StashQueries.tagUpdate(
                id = id,
                name = name,
                aliases = aliases,
                image = image,
                parentIds = parentIds,
                childIds = childIds,
                favorite = favorite,
                ignoreAutoTag = ignoreAutoTag,
                description = description,
                stashIds = stashIds
            )
        )
        return response.tagUpdate?.toDomain()
    }

    override suspend fun tagDestroy(url: String, apiKey: String?, id: String): Boolean {
        return try {
            val response = executor.execute<TagDestroyResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.tagDestroy(id)
            )
            response.tagDestroy
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete tag")
            false
        }
    }

    override suspend fun createSceneMarker(
        url: String,
        apiKey: String?,
        title: String,
        seconds: Double,
        endSeconds: Double?,
        sceneId: String,
        primaryTagId: String,
        tagIds: List<String>?
    ): SceneMarker? {
        return try {
            val response = executor.execute<SceneMarkerCreateResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.createSceneMarker(
                    title = title,
                    seconds = seconds,
                    endSeconds = endSeconds,
                    sceneId = sceneId,
                    primaryTagId = primaryTagId,
                    tagIds = tagIds
                )
            )
            response.sceneMarkerCreate?.toDomain()
        } catch (e: Exception) {
            Timber.e(e, "Failed to create scene marker")
            null
        }
    }

    override suspend fun deleteSceneMarker(url: String, apiKey: String?, id: String): Boolean {
        return try {
            val response = executor.execute<SceneMarkerDestroyResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.deleteSceneMarker(id)
            )
            response.sceneMarkerDestroy
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete scene marker")
            false
        }
    }

    @Serializable
    private data class SceneGenerateScreenshotResponse(val sceneGenerateScreenshot: String?)

    /**
     * Implementation of scene screenshot generation using the GraphQL mutation.
     */
    override suspend fun sceneGenerateScreenshot(
        url: String,
        apiKey: String?,
        sceneId: String,
        timestamp: Double
    ): Boolean {
        return try {
            val response = executor.execute<SceneGenerateScreenshotResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.sceneGenerateScreenshot(sceneId, timestamp)
            )
            response.sceneGenerateScreenshot != null
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate scene screenshot")
            false
        }
    }

    override suspend fun fetchStashBoxes(url: String, apiKey: String?): List<StashBox> {
        return try {
            val response = executor.execute<ConfigurationResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.configuration
            )
            response.configuration.general.stashBoxes.map { it.toDomain() }
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch stash boxes")
            emptyList()
        }
    }

    override suspend fun scrapeScene(
        url: String,
        apiKey: String?,
        stashBox: StashBox,
        sceneTitle: String?,
        sceneCode: String?,
        sceneDetails: String?,
        sceneDirector: String?,
        sceneUrl: String?,
        sceneDate: String?,
        query: String?
    ): List<ScrapedScene> {
        return try {
            Timber.d("🕵️‍♀️ Scraping scene: title='$sceneTitle', query='$query', stashBox='${stashBox.name}'")
            val response = executor.execute<ScrapeSingleSceneResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.scrapeSingleScene(
                    scraperName = stashBox.name,
                    scraperEndpoint = stashBox.endpoint,
                    scraperApiKey = stashBox.apiKey,
                    sceneTitle = sceneTitle,
                    sceneCode = sceneCode,
                    sceneDetails = sceneDetails,
                    sceneDirector = sceneDirector,
                    sceneUrl = sceneUrl,
                    sceneDate = sceneDate,
                    query = query
                )
            )
            val results = response.scrapeSingleScene?.map { it.toDomain() } ?: emptyList()
            Timber.d("✅ Scrape complete. Found ${results.size} results.")
            results
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to scrape scene")
            emptyList()
        }
    }

    override suspend fun scrapePerformer(
        url: String,
        apiKey: String?,
        stashBox: StashBox,
        query: String
    ): List<goonarr.stash.core.model.scraper.ScrapedPerformer> {
        return try {
            Timber.d("🕵️‍♀️ Scraping performer: query='$query', stashBox='${stashBox.name}'")
            val response = executor.execute<goonarr.stash.core.network.dto.ScrapeSinglePerformerResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.scrapeSinglePerformer(
                    scraperName = stashBox.name,
                    scraperEndpoint = stashBox.endpoint,
                    scraperApiKey = stashBox.apiKey,
                    query = query
                )
            )
            val results = response.scrapeSinglePerformer?.map { it.toDomain() } ?: emptyList()
            Timber.d("✅ Performer scrape complete. Found ${results.size} results.")
            results
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to scrape performer")
            emptyList()
        }
    }

    override suspend fun scrapeStudio(
        url: String,
        apiKey: String?,
        stashBox: StashBox,
        query: String
    ): List<goonarr.stash.core.model.scraper.ScrapedStudio> {
        return try {
            Timber.d("🕵️‍♀️ Scraping studio: query='$query', stashBox='${stashBox.name}'")
            val response = executor.execute<goonarr.stash.core.network.dto.ScrapeSingleStudioResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.scrapeSingleStudio(
                    scraperName = stashBox.name,
                    scraperEndpoint = stashBox.endpoint,
                    scraperApiKey = stashBox.apiKey,
                    query = query
                )
            )
            val results = response.scrapeSingleStudio?.map { it.toDomain() } ?: emptyList()
            Timber.d("✅ Studio scrape complete. Found ${results.size} results.")
            results
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to scrape studio")
            emptyList()
        }
    }

    override suspend fun downloadImageAsBase64(url: String): String? {
        return try {
            val response = executor.client.get(url)
            val bytes = response.readRawBytes()
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            val mimeType = getMimeType(bytes)
            "data:$mimeType;base64,$base64"
        } catch (e: Exception) {
            Timber.e(e, "Failed to download image as base64 from $url")
            null
        }
    }

    private fun getMimeType(bytes: ByteArray): String {
        return when {
            bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte() -> "image/jpeg"
            bytes.size >= 8 && bytes[0] == 0x89.toByte() && bytes[1]
                == 0x50.toByte() && bytes[2] == 0x4E.toByte() && bytes[3] == 0x47.toByte() -> "image/png"

            bytes.size >= 12 && bytes[0] == 0x52.toByte() && bytes[1]
                == 0x49.toByte() && bytes[2] == 0x46.toByte() && bytes[3] == 0x46.toByte() &&
                bytes[8] == 0x57.toByte() && bytes[9] == 0x45.toByte() && bytes[10]
                == 0x42.toByte() && bytes[11] == 0x50.toByte() -> "image/webp"

            bytes.size >= 6 && (
                bytes[0] == 0x47.toByte() && bytes[1]
                    == 0x49.toByte() && bytes[2] == 0x46.toByte() && bytes[3] == 0x38.toByte()
                ) -> "image/gif"

            else -> "image/jpeg" // Fallback
        }
    }

    override suspend fun createPerformer(
        url: String,
        apiKey: String?,
        name: String,
        urlParam: String?,
        gender: String?,
        birthdate: String?,
        ethnicity: String?,
        country: String?,
        eyeColor: String?,
        heightCm: Int?,
        measurements: String?,
        fakeTits: String?,
        careerLength: String?,
        tattoos: String?,
        piercings: String?,
        aliases: String?,
        details: String?,
        deathDate: String?,
        hairColor: String?,
        weight: Int?,
        rating: Int?,
        image: String?
    ): Performer? {
        return try {
            val response = executor.execute<PerformerCreateResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.performerCreate(
                    name = name,
                    url = urlParam,
                    gender = gender,
                    birthdate = birthdate,
                    ethnicity = ethnicity,
                    country = country,
                    eyeColor = eyeColor,
                    heightCm = heightCm,
                    measurements = measurements,
                    fakeTits = fakeTits,
                    careerLength = careerLength,
                    tattoos = tattoos,
                    piercings = piercings,
                    aliases = aliases,
                    details = details,
                    deathDate = deathDate,
                    hairColor = hairColor,
                    weight = weight,
                    rating = rating,
                    image = image
                )
            )
            response.performerCreate?.toDomain()
        } catch (e: Exception) {
            Timber.e(e, "Failed to create performer")
            null
        }
    }

    override suspend fun fetchTaggerConfig(
        url: String,
        apiKey: String?
    ): TaggerConfig? {
        return try {
            val response = executor.execute<ConfigurationUIResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.configurationUI
            )
            // The 'ui' field is a JsonObject containing all UI settings.
            // We need to extract 'taggerConfig' from it.
            val uiSettings = response.configuration.ui.jsonObject
            val taggerConfigJson = uiSettings["taggerConfig"] ?: return null
            executor.json.decodeFromJsonElement<TaggerConfig>(taggerConfigJson)
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch tagger config")
            null
        }
    }

    override suspend fun saveTaggerConfig(
        url: String,
        apiKey: String?,
        config: TaggerConfig
    ): Boolean {
        return try {
            val jsonConfig = executor.json.encodeToJsonElement(config)
            executor.execute<ConfigureUISettingResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.configureUISetting,
                variables = mapOf("config" to jsonConfig)
            )
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to save tagger config")
            false
        }
    }

    // MARK: - Sync Pruning ID Fetches

    @Serializable
    private data class FindAllSceneIdsResponse(val findScenes: SceneIdsResult)

    @Serializable
    private data class SceneIdsResult(val scenes: List<IdOnly>)

    @Serializable
    private data class FindAllPerformerIdsResponse(val findPerformers: PerformerIdsResult)

    @Serializable
    private data class PerformerIdsResult(val performers: List<IdOnly>)

    @Serializable
    private data class FindAllTagIdsResponse(val findTags: TagIdsResult)

    @Serializable
    private data class TagIdsResult(val tags: List<IdOnly>)

    @Serializable
    private data class FindAllStudioIdsResponse(val findStudios: StudioIdsResult)

    @Serializable
    private data class StudioIdsResult(val studios: List<IdOnly>)

    @Serializable
    private data class IdOnly(val id: String)

    override suspend fun fetchAllSceneIds(url: String, apiKey: String?): List<String> {
        return try {
            Timber.d("🔍 Fetching all scene IDs for sync pruning...")
            val response = executor.execute<FindAllSceneIdsResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.findAllSceneIds
            )
            val ids = response.findScenes.scenes.map { it.id }
            Timber.d("✅ Fetched ${ids.size} scene IDs from server")
            ids
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to fetch scene IDs for pruning")
            emptyList()
        }
    }

    override suspend fun fetchAllPerformerIds(url: String, apiKey: String?): List<String> {
        return try {
            Timber.d("🔍 Fetching all performer IDs for sync pruning...")
            val response = executor.execute<FindAllPerformerIdsResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.findAllPerformerIds
            )
            val ids = response.findPerformers.performers.map { it.id }
            Timber.d("✅ Fetched ${ids.size} performer IDs from server")
            ids
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to fetch performer IDs for pruning")
            emptyList()
        }
    }

    override suspend fun fetchAllTagIds(url: String, apiKey: String?): List<String> {
        return try {
            Timber.d("🔍 Fetching all tag IDs for sync pruning...")
            val response = executor.execute<FindAllTagIdsResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.findAllTagIds
            )
            val ids = response.findTags.tags.map { it.id }
            Timber.d("✅ Fetched ${ids.size} tag IDs from server")
            ids
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to fetch tag IDs for pruning")
            emptyList()
        }
    }

    override suspend fun fetchAllStudioIds(url: String, apiKey: String?): List<String> {
        return try {
            Timber.d("🔍 Fetching all studio IDs for sync pruning...")
            val response = executor.execute<FindAllStudioIdsResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.findAllStudioIds
            )
            val ids = response.findStudios.studios.map { it.id }
            Timber.d("✅ Fetched ${ids.size} studio IDs from server")
            ids
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to fetch studio IDs for pruning")
            emptyList()
        }
    }

    override suspend fun findSavedFilters(url: String, apiKey: String?, mode: String?): List<SavedFilterDto> {
        return try {
            Timber.d("🔍 Fetching saved filters${mode?.let { " for mode: $it" } ?: ""}")
            val response = executor.execute<FindSavedFiltersResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.findSavedFilters(mode)
            )
            Timber.d("✅ Fetched ${response.findSavedFilters.size} saved filters")
            response.findSavedFilters
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to fetch saved filters")
            emptyList()
        }
    }

    override suspend fun findSavedFilter(url: String, apiKey: String?, id: String): SavedFilterDto? {
        return try {
            Timber.d("🔍 Fetching saved filter id: $id")
            val response = executor.execute<FindSavedFilterResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.findSavedFilter(id)
            )
            Timber.d("✅ Fetched saved filter: ${response.findSavedFilter?.name}")
            response.findSavedFilter
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to fetch saved filter $id")
            null
        }
    }

    override suspend fun getConfiguration(url: String, apiKey: String?): UIConfiguration? {
        return try {
            Timber.d("🔍 Fetching UI configuration")
            val response = executor.execute<GetConfigurationResponse>(
                url = url,
                apiKey = apiKey,
                query = StashQueries.getConfiguration()
            )

            // Parse the UI JsonElement to get frontPageContent
            val uiConfig = executor.json.decodeFromJsonElement<UIConfiguration>(response.configuration.ui)
            Timber.d("✅ Fetched UI configuration: ${uiConfig.frontPageContent?.size ?: 0} items in frontPageContent")
            uiConfig
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to fetch UI configuration")
            null
        }
    }
}

@Serializable
private data class PerformerCreateResponse(val performerCreate: PerformerDto?)
