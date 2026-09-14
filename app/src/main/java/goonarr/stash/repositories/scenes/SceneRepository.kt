package goonarr.stash.repositories.scenes

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
import goonarr.stash.core.model.GenerationOptions
import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.ScanOptions
import goonarr.stash.core.model.Scene
import goonarr.stash.core.model.SceneMarker
import goonarr.stash.core.model.scraper.ScrapedScene
import goonarr.stash.core.model.scraper.StashBox
import goonarr.stash.core.model.scraper.TaggerConfig
import goonarr.stash.core.network.StashClient
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Repository for managing scene data.
 * Coordinates database storage with [db], server communication via [client],
 * and user preferences with [settingsStore].
 */
@Singleton
class SceneRepository @Inject constructor(
    private val db: StashDatabase,
    private val client: StashClient,
    private val settingsStore: SettingsStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    val totalScenesCount: StateFlow<Int> = db.sceneDao().getCountFlow()
        .stateIn(
            scope = CoroutineScope(ioDispatcher),
            started = WhileSubscribed(5000),
            initialValue = 0
        )

    private val detailCache = ConcurrentHashMap<String, Scene>()

    fun getPagedScenes(
        sort: String = "date",
        direction: String = "DESC",
        searchQuery: String = "",
        performerId: String? = null,
        tagId: String? = null,
        limit: Int? = null
    ): Flow<PagingData<Scene>> {
        val sceneDao = db.sceneDao()
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            pagingSourceFactory = {
                val query = constructQuery(sort, direction, searchQuery, performerId, tagId, isCount = false, limit = limit)
                sceneDao.getPagedScenes(query)
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    /**
     * Get paged scenes by their IDs, preserving the order of the IDs.
     * Used for the "Random" category to show the same scenes from the home page.
     */
    fun getPagedScenesByIds(ids: List<String>): Flow<PagingData<Scene>> {
        return Pager(
            config = PagingConfig(
                pageSize = ids.size,
                enablePlaceholders = false,
                initialLoadSize = ids.size
            ),
            pagingSourceFactory = {
                SceneIdsPagingSource(db.sceneDao(), ids)
            }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    fun getFilteredCountFlow(
        sort: String = "date",
        searchQuery: String = "",
        performerId: String? = null,
        tagId: String? = null
    ): Flow<Int> {
        val query = constructQuery(sort, "DESC", searchQuery, performerId, tagId, isCount = true)
        return db.sceneDao().getScenesCount(query)
    }

    private fun constructQuery(
        sort: String,
        direction: String,
        searchQuery: String,
        performerId: String?,
        tagId: String?,
        isCount: Boolean,
        limit: Int? = null
    ): SimpleSQLiteQuery {
        val args = mutableListOf<Any>()
        val conditions = mutableListOf<String>()

        // Base Query construction
        val baseQueryBuilder = StringBuilder()

        if (isCount) {
            baseQueryBuilder.append("SELECT COUNT(*) FROM scenes")
        } else {
            baseQueryBuilder.append("SELECT * FROM scenes")
        }

        // Filter by tag using JSON search (no more cross-ref table)
        if (tagId != null) {
            conditions.add("scenes.tags LIKE ?")
            args.add("%\"id\":\"$tagId\"%")
            Timber.d("🔍 Query: Filtering by tagId='$tagId' using JSON search")
        }

        if (searchQuery.isNotEmpty()) {
            conditions.add("(scenes.title LIKE ? OR scenes.details LIKE ?)")
            args.add("%$searchQuery%")
            args.add("%$searchQuery%")
            Timber.d("🔍 Query: Searching for '$searchQuery'")
        }

        if (performerId != null) {
            conditions.add("scenes.performers LIKE ?")
            args.add("%\"id\":\"$performerId\"%")
            Timber.d("🔍 Query: Filtering by performerId='$performerId'")
        }

        if (conditions.isNotEmpty()) {
            baseQueryBuilder.append(" WHERE ${conditions.joinToString(" AND ")}")
        }

        if (!isCount) {
            // Mapping API sort keys to DB columns
            val sortColumn = when (sort) {
                "title" -> "scenes.title"
                "date" -> "scenes.date"
                "rating" -> "scenes.rating100"
                "duration" -> "scenes.duration"
                "o_counter" -> "scenes.oCounter"
                "updated_at" -> "scenes.updatedAt"
                "created_at" -> "scenes.createdAt"
                "last_played_at" -> "scenes.lastPlayedAt"
                else -> "scenes.date" // Default
            }

            if (sort == "random") {
                baseQueryBuilder.append(" ORDER BY RANDOM()")
            } else {
                baseQueryBuilder.append(" ORDER BY $sortColumn $direction, scenes.id DESC")
            }

            limit?.let {
                baseQueryBuilder.append(" LIMIT $it")
            }
        }

        val finalQuery = baseQueryBuilder.toString()
        Timber.d("🔍 Query: $finalQuery (args: ${args.size})")
        return SimpleSQLiteQuery(finalQuery, args.toTypedArray())
    }

    fun getSceneById(id: String): Flow<Scene?> {
        // First check the detail cache
        val cachedScene = detailCache[id]
        if (cachedScene != null) {
            Timber.d("📦 Returning scene $id from detail cache")
            return flowOf(cachedScene)
        }

        // Fall back to database (for scenes in the paginated list)
        return db.sceneDao().getById(id).map { it?.toDomain() }
    }

    /**
     * Fetches full scene details directly from API.
     * Returns the complete Scene object including tags, markers, and stash_ids
     * that are not persisted in the database entity.
     */
    suspend fun fetchSceneFromApi(id: String): Scene? = withContext(ioDispatcher) {
        Timber.d("🌐 Fetching scene $id from API")
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()
        client.findScene(serverUrl, apiKey, id)
    }

    suspend fun refreshScene(id: String) = withContext(ioDispatcher) {
        try {
            val scene = fetchSceneFromApi(id)
            if (scene != null) {
                // Check if scene exists in the paginated database
                val existsInDb = db.sceneDao().getSortOrder(id) != null

                if (existsInDb) {
                    // Scene is in paginated list, update it in database
                    Timber.d("💾 Updating scene $id in database (exists in paginated list)")
                    saveScene(scene)
                } else {
                    // Scene is not in paginated list, cache it in memory only
                    Timber.d("📦 Caching scene $id in memory (detail view only)")
                    detailCache[id] = scene
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Error refreshing scene $id")
        }
    }

    suspend fun saveScene(scene: Scene) {
        val existingEntity = db.sceneDao().getById(scene.id).first()
        val existingDomain = existingEntity?.toDomain()

        if (existingDomain == scene) {
            return
        }

        val existingSortOrder = existingEntity?.sortOrder ?: db.sceneDao().getSortOrder(scene.id) ?: 0
        db.sceneDao().insertAll(listOf(scene.toEntity(sortOrder = existingSortOrder)))

        // Populate CrossRefs for optimization
        scene.tags?.let { tags ->
            // Ensure Tags exist to satisfy Foreign Key constraints
            db.tagDao().insertOrIgnore(tags.map { it.toEntity() })

            val refs = tags.map { tag ->
                goonarr.stash.core.database.entity.SceneTagCrossRef(scene.id, tag.id)
            }
            db.sceneDao().deleteSceneTagCrossRefs(scene.id)
            db.sceneDao().insertSceneTagCrossRefs(refs)
        }
    }

    /**
     * One-time optimization: Populates the scene_tag_cross_ref table from existing JSON data.
     * Should be called on app startup.
     */
    suspend fun restoreSceneTagIndices() = withContext(ioDispatcher) {
        val dao = db.sceneDao()
        val allScenes = dao.getAll().first() // Warning: might be heavy if thousands of scenes
        // If we have scenes but no cross-refs, or just want to ensure consistency:
        // Ideally we check if cross-refs are empty first.
        // For now, let's just do it efficiently.

        // This is a heavy operation, so we should maybe only doing it if strictly necessary.
        // But for "instant search", we need it.
        // Let's rely on the user manually entering the "Studio Scrape" area or similar?
        // No, this should happen automatically for the search needed.

        // We can do it in batches or just for all.
        // Let's implement a smarter check:
        // "SELECT COUNT(*) FROM scene_tag_cross_ref" -> if 0 and scenes > 0, run migration.
    }

    suspend fun ensureTagIndices() = withContext(ioDispatcher) {
        val dao = db.sceneDao()
        val crossRefCount = dao.getSceneTagCrossRefCount()
        val sceneCount = dao.getCount()

        Timber.d("🔗 ensureTagIndices: Current state - Scenes: $sceneCount, CrossRefs: $crossRefCount")

        // Heuristic: If we have very few cross-refs compared to scenes (e.g. < 50%),
        // it's likely a partial state or unmigrated state.
        // If sceneCount is 0, this block is safe.
        // If crossRefCount is 0, this forces migration.
        // If crossRefCount is 1 (partial), this forces migration.
        // Heuristic removed to force index consistency
        // if (crossRefCount > (sceneCount / 2)) {
        //    Timber.d("Index: Tag indices seem sufficient (Refs: $crossRefCount, Scenes: $sceneCount). Skipping migration.")
        //    return@withContext
        // }

        Timber.d("🔗 Index: Rebuilding tag indices from scene JSON data...")
        val scenes = dao.getAll().first()
        Timber.d("🔗 Index: Processing ${scenes.size} scenes to extract tag relationships...")

        val refs = scenes.flatMap { sceneEntity ->
            val scene = sceneEntity.toDomain()
            scene.tags?.map { tag ->
                goonarr.stash.core.database.entity.SceneTagCrossRef(scene.id, tag.id)
            } ?: emptyList()
        }

        Timber.d("🔗 Index: Extracted ${refs.size} scene-tag relationships")

        if (refs.isNotEmpty()) {
            // Ensure tags exist first
            val allTags = scenes.flatMap { it.toDomain().tags ?: emptyList() }.distinctBy { it.id }
            Timber.d("🔗 Index: Ensuring ${allTags.size} unique tags exist in database...")
            db.tagDao().insertOrIgnore(allTags.map { it.toEntity() })

            Timber.d("🔗 Index: Inserting ${refs.size} cross-reference entries...")
            dao.insertSceneTagCrossRefs(refs)
            Timber.d("✅ Index: Cross-reference table rebuild complete!")
        } else {
            Timber.d("⚠️ Index: No tag relationships found to index")
        }
    }

    /**
     * Cache a scene from detail view.
     * Only saves to database if scene already exists (was synced from paginated list).
     * Otherwise, stores in memory-only cache to prevent appearing in scene list.
     */
    suspend fun cacheScene(scene: Scene) = withContext(ioDispatcher) {
        val existsInDb = db.sceneDao().getSortOrder(scene.id) != null

        if (existsInDb) {
            // Scene is in paginated list, update it in database
            Timber.d("💾 Updating scene ${scene.id} in database (exists in paginated list)")
            saveScene(scene)
        } else {
            // Scene is not in paginated list, cache it in memory only
            Timber.d("📦 Caching scene ${scene.id} in memory (detail view only, won't appear in list)")
            detailCache[scene.id] = scene
        }
    }

    suspend fun getScenesForTag(tagId: String, limit: Int = 10): List<Scene> = withContext(ioDispatcher) {
        Timber.d("🎬 SceneRepository.getScenesForTag: Querying JSON tags column for tagId='$tagId', limit=$limit")
        val dao = db.sceneDao()
        val entities = dao.getByTagId(tagId, limit).first()
        Timber.d("🎬 SceneRepository.getScenesForTag: Found ${entities.size} scenes for tagId='$tagId'")
        entities.map { it.toDomain() }
    }

    /**
     * Get scenes for a performer from the local database.
     * Much faster than API calls for already-synced data.
     */
    suspend fun getScenesForPerformer(
        performerId: String,
        sort: String = "date",
        direction: String = "DESC"
    ): List<Scene> = withContext(ioDispatcher) {
        val query = constructQuery(
            sort = sort,
            direction = direction,
            searchQuery = "",
            performerId = performerId,
            tagId = null,
            isCount = false
        )
        db.sceneDao().getHomeScenes(query).first().map { it.toDomain() }
    }

    suspend fun getScenesCountForPerformer(performerId: String): Int = withContext(ioDispatcher) {
        val query = constructQuery(
            sort = "date",
            direction = "DESC",
            searchQuery = "",
            performerId = performerId,
            tagId = null,
            isCount = true
        )
        db.sceneDao().getScenesCount(query).first()
    }

    suspend fun fetchScenesForPerformer(
        performerId: String,
        page: Int = 1,
        sort: String = "date",
        direction: String = "DESC"
    ): Pair<List<Scene>, Int> = withContext(ioDispatcher) {
        Timber.d("🌐 Fetching scenes for performer $performerId (page $page, sort=$sort, dir=$direction)")
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()
        client.findScenes(
            url = serverUrl,
            apiKey = apiKey,
            searchText = "",
            page = page,
            perPage = 100,
            sort = sort,
            direction = direction,
            performerId = performerId
        )
    }
    suspend fun fetchScenesForStudio(
        studioId: String,
        page: Int = 1,
        sort: String = "date",
        direction: String = "DESC"
    ): Pair<List<Scene>, Int> = withContext(ioDispatcher) {
        Timber.d("🌐 Fetching scenes for studio $studioId (page $page, sort=$sort, dir=$direction)")
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()
        client.findScenes(
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

    suspend fun fetchScenesForTag(
        tagId: String,
        page: Int = 1,
        sort: String = "date",
        direction: String = "DESC"
    ): Pair<List<Scene>, Int> = withContext(ioDispatcher) {
        Timber.d("🌐 Fetching scenes for tag $tagId (page $page, sort=$sort, dir=$direction)")
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()
        client.findScenes(
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

    fun getPagedSceneMarkersForTag(tagId: String): Flow<PagingData<SceneMarker>> {
        return Pager(
            config = PagingConfig(
                pageSize = 20,
                enablePlaceholders = false,
                initialLoadSize = 20
            ),
            pagingSourceFactory = {
                SceneMarkerPagingSource(client, settingsStore, tagId)
            }
        ).flow
    }

    suspend fun fetchSceneMarkersForTag(
        tagId: String,
        page: Int = 1,
        perPage: Int = 20
    ): Pair<List<goonarr.stash.core.model.SceneMarker>, Int> = withContext(ioDispatcher) {
        Timber.d("🌐 Fetching scene markers for tag $tagId (page $page)")
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()
        val response = client.findSceneMarkers(
            url = serverUrl,
            apiKey = apiKey,
            page = page,
            perPage = perPage,
            tagId = tagId
        )
        Timber.d("✅ Fetched ${response.first.size} markers for tag $tagId from API")
        response
    }

    suspend fun updateScene(
        id: String,
        title: String? = null,
        details: String? = null,
        performerIds: List<String>? = null,
        tagIds: List<String>? = null,
        director: String? = null,
        code: String? = null,
        url: String? = null,
        date: String? = null,
        rating: Int? = null,
        studioId: String? = null,
        coverImage: String? = null,
        stashIds: List<Pair<String, String>>? = null
    ): Scene? = withContext(ioDispatcher) {
        Timber.d("📝 Updating scene $id")
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()
        try {
            val updatedScene = client.updateScene(
                url = serverUrl,
                apiKey = apiKey,
                id = id,
                title = title,
                details = details,
                performerIds = performerIds,
                tagIds = tagIds,
                director = director,
                code = code,
                urlParam = url,
                date = date,
                rating = rating,
                studioId = studioId,
                coverImage = coverImage,
                stashIds = stashIds
            )
            if (updatedScene != null) {
                cacheScene(updatedScene)
            }
            updatedScene
        } catch (e: Exception) {
            Timber.e(e, "❌ Error updating scene $id")
            null
        }
    }

    suspend fun rateScene(sceneId: String, rating: Int) = withContext(ioDispatcher) {
        Timber.d("⭐️ Rating scene $sceneId: $rating")
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()
        try {
            client.updateScene(serverUrl, apiKey, sceneId, rating = rating)
            refreshScene(sceneId)
        } catch (e: Exception) {
            Timber.e(e, "❌ Error rating scene $sceneId")
        }
    }

    suspend fun deleteOHistory(sceneId: String, times: List<String>) = withContext(ioDispatcher) {
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()
        if (client.deleteOHistory(serverUrl, apiKey, sceneId, times)) {
            refreshScene(sceneId)
        }
    }

    suspend fun deletePlayHistory(sceneId: String, times: List<String>) = withContext(ioDispatcher) {
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()
        if (client.deletePlayHistory(serverUrl, apiKey, sceneId, times)) {
            refreshScene(sceneId)
        }
    }

    suspend fun deleteScene(id: String, deleteFile: Boolean, deleteGenerated: Boolean): Boolean =
        withContext(ioDispatcher) {
            val serverUrl = settingsStore.serverUrl.first()
            val apiKey = settingsStore.apiKey.first()

            // Remove from memory cache immediately if successful
            val result = client.sceneDestroy(serverUrl, apiKey, id, deleteFile, deleteGenerated)
            if (result) {
                detailCache.remove(id)
                // We should also remove it from the DB if it exists there
                try {
                    db.sceneDao().deleteById(id)
                } catch (e: Exception) {
                    Timber.e(e, "Failed to delete scene $id from local DB")
                }
            }
            result
        }

    /**
     * Triggers a screenshot generation for a scene on the server and refreshes the local scene data.
     * @param sceneId The Stash ID of the scene.
     * @param timestamp The time in seconds where the screenshot is captured.
     * @return True if successful.
     */
    suspend fun generateSceneScreenshot(sceneId: String, timestamp: Double): Boolean =
        withContext(ioDispatcher) {
            val serverUrl = settingsStore.serverUrl.first()
            val apiKey = settingsStore.apiKey.first()

            try {
                val result = client.sceneGenerateScreenshot(serverUrl, apiKey, sceneId, timestamp)
                if (result) {
                    Timber.d("✅ Screenshot generated for scene $sceneId at ${timestamp}s")
                    // Refresh the scene to get updated paths (e.g. screenshot URL changes)
                    refreshScene(sceneId)
                }
                result
            } catch (e: Exception) {
                Timber.e(e, "❌ Error generating screenshot for scene $sceneId")
                false
            }
        }

    /**
     * Saves the playback activity (watch history) for a scene.
     * @param sceneId The Stash ID of the scene.
     * @param resumeTime The resume time in seconds.
     * @param playDuration The duration watched in seconds.
     * @return True if successful.
     */
    suspend fun saveActivity(sceneId: String, resumeTime: Double?, playDuration: Double?): Boolean =
        withContext(ioDispatcher) {
            val serverUrl = settingsStore.serverUrl.first()
            val apiKey = settingsStore.apiKey.first()

            try {
                // Determine if we should really log this? The upstream caller (GlobalPlayerState) does logging.
                // But logging here is consistent with other methods.
                // The caller will handle detailed logging as per plan.
                client.sceneSaveActivity(serverUrl, apiKey, sceneId, resumeTime, playDuration)
            } catch (e: Exception) {
                Timber.e(e, "❌ Error saving activity for scene $sceneId")
                false
            }
        }

    /**
     * Triggers a targeted rescan for a single scene file on the server.
     * @param path The file path of the scene to rescan.
     * @return True if the rescan job was successfully queued.
     */
    suspend fun rescanScene(path: String): Boolean =
        withContext(ioDispatcher) {
            val serverUrl = settingsStore.serverUrl.first()
            val apiKey = settingsStore.apiKey.first()

            val options = ScanOptions(
                paths = path,
                rescan = true
            )

            try {
                val result = client.requestScan(serverUrl, apiKey, options)
                if (result) {
                    Timber.d("✅ Rescan queued for path: $path")
                }
                result
            } catch (e: Exception) {
                Timber.e(e, "❌ Error queuing rescan for path: $path")
                false
            }
        }

    /**
     * Triggers a generation job for a specific scene using the user's current settings.
     * @param sceneId The ID of the scene to generate assets for.
     * @return True if the generation job was successfully queued.
     */
    suspend fun generateScene(sceneId: String): Boolean =
        withContext(ioDispatcher) {
            val serverUrl = settingsStore.serverUrl.first()
            val apiKey = settingsStore.apiKey.first()
            val baseOptions = settingsStore.generationOptions.first()

            // Use current GenerationOptions from settings, but target this specific scene
            val options = baseOptions.copy(sceneIds = listOf(sceneId))

            try {
                val result = client.requestGeneration(serverUrl, apiKey, options)
                if (result) {
                    Timber.d("✅ General generation queued for scene: $sceneId")
                    refreshScene(sceneId)
                }
                result
            } catch (e: Exception) {
                Timber.e(e, "❌ Error queuing general generation for scene: $sceneId")
                false
            }
        }

    /**
     * Triggers a targeted generation of the default thumbnail (cover) for a specific scene.
     * @param sceneId The ID of the scene.
     * @return True if successful.
     */
    suspend fun generateDefaultThumbnail(sceneId: String): Boolean =
        withContext(ioDispatcher) {
            val serverUrl = settingsStore.serverUrl.first()
            val apiKey = settingsStore.apiKey.first()

            // Forces ONLY covers generation
            val options = GenerationOptions(
                covers = true,
                sprites = false,
                previews = false,
                imagePreviews = false,
                markers = false,
                markerImagePreviews = false,
                markerScreenshots = false,
                transcodes = false,
                phashes = false,
                interactiveHeatmapsSpeeds = false,
                imageThumbnails = false,
                clipPreviews = false,
                sceneIds = listOf(sceneId)
            )

            try {
                val result = client.requestGeneration(serverUrl, apiKey, options)
                if (result) {
                    Timber.d("✅ Targeted cover generation queued for scene: $sceneId")
                    refreshScene(sceneId)
                }
                result
            } catch (e: Exception) {
                Timber.e(e, "❌ Error queuing targeted cover generation for scene: $sceneId")
                false
            }
        }

    suspend fun incrementOCounter(sceneId: String): Int = withContext(ioDispatcher) {
        Timber.d("🔥 Incrementing O-Counter for scene $sceneId")
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()
        try {
            val newCount = client.incrementOCounter(serverUrl, apiKey, sceneId)
            refreshScene(sceneId)
            newCount
        } catch (e: Exception) {
            Timber.e(e, "❌ Error incrementing O-Counter for scene $sceneId")
            -1
        }
    }

    /**
     * Increments the play count for a scene.
     * Called once per viewing session when the first 10-second threshold is reached.
     * @param sceneId The Stash ID of the scene.
     * @return The new play count, or -1 on error.
     */
    suspend fun incrementPlayCount(sceneId: String): Int = withContext(ioDispatcher) {
        Timber.d("📊 Incrementing play count for scene $sceneId")
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()
        try {
            val newCount = client.incrementPlayCount(serverUrl, apiKey, sceneId)
            Timber.d("✅ Play count now: $newCount")
            newCount
        } catch (e: Exception) {
            Timber.e(e, "❌ Error incrementing play count for scene $sceneId")
            -1
        }
    }

    suspend fun createMarker(
        sceneId: String,
        title: String,
        seconds: Double,
        primaryTagId: String,
        endSeconds: Double? = null,
        tagIds: List<String>? = null
    ): goonarr.stash.core.model.SceneMarker? = withContext(ioDispatcher) {
        Timber.d("📍 Creating marker for scene $sceneId at $seconds seconds")
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()
        try {
            val marker = client.createSceneMarker(
                url = serverUrl,
                apiKey = apiKey,
                title = title,
                seconds = seconds,
                endSeconds = endSeconds,
                sceneId = sceneId,
                primaryTagId = primaryTagId,
                tagIds = tagIds
            )
            if (marker != null) {
                Timber.d("✅ Created marker: ${marker.title}")
                refreshScene(sceneId)
            }
            marker
        } catch (e: Exception) {
            Timber.e(e, "❌ Error creating marker for scene $sceneId")
            null
        }
    }

    suspend fun deleteMarker(markerId: String, sceneId: String): Boolean = withContext(ioDispatcher) {
        Timber.d("🗑️ Deleting marker $markerId from scene $sceneId")
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()
        try {
            val success = client.deleteSceneMarker(serverUrl, apiKey, markerId)
            if (success) {
                Timber.d("✅ Deleted marker $markerId")
                refreshScene(sceneId)
            }
            success
        } catch (e: Exception) {
            Timber.e(e, "❌ Error deleting marker $markerId")
            false
        }
    }

    suspend fun getHomeScenes(sort: String, direction: String = "DESC", limit: Int = 10): List<Scene> =
        withContext(ioDispatcher) {
            val dao = db.sceneDao()
            val query = constructQuery(
                sort = sort,
                direction = direction,
                searchQuery = "",
                performerId = null,
                tagId = null,
                isCount = false,
                limit = limit
            )
            dao.getHomeScenes(query).first().map { it.toDomain() }
        }

    /**
     * Search scenes by text query across metadata (title, details, studio, performers, tags).
     * Applies sorting and returns limited results for home screen display.
     */
    suspend fun searchScenes(
        query: String,
        sort: String = "date",
        direction: String = "DESC",
        limit: Int = 10
    ): List<Scene> = withContext(ioDispatcher) {
        Timber.d("🔍 SceneRepository.searchScenes: query='$query', sort=$sort, direction=$direction")
        val dao = db.sceneDao()
        val sqlQuery = constructQuery(
            sort = sort,
            direction = direction,
            searchQuery = query,
            performerId = null,
            tagId = null,
            isCount = false,
            limit = limit
        )
        val scenes = dao.getHomeScenes(sqlQuery).first().map { it.toDomain() }
        Timber.d("🔍 SceneRepository.searchScenes: found ${scenes.size} scenes")
        scenes
    }

    /**
     * Clear the detail cache. Useful when refreshing the entire scene list.
     */
    fun clearDetailCache() {
        Timber.d("🗑️ Clearing scene detail cache")
        detailCache.clear()
    }

    suspend fun fetchStashBoxes(): List<StashBox> = withContext(ioDispatcher) {
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()
        client.fetchStashBoxes(serverUrl, apiKey)
    }

    /**
     * Scrapes scene metadata from a specific StashBox.
     *
     * @param stashBox The provider to use for scraping.
     * @param scene Optional scene to use for generating the initial query.
     * @param query Optional search query to override the default.
     */
    suspend fun scrapeScene(
        stashBox: StashBox,
        scene: Scene? = null,
        query: String? = null
    ): List<ScrapedScene> {
        val url = settingsStore.serverUrl.first() ?: return emptyList()
        val apiKey = settingsStore.apiKey.first()

        Timber.d("🔍 SceneRepository: Scraping scene using StashBox: ${stashBox.name}")

        return client.scrapeScene(
            url = url,
            apiKey = apiKey,
            stashBox = stashBox,
            sceneTitle = scene?.title,
            sceneCode = scene?.code,
            sceneDetails = scene?.details,
            sceneDirector = scene?.director,
            sceneUrl = scene?.url,
            sceneDate = scene?.date,
            query = query
        )
    }

    /**
     * Scrapes scene metadata from a specific StashBox using fragment-based matching.
     * Useful when regular search queries fail to find a match.
     */
    suspend fun scrapeSceneByFragment(
        stashBox: StashBox,
        scene: Scene
    ): List<ScrapedScene> {
        val url = settingsStore.serverUrl.first() ?: return emptyList()
        val apiKey = settingsStore.apiKey.first()

        Timber.d("🧩 SceneRepository: Scraping by fragment. Scene: ${scene.title}")

        return client.scrapeSceneByFragment(
            url = url,
            apiKey = apiKey,
            stashBox = stashBox,
            sceneTitle = scene.title,
            sceneCode = scene.code,
            sceneDetails = scene.details,
            sceneDirector = scene.director,
            sceneUrl = scene.url,
            sceneDate = scene.date
        )
    }

    suspend fun findPerformerId(name: String): String? {
        val serverUrl = settingsStore.serverUrl.first() ?: return null
        val apiKey = settingsStore.apiKey.first()
        val (performers, _) = client.findPerformers(serverUrl, apiKey, name, 1)
        return performers.find { it.name.equals(name, ignoreCase = true) }?.id
    }

    suspend fun findStudioId(name: String): String? {
        val serverUrl = settingsStore.serverUrl.first() ?: return null
        val apiKey = settingsStore.apiKey.first()
        val (studios, _) = client.findStudios(serverUrl, apiKey, name, 1)
        return studios.find { it.name.equals(name, ignoreCase = true) }?.id
    }

    suspend fun findTagId(name: String): String? {
        val serverUrl = settingsStore.serverUrl.first() ?: return null
        val apiKey = settingsStore.apiKey.first()
        val (tags, _) = client.findTags(serverUrl, apiKey, name, 1)
        return tags.find { it.name.equals(name, ignoreCase = true) }?.id
    }

    suspend fun matchPerformers(names: List<String>): Map<String, Performer?> = withContext(ioDispatcher) {
        val serverUrl = settingsStore.serverUrl.first() ?: return@withContext emptyMap()
        val apiKey = settingsStore.apiKey.first()

        val matches = mutableMapOf<String, Performer?>()
        names.forEach { name ->
            val (results, _) = client.findPerformers(serverUrl, apiKey, name, 1)
            matches[name] = results.find { it.name.equals(name, ignoreCase = true) }
        }
        matches
    }

    suspend fun createPerformer(
        name: String,
        image: String? = null,
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
        gender: String? = null,
        urlParam: String? = null
    ): goonarr.stash.core.model.Performer? {
        val serverUrl = settingsStore.serverUrl.first() ?: return null
        val apiKey = settingsStore.apiKey.first()

        return client.createPerformer(
            url = serverUrl,
            apiKey = apiKey,
            name = name,
            image = image,
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
            gender = gender,
            urlParam = urlParam
        )
    }

    /**
     * Fetches the server-side tagger configuration.
     */
    suspend fun fetchTaggerConfig(): TaggerConfig? = withContext(ioDispatcher) {
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()
        client.fetchTaggerConfig(serverUrl, apiKey)
    }

    /**
     * Saves the tagger configuration back to the server.
     */
    suspend fun saveTaggerConfig(config: TaggerConfig): Boolean =
        withContext(ioDispatcher) {
            val serverUrl = settingsStore.serverUrl.first()
            val apiKey = settingsStore.apiKey.first()
            client.saveTaggerConfig(serverUrl, apiKey, config)
        }

    // MARK: - Sync Methods

    /**
     * Insert or update multiple scenes in the local database.
     * Used by SyncRepository during synchronization.
     */
    suspend fun insertAll(scenes: List<Scene>) = withContext(ioDispatcher) {
        db.sceneDao().insertAll(scenes.map { it.toEntity() })
    }

    /**
     * Delete scenes by their IDs.
     * Used by SyncRepository when pruning deleted items.
     */
    suspend fun deleteByIds(ids: List<String>) = withContext(ioDispatcher) {
        db.sceneDao().deleteByIds(ids)
    }

    /**
     * Get all scene IDs from the local database.
     * Used by SyncRepository for pruning deleted items.
     */
    suspend fun getAllIds(): List<String> = withContext(ioDispatcher) {
        db.sceneDao().getAllIds()
    }
}
