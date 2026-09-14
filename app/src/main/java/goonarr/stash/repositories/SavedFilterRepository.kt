package goonarr.stash.repositories

import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.database.StashDatabase
import goonarr.stash.core.database.entity.toDomain
import goonarr.stash.core.di.IoDispatcher
import goonarr.stash.core.model.FilterCriterion
import goonarr.stash.core.model.FilterMode
import goonarr.stash.core.model.FilterModifier
import goonarr.stash.core.model.FindFilter
import goonarr.stash.core.model.SavedFilter
import goonarr.stash.core.network.SavedFilterDto
import goonarr.stash.core.network.StashClient
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import timber.log.Timber

/**
 * Repository for managing saved filters.
 * Handles syncing from server and local caching.
 */
@Singleton
class SavedFilterRepository @Inject constructor(
    private val db: StashDatabase,
    private val client: StashClient,
    private val settingsStore: SettingsStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private val savedFilterDao = db.savedFilterDao()
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Get all saved filters from local cache.
     */
    fun getAllFilters(): Flow<List<SavedFilter>> {
        return savedFilterDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Get saved filters by mode from local cache.
     */
    fun getFiltersByMode(mode: FilterMode): Flow<List<SavedFilter>> {
        return savedFilterDao.getByMode(mode).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /**
     * Get a single saved filter by ID.
     */
    fun getFilter(id: String): Flow<SavedFilter?> {
        return savedFilterDao.getById(id).map { it?.toDomain() }
    }

    /**
     * Fetch saved filters from the server and cache locally.
     * Only fetches if sync is enabled in settings.
     */
    suspend fun syncFilters(force: Boolean = false): Boolean = withContext(ioDispatcher) {
        val syncEnabled = settingsStore.syncSavedFilters.first()
        if (!syncEnabled && !force) {
            Timber.d("🔧 Saved filter sync is disabled, skipping")
            return@withContext false
        }

        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()

        try {
            Timber.d("🔄 Syncing saved filters from server...")
            val filtersDto = client.findSavedFilters(serverUrl, apiKey, mode = null)
            Timber.d("📦 Retrieved ${filtersDto.size} saved filters from server")

            val entities = filtersDto.mapIndexed { index, dto ->
                dto.toEntity(sortOrder = index)
            }

            savedFilterDao.insertAll(entities)
            Timber.d("✅ Saved ${entities.size} filters to local database")
            true
        } catch (e: Exception) {
            Timber.e(e, "❌ Error syncing saved filters")
            false
        }
    }

    /**
     * Extract entity IDs from a saved filter's object_filter.
     * Returns maps of tags, performers, studios, and scene IDs.
     */
    fun extractFilterIds(filter: SavedFilter): FilterIds {
        val objectFilter = filter.objectFilter

        if (objectFilter == null) {
            Timber.d("🔍 Filter '${filter.name}' has no objectFilter")
            return FilterIds()
        }

        Timber.d("🔍 Filter '${filter.name}' objectFilter keys: ${objectFilter.keys}")
        objectFilter.forEach { (key, criterion) ->
            Timber.d("  - $key: value=${criterion.value?.take(3)}, modifier=${criterion.modifier}")
        }

        val tagIds = objectFilter["tags"]?.value ?: emptyList()
        val performerIds = objectFilter["performers"]?.value ?: emptyList()
        val studioIds = objectFilter["studios"]?.value ?: emptyList()

        Timber.d(
            "🔍 Extracted IDs from filter '${filter.name}': tags=${tagIds.size}, " +
                "performers=${performerIds.size}, studios=${studioIds.size}"
        )

        return FilterIds(
            tagIds = tagIds,
            performerIds = performerIds,
            studioIds = studioIds
        )
    }
}

/**
 * Helper data class for extracted filter IDs.
 */
data class FilterIds(
    val tagIds: List<String> = emptyList(),
    val performerIds: List<String> = emptyList(),
    val studioIds: List<String> = emptyList(),
    val sceneIds: List<String> = emptyList()
)

/**
 * Convert SavedFilterDto to entity for database storage.
 */
private fun SavedFilterDto.toEntity(sortOrder: Int = 0): goonarr.stash.core.database.entity.SavedFilterEntity {
    val json = Json { ignoreUnknownKeys = true }

    // Parse FilterMode from string
    val filterMode = try {
        FilterMode.valueOf(mode)
    } catch (e: Exception) {
        Timber.w("⚠️ Unknown filter mode: $mode, defaulting to SCENES")
        FilterMode.SCENES
    }

    // Parse FindFilter
    val parsedFindFilter = find_filter?.let {
        FindFilter(
            q = it.q,
            page = it.page,
            perPage = it.per_page,
            sort = it.sort,
            direction = it.direction
        )
    }

    // Parse object_filter JSON to Map<String, FilterCriterion>
    // Following the guide's approach: parse manually instead of auto-deserializing
    val parsedObjectFilter = object_filter?.let { jsonElement ->
        try {
            Timber.d("🔍 Raw object_filter JSON for '$name': $jsonElement")

            // Parse as generic map first
            val rawMap = json.decodeFromJsonElement<Map<String, JsonElement>>(jsonElement)
            Timber.d("🔍 Found ${rawMap.keys.size} criteria keys: ${rawMap.keys}")

            // Manually parse each criterion
            val criteriaMap = mutableMapOf<String, FilterCriterion>()
            rawMap.forEach { (key, criterionJson) ->
                try {
                    val criterionObj = criterionJson.jsonObject

                    // Extract value array - handle multiple formats
                    val valueList = criterionObj["value"]?.let { valueElement ->
                        when {
                            // Format 1: Simple array ["174", "175"]
                            valueElement is JsonArray -> {
                                valueElement.mapNotNull { elem ->
                                    when {
                                        elem is JsonPrimitive && elem.isString -> elem.content
                                        elem is JsonPrimitive -> elem.content // Numbers as strings
                                        else -> null
                                    }
                                }
                            }
                            // Format 2: Nested object with items array
                            valueElement.jsonObject.containsKey("items") -> {
                                val items = valueElement.jsonObject["items"]
                                if (items is JsonArray) {
                                    items.mapNotNull { item ->
                                        // Extract ID from {"id": "174", "label": "Redhead"}
                                        (item as? JsonObject)?.get("id")?.let {
                                            (it as? JsonPrimitive)?.content
                                        }
                                    }
                                } else {
                                    emptyList()
                                }
                            }
                            else -> null
                        }
                    }

                    // Extract modifier
                    val modifierStr = criterionObj["modifier"]?.let { modElement ->
                        (modElement as? JsonPrimitive)?.content
                    }

                    if (valueList != null && modifierStr != null) {
                        val modifier = try {
                            FilterModifier.valueOf(modifierStr)
                        } catch (e: Exception) {
                            FilterModifier.INCLUDES // Default
                        }

                        criteriaMap[key] = FilterCriterion(
                            value = valueList,
                            modifier = modifier
                        )
                        Timber.d("  ✅ $key: value=${valueList.take(3)}, modifier=$modifier")
                    } else {
                        Timber.w("  ⚠️ $key: missing value or modifier")
                    }
                } catch (e: Exception) {
                    Timber.w(e, "  ⚠️ Failed to parse criterion '$key'")
                }
            }

            Timber.d("🔍 Successfully parsed ${criteriaMap.size} criteria for '$name'")
            criteriaMap
        } catch (e: Exception) {
            // Skip complex/unsupported filter types (only simple tag filters supported in Phase 1)
            Timber.w("⚠️ Skipping unsupported object_filter for '$name'")
            null
        }
    }

    // Parse ui_options
    val parsedUiOptions = ui_options?.let { jsonElement ->
        try {
            json.decodeFromJsonElement<Map<String, String>>(jsonElement)
        } catch (e: Exception) {
            null
        }
    }

    return goonarr.stash.core.database.entity.SavedFilterEntity(
        id = id,
        mode = filterMode,
        name = name,
        findFilter = parsedFindFilter,
        objectFilter = parsedObjectFilter,
        uiOptions = parsedUiOptions,
        sortOrder = sortOrder
    )
}
