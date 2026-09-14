package goonarr.stash.features.sync

import goonarr.stash.core.database.dao.SyncMetadataDao
import goonarr.stash.core.database.entity.SyncMetadataEntity
import goonarr.stash.core.network.StashClient
import goonarr.stash.repositories.SavedFilterRepository
import goonarr.stash.repositories.StudioRepository
import goonarr.stash.repositories.TagRepository
import goonarr.stash.repositories.performers.PerformerRepository
import goonarr.stash.repositories.scenes.SceneRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

/**
 * Repository responsible for synchronizing data between the Stash server and local database.
 * Manages incremental syncing of studios, tags, performers, and scenes based on their updated_at timestamps.
 * Also handles pruning of locally cached items that have been deleted on the server.
 *
 * This repository coordinates the sync process by delegating to domain repositories
 * (SceneRepository, PerformerRepository, etc.) rather than accessing DAOs directly.
 */
@Singleton
class SyncRepository @Inject constructor(
    private val stashClient: StashClient,
    private val syncMetadataDao: SyncMetadataDao,
    private val sceneRepository: SceneRepository,
    private val performerRepository: PerformerRepository,
    private val studioRepository: StudioRepository,
    private val tagRepository: TagRepository,
    private val savedFilterRepository: SavedFilterRepository
) {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncProgress = MutableStateFlow(0f)
    val syncProgress: StateFlow<Float> = _syncProgress.asStateFlow()

    private val _syncUiState = MutableStateFlow(SyncUiState())
    val syncUiState: StateFlow<SyncUiState> = _syncUiState.asStateFlow()

    suspend fun hasCompletedInitialSync(): Boolean {
        return syncMetadataDao.getSyncedCount() >= 4 // We sync 4 categories: studios, tags, performers, scenes
    }

    suspend fun syncAll(url: String, apiKey: String?) {
        if (_isSyncing.value) return
        _isSyncing.value = true
        _syncProgress.value = 0f

        try {
            Timber.d("🔄 Starting full sync...")
            _syncUiState.update { it.copy(isInitialSync = true) }
            syncStudios(url, apiKey)
            _syncProgress.value = 0.20f
            syncTags(url, apiKey)
            _syncProgress.value = 0.40f
            syncPerformers(url, apiKey)
            _syncProgress.value = 0.60f
            syncScenes(url, apiKey)
            _syncProgress.value = 0.75f

            // Sync saved filters (if enabled)
            savedFilterRepository.syncFilters()
            _syncProgress.value = 0.85f

            // Prune deleted items after sync
            pruneDeletedItems(url, apiKey)
            _syncProgress.value = 1.0f
            Timber.d("✅ Full sync completed successfully.")
        } catch (e: Exception) {
            Timber.e(e, "❌ Error during sync")
        } finally {
            _isSyncing.value = false
            _syncUiState.update { it.copy(isInitialSync = false) }
        }
    }

    private suspend fun syncScenes(url: String, apiKey: String?) {
        val lastSync = syncMetadataDao.getLastUpdatedAt("scenes")
        Timber.d("Syncing Scenes (lastSync: $lastSync)")

        var page = 1
        val perPage = 100
        var hasMore = true
        var latestUpdate: String? = lastSync
        var totalSynced = 0

        // Get initial count
        val (firstBatch, totalCount) = stashClient.findScenes(
            url = url,
            apiKey = apiKey,
            searchText = "",
            page = 1,
            perPage = perPage,
            sort = "updated_at",
            direction = "ASC",
            updatedAt = lastSync
        )
        _syncUiState.update { it.copy(scenes = SyncCategoryProgress(total = totalCount)) }

        // Process first batch
        if (firstBatch.isNotEmpty()) {
            sceneRepository.insertAll(firstBatch)
            totalSynced += firstBatch.size
            _syncUiState.update { it.copy(scenes = it.scenes.copy(synced = totalSynced)) }
            firstBatch.lastOrNull()?.updatedAt?.let { latestUpdate = it }
            Timber.d("✅ Synced ${firstBatch.size} scenes (Page 1)")
            page = 2
        } else {
            hasMore = false
        }

        while (hasMore) {
            val (scenes, count) = stashClient.findScenes(
                url = url,
                apiKey = apiKey,
                searchText = "",
                page = page,
                perPage = perPage,
                sort = "updated_at",
                direction = "ASC",
                updatedAt = lastSync
            )

            if (scenes.isNotEmpty()) {
                sceneRepository.insertAll(scenes)
                totalSynced += scenes.size
                _syncUiState.update { it.copy(scenes = it.scenes.copy(synced = totalSynced)) }
                scenes.lastOrNull()?.updatedAt?.let { latestUpdate = it }
                Timber.d("✅ Synced ${scenes.size} scenes (Page $page)")
                page++
            } else {
                hasMore = false
            }

            if (page * perPage > count + perPage) hasMore = false
        }

        _syncUiState.update { it.copy(scenes = it.scenes.copy(isComplete = true)) }
        latestUpdate?.let {
            syncMetadataDao.updateLastSync(SyncMetadataEntity("scenes", it))
        }
    }

    private suspend fun syncPerformers(url: String, apiKey: String?) {
        val lastSync = syncMetadataDao.getLastUpdatedAt("performers")
        Timber.d("Syncing Performers (lastSync: $lastSync)")

        var page = 1
        val perPage = 20
        var hasMore = true
        var latestUpdate: String? = lastSync
        var totalSynced = 0

        // Get initial count
        val (firstBatch, totalCount) = stashClient.findPerformers(
            url = url,
            apiKey = apiKey,
            searchText = "",
            page = 1,
            sort = "updated_at",
            direction = "ASC",
            updatedAt = lastSync
        )
        _syncUiState.update { it.copy(performers = SyncCategoryProgress(total = totalCount)) }

        if (firstBatch.isNotEmpty()) {
            performerRepository.insertAll(firstBatch)
            totalSynced += firstBatch.size
            _syncUiState.update { it.copy(performers = it.performers.copy(synced = totalSynced)) }
            firstBatch.lastOrNull()?.updatedAt?.let { latestUpdate = it }
            Timber.d("✅ Synced ${firstBatch.size} performers (Page 1)")
            page = 2
        } else {
            hasMore = false
        }

        while (hasMore) {
            val (performers, count) = stashClient.findPerformers(
                url = url,
                apiKey = apiKey,
                searchText = "",
                page = page,
                sort = "updated_at",
                direction = "ASC",
                updatedAt = lastSync
            )

            if (performers.isNotEmpty()) {
                performerRepository.insertAll(performers)
                totalSynced += performers.size
                _syncUiState.update { it.copy(performers = it.performers.copy(synced = totalSynced)) }
                performers.lastOrNull()?.updatedAt?.let { latestUpdate = it }
                Timber.d("✅ Synced ${performers.size} performers (Page $page)")
                page++
            } else {
                hasMore = false
            }
            if (page * perPage > count + perPage) hasMore = false
        }

        _syncUiState.update { it.copy(performers = it.performers.copy(isComplete = true)) }
        latestUpdate?.let {
            syncMetadataDao.updateLastSync(SyncMetadataEntity("performers", it))
        }
    }

    private suspend fun syncTags(url: String, apiKey: String?) {
        val lastSync = syncMetadataDao.getLastUpdatedAt("tags")
        Timber.d("Syncing Tags (lastSync: $lastSync)")

        var page = 1
        val perPage = 100
        var hasMore = true
        var latestUpdate: String? = lastSync
        var totalSynced = 0

        // Get initial count
        val (firstBatch, totalCount) = stashClient.findTags(
            url = url,
            apiKey = apiKey,
            searchText = "",
            page = 1,
            updatedAt = lastSync
        )
        _syncUiState.update { it.copy(tags = SyncCategoryProgress(total = totalCount)) }

        if (firstBatch.isNotEmpty()) {
            tagRepository.insertAll(firstBatch)
            totalSynced += firstBatch.size
            _syncUiState.update { it.copy(tags = it.tags.copy(synced = totalSynced)) }
            firstBatch.maxOfOrNull { it.updatedAt ?: "" }?.let {
                if (latestUpdate == null || it > latestUpdate!!) latestUpdate = it
            }
            Timber.d("✅ Synced ${firstBatch.size} tags (Page 1)")
            page = 2
        } else {
            hasMore = false
        }

        while (hasMore) {
            val (tags, count) = stashClient.findTags(
                url = url,
                apiKey = apiKey,
                searchText = "",
                page = page,
                updatedAt = lastSync
            )

            if (tags.isNotEmpty()) {
                tagRepository.insertAll(tags)
                totalSynced += tags.size
                _syncUiState.update { it.copy(tags = it.tags.copy(synced = totalSynced)) }
                tags.maxOfOrNull { it.updatedAt ?: "" }?.let {
                    if (latestUpdate == null || it > latestUpdate!!) latestUpdate = it
                }
                Timber.d("✅ Synced ${tags.size} tags (Page $page)")
                page++
            } else {
                hasMore = false
            }
            if (page * perPage > count + perPage) hasMore = false
        }

        _syncUiState.update { it.copy(tags = it.tags.copy(isComplete = true)) }
        latestUpdate?.let {
            syncMetadataDao.updateLastSync(SyncMetadataEntity("tags", it))
        }
    }

    private suspend fun syncStudios(url: String, apiKey: String?) {
        val lastSync = syncMetadataDao.getLastUpdatedAt("studios")
        Timber.d("Syncing Studios (lastSync: $lastSync)")

        var page = 1
        val perPage = 20
        var hasMore = true
        var latestUpdate: String? = lastSync
        var totalSynced = 0

        // Get initial count
        val (firstBatch, totalCount) = stashClient.findStudios(
            url = url,
            apiKey = apiKey,
            searchText = "",
            page = 1,
            updatedAt = lastSync
        )
        _syncUiState.update { it.copy(studios = SyncCategoryProgress(total = totalCount)) }

        if (firstBatch.isNotEmpty()) {
            studioRepository.insertAll(firstBatch)
            totalSynced += firstBatch.size
            _syncUiState.update { it.copy(studios = it.studios.copy(synced = totalSynced)) }
            firstBatch.maxOfOrNull { it.updatedAt ?: "" }?.let {
                if (latestUpdate == null || it > latestUpdate!!) latestUpdate = it
            }
            Timber.d("✅ Synced ${firstBatch.size} studios (Page 1)")
            page = 2
        } else {
            hasMore = false
        }

        while (hasMore) {
            val (studios, count) = stashClient.findStudios(
                url = url,
                apiKey = apiKey,
                searchText = "",
                page = page,
                updatedAt = lastSync
            )

            if (studios.isNotEmpty()) {
                studioRepository.insertAll(studios)
                totalSynced += studios.size
                _syncUiState.update { it.copy(studios = it.studios.copy(synced = totalSynced)) }
                studios.maxOfOrNull { it.updatedAt ?: "" }?.let {
                    if (latestUpdate == null || it > latestUpdate!!) latestUpdate = it
                }
                Timber.d("✅ Synced ${studios.size} studios (Page $page)")
                page++
            } else {
                hasMore = false
            }
            if (page * perPage > count + perPage) hasMore = false
        }

        _syncUiState.update { it.copy(studios = it.studios.copy(isComplete = true)) }
        latestUpdate?.let {
            syncMetadataDao.updateLastSync(SyncMetadataEntity("studios", it))
        }
    }

    /**
     * Prunes items from the local database that no longer exist on the server.
     * This handles the case where items are deleted on the server between syncs.
     */
    private suspend fun pruneDeletedItems(url: String, apiKey: String?) {
        Timber.d("🧹 Starting pruning of deleted items...")

        try {
            // Prune Scenes
            val serverSceneIds = stashClient.fetchAllSceneIds(url, apiKey).toSet()
            if (serverSceneIds.isNotEmpty()) {
                val localSceneIds = sceneRepository.getAllIds()
                val deletedSceneIds = localSceneIds.filter { it !in serverSceneIds }
                if (deletedSceneIds.isNotEmpty()) {
                    Timber.d("🗑️ Pruning ${deletedSceneIds.size} deleted scenes: $deletedSceneIds")
                    sceneRepository.deleteByIds(deletedSceneIds)
                } else {
                    Timber.d("✅ No deleted scenes to prune")
                }
            }

            // Prune Performers
            val serverPerformerIds = stashClient.fetchAllPerformerIds(url, apiKey).toSet()
            if (serverPerformerIds.isNotEmpty()) {
                val localPerformerIds = performerRepository.getAllIds()
                val deletedPerformerIds = localPerformerIds.filter { it !in serverPerformerIds }
                if (deletedPerformerIds.isNotEmpty()) {
                    Timber.d("🗑️ Pruning ${deletedPerformerIds.size} deleted performers: $deletedPerformerIds")
                    performerRepository.deleteByIds(deletedPerformerIds)
                } else {
                    Timber.d("✅ No deleted performers to prune")
                }
            }

            // Prune Tags
            val serverTagIds = stashClient.fetchAllTagIds(url, apiKey).toSet()
            if (serverTagIds.isNotEmpty()) {
                val localTagIds = tagRepository.getAllIds()
                val deletedTagIds = localTagIds.filter { it !in serverTagIds }
                if (deletedTagIds.isNotEmpty()) {
                    Timber.d("🗑️ Pruning ${deletedTagIds.size} deleted tags: $deletedTagIds")
                    tagRepository.deleteByIds(deletedTagIds)
                } else {
                    Timber.d("✅ No deleted tags to prune")
                }
            }

            // Prune Studios
            val serverStudioIds = stashClient.fetchAllStudioIds(url, apiKey).toSet()
            if (serverStudioIds.isNotEmpty()) {
                val localStudioIds = studioRepository.getAllIds()
                val deletedStudioIds = localStudioIds.filter { it !in serverStudioIds }
                if (deletedStudioIds.isNotEmpty()) {
                    Timber.d("🗑️ Pruning ${deletedStudioIds.size} deleted studios: $deletedStudioIds")
                    studioRepository.deleteByIds(deletedStudioIds)
                } else {
                    Timber.d("✅ No deleted studios to prune")
                }
            }

            Timber.d("🧹 Pruning completed successfully")
        } catch (e: Exception) {
            Timber.e(e, "❌ Error during pruning")
        }
    }
}
