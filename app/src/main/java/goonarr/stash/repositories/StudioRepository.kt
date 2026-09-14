package goonarr.stash.repositories

import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.database.dao.StudioDao
import goonarr.stash.core.database.entity.toDomain
import goonarr.stash.core.database.entity.toEntity
import goonarr.stash.core.model.Studio
import goonarr.stash.core.model.scraper.ScrapedStudio
import goonarr.stash.core.model.scraper.StashBox
import goonarr.stash.core.network.StashClient
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber

// ... (other imports)

@Singleton
class StudioRepository @Inject constructor(
    private val studioDao: StudioDao,
    private val client: StashClient,
    private val settingsStore: SettingsStore,
    private val ioDispatcher: CoroutineDispatcher
) {
    /**
     * Helper to resolve relative paths to absolute URLs.
     */
    private fun resolvePath(baseUrl: String, path: String?): String? {
        if (path.isNullOrBlank()) return null
        if (path.startsWith("http")) return path
        val cleanBase = baseUrl.removeSuffix("/")
        val cleanPath = if (path.startsWith("/")) path else "/$path"
        return "$cleanBase$cleanPath"
    }

    /**
     * Gets a live stream of all studios from the local database.
     */
    fun getStudios(): Flow<List<Studio>> {
        return studioDao.getAll().combine(settingsStore.serverUrl) { entities, serverUrl ->
            entities.map { entity ->
                val studio = entity.toDomain()
                studio.copy(imagePath = resolvePath(serverUrl, studio.imagePath))
            }
        }
    }

    /**
     * Gets a live stream of a single studio by ID from the local database.
     *
     * @param id The studio ID.
     */
    fun getStudio(id: String): Flow<Studio?> {
        return studioDao.getById(id).combine(settingsStore.serverUrl) { entity, serverUrl ->
            entity?.toDomain()?.let { studio ->
                studio.copy(imagePath = resolvePath(serverUrl, studio.imagePath))
            }
        }
    }

    /**
     * Fetches studios from the network with pagination.
     *
     * @param searchText Search query
     * @param page Page number (1-based)
     * @return Pair of (studios list, total count)
     */
    suspend fun fetchStudios(
        searchText: String = "",
        page: Int = 1,
        sort: String = "name",
        direction: String = "ASC"
    ): Pair<List<Studio>, Int> = withContext(ioDispatcher) {
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()

        try {
            val result = client.findStudios(
                url = serverUrl,
                apiKey = apiKey,
                searchText = searchText,
                page = page,
                sort = sort,
                direction = direction
            )
            Timber.d("📡 Fetched ${result.first.size} studios (page $page, total ${result.second}, sort $sort $direction)")

            // Resolve URLs
            val resolvedStudios = result.first.map { studio ->
                studio.copy(imagePath = resolvePath(serverUrl, studio.imagePath))
            }
            Pair(resolvedStudios, result.second)
        } catch (e: Exception) {
            Timber.e(e, "❌ Error fetching studios")
            Pair(emptyList(), 0)
        }
    }

    suspend fun fetchStudiosForTag(
        tagId: String,
        page: Int = 1,
        sort: String = "name",
        direction: String = "ASC"
    ): Pair<List<Studio>, Int> = withContext(ioDispatcher) {
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()

        try {
            val result = client.findStudios(
                url = serverUrl,
                apiKey = apiKey,
                searchText = "",
                page = page,
                sort = sort,
                direction = direction,
                tagId = tagId
            )
            Timber.d("📡 Fetched ${result.first.size} studios for tag $tagId (page $page, total ${result.second})")

            // Resolve URLs
            val resolvedStudios = result.first.map { studio ->
                studio.copy(imagePath = resolvePath(serverUrl, studio.imagePath))
            }
            Pair(resolvedStudios, result.second)
        } catch (e: Exception) {
            Timber.e(e, "❌ Error fetching studios for tag $tagId")
            Pair(emptyList(), 0)
        }
    }

    /**
     * Fetches a single studio by ID from the network.
     */
    suspend fun fetchStudio(id: String): Studio? = withContext(ioDispatcher) {
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()

        try {
            val result = client.findStudio(serverUrl, apiKey, id)
            if (result != null) {
                studioDao.insertAll(listOf(result.toEntity()))
                result.copy(imagePath = resolvePath(serverUrl, result.imagePath))
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Error fetching studio $id")
            null
        }
    }

    /**
     * Updates a studio with the given fields.
     *
     * @return The updated Studio on success, null on failure.
     */
    suspend fun updateStudio(
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
    ): Studio? = withContext(ioDispatcher) {
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()

        try {
            val result = client.updateStudio(
                url = serverUrl,
                apiKey = apiKey,
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
            if (result != null) {
                studioDao.insertAll(listOf(result.toEntity()))
                result.copy(imagePath = resolvePath(serverUrl, result.imagePath))
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Error updating studio $id")
            null
        }
    }

    /**
     * Deletes a studio.
     *
     * @return True on success, false on failure.
     */
    suspend fun deleteStudio(id: String): Boolean = withContext(ioDispatcher) {
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()

        try {
            client.studioDestroy(serverUrl, apiKey, id)
        } catch (e: Exception) {
            Timber.e(e, "❌ Error deleting studio $id")
            false
        }
    }

    /**
     * Scrapes studio metadata from a specific StashBox.
     *
     * @param stashBox The StashBox to use for scraping.
     * @param query The search query.
     */
    suspend fun scrapeStudio(
        stashBox: StashBox,
        query: String
    ): List<ScrapedStudio> = withContext(ioDispatcher) {
        val url = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()

        Timber.d("🔍 StudioRepository: Scraping studio with query: $query using StashBox: ${stashBox.name}")

        client.scrapeStudio(
            url = url,
            apiKey = apiKey,
            stashBox = stashBox,
            query = query
        )
    }

    // MARK: - Sync Methods

    /**
     * Insert or update multiple studios in the local database.
     * Used by SyncRepository during synchronization.
     */
    suspend fun insertAll(studios: List<Studio>) = withContext(ioDispatcher) {
        studioDao.insertAll(studios.map { it.toEntity() })
    }

    /**
     * Delete studios by their IDs.
     * Used by SyncRepository when pruning deleted items.
     */
    suspend fun deleteByIds(ids: List<String>) = withContext(ioDispatcher) {
        studioDao.deleteByIds(ids)
    }

    /**
     * Get all studio IDs from the local database.
     * Used by SyncRepository for pruning deleted items.
     */
    suspend fun getAllIds(): List<String> = withContext(ioDispatcher) {
        studioDao.getAllIds()
    }

    /**
     * Get a limited list of studios for saved filters
     */
    suspend fun getTopStudios(
        limit: Int = 20,
        sort: String = "name",
        direction: String = "ASC"
    ): List<Studio> = withContext(ioDispatcher) {
        val entities = studioDao.getAll().first()

        // Sort in memory based on criteria
        val sorted = when (sort.lowercase()) {
            "name" -> if (direction == "DESC") entities.sortedByDescending { it.name } else entities.sortedBy { it.name }
            "scenes_count", "scene_count" -> if (direction == "DESC") {
                entities.sortedByDescending {
                    it.sceneCount ?: 0
                }
            } else {
                entities.sortedBy { it.sceneCount ?: 0 }
            }
            "rating", "rating100" -> if (direction == "DESC") {
                entities.sortedByDescending {
                    it.rating100 ?: 0
                }
            } else {
                entities.sortedBy { it.rating100 ?: 0 }
            }
            "created_at" -> if (direction == "DESC") {
                entities.sortedByDescending { it.createdAt }
            } else {
                entities.sortedBy { it.createdAt }
            }
            "updated_at" -> if (direction == "DESC") {
                entities.sortedByDescending { it.updatedAt }
            } else {
                entities.sortedBy { it.updatedAt }
            }
            else -> {
                Timber.w("📂 Unknown sort field '$sort', using database order")
                entities
            }
        }

        Timber.d("📂 StudioRepository: Sorted by $sort $direction, top 5:")
        sorted.take(5).forEach { s ->
            Timber.d("  - ${s.name} (created: ${s.createdAt}, scenes: ${s.sceneCount})")
        }

        sorted.take(limit).map { it.toDomain() }
    }
}
