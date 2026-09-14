package goonarr.stash.repositories.performers

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.sqlite.db.SimpleSQLiteQuery
import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.database.StashDatabase
import goonarr.stash.core.database.entity.toDomain
import goonarr.stash.core.database.entity.toEntity
import goonarr.stash.core.di.IoDispatcher
import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.scraper.ScrapedPerformer
import goonarr.stash.core.model.scraper.StashBox
import goonarr.stash.core.network.StashClient
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Repository for managing performer data.
 * Handles database operations via [performerDao], network operations via [client],
 * and user preferences via [settingsStore].
 */
@Singleton
class PerformerRepository @Inject constructor(
    private val db: StashDatabase,
    private val client: StashClient,
    private val settingsStore: SettingsStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private val performerDao = db.performerDao()

    val totalPerformersCount: StateFlow<Int> = performerDao.getCountFlow()
        .stateIn(
            scope = CoroutineScope(ioDispatcher),
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val detailCache = ConcurrentHashMap<String, Performer>()

    @OptIn(ExperimentalPagingApi::class)
    fun getPagedPerformers(
        sort: String = "name",
        direction: String = "ASC",
        searchQuery: String = ""
    ): Flow<PagingData<Performer>> {
        Timber.d("📂 PerformerRepository.getPagedPerformers called with sort=$sort, direction=$direction, search='$searchQuery'")
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = true,
                initialLoadSize = 20
            ),
            pagingSourceFactory = {
                val query = constructQuery(sort, direction, searchQuery)
                performerDao.getPagedPerformers(query)
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }.flowOn(ioDispatcher)
    }

    private fun constructQuery(
        sort: String,
        direction: String,
        searchQuery: String
    ): SimpleSQLiteQuery {
        val queryBuilder = StringBuilder("SELECT * FROM performers")
        val args = mutableListOf<Any>()
        val conditions = mutableListOf<String>()

        if (searchQuery.isNotEmpty()) {
            conditions.add("(name LIKE ? OR details LIKE ?)")
            args.add("%$searchQuery%")
            args.add("%$searchQuery%")
        }

        if (conditions.isNotEmpty()) {
            queryBuilder.append(" WHERE ${conditions.joinToString(" AND ")}")
        }

        // Mapping API sort keys to DB columns
        val sortColumn = when (sort) {
            "name" -> "name"
            "birthdate" -> "birthdate"
            "scene_count" -> "sceneCount"
            "o_counter" -> "oCounter"
            "rating" -> "rating100" // map rating to rating100
            "updated_at" -> "updatedAt"
            "created_at" -> "createdAt"
            else -> "name" // Default
        }

        queryBuilder.append(" ORDER BY $sortColumn $direction, id DESC")

        return SimpleSQLiteQuery(queryBuilder.toString(), args.toTypedArray())
    }

    fun getPerformer(id: String): Flow<Performer?> {
        // Return reactive flow from database
        // We can still use detailCache for initial non-db loading if strictly necessary,
        // but for reactivity we rely on the DB flow.
        return performerDao.getById(id).map { entity ->
            entity?.toDomain() ?: detailCache[id]
        }
    }

    suspend fun fetchPerformerFromApi(id: String): Performer? = withContext(ioDispatcher) {
        val url = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()
        client.findPerformer(url, apiKey, id)
    }

    suspend fun refreshPerformer(id: String) = withContext(ioDispatcher) {
        try {
            val performer = fetchPerformerFromApi(id)
            if (performer != null) {
                // Check if performer exists in the paginated database
                val existsInDb = performerDao.getSortOrder(id) != null

                if (existsInDb) {
                    // Performer is in paginated list, update it in database
                    Timber.d("💾 Updating performer $id in database (exists in paginated list)")
                    savePerformer(performer)
                } else {
                    // Performer is not in paginated list, cache it in memory only
                    Timber.d("📦 Caching performer $id in memory (detail view only)")
                    detailCache[id] = performer
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Error refreshing performer $id")
        }
    }

    suspend fun fetchPerformersForStudio(
        studioId: String,
        page: Int = 1,
        sort: String = "name",
        direction: String = "ASC"
    ): Pair<List<Performer>, Int> = withContext(ioDispatcher) {
        Timber.d("🌐 Fetching performers for studio $studioId (page $page, sort=$sort, dir=$direction)")
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()
        client.findPerformers(
            url = serverUrl,
            apiKey = apiKey,
            searchText = "",
            page = page,
            perPage = 40,
            sort = sort,
            direction = direction,
            studioId = studioId
        )
    }

    suspend fun fetchPerformersForTag(
        tagId: String,
        page: Int = 1,
        sort: String = "name",
        direction: String = "ASC"
    ): Pair<List<Performer>, Int> = withContext(ioDispatcher) {
        Timber.d("🌐 Fetching performers for tag $tagId (page $page, sort=$sort, dir=$direction)")
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()
        client.findPerformers(
            url = serverUrl,
            apiKey = apiKey,
            searchText = "",
            page = page,
            perPage = 40,
            sort = sort,
            direction = direction,
            tagId = tagId
        )
    }

    suspend fun savePerformer(performer: Performer) = withContext(ioDispatcher) {
        val existingEntity = performerDao.getById(performer.id).first()
        val existingDomain = existingEntity?.toDomain()

        if (existingDomain == performer) {
            Timber.d("⏭️ Skipping DB write for performer ${performer.id} (Data unchanged)")
            return@withContext
        }

        val existingSortOrder = existingEntity?.sortOrder ?: performerDao.getSortOrder(performer.id) ?: 0
        Timber.d(
            "💾 Writing performer ${performer.id} to DB. Name='${performer.name}', " +
                "Rating=${performer.rating100}, SortOrder=$existingSortOrder via REPLACE"
        )
        performerDao.insertAll(listOf(performer.toEntity(sortOrder = existingSortOrder)))
    }

    /**
     * Cache a performer from detail view.
     * Only saves to database if performer already exists (was synced from paginated list).
     * Otherwise, stores in memory-only cache to prevent appearing in performer list.
     */
    suspend fun cachePerformer(performer: Performer) = withContext(ioDispatcher) {
        val existsInDb = performerDao.getSortOrder(performer.id) != null

        if (existsInDb) {
            // Performer is in paginated list, update it in database
            Timber.d("💾 Updating performer ${performer.id} in database (exists in paginated list)")
            savePerformer(performer)
        } else {
            // Performer is not in paginated list, cache it in memory only
            Timber.d("📦 Caching performer ${performer.id} in memory (detail view only, won't appear in list)")
            detailCache[performer.id] = performer
        }
    }

    /**
     * Updates a performer.
     */
    suspend fun updatePerformer(
        id: String,
        name: String? = null,
        disambiguation: String? = null,
        url: String? = null,
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
    ): Performer? = withContext(ioDispatcher) {
        Timber.d("📝 Updating performer $id")
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()
        try {
            val updatedPerformer = client.updatePerformer(
                url = serverUrl,
                apiKey = apiKey,
                id = id,
                name = name,
                disambiguation = disambiguation,
                urlParam = url,
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
            if (updatedPerformer != null) {
                Timber.d("🔄 API returned updated performer: ${updatedPerformer.name}, rating=${updatedPerformer.rating100}")
                cachePerformer(updatedPerformer)
            } else {
                Timber.e("❌ API returned NULL for updated performer $id")
            }
            updatedPerformer
        } catch (e: Exception) {
            Timber.e(e, "❌ Error updating performer $id")
            null
        }
    }

    suspend fun deletePerformer(id: String): Boolean = withContext(ioDispatcher) {
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()

        // Remove from memory cache immediately if successful
        val result = client.performerDestroy(serverUrl, apiKey, id)
        if (result) {
            detailCache.remove(id)
            // We should also remove it from the DB if it exists there
            try {
                db.performerDao().deleteById(id)
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete performer $id from local DB")
            }
        }
        result
    }

    /**
     * Fetches the list of configured StashBox endpoints from the server.
     */
    suspend fun fetchStashBoxes(): List<StashBox> =
        withContext(ioDispatcher) {
            val serverUrl = settingsStore.serverUrl.first()
            val apiKey = settingsStore.apiKey.first()
            client.fetchStashBoxes(serverUrl, apiKey)
        }

    /**
     * Scrapes performer metadata from a specific StashBox.
     *
     * @param stashBox The StashBox to use for scraping.
     * @param query The search query (name or StashID).
     */
    suspend fun scrapePerformer(
        stashBox: StashBox,
        query: String
    ): List<ScrapedPerformer> = withContext(ioDispatcher) {
        val url = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()

        Timber.d("🔍 PerformerRepository: Scraping performer with query: $query using StashBox: ${stashBox.name}")

        client.scrapePerformer(
            url = url,
            apiKey = apiKey,
            stashBox = stashBox,
            query = query
        )
    }

    /**
     * Downloads an image from a URL and returns it as a Data URI string.
     */
    suspend fun downloadImageAsBase64(url: String): String? = withContext(ioDispatcher) {
        client.downloadImageAsBase64(url)
    }

    // MARK: - Sync Methods

    /**
     * Insert or update multiple performers in the local database.
     * Used by SyncRepository during synchronization.
     */
    suspend fun insertAll(performers: List<Performer>) = withContext(ioDispatcher) {
        performerDao.insertAll(performers.map { it.toEntity() })
    }

    /**
     * Delete performers by their IDs.
     * Used by SyncRepository when pruning deleted items.
     */
    suspend fun deleteByIds(ids: List<String>) = withContext(ioDispatcher) {
        performerDao.deleteByIds(ids)
    }

    /**
     * Get all performer IDs from the local database.
     * Used by SyncRepository for pruning deleted items.
     */
    suspend fun getAllIds(): List<String> = withContext(ioDispatcher) {
        performerDao.getAllIds()
    }

    /**
     * Get a limited list of performers for saved filters
     */
    suspend fun getTopPerformers(
        limit: Int = 20,
        sort: String = "name",
        direction: String = "ASC"
    ): List<Performer> = withContext(ioDispatcher) {
        // Build query with sort/direction from filter
        val query = constructQuery(sort, direction, "")
        Timber.d("📂 PerformerRepository.getTopPerformers: sort=$sort, direction=$direction")

        // Execute raw query to get entities
        val entities = performerDao.getAll().first()

        // Sort in memory based on the requested criteria
        val sorted = when (sort.lowercase()) {
            "name" -> if (direction == "DESC") entities.sortedByDescending { it.name } else entities.sortedBy { it.name }
            "scenes_count" -> if (direction == "DESC") {
                entities.sortedByDescending { it.sceneCount }
            } else {
                entities.sortedBy { it.sceneCount }
            }

            "height" -> if (direction == "DESC") {
                entities.sortedByDescending {
                    it.heightCm ?: 0
                }
            } else {
                entities.sortedBy { it.heightCm ?: 0 }
            }

            "birthdate" -> if (direction == "DESC") {
                entities.sortedByDescending {
                    it.birthdate
                }
            } else {
                entities.sortedBy { it.birthdate }
            }

            "rating" -> if (direction == "DESC") {
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
                entities // Default: use database order
            }
        }

        // Log sorted results
        Timber.d("📂 Sorted by $sort $direction, showing top $limit of ${sorted.size} total:")
        sorted.take(5).forEach { p ->
            Timber.d("  - ${p.name} (created: ${p.createdAt}, updated: ${p.updatedAt}, scenes: ${p.sceneCount})")
        }

        sorted.take(limit).map { it.toDomain() }
    }
}
