package goonarr.stash.features.home.discovery

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import goonarr.stash.core.model.Scene
import goonarr.stash.features.scenes.SceneLayoutType
import goonarr.stash.features.scenes.SceneSortType
import goonarr.stash.repositories.scenes.SceneRepository
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@HiltViewModel
class SceneDiscoveryViewModel @Inject constructor(
    private val sceneRepository: SceneRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialSort = savedStateHandle.get<String>("sort") ?: SceneSortType.DATE.apiValue
    private val initialDirection = savedStateHandle.get<String>("direction") ?: "DESC"
    private val initialTagId = savedStateHandle.get<String>("tagId")?.let { Uri.decode(it) }
    private val initialPerformerId = savedStateHandle.get<String>("performerId")?.let { Uri.decode(it) }
    private val initialCount = savedStateHandle.get<Int>("count")?.takeIf { it != -1 }

    // Scene IDs passed from home page for preserving random order
    private val initialSceneIds: List<String>? = savedStateHandle.get<String>("sceneIds")
        ?.let { Uri.decode(it) }
        ?.split(",")
        ?.filter { it.isNotEmpty() }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortType = MutableStateFlow(SceneSortType.fromApiValue(initialSort))
    val sortType: StateFlow<SceneSortType> = _sortType.asStateFlow()

    private val _sortDirection = MutableStateFlow(initialDirection)
    val sortDirection: StateFlow<String> = _sortDirection.asStateFlow()

    private val _layoutType = MutableStateFlow(SceneLayoutType.LIST)
    val layoutType: StateFlow<SceneLayoutType> = _layoutType.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val totalCount: Flow<Int?> = combine(
        _sortType,
        _searchQuery.debounce(300),
    ) { sortType, query ->
        sortType to query
    }.flatMapLatest { (sortType, query) ->
        // For global sections (no tag/performer), only show count if searching
        if (initialTagId == null && initialPerformerId == null && query.isEmpty()) {
            flowOf(null)
        } else if (query.isEmpty() && initialCount != null && sortType.apiValue == initialSort) {
            // Use passed count for immediate display in tag/performer sections
            flowOf(initialCount)
        } else {
            // Fetch dynamic count for search or if initial count is missing
            sceneRepository.getFilteredCountFlow(
                sort = sortType.apiValue,
                searchQuery = query,
                tagId = initialTagId,
                performerId = initialPerformerId
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val scenes: Flow<PagingData<Scene>> = combine(
        _sortType,
        _sortDirection,
        _searchQuery.debounce(300)
    ) { type, direction, query ->
        Triple(type, direction, query)
    }.flatMapLatest { (type, direction, query) ->
        val useSceneIds = type.apiValue == "random" && !initialSceneIds.isNullOrEmpty() && query.isEmpty()

        if (useSceneIds) {
            // Use passed scene IDs to preserve random order from home page
            sceneRepository.getPagedScenesByIds(initialSceneIds!!)
        } else {
            sceneRepository.getPagedScenes(
                sort = type.apiValue,
                direction = direction,
                searchQuery = query,
                tagId = initialTagId,
                performerId = initialPerformerId
            )
        }
    }.cachedIn(viewModelScope)

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSortType(type: SceneSortType) {
        _sortType.value = type
    }

    fun updateSortDirection(direction: String) {
        _sortDirection.value = direction
    }

    fun updateLayoutType(type: SceneLayoutType) {
        _layoutType.value = type
    }
}
