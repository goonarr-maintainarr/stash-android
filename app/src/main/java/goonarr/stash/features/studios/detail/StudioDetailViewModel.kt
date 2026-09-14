package goonarr.stash.features.studios.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.Scene
import goonarr.stash.core.model.Studio
import goonarr.stash.repositories.StudioRepository
import goonarr.stash.repositories.performers.PerformerRepository
import goonarr.stash.repositories.scenes.SceneRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

sealed class StudioDetailState {
    data object Loading : StudioDetailState()
    data class Content(
        val studio: Studio,
        val scenes: List<Scene> = emptyList(),
        val performers: List<Performer> = emptyList(),
        val hasMoreScenes: Boolean = false,
        val isLoadingMoreScenes: Boolean = false,
        val hasMorePerformers: Boolean = false,
        val isLoadingMorePerformers: Boolean = false
    ) : StudioDetailState()
    data object NotFound : StudioDetailState()
    data class Error(val message: String) : StudioDetailState()
}

enum class StudioDetailTab(val title: String) {
    OVERVIEW("Overview"),
    SCENES("Scenes"),
    PERFORMERS("Performers")
}

/**
 * ViewModel for the [StudioDetailScreen].
 *
 * Manages the UI state, tab selection, and pagination for scenes and performers
 * related to a specific studio.
 */
@HiltViewModel
class StudioDetailViewModel @Inject constructor(
    private val studioRepository: StudioRepository,
    private val sceneRepository: SceneRepository,
    private val performerRepository: PerformerRepository
) : ViewModel() {

    private val _state = MutableStateFlow<StudioDetailState>(StudioDetailState.Loading)

    /** Current detailed state of the studio. */
    val state: StateFlow<StudioDetailState> = _state.asStateFlow()

    private val _selectedTab = MutableStateFlow(StudioDetailTab.OVERVIEW)

    /** Currently selected tab (Overview, Scenes, or Performers). */
    val selectedTab: StateFlow<StudioDetailTab> = _selectedTab.asStateFlow()

    private var currentStudioId: String? = null
    private var isScenesLoaded = false
    private var isPerformersLoaded = false
    private var scenesPage = 1
    private var performersPage = 1
    private var totalScenes = 0
    private var totalPerformers = 0

    /**
     * Loads the studio details. Scenes and performers are now loaded lazily
     * when their respective tabs are selected.
     *
     * @param id The studio ID.
     */
    fun loadStudio(id: String) {
        if (currentStudioId == id && _state.value is StudioDetailState.Content) return

        Timber.d("📂 Loading studio details for $id")
        currentStudioId = id
        _state.value = StudioDetailState.Loading
        scenesPage = 1
        performersPage = 1
        isScenesLoaded = false
        isPerformersLoaded = false

        viewModelScope.launch {
            try {
                val studio = studioRepository.fetchStudio(id)
                if (studio != null) {
                    _state.value = StudioDetailState.Content(
                        studio = studio,
                        scenes = emptyList(),
                        performers = emptyList(),
                        hasMoreScenes = false,
                        hasMorePerformers = false
                    )
                    // If we were already on a non-overview tab (e.g. from process death restore), load it
                    when (_selectedTab.value) {
                        StudioDetailTab.SCENES -> loadScenesForStudio()
                        StudioDetailTab.PERFORMERS -> loadPerformersForStudio()
                        else -> {}
                    }
                } else {
                    _state.value = StudioDetailState.NotFound
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Error loading studio $id")
                _state.value = StudioDetailState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun loadScenesForStudio() {
        val currentState = _state.value as? StudioDetailState.Content ?: return
        val id = currentStudioId ?: return
        if (isScenesLoaded || currentState.isLoadingMoreScenes) return

        Timber.d("🎬 Lazy loading scenes for studio $id")
        _state.value = currentState.copy(isLoadingMoreScenes = true)

        viewModelScope.launch {
            try {
                val (scenes, sCount) = sceneRepository.fetchScenesForStudio(id, 1)
                totalScenes = sCount
                scenesPage = 1
                isScenesLoaded = true

                _state.value = (_state.value as? StudioDetailState.Content)?.copy(
                    scenes = scenes,
                    hasMoreScenes = scenes.size < totalScenes,
                    isLoadingMoreScenes = false
                ) ?: return@launch
            } catch (e: Exception) {
                Timber.e(e, "❌ Error lazy loading scenes for studio $id")
                _state.value = (_state.value as? StudioDetailState.Content)?.copy(isLoadingMoreScenes = false) ?: return@launch
            }
        }
    }

    private fun loadPerformersForStudio() {
        val currentState = _state.value as? StudioDetailState.Content ?: return
        val id = currentStudioId ?: return
        if (isPerformersLoaded || currentState.isLoadingMorePerformers) return

        Timber.d("👥 Lazy loading performers for studio $id")
        _state.value = currentState.copy(isLoadingMorePerformers = true)

        viewModelScope.launch {
            try {
                val (performers, pCount) = performerRepository.fetchPerformersForStudio(id, 1)
                totalPerformers = pCount
                performersPage = 1
                isPerformersLoaded = true

                _state.value = (_state.value as? StudioDetailState.Content)?.copy(
                    performers = performers,
                    hasMorePerformers = performers.size < totalPerformers,
                    isLoadingMorePerformers = false
                ) ?: return@launch
            } catch (e: Exception) {
                Timber.e(e, "❌ Error lazy loading performers for studio $id")
                _state.value = (_state.value as? StudioDetailState.Content)?.copy(isLoadingMorePerformers = false) ?: return@launch
            }
        }
    }

    fun loadMoreScenes() {
        val currentState = _state.value as? StudioDetailState.Content ?: return
        val id = currentStudioId ?: return
        if (currentState.isLoadingMoreScenes || !currentState.hasMoreScenes) return

        _state.value = currentState.copy(isLoadingMoreScenes = true)

        viewModelScope.launch {
            try {
                val nextPage = scenesPage + 1
                val (newScenes, count) = sceneRepository.fetchScenesForStudio(id, nextPage)
                scenesPage = nextPage
                totalScenes = count

                _state.value = (_state.value as? StudioDetailState.Content)?.copy(
                    scenes = currentState.scenes + newScenes,
                    hasMoreScenes = (currentState.scenes.size + newScenes.size) < totalScenes,
                    isLoadingMoreScenes = false
                ) ?: return@launch
            } catch (e: Exception) {
                Timber.e(e, "❌ Error loading more scenes")
                _state.value = (_state.value as? StudioDetailState.Content)?.copy(isLoadingMoreScenes = false) ?: return@launch
            }
        }
    }

    fun loadMorePerformers() {
        val currentState = _state.value as? StudioDetailState.Content ?: return
        val id = currentStudioId ?: return
        if (currentState.isLoadingMorePerformers || !currentState.hasMorePerformers) return

        _state.value = currentState.copy(isLoadingMorePerformers = true)

        viewModelScope.launch {
            try {
                val nextPage = performersPage + 1
                val (newPerformers, count) = performerRepository.fetchPerformersForStudio(id, nextPage)
                performersPage = nextPage
                totalPerformers = count

                _state.value = (_state.value as? StudioDetailState.Content)?.copy(
                    performers = currentState.performers + newPerformers,
                    hasMorePerformers = (currentState.performers.size + newPerformers.size) < totalPerformers,
                    isLoadingMorePerformers = false
                ) ?: return@launch
            } catch (e: Exception) {
                Timber.e(e, "❌ Error loading more performers")
                _state.value = (_state.value as? StudioDetailState.Content)?.copy(isLoadingMorePerformers = false) ?: return@launch
            }
        }
    }

    fun selectTab(tab: StudioDetailTab) {
        _selectedTab.value = tab
        when (tab) {
            StudioDetailTab.SCENES -> loadScenesForStudio()
            StudioDetailTab.PERFORMERS -> loadPerformersForStudio()
            else -> {}
        }
    }

    /**
     * Soft refresh that preserves existing content while fetching new data.
     * This avoids showing a loading spinner when navigating back to this screen.
     *
     * @param id The studio ID.
     */
    fun refresh(id: String) {
        // If we already have content, do a soft refresh without showing loading state
        val currentState = _state.value
        if (currentState is StudioDetailState.Content && currentStudioId == id) {
            Timber.d("📂 Soft refreshing studio details for $id")
            viewModelScope.launch {
                try {
                    val studio = studioRepository.fetchStudio(id)
                    if (studio != null) {
                        // Reset pagination and data when we refresh
                        // Alternatively, we could only refresh what's currently visible
                        isScenesLoaded = false
                        isPerformersLoaded = false

                        _state.value = currentState.copy(
                            studio = studio,
                            scenes = emptyList(),
                            performers = emptyList(),
                            hasMoreScenes = false,
                            hasMorePerformers = false
                        )

                        // Re-trigger lazy load for current tab if needed
                        when (_selectedTab.value) {
                            StudioDetailTab.SCENES -> loadScenesForStudio()
                            StudioDetailTab.PERFORMERS -> loadPerformersForStudio()
                            else -> {}
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "❌ Error refreshing studio $id")
                    // Keep existing content on error
                }
            }
        } else {
            // No existing content, do a full load
            currentStudioId = null
            loadStudio(id)
        }
    }

    /**
     * Deletes the current studio.
     * On success, sets [deleteSuccess] to true so the UI can navigate away.
     */
    fun deleteStudio() {
        val id = currentStudioId ?: return
        Timber.d("🗑️ Deleting studio $id")
        _state.value = StudioDetailState.Loading

        viewModelScope.launch {
            try {
                val success = studioRepository.deleteStudio(id)
                if (success) {
                    Timber.d("✅ Successfully deleted studio $id")
                    _deleteSuccess.value = true
                } else {
                    Timber.e("❌ Failed to delete studio $id")
                    _state.value = StudioDetailState.Error("Failed to delete studio")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Error deleting studio $id")
                _state.value = StudioDetailState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private val _deleteSuccess = MutableStateFlow(false)

    /**
     * Emits true when the studio has been successfully deleted.
     * The UI should observe this and navigate away.
     */
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess.asStateFlow()

    /**
     * Updates the studio rating locally and remotely.
     *
     * @param rating The new rating (0-5).
     */
    fun updateRating(rating: Int) {
        val currentState = _state.value as? StudioDetailState.Content ?: return
        val id = currentState.studio.id
        val rating100 = rating * 20

        // Optimistic update
        _state.value = currentState.copy(
            studio = currentState.studio.copy(rating100 = rating100)
        )

        viewModelScope.launch {
            try {
                studioRepository.updateStudio(id = id, rating100 = rating100)
                // We could refresh here to ensure consistency, but optimistic update is smoother.
                // If update fails, we might want to revert, but for now we'll just log.
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to update rating for studio $id")
                // Revert on failure?
                // _state.value = currentState
            }
        }
    }

    /**
     * Toggles the favorite status of the current studio.
     */
    fun toggleFavorite() {
        val currentState = _state.value as? StudioDetailState.Content ?: return
        val id = currentState.studio.id
        val newFavoriteStatus = !(currentState.studio.favorite ?: false)

        // Optimistic update
        _state.value = currentState.copy(
            studio = currentState.studio.copy(favorite = newFavoriteStatus)
        )

        viewModelScope.launch {
            try {
                studioRepository.updateStudio(id = id, favorite = newFavoriteStatus)
                Timber.d("✅ Updated studio favorite status to $newFavoriteStatus")
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to update favorite status for studio $id")
                // Revert on failure
                _state.value = currentState
            }
        }
    }
}
