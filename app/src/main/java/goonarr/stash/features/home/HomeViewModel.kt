package goonarr.stash.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.di.IoDispatcher
import goonarr.stash.core.model.FilterMode
import goonarr.stash.core.model.HomeSectionIds
import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.Scene
import goonarr.stash.core.model.StashDBScene
import goonarr.stash.core.model.Studio
import goonarr.stash.core.model.Tag
import goonarr.stash.features.sync.SyncRepository
import goonarr.stash.repositories.SavedFilterRepository
import goonarr.stash.repositories.StudioRepository
import goonarr.stash.repositories.TagRepository
import goonarr.stash.repositories.performers.PerformerRepository
import goonarr.stash.repositories.scenes.SceneRepository
import goonarr.stash.repositories.stashdb.StashDBRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Represents a section on the home screen.
 * Can display scenes, performers, studios, or tags.
 */
sealed class HomeSectionData {
    abstract val id: String
    abstract val title: String
    abstract val count: Int?

    data class SceneSection(
        override val id: String,
        override val title: String,
        val scenes: List<Scene>,
        override val count: Int? = null,
        val sort: String? = null,
        val direction: String? = "DESC"
    ) : HomeSectionData()

    data class PerformerSection(
        override val id: String,
        override val title: String,
        val performers: List<Performer>,
        override val count: Int? = null
    ) : HomeSectionData()

    data class StudioSection(
        override val id: String,
        override val title: String,
        val studios: List<Studio>,
        override val count: Int? = null
    ) : HomeSectionData()

    data class TagSection(
        override val id: String,
        override val title: String,
        val tags: List<Tag>,
        override val count: Int? = null
    ) : HomeSectionData()

    data class StashDBSceneSection(
        override val id: String,
        override val title: String,
        val scenes: List<StashDBScene>,
        override val count: Int? = null
    ) : HomeSectionData()

    // Loading state for StashDB section - shows shimmer while data loads in background
    data class StashDBLoadingSection(
        override val id: String,
        override val title: String,
        override val count: Int? = null
    ) : HomeSectionData()
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val sections: List<HomeSectionData>
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sceneRepository: SceneRepository,
    private val performerRepository: PerformerRepository,
    private val studioRepository: StudioRepository,
    private val tagRepository: TagRepository,
    private val savedFilterRepository: SavedFilterRepository,
    private val settingsStore: SettingsStore,
    private val syncRepository: SyncRepository,
    private val stashDBRepository: StashDBRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _sections = MutableStateFlow<Map<String, HomeSectionData>>(emptyMap())

    val uiState: StateFlow<HomeUiState> = combine(
        settingsStore.homeSectionOrder,
        settingsStore.homeSectionHidden,
        _sections
    ) { order, hidden, sections ->
        val hiddenSet = hidden.toSet()
        val orderedSections = order.mapNotNull { id ->
            if (!hiddenSet.contains(id)) sections[id] else null
        }

        // Add remaining cached sections that might not be in the order list yet
        val remainingSections = sections.filter { (id, _) ->
            !hiddenSet.contains(id) && !order.contains(id)
        }.values

        val allSections = (orderedSections + remainingSections).distinctBy { it.id }

        if (allSections.isEmpty() && sections.isEmpty()) {
            HomeUiState.Loading
        } else {
            HomeUiState.Success(allSections)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = HomeUiState.Loading
    )

    private data class HomeSettings(
        val order: List<String>,
        val hidden: List<String>,
        val followedTagIds: List<String>
    )

    init {
        Timber.d("🏠 HomeViewModel initialized")
        observeSettingsAndLoadData()
    }

    @OptIn(FlowPreview::class)
    private fun observeSettingsAndLoadData() {
        viewModelScope.launch {
            combine(
                settingsStore.homeSectionHidden,
                settingsStore.followedTagIds
            ) { hidden, followedTagIds ->
                hidden to followedTagIds
            }
                .debounce(300)
                .collect { (hidden, followedTagIds) ->
                    val settings = HomeSettings(emptyList(), hidden, followedTagIds)
                    val hiddenSet = hidden.toSet()
                    try {
                        val result = loadHomeDataInternal(settings)
                        val sectionsMap = result.associateBy { it.id }.toMutableMap()

                        // Add StashDB loading section if not hidden and not already loaded
                        if (!hiddenSet.contains(HomeSectionIds.STASHDB_FAVORITES)) {
                            sectionsMap[HomeSectionIds.STASHDB_FAVORITES] = HomeSectionData.StashDBLoadingSection(
                                id = HomeSectionIds.STASHDB_FAVORITES,
                                title = "Scenes from StashDB Favorites"
                            )
                        }

                        _sections.value = sectionsMap

                        // Load StashDB in background
                        if (!hiddenSet.contains(HomeSectionIds.STASHDB_FAVORITES)) {
                            loadStashDBFavoritesInBackground()
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Error loading home data")
                    }
                }
        }
    }

    /**
     * Loads StashDB favorites in background and updates _sections when complete.
     */
    private fun loadStashDBFavoritesInBackground() {
        viewModelScope.launch(ioDispatcher) {
            try {
                Timber.d("🌟 Loading StashDB favorites in background...")

                // Try cache first for quick display
                val cached = stashDBRepository.getCachedFavoriteScenes()
                if (cached != null && cached.isNotEmpty()) {
                    Timber.d("✅ Loaded ${cached.size} StashDB favorites from cache")
                    updateStashDBSection(cached)
                }

                // Then fetch from API with timeout
                val apiScenes = kotlinx.coroutines.withTimeoutOrNull(10000L) {
                    stashDBRepository.fetchFavoriteScenesFromStashDB()
                }

                if (apiScenes != null && apiScenes.isNotEmpty()) {
                    Timber.d("✅ Fetched ${apiScenes.size} StashDB favorites from API")
                    stashDBRepository.cacheFavoriteScenes(apiScenes)
                    updateStashDBSection(apiScenes)
                } else if (cached == null || cached.isEmpty()) {
                    // No data available - remove the loading section
                    Timber.d("⚠️ No StashDB favorites available, removing loading section")
                    removeStashDBSection()
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to load StashDB favorites")
                // Try to use cache on error
                val cached = stashDBRepository.getCachedFavoriteScenes()
                if (cached != null && cached.isNotEmpty()) {
                    updateStashDBSection(cached)
                } else {
                    removeStashDBSection()
                }
            }
        }
    }

    private suspend fun updateStashDBSection(scenes: List<StashDBScene>) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
            val newSections = _sections.value.toMutableMap()
            newSections[HomeSectionIds.STASHDB_FAVORITES] = HomeSectionData.StashDBSceneSection(
                id = HomeSectionIds.STASHDB_FAVORITES,
                title = "Scenes from StashDB Favorites",
                scenes = scenes,
                count = scenes.size
            )
            _sections.value = newSections
            Timber.d("✅ Updated StashDB section with ${scenes.size} scenes")
        }
    }

    private suspend fun removeStashDBSection() {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
            val newSections = _sections.value.toMutableMap()
            newSections.remove(HomeSectionIds.STASHDB_FAVORITES)
            _sections.value = newSections
            Timber.d("🗑️ Removed StashDB loading section")
        }
    }

    /**
     * Called on pull-to-refresh. Triggers a full sync (with pruning) then reloads home data.
     */
    fun refreshWithSync() {
        viewModelScope.launch {
            Timber.d("🔄 Pull-to-refresh: Starting sync...")
            // Run full sync (delta sync + pruning)
            val serverUrl = settingsStore.serverUrl.first()
            val apiKey = settingsStore.apiKey.first()
            if (serverUrl.isNotEmpty()) {
                syncRepository.syncAll(serverUrl, apiKey)
            }
            // Manually trigger a reload by clearing specific cache if needed or just let the flow react
            val currentHidden = settingsStore.homeSectionHidden.first()
            val currentFollowed = settingsStore.followedTagIds.first()
            val result = loadHomeDataInternal(HomeSettings(emptyList(), currentHidden, currentFollowed))
            _sections.value = result.associateBy { it.id }
        }
    }

    private suspend fun loadHomeDataInternal(settings: HomeSettings): List<HomeSectionData> {
        Timber.d("🔄 Starting home data load with settings: $settings")
        try {
            val order = settings.order
            val hidden = settings.hidden
            val hiddenSet = hidden.toSet()
            Timber.d(
                "⚙️ Loaded settings: hidden=${hiddenSet.size} items"
            )

            // Fetch all raw data in parallel
            Timber.d("🚀 Starting parallel fetch for sections...")
            val results = withContext(ioDispatcher) {
                supervisorScope {
                    val randomDeferred = async {
                        if (!hiddenSet.contains(HomeSectionIds.RANDOM)) {
                            val scenes = sceneRepository.getHomeScenes(sort = "random")
                            Timber.d("✅ Fetched Random: ${scenes.size} scenes")
                            scenes
                        } else {
                            emptyList()
                        }
                    }
                    val recentlyReleasedDeferred = async {
                        if (!hiddenSet.contains(HomeSectionIds.RECENTLY_RELEASED)) {
                            val scenes = sceneRepository.getHomeScenes(sort = "date")
                            Timber.d("✅ Fetched Recently Released: ${scenes.size} scenes")
                            scenes
                        } else {
                            emptyList()
                        }
                    }
                    val recentlyAddedDeferred = async {
                        if (!hiddenSet.contains(HomeSectionIds.RECENTLY_ADDED)) {
                            val scenes = sceneRepository.getHomeScenes(sort = "created_at")
                            Timber.d("✅ Fetched Recently Added: ${scenes.size} scenes")
                            scenes
                        } else {
                            emptyList()
                        }
                    }
                    val topRatedDeferred = async {
                        if (!hiddenSet.contains(HomeSectionIds.TOP_RATED)) {
                            val scenes = sceneRepository.getHomeScenes(sort = "rating")
                            Timber.d("✅ Fetched Top Rated: ${scenes.size} scenes")
                            scenes
                        } else {
                            emptyList()
                        }
                    }
                    val mostWatchedDeferred = async {
                        if (!hiddenSet.contains(HomeSectionIds.MOST_WATCHED)) {
                            val scenes = sceneRepository.getHomeScenes(sort = "o_counter")
                            Timber.d("✅ Fetched Most Watched: ${scenes.size} scenes")
                            scenes
                        } else {
                            emptyList()
                        }
                    }
                    val recentlyWatchedDeferred = async {
                        if (!hiddenSet.contains(HomeSectionIds.RECENTLY_WATCHED)) {
                            val scenes = sceneRepository.getHomeScenes(sort = "last_played_at")
                            Timber.d("✅ Fetched Recently Watched: ${scenes.size} scenes")
                            scenes
                        } else {
                            emptyList()
                        }
                    }

                    // Fetch saved filters (always enabled if they exist and are unhidden)
                    val savedFilterRowsDeferred = async {
                        val allFilters = savedFilterRepository.getAllFilters().first()
                        Timber.d("📋 Retrieved ${allFilters.size} saved filters from DB")

                        if (allFilters.isEmpty()) {
                            Timber.w("⚠️ No saved filters in DB - you may need to pull to refresh to sync them from server")
                            return@async emptyList()
                        }

                        // Process supported filter types
                        val supportedModes = setOf(
                            FilterMode.SCENES,
                            FilterMode.PERFORMERS,
                            FilterMode.STUDIOS,
                            FilterMode.TAGS
                        )
                        val supportedFilters = allFilters.filter { it.mode in supportedModes }
                        Timber.d(
                            "📋 Processing ${supportedFilters.size} saved " +
                                "filters (${supportedFilters.groupBy { it.mode }.mapValues { it.value.size }})"
                        )

                        supportedFilters.mapNotNull { filter ->
                            val sectionId = "SAVED_FILTER_${filter.id}"
                            if (hiddenSet.contains(sectionId)) {
                                Timber.d("📋 Skipping filter '${filter.name}' (sectionId=$sectionId) - HIDDEN")
                                return@mapNotNull null
                            }

                            Timber.d("📋 Processing filter '${filter.name}' (mode=${filter.mode}, sectionId=$sectionId)")

                            // Handle based on filter mode
                            when (filter.mode) {
                                FilterMode.SCENES -> {
                                    val searchQuery = filter.findFilter?.q
                                    val sort = filter.findFilter?.sort ?: "date"
                                    val direction = filter.findFilter?.direction ?: "DESC"

                                    // Log filter details
                                    Timber.d("📋 SCENES filter '${filter.name}':")
                                    Timber.d("  - searchQuery: '$searchQuery'")
                                    Timber.d("  - sort: $sort, direction: $direction")

                                    // Check if this is a text search filter
                                    if (!searchQuery.isNullOrBlank()) {
                                        // Text search across scene metadata
                                        Timber.d("📋 Filter '${filter.name}': performing text search for '$searchQuery'")
                                        val scenes = sceneRepository.searchScenes(
                                            query = searchQuery,
                                            sort = sort,
                                            direction = direction,
                                            limit = 10
                                        )
                                        Timber.d("📋 Filter '${filter.name}': found ${scenes.size} scenes matching '$searchQuery'")

                                        if (scenes.isNotEmpty()) {
                                            HomeSectionData.SceneSection(
                                                id = sectionId,
                                                title = filter.name,
                                                scenes = scenes,
                                                count = null,
                                                sort = sort,
                                                direction = direction
                                            )
                                        } else {
                                            null
                                        }
                                    } else {
                                        // Extract IDs from object_filter
                                        val filterIds = savedFilterRepository.extractFilterIds(filter)
                                        Timber.d(
                                            "📋 Filter '${filter.name}': object_filter has tags=${filterIds.tagIds.size}, " +
                                                "performers=${filterIds.performerIds.size}, studios=${filterIds.studioIds.size}"
                                        )

                                        // For simple tag-only filters, fetch scenes by tag
                                        if (filterIds.tagIds.isNotEmpty() &&
                                            filterIds.performerIds.isEmpty() && filterIds.studioIds.isEmpty()
                                        ) {
                                            val tagId = filterIds.tagIds.firstOrNull()
                                            if (tagId != null) {
                                                val scenes = sceneRepository.getScenesForTag(tagId, limit = 10)
                                                Timber.d("📋 Filter '${filter.name}': fetched ${scenes.size} scenes by tag $tagId")

                                                if (scenes.isNotEmpty()) {
                                                    HomeSectionData.SceneSection(
                                                        id = sectionId,
                                                        title = filter.name,
                                                        scenes = scenes,
                                                        count = null,
                                                        sort = "tag_id:$tagId",
                                                        direction = direction
                                                    )
                                                } else {
                                                    null
                                                }
                                            } else {
                                                null
                                            }
                                        } else {
                                            // No search query and no filter criteria, show recent scenes
                                            Timber.d("📋 Filter '${filter.name}': no criteria, showing recent scenes")
                                            val scenes = sceneRepository.getHomeScenes(sort = sort, limit = 10)
                                            Timber.d("📋 Filter '${filter.name}': fetched ${scenes.size} scenes")

                                            if (scenes.isNotEmpty()) {
                                                HomeSectionData.SceneSection(
                                                    id = sectionId,
                                                    title = filter.name,
                                                    scenes = scenes,
                                                    count = null,
                                                    sort = sort,
                                                    direction = direction
                                                )
                                            } else {
                                                null
                                            }
                                        }
                                    }
                                }
                                FilterMode.PERFORMERS -> {
                                    // Log filter details
                                    Timber.d("📋 PERFORMERS filter '${filter.name}':")
                                    Timber.d("  - findFilter: ${filter.findFilter}")
                                    Timber.d("  - objectFilter keys: ${filter.objectFilter?.keys}")
                                    filter.objectFilter?.forEach { (key, criterion) ->
                                        Timber.d(
                                            "    - $key: modifier=${criterion.modifier}, value=${criterion.value}, " +
                                                "depth=${criterion.depth}"
                                        )
                                    }

                                    // Apply sort/direction from filter's findFilter
                                    val sort = filter.findFilter?.sort ?: "name"
                                    val direction = filter.findFilter?.direction ?: "ASC"
                                    val performers = performerRepository.getTopPerformers(
                                        limit = 20,
                                        sort = sort,
                                        direction = direction
                                    )
                                    Timber.d(
                                        "📋 PERFORMERS filter '${filter.name}': fetched ${performers.size} " +
                                            "performers (sorted by $sort $direction)"
                                    )
                                    performers.take(5).forEach { p ->
                                        Timber.d("  - ${p.name} (scenes=${p.sceneCount}, height=${p.heightCm}cm)")
                                    }

                                    if (performers.isNotEmpty()) {
                                        HomeSectionData.PerformerSection(
                                            id = sectionId,
                                            title = filter.name,
                                            performers = performers,
                                            count = null
                                        )
                                    } else {
                                        null
                                    }
                                }
                                FilterMode.STUDIOS -> {
                                    // Log filter details
                                    Timber.d("📋 STUDIOS filter '${filter.name}':")
                                    Timber.d("  - findFilter: ${filter.findFilter}")
                                    Timber.d("  - objectFilter keys: ${filter.objectFilter?.keys}")

                                    // Apply sort/direction from filter's findFilter
                                    val sort = filter.findFilter?.sort ?: "name"
                                    val direction = filter.findFilter?.direction ?: "ASC"
                                    val studios = studioRepository.getTopStudios(
                                        limit = 20,
                                        sort = sort,
                                        direction = direction
                                    )
                                    Timber.d(
                                        "📋 STUDIOS filter '${filter.name}': fetched ${studios.size} " +
                                            "studios (sorted by $sort $direction)"
                                    )

                                    if (studios.isNotEmpty()) {
                                        HomeSectionData.StudioSection(
                                            id = sectionId,
                                            title = filter.name,
                                            studios = studios,
                                            count = null
                                        )
                                    } else {
                                        null
                                    }
                                }
                                FilterMode.TAGS -> {
                                    // Log filter details
                                    Timber.d("📋 TAGS filter '${filter.name}':")
                                    Timber.d("  - findFilter: ${filter.findFilter}")
                                    Timber.d("  - objectFilter keys: ${filter.objectFilter?.keys}")

                                    // Apply sort/direction from filter's findFilter
                                    val sort = filter.findFilter?.sort ?: "name"
                                    val direction = filter.findFilter?.direction ?: "ASC"
                                    val tags = tagRepository.getTopTags(
                                        limit = 20,
                                        sort = sort,
                                        direction = direction
                                    )
                                    Timber.d("📋 TAGS filter '${filter.name}': fetched ${tags.size} tags (sorted by $sort $direction)")

                                    if (tags.isNotEmpty()) {
                                        HomeSectionData.TagSection(
                                            id = sectionId,
                                            title = filter.name,
                                            tags = tags,
                                            count = null
                                        )
                                    } else {
                                        null
                                    }
                                }
                                else -> {
                                    Timber.d("📋 Filter '${filter.name}': unsupported mode ${filter.mode}, skipping")
                                    null
                                }
                            }
                        }
                    }

                    // Fetch followed tags
                    // We fetch ALL followed tags scene data if the tag section isn't hidden
                    // This might be over-fetching if many tags are hidden,
                    // but avoiding individual queries per tag implies we know the list.
                    val followedTagRowsDeferred = async {
                        val followedIds = settings.followedTagIds
                        Timber.d("🏷️ Followed tag IDs from settings: ${followedIds.size} tags - $followedIds")

                        if (followedIds.isNotEmpty()) {
                            val tags = tagRepository.getTags(followedIds).first()
                            Timber.d("🏷️ Retrieved ${tags.size} tag entities from DB")
                            tags.forEachIndexed { index, tag ->
                                Timber.d("🏷️ [$index] Tag: '${tag.name}' (id=${tag.id}, sceneCount=${tag.sceneCount})")
                            }

                            val tagSectionDeferreds = tags.map { tag ->
                                val sectionId = HomeSectionIds.getTagSectionId(tag.id)
                                if (!hiddenSet.contains(sectionId)) {
                                    Timber.d("🏷️ Processing tag '${tag.name}' (sectionId=$sectionId) - NOT hidden")
                                    async {
                                        val scenes = sceneRepository.getScenesForTag(tag.id)
                                        val count = tag.sceneCount
                                        Timber.d("🏷️ Tag '${tag.name}': fetched ${scenes.size} scenes (reported count=$count)")
                                        HomeSectionData.SceneSection(
                                            id = sectionId,
                                            title = tag.name,
                                            count = count,
                                            scenes = scenes,
                                            // Special sort trigger for tags
                                            sort = "tag_id:${tag.id}",
                                            direction = "DESC"
                                        )
                                    }
                                } else {
                                    Timber.d("🏷️ Skipping tag '${tag.name}' (sectionId=$sectionId) - HIDDEN in settings")
                                    null
                                }
                            }.filterNotNull()

                            val awaitedSections = tagSectionDeferreds.awaitAll()
                            Timber.d("🏷️ Awaited ${awaitedSections.size} tag sections before filtering")

                            val filteredSections = awaitedSections.filter { it.scenes.isNotEmpty() }
                            Timber.d("🏷️ After filtering empty scenes: ${filteredSections.size} tag sections remain")
                            filteredSections.forEach { section ->
                                Timber.d("🏷️ ✅ Final tag section: '${section.title}' with ${section.scenes.size} scenes")
                            }

                            filteredSections
                        } else {
                            Timber.d("🏷️ No followed tags configured")
                            emptyList()
                        }
                    }

                    // Collect results
                    val random = randomDeferred.await()
                    val recentlyReleased = recentlyReleasedDeferred.await()
                    val recentlyAdded = recentlyAddedDeferred.await()
                    val topRated = topRatedDeferred.await()
                    val mostWatched = mostWatchedDeferred.await()
                    val recentlyWatched = recentlyWatchedDeferred.await()
                    val savedFilterSections = savedFilterRowsDeferred.await()
                    val tagSections = followedTagRowsDeferred.await()

                    Timber.d("📋 Saved filter sections: ${savedFilterSections.size}")
                    savedFilterSections.forEach { section ->
                        Timber.d("📋 ✅ Saved filter section: '${section.title}'")
                    }

                    // Build available sections map
                    val availableSections = mutableMapOf<String, HomeSectionData>()

                    if (random.isNotEmpty()) {
                        availableSections[HomeSectionIds.RANDOM] = HomeSectionData.SceneSection(
                            id = HomeSectionIds.RANDOM,
                            title = "Random",
                            scenes = random,
                            sort = "random",
                            direction = "DESC"
                        )
                    }
                    if (recentlyReleased.isNotEmpty()) {
                        availableSections[HomeSectionIds.RECENTLY_RELEASED] = HomeSectionData.SceneSection(
                            id = HomeSectionIds.RECENTLY_RELEASED,
                            title = "Recently Released",
                            scenes = recentlyReleased,
                            sort = "date",
                            direction = "DESC"
                        )
                    }
                    if (recentlyAdded.isNotEmpty()) {
                        availableSections[HomeSectionIds.RECENTLY_ADDED] = HomeSectionData.SceneSection(
                            id = HomeSectionIds.RECENTLY_ADDED,
                            title = "Recently Added",
                            scenes = recentlyAdded,
                            sort = "created_at",
                            direction = "DESC"
                        )
                    }
                    if (topRated.isNotEmpty()) {
                        availableSections[HomeSectionIds.TOP_RATED] = HomeSectionData.SceneSection(
                            id = HomeSectionIds.TOP_RATED,
                            title = "Top Rated",
                            scenes = topRated,
                            sort = "rating",
                            direction = "DESC"
                        )
                    }
                    if (mostWatched.isNotEmpty()) {
                        availableSections[HomeSectionIds.MOST_WATCHED] = HomeSectionData.SceneSection(
                            id = HomeSectionIds.MOST_WATCHED,
                            title = "Most Watched",
                            scenes = mostWatched,
                            sort = "o_counter",
                            direction = "DESC"
                        )
                    }
                    if (recentlyWatched.isNotEmpty()) {
                        availableSections[HomeSectionIds.RECENTLY_WATCHED] = HomeSectionData.SceneSection(
                            id = HomeSectionIds.RECENTLY_WATCHED,
                            title = "Recently Watched",
                            scenes = recentlyWatched,
                            sort = "last_played_at",
                            direction = "DESC"
                        )
                    }

                    // Add saved filter sections
                    savedFilterSections.forEach { availableSections[it.id] = it }

                    // Add followed tag sections
                    tagSections.forEach { availableSections[it.id] = it }
                    Timber.d(
                        "📦 Total available sections: ${availableSections.size} (including ${savedFilterSections.size} " +
                            "saved filters + ${tagSections.size} tag sections)"
                    )

                    // Construct final list based on order
                    val finalSections = mutableListOf<HomeSectionData>()

                    // 1. Add ordered sections
                    Timber.d("📋 Applying saved order with ${order.size} items")
                    order.forEach { id ->
                        availableSections.remove(id)?.let {
                            Timber.d("📋 ✅ Added ordered section: '$id' -> '${it.title}'")
                            finalSections.add(it)
                        }
                    }

                    // 2. Add remaining (new/unordered) sections, unless hidden
                    // (We already didn't fetch hidden ones mostly, but tagSections check was strict)
                    // Standard sections keys are effectively their IDs.
                    // Order of remaining: Standard first, then tags? or just arbitrary iteration order?
                    // Map iteration order is undefined usually, but standard keys are few.

                    // We'll enforce a default order for standards if not in saved order
                    val defaultOrder = listOf(
                        HomeSectionIds.STASHDB_FAVORITES,
                        HomeSectionIds.RANDOM,
                        HomeSectionIds.RECENTLY_RELEASED,
                        HomeSectionIds.RECENTLY_ADDED,
                        HomeSectionIds.TOP_RATED,
                        HomeSectionIds.MOST_WATCHED,
                        HomeSectionIds.RECENTLY_WATCHED
                    )

                    Timber.d(
                        "📋 Adding unordered standard sections " +
                            "(${availableSections.keys.intersect(defaultOrder.toSet()).size} remaining)"
                    )
                    defaultOrder.forEach { id ->
                        availableSections.remove(id)?.let {
                            Timber.d("📋 ✅ Added unordered standard section: '$id' -> '${it.title}'")
                            finalSections.add(it)
                        }
                    }

                    // Add remaining (tags)
                    Timber.d("📋 Adding remaining sections (mostly tags): ${availableSections.size} items")
                    availableSections.values.forEach {
                        Timber.d("📋 ✅ Added remaining section: '${it.id}' -> '${it.title}'")
                        finalSections.add(it)
                    }

                    finalSections
                }
            }
            Timber.d("🏁 Home data loaded: ${results.size} sections ready")
            results.forEachIndexed { index, section ->
                val itemCount = when (section) {
                    is HomeSectionData.SceneSection -> "${section.scenes.size} scenes"
                    is HomeSectionData.PerformerSection -> "${section.performers.size} performers"
                    is HomeSectionData.StudioSection -> "${section.studios.size} studios"
                    is HomeSectionData.TagSection -> "${section.tags.size} tags"
                    is HomeSectionData.StashDBSceneSection -> "${section.scenes.size} stashdb scenes"
                    is HomeSectionData.StashDBLoadingSection -> "loading"
                }
                Timber.d("🏁 Section [$index]: '${section.title}' (id=${section.id}, $itemCount)")
            }
            return results
        } catch (e: Exception) {
            Timber.e(e, "❌ Error loading home data")
            throw e
        }
    }

    fun updateSceneSort(sort: String, direction: String = "DESC") {
        viewModelScope.launch {
            Timber.d("📱 HomeViewModel: Setting scene sort to $sort $direction for 'See All'")
            settingsStore.setSceneSort(sort, direction)
        }
    }

    fun toggleBlurNsfw(currentValue: Boolean) {
        viewModelScope.launch {
            Timber.d("👁️ HomeViewModel: Toggling NSFW Blur to ${!currentValue}")
            settingsStore.setBlurNsfw(!currentValue)
        }
    }
}
