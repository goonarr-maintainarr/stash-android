package goonarr.stash.features.studios.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.di.IoDispatcher
import goonarr.stash.core.model.Studio
import goonarr.stash.features.studios.StudioSortType
import goonarr.stash.features.studios.StudioViewType
import goonarr.stash.repositories.StudioRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

sealed class StudioListState {
    data object Loading : StudioListState()
    data class Content(val studios: List<Studio>, val totalCount: Int) : StudioListState()
    data object Empty : StudioListState()
    data class Error(val message: String) : StudioListState()
}

@HiltViewModel
class StudioListViewModel @Inject constructor(
    private val repository: StudioRepository,
    private val settingsStore: SettingsStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _state = MutableStateFlow<StudioListState>(StudioListState.Loading)
    val state: StateFlow<StudioListState> = _state.asStateFlow()

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()

    val sortType: StateFlow<StudioSortType> = settingsStore.studioSortType
        .map { apiValue -> StudioSortType.entries.find { it.value == apiValue } ?: StudioSortType.NAME }
        .stateIn(viewModelScope, SharingStarted.Lazily, StudioSortType.NAME)

    val sortDirection: StateFlow<String> = settingsStore.studioSortDirection
        .stateIn(viewModelScope, SharingStarted.Lazily, "ASC")

    val viewType: StateFlow<StudioViewType> = settingsStore.studioViewType
        .map { StudioViewType.fromString(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StudioViewType.GRID)

    private var currentPage = 1
    private var totalCount = 0
    private var allStudios = mutableListOf<Studio>()
    private var isLoadingMore = false

    init {
        viewModelScope.launch {
            combine(
                searchText,
                settingsStore.studioSortType,
                settingsStore.studioSortDirection
            ) { _, _, _ ->
                Unit
            }.collect {
                fetchStudios(reset = true)
            }
        }
    }

    fun onSearchTextChange(text: String) {
        if (_searchText.value == text) return
        _searchText.value = text
    }

    fun updateSortType(type: StudioSortType) {
        viewModelScope.launch(ioDispatcher) {
            val currentDirection = settingsStore.studioSortDirection.first()
            settingsStore.setStudioSort(type.value, currentDirection)
        }
    }

    fun updateSortDirection(direction: String) {
        viewModelScope.launch(ioDispatcher) {
            val currentType = settingsStore.studioSortType.first()
            settingsStore.setStudioSort(currentType, direction)
        }
    }

    fun updateViewType(type: StudioViewType) {
        viewModelScope.launch(ioDispatcher) {
            settingsStore.setStudioViewType(type.name)
        }
    }

    fun refresh() {
        fetchStudios(reset = true)
    }

    fun loadMore() {
        if (isLoadingMore || allStudios.size >= totalCount) return
        fetchStudios(reset = false)
    }

    private fun fetchStudios(reset: Boolean) {
        if (reset) {
            currentPage = 1
            allStudios.clear()
            _state.value = StudioListState.Loading
        }

        isLoadingMore = true

        viewModelScope.launch {
            try {
                val (studios, count) = repository.fetchStudios(
                    searchText = _searchText.value,
                    page = currentPage,
                    sort = sortType.value.value,
                    direction = sortDirection.value
                )
                totalCount = count

                // Filter out duplicates to prevent key collision in LazyVerticalGrid
                val newUniqueStudios = studios.filter { newStudio ->
                    allStudios.none { it.id == newStudio.id }
                }
                allStudios.addAll(newUniqueStudios)

                _state.value = if (allStudios.isEmpty()) {
                    StudioListState.Empty
                } else {
                    StudioListState.Content(allStudios.toList(), totalCount)
                }

                currentPage++
                Timber.d("📺 Loaded ${allStudios.size}/$totalCount studios")
            } catch (e: Exception) {
                Timber.e(e, "❌ Error loading studios")
                _state.value = StudioListState.Error(e.message ?: "Unknown error")
            } finally {
                isLoadingMore = false
            }
        }
    }
}
