package goonarr.stash.repositories

import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.database.StashDatabase
import goonarr.stash.core.database.entity.toDomain
import goonarr.stash.core.database.entity.toEntity
import goonarr.stash.core.model.Tag
import goonarr.stash.core.network.StashClient
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Repository for managing tag data.
 * Handles local caching and followed tags via [settingsStore].
 */
@Singleton
class TagRepository @Inject constructor(
    private val db: StashDatabase,
    private val client: StashClient,
    private val settingsStore: SettingsStore,
    private val ioDispatcher: CoroutineDispatcher
) {
    private val tagDao = db.tagDao()

    /**
     * Fetches a single tag by ID from the local cache.
     */
    fun getTag(id: String): Flow<Tag?> {
        return tagDao.getById(id).map { it?.toDomain() }
    }

    /**
     * Fetches all tags from the API.
     * Used by the tag list screen which loads all tags at once for local search.
     */
    suspend fun getAllTags(): List<Tag> {
        Timber.d("🏷️ Fetching all tags from local DB")
        return tagDao.getAll().first().map { it.toDomain() }
    }

    /**
     * Get tags by IDs from local cache.
     * Used for displaying followed tags.
     */
    fun getTags(ids: List<String>): Flow<List<Tag>> {
        Timber.d("🏷️ TagRepository.getTags called with ${ids.size} IDs: $ids")
        if (ids.isEmpty()) {
            Timber.d("🏷️ TagRepository.getTags returning empty list (no IDs)")
            return kotlinx.coroutines.flow.flowOf(emptyList())
        }
        return tagDao.loadByIds(ids).map { entities ->
            Timber.d("🏷️ TagRepository.getTags: DAO returned ${entities.size} entities")
            entities.forEachIndexed { index, entity ->
                Timber.d("🏷️ TagRepository.getTags [$index]: '${entity.name}' (id=${entity.id})")
            }
            entities.map { it.toDomain() }
        }
    }

    suspend fun addFollowedTag(id: String) {
        settingsStore.addFollowedTag(id)
    }

    suspend fun removeFollowedTag(id: String) {
        settingsStore.removeFollowedTag(id)
    }

    val followedTagIds: Flow<List<String>> = settingsStore.followedTagIds

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
     * Fetches a single tag by ID from the network.
     */
    suspend fun fetchTag(id: String): Tag? = withContext(ioDispatcher) {
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()

        try {
            val result = client.findTagsByIds(serverUrl, apiKey, listOf(id)).firstOrNull()
            if (result != null) {
                tagDao.insertAll(listOf(result.toEntity()))
                result.copy(imagePath = resolvePath(serverUrl, result.imagePath))
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Error fetching tag $id")
            null
        }
    }

    /**
     * Searches for tags from the network.
     */
    suspend fun searchTags(query: String): List<Tag> = withContext(ioDispatcher) {
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()

        try {
            val result = client.findTags(
                url = serverUrl,
                apiKey = apiKey,
                searchText = query,
                page = 1,
                updatedAt = null
            )
            result.first.map { tag ->
                tag.copy(imagePath = resolvePath(serverUrl, tag.imagePath))
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Error searching tags")
            emptyList()
        }
    }

    /**
     * Updates a tag.
     */
    suspend fun updateTag(
        id: String,
        name: String? = null,
        aliases: List<String>? = null,
        description: String? = null,
        favorite: Boolean? = null,
        ignoreAutoTag: Boolean? = null,
        parentIds: List<String>? = null,
        childIds: List<String>? = null,
        image: String? = null,
        stashIds: List<Pair<String, String>>? = null
    ): Tag? = withContext(ioDispatcher) {
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()

        try {
            val result = client.updateTag(
                url = serverUrl,
                apiKey = apiKey,
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
            if (result != null) {
                tagDao.insertAll(listOf(result.toEntity()))
                result.copy(imagePath = resolvePath(serverUrl, result.imagePath))
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Error updating tag $id")
            null
        }
    }

    /**
     * Deletes a tag from the server and local cache.
     */
    suspend fun deleteTag(id: String): Boolean = withContext(ioDispatcher) {
        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()

        try {
            val success = client.tagDestroy(serverUrl, apiKey, id)
            if (success) {
                tagDao.deleteById(id)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Error deleting tag $id")
            false
        }
    }

    // MARK: - Sync Methods

    /**
     * Insert or update multiple tags in the local database.
     * Used by SyncRepository during synchronization.
     */
    suspend fun insertAll(tags: List<Tag>) = withContext(ioDispatcher) {
        tagDao.insertAll(tags.map { it.toEntity() })
    }

    /**
     * Delete tags by their IDs.
     * Used by SyncRepository when pruning deleted items.
     */
    suspend fun deleteByIds(ids: List<String>) = withContext(ioDispatcher) {
        tagDao.deleteByIds(ids)
    }

    /**
     * Get all tag IDs from the local database.
     * Used by SyncRepository for pruning deleted items.
     */
    suspend fun getAllIds(): List<String> = withContext(ioDispatcher) {
        tagDao.getAllIds()
    }

    /**
     * Get a limited list of tags for saved filters
     */
    suspend fun getTopTags(
        limit: Int = 20,
        sort: String = "name",
        direction: String = "ASC"
    ): List<Tag> {
        val tags = getAllTags()

        // Sort based on criteria
        val sorted = when (sort.lowercase()) {
            "name" -> if (direction == "DESC") tags.sortedByDescending { it.name } else tags.sortedBy { it.name }
            "scene_count", "scenes_count" -> if (direction == "DESC") {
                tags.sortedByDescending {
                    it.sceneCount
                }
            } else {
                tags.sortedBy { it.sceneCount }
            }
            "created_at" -> if (direction == "DESC") {
                tags.sortedByDescending { it.createdAt }
            } else {
                tags.sortedBy { it.createdAt }
            }
            "updated_at" -> if (direction == "DESC") {
                tags.sortedByDescending { it.updatedAt }
            } else {
                tags.sortedBy { it.updatedAt }
            }
            else -> {
                Timber.w("📂 Unknown sort field '$sort', using database order")
                tags
            }
        }

        Timber.d("📂 TagRepository: Sorted by $sort $direction, top 5:")
        sorted.take(5).forEach { t ->
            Timber.d("  - ${t.name} (created: ${t.createdAt}, count: ${t.sceneCount})")
        }

        return sorted.take(limit)
    }
}
