package goonarr.stash.features.performers.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.di.IoDispatcher
import goonarr.stash.core.model.Performer
import goonarr.stash.features.performers.PerformerSortType
import goonarr.stash.features.performers.PerformerViewType
import goonarr.stash.repositories.performers.PerformerRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

sealed interface PerformerListUiState {
    data object Loading : PerformerListUiState
    data class Success(val performers: Flow<PagingData<Performer>>) : PerformerListUiState
    data class Error(val message: String) : PerformerListUiState
}

/**
 * ViewModel for the Performer List screen.
 * Manages performer data, sorting, filtering, and UI state persistence.
 *
 * @param performerRepository Repository for performer data operations.
 * @param settingsStore Store for app settings.
 * @param ioDispatcher Dispatcher for IO operations.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class PerformerListViewModel @Inject constructor(
    private val performerRepository: PerformerRepository,
    private val settingsStore: SettingsStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val totalCount: Flow<Int?> = performerRepository.totalPerformersCount.map { it as Int? }

    val sortType: Flow<PerformerSortType> = settingsStore.performerSortType.map { apiValue ->
        PerformerSortType.entries.find { it.apiValue == apiValue } ?: PerformerSortType.NAME
    }.flowOn(ioDispatcher)

    val sortDirection: Flow<String> = settingsStore.performerSortDirection

    val viewType: Flow<PerformerViewType> = settingsStore.performerViewType.map { value ->
        PerformerViewType.fromString(value)
    }.flowOn(ioDispatcher)

    val performers: Flow<PagingData<Performer>> = combine(
        sortType.distinctUntilChanged(),
        sortDirection.distinctUntilChanged(),
        // Debounce search to avoid too many API calls
        _searchQuery.debounce(300).distinctUntilChanged()
    ) { type, direction, query ->
        Triple(type, direction, query)
    }.flatMapLatest { (type, direction, query) ->
        Timber.d("📱 PerformerListViewModel: Refreshing performers flow (sort=$type, direction=$direction, search='$query')")
        performerRepository.getPagedPerformers(
            sort = type.apiValue,
            direction = direction,
            searchQuery = query
        )
    }.cachedIn(viewModelScope)

    /**
     * Updates the current search query.
     */
    fun updateSearchQuery(query: String) {
        Timber.d("📱 PerformerListViewModel: Updating search query to '$query'")
        _searchQuery.value = query
    }

    /**
     * Updates the sort type in settings.
     */
    fun updateSortType(type: PerformerSortType) {
        Timber.d("📱 PerformerListViewModel: Updating sort type to $type")
        viewModelScope.launch(ioDispatcher) {
            val currentDirection = settingsStore.performerSortDirection.first()
            settingsStore.setPerformerSort(type.apiValue, currentDirection)
        }
    }

    /**
     * Updates the sort direction in settings.
     */
    fun updateSortDirection(direction: String) {
        Timber.d("📱 PerformerListViewModel: Updating sort direction to $direction")
        viewModelScope.launch(ioDispatcher) {
            val currentType = settingsStore.performerSortType.first()
            settingsStore.setPerformerSort(currentType, direction)
        }
    }

    /**
     * Updates the visual layout type in settings.
     */
    fun updateViewType(type: PerformerViewType) {
        Timber.d("📱 PerformerListViewModel: Updating view type to $type")
        viewModelScope.launch(ioDispatcher) {
            settingsStore.setPerformerViewType(type.name)
        }
    }
}
