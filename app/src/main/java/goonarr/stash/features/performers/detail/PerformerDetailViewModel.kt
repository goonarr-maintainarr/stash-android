package goonarr.stash.features.performers.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import goonarr.stash.core.di.IoDispatcher
import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.Scene
import goonarr.stash.repositories.performers.PerformerRepository
import goonarr.stash.repositories.scenes.SceneRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * UI state for the performer detail screen.
 */
sealed interface PerformerDetailUiState {
    /** Loading state when fetching initial data. */
    data object Loading : PerformerDetailUiState

    /**
     * Successful state containing performer details and their scenes.
     *
     * @property performer The detailed performer information.
     * @property scenes The list of scenes featuring this performer.
     * @property hasMoreScenes Whether more scenes can be loaded from the network.
     * @property isLoadingMore Whether a pagination request for more scenes is in progress.
     */
    data class Success(
        val performer: Performer,
        val scenes: List<Scene> = emptyList(),
        val hasMoreScenes: Boolean = false,
        val isLoadingMore: Boolean = false
    ) : PerformerDetailUiState

    /** Error state with a displayable message. */
    data class Error(val message: String) : PerformerDetailUiState

    /** State indicating the performer has been successfully deleted. */
    data object Deleted : PerformerDetailUiState
}

/**
 * ViewModel for the Performer Detail screen.
 * Coordinates loading detailed performer metadata and their associated scenes.
 */
@HiltViewModel
class PerformerDetailViewModel @Inject constructor(
    private val performerRepository: PerformerRepository,
    private val sceneRepository: SceneRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<PerformerDetailUiState>(PerformerDetailUiState.Loading)
    val uiState: StateFlow<PerformerDetailUiState> = _uiState.asStateFlow()

    private var currentPerformerId: String? = null
    private var currentPage = 1
    private var totalScenes = 0

    fun refresh(id: String) {
        currentPerformerId = id
        currentPage = 1
        fetchPerformerData(id)
    }

    fun loadPerformer(id: String) {
        val currentState = _uiState.value
        if (currentState is PerformerDetailUiState.Success && currentState.performer.id == id && currentState.scenes.isNotEmpty()) {
            Timber.d("📱 Performer $id already loaded with scenes, skipping reset to preserve scroll position")
            currentPerformerId = id
            return
        }

        Timber.d("📱 Loading performer details for id: $id")
        currentPerformerId = id
        currentPage = 1

        // 1. Observe performer data from repository (DB + Cache)
        viewModelScope.launch(ioDispatcher) {
            performerRepository.getPerformer(id).collect { cachedPerformer ->
                if (cachedPerformer != null) {
                    Timber.d("📦 Showing cached/db performer: ${cachedPerformer.name}")
                    // Keep existing scenes if we have them
                    val existingScenes = (_uiState.value as? PerformerDetailUiState.Success)?.scenes ?: emptyList()
                    _uiState.value = PerformerDetailUiState.Success(cachedPerformer, existingScenes)
                } else {
                    Timber.d("📦 No cached performer found for id: $id")
                }
            }
        }

        fetchPerformerData(id)
    }

    /**
     * Fetches performer details from API and scenes from local database.
     *
     * Performer data is fetched from API to get fresh detailed metrics.
     * Scenes are loaded from local database for faster performance - no network
     * round trip needed. All synced scenes for this performer are loaded at once
     * (no pagination).
     */
    private fun fetchPerformerData(id: String) {
        // Background fetch performer from API and scenes from local database
        viewModelScope.launch(ioDispatcher) {
            try {
                Timber.d("🌐 Background fetching full performer details from API, scenes from local DB for id: $id")
                val fullPerformer = performerRepository.fetchPerformerFromApi(id)

                // Use local database for scenes - much faster than API
                val scenes = sceneRepository.getScenesForPerformer(id)
                totalScenes = sceneRepository.getScenesCountForPerformer(id)

                if (fullPerformer != null) {
                    Timber.d("✅ Got full performer from API: name=${fullPerformer.name}, local scenes=${scenes.size}/$totalScenes")
                    _uiState.value = PerformerDetailUiState.Success(
                        performer = fullPerformer,
                        scenes = scenes,
                        // All local scenes loaded at once
                        hasMoreScenes = false
                    )
                } else if (_uiState.value is PerformerDetailUiState.Loading) {
                    _uiState.value = PerformerDetailUiState.Error("Performer not found")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Error fetching performer/scenes for id: $id")
                if (_uiState.value is PerformerDetailUiState.Loading) {
                    _uiState.value = PerformerDetailUiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }

    fun loadMoreScenes(performerId: String) {
        val currentState = _uiState.value as? PerformerDetailUiState.Success ?: return
        if (currentState.isLoadingMore || !currentState.hasMoreScenes) return

        Timber.d("📱 Loading more scenes for performer $performerId, page ${currentPage + 1}")
        _uiState.value = currentState.copy(isLoadingMore = true)

        viewModelScope.launch(ioDispatcher) {
            try {
                val nextPage = currentPage + 1
                val (newScenes, count) = sceneRepository.fetchScenesForPerformer(
                    performerId,
                    nextPage
                )
                currentPage = nextPage
                totalScenes = count

                val updatedScenes = currentState.scenes + newScenes
                _uiState.value = currentState.copy(
                    scenes = updatedScenes,
                    hasMoreScenes = updatedScenes.size < totalScenes,
                    isLoadingMore = false
                )
                Timber.d("✅ Loaded more scenes: total loaded=${updatedScenes.size}/$totalScenes")
            } catch (e: Exception) {
                Timber.e(e, "❌ Error loading more scenes")
                _uiState.value = currentState.copy(isLoadingMore = false)
            }
        }
    }

    fun setRatingStars(stars: Int) {
        val current = _uiState.value as? PerformerDetailUiState.Success ?: return
        val id = current.performer.id
        val rating100 = if (stars <= 0) -1 else stars * 20
        viewModelScope.launch(ioDispatcher) {
            try {
                val updated = performerRepository.updatePerformer(id = id, rating = rating100)
                if (updated != null) {
                    // Update UI state with new performer
                    _uiState.value = current.copy(performer = updated)
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to update performer rating")
            }
        }
    }

    fun toggleFavorite() {
        val current = _uiState.value as? PerformerDetailUiState.Success ?: return
        val id = current.performer.id
        val newFavoriteStatus = !(current.performer.favorite ?: false)
        viewModelScope.launch(ioDispatcher) {
            try {
                val updated = performerRepository.updatePerformer(id = id, favorite = newFavoriteStatus)
                if (updated != null) {
                    // Update UI state with new performer
                    _uiState.value = current.copy(performer = updated)
                    Timber.d("✅ Performer favorite status updated to $newFavoriteStatus")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to update performer favorite status")
            }
        }
    }

    fun deletePerformer() {
        val currentState = _uiState.value
        if (currentState is PerformerDetailUiState.Success) {
            val id = currentState.performer.id
            viewModelScope.launch {
                Timber.d("🗑️ Deleting performer $id")
                // Keep showing content but maybe show loading overlay (omitted for now)

                try {
                    val success = performerRepository.deletePerformer(id)
                    if (success) {
                        Timber.d("✅ Performer $id deleted successfully")
                        _uiState.value = PerformerDetailUiState.Deleted
                    } else {
                        Timber.e("❌ Failed to delete performer $id")
                        _uiState.value = PerformerDetailUiState.Error("Failed to delete performer")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "❌ Exception deleting performer $id")
                    _uiState.value = PerformerDetailUiState.Error(e.message ?: "Failed to delete performer")
                }
            }
        }
    }
}
