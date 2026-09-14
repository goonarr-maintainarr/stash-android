package goonarr.stash.features.scenes.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.di.IoDispatcher
import goonarr.stash.core.model.Scene
import goonarr.stash.features.scenes.SceneLayoutType
import goonarr.stash.features.scenes.SceneSortType
import goonarr.stash.repositories.scenes.SceneRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class SceneListViewModel @Inject constructor(
    private val sceneRepository: SceneRepository,
    private val settingsStore: SettingsStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val totalCount: Flow<Int?> = sceneRepository.totalScenesCount.map { it as Int? }

    val sortType: StateFlow<SceneSortType> = settingsStore.sceneSortType.map { apiValue ->
        SceneSortType.fromApiValue(apiValue)
    }.flowOn(ioDispatcher)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SceneSortType.DATE)

    val sortDirection: StateFlow<String> = settingsStore.sceneSortDirection
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "DESC")

    val layoutType: StateFlow<SceneLayoutType> = settingsStore.sceneViewType.map { value ->
        SceneLayoutType.fromString(value)
    }.flowOn(ioDispatcher)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SceneLayoutType.LIST)

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val scenes: Flow<PagingData<Scene>> = combine(
        sortType,
        sortDirection,
        // Debounce search to avoid too many API calls
        _searchQuery.debounce(300)
    ) { type, direction, query ->
        Triple(type, direction, query)
    }.flatMapLatest { (type, direction, query) ->
        Timber.d("📱 SceneListViewModel: Refreshing scenes flow (sort=${type.apiValue}, direction=$direction, search='$query')")
        sceneRepository.getPagedScenes(
            sort = type.apiValue,
            direction = direction,
            searchQuery = query
        )
    }
        .cachedIn(viewModelScope)

    fun updateSearchQuery(query: String) {
        Timber.d("📱 SceneListViewModel: Updating search query to '$query'")
        _searchQuery.value = query
    }

    fun updateSortType(type: SceneSortType) {
        Timber.d("📱 SceneListViewModel: Updating sort type to $type")
        viewModelScope.launch(ioDispatcher) {
            val currentDirection = settingsStore.sceneSortDirection.first()
            settingsStore.setSceneSort(type.apiValue, currentDirection)
        }
    }

    fun updateSortDirection(direction: String) {
        Timber.d("📱 SceneListViewModel: Updating sort direction to $direction")
        viewModelScope.launch(ioDispatcher) {
            val currentType = settingsStore.sceneSortType.first()
            settingsStore.setSceneSort(currentType, direction)
        }
    }

    fun updateLayoutType(type: SceneLayoutType) {
        Timber.d("📱 SceneListViewModel: Updating layout type to $type")
        viewModelScope.launch(ioDispatcher) {
            settingsStore.setSceneViewType(type.name)
        }
    }
}
