package goonarr.stash.core.network.stashdb

import goonarr.stash.core.model.StashDBPerformer
import goonarr.stash.core.model.StashDBPerformersResult
import goonarr.stash.core.model.StashDBScene
import goonarr.stash.core.model.StashDBScenesData
import goonarr.stash.core.model.StashDBScenesQueryResult
import goonarr.stash.core.model.StashDBStudio
import goonarr.stash.core.model.StashDBStudiosResult
import goonarr.stash.core.network.GraphQLExecutor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import timber.log.Timber

/**
 * StashDB GraphQL client for querying external scene and performer metadata.
 *
 * This client communicates with StashDB (https://stashdb.org) to:
 * - Fetch favorite performers and studios
 * - Query scenes by performer or studio
 * - Retrieve detailed scene information
 *
 * All methods use GraphQL queries defined in [StashDBQueries].
 */
class StashDBClient @Inject constructor(
    private val executor: GraphQLExecutor
) {
    companion object {
        private const val DEFAULT_PER_PAGE = 100
    }

    /**
     * Tests the connection to StashDB.
     * Returns true if connection is successful, false otherwise.
     */
    suspend fun testConnection(url: String, apiKey: String): Boolean {
        return try {
            val variables = mapOf<String, JsonElement>(
                "page" to JsonPrimitive(1),
                "perPage" to JsonPrimitive(1)
            )
            executor.execute<StashDBPerformersResult>(
                url = url,
                apiKey = apiKey,
                query = StashDBQueries.favoritePerformersOverview,
                variables = variables
            )
            true
        } catch (e: Exception) {
            Timber.e(e, "❌ StashDB connection test failed")
            false
        }
    }

    /**
     * Fetches all favorite performers (paginated internally).
     */
    suspend fun fetchFavoritePerformers(
        url: String,
        apiKey: String
    ): List<StashDBPerformer> {
        Timber.i("🌟 Fetching favorite performers from StashDB")

        val allPerformers = mutableListOf<StashDBPerformer>()
        var currentPage = 1
        var totalCount = 0

        do {
            val variables = mapOf<String, JsonElement>(
                "page" to JsonPrimitive(currentPage),
                "perPage" to JsonPrimitive(DEFAULT_PER_PAGE)
            )

            val result = executor.execute<StashDBPerformersResult>(
                url = url,
                apiKey = apiKey,
                query = StashDBQueries.favoritePerformersOverview,
                variables = variables
            )

            val data = result.queryPerformers
            totalCount = data.count
            allPerformers.addAll(data.performers)

            Timber.d("📥 Fetched page $currentPage: ${data.performers.size} performers")
            currentPage++
        } while (allPerformers.size < totalCount)

        Timber.i("✅ Fetched ${allPerformers.size} favorite performers")
        return allPerformers
    }

    /**
     * Fetches all favorite studios (paginated internally).
     */
    suspend fun fetchFavoriteStudios(
        url: String,
        apiKey: String
    ): List<StashDBStudio> {
        Timber.i("🏢 Fetching favorite studios from StashDB")

        val allStudios = mutableListOf<StashDBStudio>()
        var currentPage = 1
        var totalCount = 0

        do {
            val variables = mapOf<String, JsonElement>(
                "page" to JsonPrimitive(currentPage),
                "perPage" to JsonPrimitive(DEFAULT_PER_PAGE)
            )

            val result = executor.execute<StashDBStudiosResult>(
                url = url,
                apiKey = apiKey,
                query = StashDBQueries.favoriteStudios,
                variables = variables
            )

            val data = result.queryStudios
            totalCount = data.count
            allStudios.addAll(data.studios)

            Timber.d("📥 Fetched page $currentPage: ${data.studios.size} studios")
            currentPage++
        } while (allStudios.size < totalCount)

        Timber.i("✅ Fetched ${allStudios.size} favorite studios")
        return allStudios
    }

    /**
     * Fetches scenes from the past month that feature any of the given performers OR studios.
     */
    suspend fun fetchScenesByPerformersAndStudios(
        url: String,
        apiKey: String,
        performerIds: List<String>,
        studioIds: List<String>,
        excludeVR: Boolean,
        excludeCompilations: Boolean
    ): StashDBScenesData {
        Timber.i("🎬 Fetching scenes for ${performerIds.size} performers and ${studioIds.size} studios")

        if (performerIds.isEmpty() && studioIds.isEmpty()) {
            Timber.w("⚠️ No performer or studio IDs provided")
            return StashDBScenesData(count = 0, scenes = emptyList())
        }

        val excludedTags = StashDBQueries.buildExcludedTags(excludeVR, excludeCompilations)

        // Build filter strings
        val performersFilter = if (performerIds.isNotEmpty()) {
            val ids = performerIds.joinToString(", ") { "\"$it\"" }
            "{ value: [$ids], modifier: INCLUDES }"
        } else {
            "null"
        }

        val studiosFilter = if (studioIds.isNotEmpty()) {
            val ids = studioIds.joinToString(", ") { "\"$it\"" }
            "{ value: [$ids], modifier: INCLUDES }"
        } else {
            "null"
        }

        // Date filter: past month
        val startDate = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val dateFilter = "{ value: \"$startDate\", modifier: GREATER_THAN }"

        // Tags filter
        val tagsFilter = if (excludedTags.isNotEmpty()) {
            val tags = excludedTags.joinToString(", ") { "\"$it\"" }
            "{ value: [$tags], modifier: EXCLUDES }"
        } else {
            "null"
        }

        // Fetch scenes by performers
        val performerScenes = if (performerIds.isNotEmpty()) {
            fetchScenesWithFilter(
                url = url,
                apiKey = apiKey,
                performersFilter = performersFilter,
                studiosFilter = "null",
                dateFilter = dateFilter,
                tagsFilter = tagsFilter
            )
        } else {
            emptyList()
        }

        // Fetch scenes by studios
        val studioScenes = if (studioIds.isNotEmpty()) {
            fetchScenesWithFilter(
                url = url,
                apiKey = apiKey,
                performersFilter = "null",
                studiosFilter = studiosFilter,
                dateFilter = dateFilter,
                tagsFilter = tagsFilter
            )
        } else {
            emptyList()
        }

        // Combine and deduplicate by scene ID
        val allScenes = (performerScenes + studioScenes)
            .distinctBy { it.id }
            .sortedByDescending { it.date }

        Timber.i("✅ Fetched ${allScenes.size} unique scenes (${performerScenes.size} from performers, ${studioScenes.size} from studios)")
        return StashDBScenesData(count = allScenes.size, scenes = allScenes)
    }

    private suspend fun fetchScenesWithFilter(
        url: String,
        apiKey: String,
        performersFilter: String,
        studiosFilter: String,
        dateFilter: String,
        tagsFilter: String
    ): List<StashDBScene> {
        val query = StashDBQueries.scenesWithFilters(
            performersFilter = performersFilter,
            studiosFilter = studiosFilter,
            dateFilter = dateFilter,
            tagsFilter = tagsFilter
        )

        val variables = mapOf<String, JsonElement>(
            "page" to JsonPrimitive(1),
            "perPage" to JsonPrimitive(100)
        )

        return try {
            val result = executor.execute<StashDBScenesQueryResult>(
                url = url,
                apiKey = apiKey,
                query = query,
                variables = variables
            )
            result.queryScenes.scenes
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to fetch scenes with filter")
            emptyList()
        }
    }
}
