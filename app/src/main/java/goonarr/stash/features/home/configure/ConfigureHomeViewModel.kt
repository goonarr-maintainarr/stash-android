package goonarr.stash.features.home.configure

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.model.FilterMode
import goonarr.stash.core.model.HomeSectionIds
import goonarr.stash.core.model.SavedFilter
import goonarr.stash.core.network.StashClient
import goonarr.stash.repositories.SavedFilterRepository
import goonarr.stash.repositories.TagRepository
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

data class HomeConfigItem(
    val id: String,
    val title: String,
    val isHidden: Boolean,
    // True for built-in rows, false for saved filters
    val isDefault: Boolean = false,
    // For saved filters, the mode (SCENES, PERFORMERS, etc.)
    val filterMode: FilterMode? = null
)

data class SavedFilterItem(
    val filter: SavedFilter,
    // Whether it's shown in the draggable list above
    val isEnabled: Boolean
)

data class SavedFilterSection(
    val mode: FilterMode,
    val title: String,
    val filters: List<SavedFilterItem>
)

enum class ConfigureHomeTab(val title: String) {
    OVERVIEW("Overview"),
    DEFAULT_ROWS("Default Rows"),
    SAVED_FILTERS("Saved Filters"),
    TAGS("Tags")
}

@HiltViewModel
class ConfigureHomeViewModel @Inject constructor(
    private val settingsStore: SettingsStore,
    private val tagRepository: TagRepository,
    private val savedFilterRepository: SavedFilterRepository,
    private val stashClient: StashClient
) : ViewModel() {

    private val _items = MutableStateFlow<List<HomeConfigItem>>(emptyList())
    val items = _items.asStateFlow()

    private val _savedFilterSections = MutableStateFlow<List<SavedFilterSection>>(emptyList())
    val savedFilterSections = _savedFilterSections.asStateFlow()

    private val _syncHomeLayout = MutableStateFlow(false)
    val syncHomeLayout = _syncHomeLayout.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _defaultRows = MutableStateFlow<List<HomeConfigItem>>(emptyList())
    val defaultRows = _defaultRows.asStateFlow()

    private val _tagRows = MutableStateFlow<List<HomeConfigItem>>(emptyList())
    val tagRows = _tagRows.asStateFlow()

    private val _selectedTab = MutableStateFlow(ConfigureHomeTab.OVERVIEW)
    val selectedTab = _selectedTab.asStateFlow()

    private val _scrollToItemId = MutableStateFlow<String?>(null)
    val scrollToItemId = _scrollToItemId.asStateFlow()

    private var saveJob: Job? = null

    init {
        loadConfig()
        loadDefaultRows()
        loadTagRows()
        loadSavedFilters()
        autoHideNewFilters()
        observeSyncHomeLayout()
    }

    private fun observeSyncHomeLayout() {
        viewModelScope.launch {
            settingsStore.syncHomeLayout.collect { enabled ->
                _syncHomeLayout.value = enabled
            }
        }
    }

    private fun loadConfig() {
        viewModelScope.launch {
            combine(
                settingsStore.homeSectionOrder,
                settingsStore.homeSectionHidden,
                tagRepository.followedTagIds,
                savedFilterRepository.getAllFilters()
            ) { flows ->
                val order = flows[0] as List<String>
                val hidden = flows[1] as List<String>
                val followedTagIds = flows[2] as List<String>
                val allFilters = flows[3] as List<SavedFilter>

                val hiddenSet = hidden.toSet()

                // 1. Identify all available sections
                // Standard Sections
                val standardSections: List<Pair<String, String>> = STANDARD_SECTIONS
                val standardSectionIds = standardSections.map { it.first }.toSet()

                // Tag Sections (fetch names)
                val tagSections: List<Pair<String, String>> = if (followedTagIds.isNotEmpty()) {
                    try {
                        val tags = tagRepository.getTags(followedTagIds).first()
                        tags.map { tag ->
                            HomeSectionIds.getTagSectionId(tag.id) to tag.name
                        }
                    } catch (e: Exception) {
                        emptyList()
                    }
                } else {
                    emptyList()
                }

                // Saved Filter Sections (always identify them for the draggable list if not hidden)
                val enabledSavedFilters: List<Pair<String, String>> = run {
                    val supportedModes = setOf(
                        FilterMode.SCENES,
                        FilterMode.PERFORMERS,
                        FilterMode.STUDIOS,
                        FilterMode.TAGS
                    )
                    val filtered = allFilters
                        .filter { it.mode in supportedModes }
                        .filter { !hiddenSet.contains("SAVED_FILTER_${it.id}") }

                    filtered.map { filter ->
                        "SAVED_FILTER_${filter.id}" to filter.name
                    }
                }

                // Also create a map of saved filter modes for display
                val savedFilterModes = allFilters
                    .filter { it.mode in setOf(FilterMode.SCENES, FilterMode.PERFORMERS, FilterMode.STUDIOS, FilterMode.TAGS) }
                    .associate { "SAVED_FILTER_${it.id}" to it.mode }

                val allSectionsMap: Map<String, String> = (standardSections + tagSections + enabledSavedFilters).toMap()

                Timber.d(
                    "📋 ConfigureHome: Loading sections - standard=${STANDARD_SECTIONS.size}, tags=${tagSections.size}, " +
                        "savedFilters=${enabledSavedFilters.size}"
                )

                // 2. Build Ordered List
                val finalItems = mutableListOf<HomeConfigItem>()
                val appendedIds = mutableSetOf<String>()

                // Add items from saved order if they still exist
                order.forEach { id ->
                    if (allSectionsMap.containsKey(id)) {
                        finalItems.add(
                            HomeConfigItem(
                                id = id,
                                title = allSectionsMap[id] ?: id,
                                isHidden = hiddenSet.contains(id),
                                // Mark standard sections as default
                                isDefault = standardSectionIds.contains(id),
                                // Add filter mode for saved filters
                                filterMode = savedFilterModes[id]
                            )
                        )
                        appendedIds.add(id)
                    }
                }

                // Add remaining items (newly added or not yet ordered)
                // Standard first, then tags? Or just order of map?
                // Enforce standard default order first
                standardSections.forEach { (id, title) ->
                    if (!appendedIds.contains(id)) {
                        finalItems.add(
                            HomeConfigItem(
                                id = id,
                                title = title,
                                isHidden = hiddenSet.contains(id),
                                // Standard sections are default
                                isDefault = true,
                                filterMode = null
                            )
                        )
                        appendedIds.add(id)
                    }
                }

                // Add remaining tags
                tagSections.forEach { (id, title) ->
                    if (!appendedIds.contains(id)) {
                        finalItems.add(
                            HomeConfigItem(
                                id = id,
                                title = title,
                                isHidden = hiddenSet.contains(id),
                                // Tag sections are not default
                                isDefault = false,
                                filterMode = null
                            )
                        )
                        appendedIds.add(id)
                    }
                }

                // Add remaining saved filters
                enabledSavedFilters.forEach { (id, title) ->
                    if (!appendedIds.contains(id)) {
                        finalItems.add(
                            HomeConfigItem(
                                id = id,
                                title = title,
                                isHidden = hiddenSet.contains(id),
                                // Saved filters are not default
                                isDefault = false,
                                // Add filter mode
                                filterMode = savedFilterModes[id]
                            )
                        )
                        appendedIds.add(id)
                        Timber.d("📋 Adding enabled saved filter to draggable list: '$title' ($id)")
                    }
                }

                // Filter for Overview (only unhidden items)
                val filteredItems = finalItems.filter { item ->
                    !item.isHidden
                }

                Timber.d(
                    "📋 Filtered items: ${filteredItems.size}"
                )

                filteredItems
            }.collect {
                _items.value = it
            }
        }
    }

    fun toggleVisibility(id: String) {
        viewModelScope.launch {
            val currentHidden = settingsStore.homeSectionHidden.first().toMutableSet()
            val currentOrder = settingsStore.homeSectionOrder.first().toMutableList()

            if (currentHidden.contains(id)) {
                currentHidden.remove(id)
                // If it's being unhidden, make sure it's in the order list
                if (!currentOrder.contains(id)) {
                    currentOrder.add(id)
                }
            } else {
                currentHidden.add(id)
            }

            settingsStore.setHomeLayoutConfig(currentOrder, currentHidden.toList())

            Timber.d("📋 Toggled visibility for: $id (Hidden: ${currentHidden.contains(id)})")

            disableSyncIfEnabled()
        }
    }

    fun resetToDefaultLayout() {
        viewModelScope.launch {
            val defaultOrder = STANDARD_SECTIONS.map { it.first }

            // Collect all possible non-standard IDs to hide them
            val followedTagIds = tagRepository.followedTagIds.first()
            val tagIds = followedTagIds.map { HomeSectionIds.getTagSectionId(it) }

            val allFilters = savedFilterRepository.getAllFilters().first()
            val filterIds = allFilters.map { "SAVED_FILTER_${it.id}" }

            // Combine all into a new hidden list (all custom content)
            // Standard rows will all be visible (not in hidden list)
            val newHidden = (tagIds + filterIds).toList()

            settingsStore.setHomeLayoutConfig(defaultOrder, newHidden)

            Timber.d("📋 Reset layout to default: order=$defaultOrder, hidden=$newHidden")

            disableSyncIfEnabled()
        }
    }

    fun moveItem(fromIndex: Int, toIndex: Int) {
        val currentItems = _items.value.toMutableList()
        val item = currentItems.removeAt(fromIndex)
        currentItems.add(toIndex, item)
        _items.value = currentItems
        save(currentItems)

        // Disable sync since user made manual changes
        viewModelScope.launch {
            disableSyncIfEnabled()
        }
    }
    fun navigateToSourceTab(item: HomeConfigItem) {
        val targetTab = when {
            item.isDefault -> ConfigureHomeTab.DEFAULT_ROWS
            item.id.startsWith("SAVED_FILTER_") -> ConfigureHomeTab.SAVED_FILTERS
            item.id.startsWith("TAG_") -> ConfigureHomeTab.TAGS
            else -> return
        }
        _selectedTab.value = targetTab

        // For saved filters, we key by the raw ID in the sub-tab
        val scrollId = when {
            item.id.startsWith("SAVED_FILTER_") -> item.id.removePrefix("SAVED_FILTER_")
            else -> item.id
        }
        _scrollToItemId.value = scrollId

        Timber.d("📋 Navigating to $targetTab for item: ${item.id}, scrolling to: $scrollId")
    }

    fun clearScrollToItem() {
        _scrollToItemId.value = null
    }

    fun toggleShowSavedFilters() {
        // No longer used, but keeping for compatibility if needed or until cleaned up
    }

    private fun save(visibleItems: List<HomeConfigItem>) {
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            // Debounce persistence to prevent UI flickering during rapid dragging
            delay(100)

            try {
                val currentOrder = settingsStore.homeSectionOrder.first()
                val currentHidden = settingsStore.homeSectionHidden.first()

                val visibleIds = visibleItems.map { it.id }.toSet()

                // newOrder = visible items in their new order + any remaining items (hidden) from the current order
                val newOrder = visibleItems.map { it.id } + currentOrder.filter { it !in visibleIds }

                Timber.d("📋 Persisting home config change (debounced)")
                settingsStore.setHomeLayoutConfig(newOrder, currentHidden)
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to persist home config")
            }
        }
    }

    private fun loadSavedFilters() {
        viewModelScope.launch {
            combine(
                savedFilterRepository.getAllFilters(),
                settingsStore.homeSectionHidden
            ) { allFilters, hidden ->

                val hiddenSet = hidden.toSet()

                // Filter to only show supported modes
                val supportedModes = setOf(
                    FilterMode.SCENES,
                    FilterMode.PERFORMERS,
                    FilterMode.STUDIOS,
                    FilterMode.TAGS
                )
                val supportedFilters = allFilters.filter { it.mode in supportedModes }

                Timber.d("📋 ConfigureHome: Found ${supportedFilters.size} supported filters (hiddenSet size=${hiddenSet.size})")

                // Group by mode
                val filtersByMode = supportedFilters.groupBy { it.mode }

                // Create sections for each mode
                val sections = listOf(
                    FilterMode.SCENES to "Scenes",
                    FilterMode.PERFORMERS to "Performers",
                    FilterMode.STUDIOS to "Studios",
                    FilterMode.TAGS to "Tags"
                ).mapNotNull { (mode, title) ->
                    val filtersForMode = filtersByMode[mode] ?: emptyList()
                    if (filtersForMode.isEmpty()) {
                        null
                    } else {
                        val items = filtersForMode
                            .sortedBy { it.name.lowercase() }
                            .map { filter ->
                                val sectionId = "SAVED_FILTER_${filter.id}"
                                val isEnabled = !hiddenSet.contains(sectionId)
                                SavedFilterItem(
                                    filter = filter,
                                    isEnabled = isEnabled
                                )
                            }
                        Timber.d("📋 Section '$title': ${items.size} filters")
                        SavedFilterSection(
                            mode = mode,
                            title = title,
                            filters = items
                        )
                    }
                }

                sections
            }.collect {
                _savedFilterSections.value = it
                val totalFilters = it.sumOf { section -> section.filters.size }
                Timber.d("📋 Updated savedFilterSections: ${it.size} sections, $totalFilters total filters")
            }
        }
    }

    private fun loadDefaultRows() {
        viewModelScope.launch {
            settingsStore.homeSectionHidden.collect { hidden ->
                val hiddenSet = hidden.toSet()
                _defaultRows.value = STANDARD_SECTIONS.map { (id, title) ->
                    HomeConfigItem(
                        id = id,
                        title = title,
                        isHidden = hiddenSet.contains(id),
                        isDefault = true
                    )
                }
            }
        }
    }

    private fun loadTagRows() {
        viewModelScope.launch {
            combine(
                tagRepository.followedTagIds,
                settingsStore.homeSectionHidden
            ) { ids, hidden ->
                val hiddenSet = hidden.toSet()
                if (ids.isEmpty()) {
                    emptyList<HomeConfigItem>()
                } else {
                    try {
                        val tags = tagRepository.getTags(ids).first()
                        tags.sortedBy { it.name.lowercase() }.map { tag ->
                            val sectionId = HomeSectionIds.getTagSectionId(tag.id)
                            HomeConfigItem(
                                id = sectionId,
                                title = tag.name,
                                isHidden = hiddenSet.contains(sectionId),
                                isDefault = false
                            )
                        }
                    } catch (e: Exception) {
                        emptyList<HomeConfigItem>()
                    }
                }
            }.collect {
                _tagRows.value = it
            }
        }
    }

    /**
     * Auto-hide new saved filters that haven't been seen before.
     * This runs once on init to mark new filters as hidden by default.
     */
    private fun autoHideNewFilters() {
        viewModelScope.launch {
            val syncEnabled = settingsStore.syncSavedFilters.first()
            if (!syncEnabled) return@launch

            val allFilters = savedFilterRepository.getAllFilters().first()
            val hidden = settingsStore.homeSectionHidden.first().toMutableSet()
            val order = settingsStore.homeSectionOrder.first().toSet()
            var needsUpdate = false

            // Auto-hide all supported filter types
            val supportedModes = setOf(
                FilterMode.SCENES,
                FilterMode.PERFORMERS,
                FilterMode.STUDIOS,
                FilterMode.TAGS
            )
            val supportedFilters = allFilters.filter { it.mode in supportedModes }

            supportedFilters.forEach { filter ->
                val sectionId = "SAVED_FILTER_${filter.id}"
                if (!hidden.contains(sectionId) && !order.contains(sectionId)) {
                    // New filter - hide by default
                    hidden.add(sectionId)
                    needsUpdate = true
                    Timber.d("📋 New saved filter detected, hiding by default: '${filter.name}' (${filter.mode}, $sectionId)")
                }
            }

            // Save updated hidden list if we added new filters
            if (needsUpdate) {
                Timber.d("📋 Saving updated hidden list with ${hidden.size} items")
                settingsStore.setHomeSectionHidden(hidden.toList())
            }
        }
    }

    fun toggleSavedFilterVisibility(filterId: String) {
        viewModelScope.launch {
            val sectionId = "SAVED_FILTER_$filterId"
            val currentHidden = settingsStore.homeSectionHidden.first().toMutableList()
            val currentOrder = settingsStore.homeSectionOrder.first().toMutableList()

            Timber.d("📋 Toggle called for filterId=$filterId, sectionId=$sectionId")
            Timber.d("📋 Current hidden list: $currentHidden")
            Timber.d("📋 Current order list: $currentOrder")

            val wasHidden = currentHidden.contains(sectionId)
            if (wasHidden) {
                // Enable: Remove from hidden list and add to order if not present
                currentHidden.remove(sectionId)
                if (!currentOrder.contains(sectionId)) {
                    currentOrder.add(sectionId)
                    Timber.d("📋 Added to order list: $sectionId")
                }
                Timber.d("📋 Enabling saved filter: $filterId (removed from hidden)")
            } else {
                // Disable: Add to hidden list
                currentHidden.add(sectionId)
                Timber.d("📋 Disabling saved filter: $filterId (added to hidden)")
            }

            Timber.d("📋 New hidden list: $currentHidden")
            Timber.d("📋 New order list: $currentOrder")
            settingsStore.setHomeLayoutConfig(currentOrder, currentHidden)
            Timber.d("📋 Settings saved successfully")

            // Disable sync since user made manual changes
            disableSyncIfEnabled()
        }
    }

    fun useStashHomePage() {
        viewModelScope.launch {
            syncFromServer()
        }
    }

    fun setTab(tab: ConfigureHomeTab) {
        _selectedTab.value = tab

        if (tab == ConfigureHomeTab.SAVED_FILTERS) {
            viewModelScope.launch {
                _isSyncing.value = true
                try {
                    savedFilterRepository.syncFilters(force = true)
                } finally {
                    _isSyncing.value = false
                }
            }
        }
    }

    private suspend fun syncFromServer() {
        _isSyncing.value = true
        try {
            val serverUrl = settingsStore.serverUrl.first()
            val apiKey = settingsStore.apiKey.first()

            if (serverUrl.isEmpty()) {
                Timber.w("⚠️ Cannot sync - no server URL configured")
                _isSyncing.value = false
                return
            }

            Timber.d("🔄 Fetching home layout from server...")
            val uiConfig = stashClient.getConfiguration(serverUrl, apiKey)

            if (uiConfig == null || uiConfig.frontPageContent == null) {
                Timber.w("⚠️ No frontPageContent in server configuration")
                _isSyncing.value = false
                return
            }

            Timber.d("📋 Applying server configuration with ${uiConfig.frontPageContent.size} items")
            applyServerConfiguration(uiConfig.frontPageContent)

            // Enable sync setting
            settingsStore.setSyncHomeLayout(true)
            Timber.d("✅ Home layout synced from server")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to sync home layout from server")
        } finally {
            _isSyncing.value = false
        }
    }

    private suspend fun applyServerConfiguration(frontPageContent: List<goonarr.stash.core.network.FrontPageContentItem>) {
        val allSavedFilters = savedFilterRepository.getAllFilters().first()
        val savedFilterMap = allSavedFilters.associateBy { it.id }

        val order = mutableListOf<String>()
        val hidden = mutableSetOf<String>()

        frontPageContent.forEach { item ->
            when (item.__typename) {
                "SavedFilter" -> {
                    item.savedFilterId?.let { id ->
                        val sectionId = "SAVED_FILTER_$id"
                        order.add(sectionId)
                        Timber.d("📋 Server layout includes SavedFilter: $id")
                    }
                }
                "CustomFilter" -> {
                    // Map to standard section IDs based on mode/sortBy/direction
                    val sectionId = mapCustomFilterToSectionId(item)
                    if (sectionId != null) {
                        order.add(sectionId)
                        Timber.d("📋 Server layout includes CustomFilter: ${item.mode} (${item.sortBy})")
                    }
                }
            }
        }

        // Hide all sections not in server configuration
        val allStandardSections = listOf(
            HomeSectionIds.RANDOM,
            HomeSectionIds.RECENTLY_RELEASED,
            HomeSectionIds.RECENTLY_ADDED,
            HomeSectionIds.TOP_RATED,
            HomeSectionIds.MOST_WATCHED,
            HomeSectionIds.RECENTLY_WATCHED
        )
        allStandardSections.forEach { id ->
            if (!order.contains(id)) hidden.add(id)
        }

        // Hide all saved filters not in server configuration
        allSavedFilters.forEach { filter ->
            val sectionId = "SAVED_FILTER_${filter.id}"
            if (!order.contains(sectionId)) hidden.add(sectionId)
        }

        // Hide all followed tags not in server configuration
        val followedTagIds = tagRepository.followedTagIds.first()
        followedTagIds.forEach { tagId ->
            val sectionId = HomeSectionIds.getTagSectionId(tagId)
            if (!order.contains(sectionId)) hidden.add(sectionId)
        }

        Timber.d("📋 Applying: order=${order.size} items, hidden=${hidden.size} items")
        settingsStore.setHomeLayoutConfig(order, hidden.toList())
    }

    private suspend fun disableSyncIfEnabled() {
        if (_syncHomeLayout.value) {
            Timber.d("📋 User made manual changes - disabling sync")
            settingsStore.setSyncHomeLayout(false)
        }
    }

    companion object {
        internal val STANDARD_SECTIONS = listOf(
            HomeSectionIds.RANDOM to "Random",
            HomeSectionIds.RECENTLY_RELEASED to "Recently Released",
            HomeSectionIds.RECENTLY_ADDED to "Recently Added",
            HomeSectionIds.TOP_RATED to "Top Rated",
            HomeSectionIds.MOST_WATCHED to "Most Watched",
            HomeSectionIds.RECENTLY_WATCHED to "Recently Watched"
        )

        private fun mapCustomFilterToSectionId(item: goonarr.stash.core.network.FrontPageContentItem): String? {
            // Map premade filters to standard section IDs
            return when {
                item.mode == "SCENES" && item.sortBy == "random" -> HomeSectionIds.RANDOM
                item.mode == "SCENES" && item.sortBy == "date" -> HomeSectionIds.RECENTLY_RELEASED
                item.mode == "SCENES" && item.sortBy == "created_at" -> HomeSectionIds.RECENTLY_ADDED
                item.mode == "SCENES" && item.sortBy == "rating" -> HomeSectionIds.TOP_RATED
                item.mode == "SCENES" && item.sortBy == "o_counter" -> HomeSectionIds.MOST_WATCHED
                item.mode == "SCENES" && item.sortBy == "last_played_at" -> HomeSectionIds.RECENTLY_WATCHED
                else -> {
                    Timber.w("⚠️ Unknown CustomFilter: mode=${item.mode}, sortBy=${item.sortBy}")
                    null
                }
            }
        }
    }
}
