package goonarr.stash.repositories.stashdb

import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.database.StashDatabase
import goonarr.stash.core.model.StashDBScene
import goonarr.stash.core.network.stashdb.StashDBClient
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import timber.log.Timber

/**
 * Repository coordinating StashDB operations.
 *
 * Handles fetching favorite scenes from StashDB, filtering out owned scenes,
 * and caching results for quick display on app restart.
 */
@Singleton
class StashDBRepository @Inject constructor(
    private val client: StashDBClient,
    private val settingsStore: SettingsStore,
    private val db: StashDatabase
) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Tests the StashDB connection with current settings.
     */
    suspend fun testConnection(): Boolean {
        val url = settingsStore.stashDBUrl.first()
        val apiKey = settingsStore.stashDBApiKey.first()
        if (apiKey.isEmpty()) return false
        return client.testConnection(url, apiKey)
    }

    /**
     * Fetches favorite scenes from StashDB.
     *
     * Flow:
     * 1. Fetch favorite performers and studios in parallel
     * 2. Fetch scenes featuring those favorites from last month
     * 3. Filter out scenes already in local library (by stash_id)
     * 4. Apply VR/compilation filters based on settings
     *
     * @return List of favorite scenes not in local library, or null if no API key configured
     */
    suspend fun fetchFavoriteScenesFromStashDB(): List<StashDBScene>? = coroutineScope {
        val url = settingsStore.stashDBUrl.first()
        val apiKey = settingsStore.stashDBApiKey.first()
        val excludeVR = settingsStore.excludeVRFromStashDB.first()
        val excludeCompilations = settingsStore.excludeCompilationsFromStashDB.first()

        if (apiKey.isEmpty()) {
            Timber.i("⏭️ No StashDB API key configured, skipping favorites fetch")
            return@coroutineScope null
        }

        Timber.i("🌟 Fetching favorite performers and studios from StashDB")

        // Fetch favorites and owned scene IDs in parallel
        val performersDeferred = async { client.fetchFavoritePerformers(url, apiKey) }
        val studiosDeferred = async { client.fetchFavoriteStudios(url, apiKey) }
        val ownedIdsDeferred = async { fetchLocalSceneStashDBIds() }

        val performers = performersDeferred.await()
        val studios = studiosDeferred.await()
        val ownedIds = ownedIdsDeferred.await()

        val performerIds = performers.map { it.id }
        val studioIds = studios.map { it.id }

        Timber.i("✅ Found ${performerIds.size} favorite performers")
        Timber.i("✅ Found ${studioIds.size} favorite studios")

        if (performerIds.isEmpty() && studioIds.isEmpty()) {
            Timber.i("⏭️ No favorite performers or studios found")
            return@coroutineScope null
        }

        // Fetch scenes from those favorites
        val scenesData = client.fetchScenesByPerformersAndStudios(
            url = url,
            apiKey = apiKey,
            performerIds = performerIds,
            studioIds = studioIds,
            excludeVR = excludeVR,
            excludeCompilations = excludeCompilations
        )

        Timber.i("✅ Fetched ${scenesData.scenes.size} scenes from favorites")

        // Filter out scenes already in local library
        val beforeFilter = scenesData.scenes.size
        val scenes = scenesData.scenes.filter { scene -> scene.id !in ownedIds }
        val ownedCount = beforeFilter - scenes.size
        Timber.i("🏠 Filtered out $ownedCount scenes already in library, ${scenes.size} remaining")

        if (scenes.isEmpty()) {
            return@coroutineScope null
        }

        scenes
    }

    /**
     * Fetches all StashDB IDs for scenes in the local library.
     * Reads from database entities where stashIds JSON contains stashdb.org endpoint.
     */
    private suspend fun fetchLocalSceneStashDBIds(): Set<String> {
        return try {
            // Get all StashIDs directly from the DB column to avoid loading full entities
            val allStashIds = db.sceneDao().getAllStashIds()
            val stashDBIds = allStashIds
                .flatMap { item ->
                    item.stashIds?.filter { stashId ->
                        stashId.endpoint.contains("stashdb", ignoreCase = true)
                    }?.map { it.stashId } ?: emptyList()
                }
                .toSet()
            Timber.d("📚 Found ${stashDBIds.size} local scenes with StashDB IDs")
            stashDBIds
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to fetch local scene stash IDs")
            emptySet()
        }
    }

    /**
     * Retrieves cached favorite scenes from SettingsStore.
     */
    suspend fun getCachedFavoriteScenes(): List<StashDBScene>? {
        return try {
            val jsonString = settingsStore.stashDBFavoritesCache.first()
            if (jsonString.isEmpty()) {
                return null
            }
            json.decodeFromString<List<StashDBScene>>(jsonString)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to load StashDB favorites cache")
            null
        }
    }

    /**
     * Caches favorite scenes for next app launch (using SettingsStore).
     */
    suspend fun cacheFavoriteScenes(scenes: List<StashDBScene>) {
        try {
            val jsonString = json.encodeToString(
                kotlinx.serialization.serializer<List<StashDBScene>>(),
                scenes
            )
            settingsStore.setStashDBFavoritesCache(jsonString)
            Timber.i("💾 Cached ${scenes.size} StashDB favorite scenes")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to save StashDB favorites cache")
        }
    }
}
