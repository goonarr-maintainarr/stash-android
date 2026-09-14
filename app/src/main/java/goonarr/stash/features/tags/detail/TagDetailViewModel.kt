package goonarr.stash.features.tags.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.Scene
import goonarr.stash.core.model.SceneMarker
import goonarr.stash.core.model.Studio
import goonarr.stash.core.model.Tag
import goonarr.stash.repositories.StudioRepository
import goonarr.stash.repositories.TagRepository
import goonarr.stash.repositories.performers.PerformerRepository
import goonarr.stash.repositories.scenes.SceneRepository
import goonarr.stash.util.StashUrlFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

sealed class TagDetailState {
    data object Loading : TagDetailState()
    data class Content(
        val tag: Tag,
        val scenes: List<Scene> = emptyList(),
        val markers: List<SceneMarker> = emptyList(),
        val performers: List<Performer> = emptyList(),
        val studios: List<Studio> = emptyList(),
        val studioCount: Int = 0,
        val hasMoreScenes: Boolean = false,
        val isLoadingMoreScenes: Boolean = false,
        val hasMoreMarkers: Boolean = false,
        val isLoadingMoreMarkers: Boolean = false,
        val hasMorePerformers: Boolean = false,
        val isLoadingMorePerformers: Boolean = false,
        val hasMoreStudios: Boolean = false,
        val isLoadingMoreStudios: Boolean = false
    ) : TagDetailState()
    data object NotFound : TagDetailState()
    data class Error(val message: String) : TagDetailState()
}

enum class TagDetailTab(val title: String) {
    OVERVIEW("Overview"),
    SCENES("Scenes"),
    MARKERS("Markers"),
    PERFORMERS("Performers"),
    STUDIOS("Studios")
}

@HiltViewModel
class TagDetailViewModel @Inject constructor(
    private val tagRepository: TagRepository,
    private val sceneRepository: SceneRepository,
    private val performerRepository: PerformerRepository,
    private val studioRepository: StudioRepository,
    private val urlFormatter: StashUrlFormatter
) : ViewModel() {

    private val _state = MutableStateFlow<TagDetailState>(TagDetailState.Loading)
    val state: StateFlow<TagDetailState> = _state.asStateFlow()

    private val _selectedTab = MutableStateFlow(TagDetailTab.OVERVIEW)
    val selectedTab: StateFlow<TagDetailTab> = _selectedTab.asStateFlow()

    private val _isFollowed = MutableStateFlow(false)
    val isFollowed: StateFlow<Boolean> = _isFollowed.asStateFlow()

    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess = _deleteSuccess.asStateFlow()

    // Sort/Filter state - kept for potential future use or filtered queries
    // Currently minimal implementation to match request

    private var currentTagId: String? = null
    private var isScenesLoaded = false
    private var isMarkersLoaded = false
    private var isPerformersLoaded = false
    private var isStudiosLoaded = false
    private var scenesPage = 1
    private var markersPage = 1
    private var performersPage = 1
    private var studiosPage = 1
    private var totalScenes = 0
    private var totalMarkers = 0
    private var totalPerformers = 0
    private var totalStudios = 0

    fun loadTag(tagId: String) {
        if (currentTagId == tagId && _state.value is TagDetailState.Content) return

        Timber.d("🏷️ Loading tag details for $tagId")
        currentTagId = tagId
        _state.value = TagDetailState.Loading
        scenesPage = 1
        markersPage = 1
        performersPage = 1
        studiosPage = 1
        isScenesLoaded = false
        isMarkersLoaded = false
        isPerformersLoaded = false
        isStudiosLoaded = false

        viewModelScope.launch {
            try {
                // Check if followed
                launch {
                    tagRepository.followedTagIds.collect {
                        _isFollowed.value = it.contains(tagId)
                    }
                }

                // Initial load: prefer DB, fetch from API if needed
                // We use fetchTag to ensure we have latest details
                tagRepository.fetchTag(tagId)

                tagRepository.getTag(tagId).collect { tag ->
                    if (tag != null) {
                        // If state is already content (from previous emission), update it
                        // If not, transition to Content
                        val currentState = _state.value
                        if (currentState is TagDetailState.Content) {
                            _state.value = currentState.copy(tag = tag)
                        } else {
                            _state.value = TagDetailState.Content(
                                tag = tag
                            )
                            // Load initial data for all tabs to ensure "instant" availability
                            // The user requested: "fetch these after the overview page has loaded"
                            // So we trigger these *after* setting content state.
                            loadPerformersForTag()
                            loadStudiosForTag()

                            // Load tab content if needed (scenes/markers might need separate triggers if not redundant)
                            when (_selectedTab.value) {
                                TagDetailTab.SCENES -> loadScenesForTag()
                                TagDetailTab.MARKERS -> loadMarkersForTag()
                                else -> {} // Performers/Studios already triggered above
                            }
                        }
                    } else {
                        // Only show NotFound if we really have no data and fetch failed?
                        // For flow, getting null might just mean "not in DB yet".
                        // But fetchTag should have populated it.
                        // Let's assume if it remains null it's an issue, but flow might emit null initially.
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Error loading tag $tagId")
                _state.value = TagDetailState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun selectTab(tab: TagDetailTab) {
        _selectedTab.value = tab
        when (tab) {
            TagDetailTab.SCENES -> loadScenesForTag()
            TagDetailTab.MARKERS -> loadMarkersForTag()
            TagDetailTab.PERFORMERS -> loadPerformersForTag() // Ensure loaded if specific tab selected
            TagDetailTab.STUDIOS -> loadStudiosForTag()
            else -> {}
        }
    }

    private fun loadScenesForTag() {
        val currentState = _state.value as? TagDetailState.Content ?: return
        val id = currentTagId ?: return
        if (isScenesLoaded || currentState.isLoadingMoreScenes) return

        Timber.d("🎬 Lazy loading scenes for tag $id")
        _state.value = currentState.copy(isLoadingMoreScenes = true)

        viewModelScope.launch {
            try {
                val (scenes, sCount) = sceneRepository.fetchScenesForTag(id, 1)
                totalScenes = sCount
                scenesPage = 1
                isScenesLoaded = true

                _state.value = (_state.value as? TagDetailState.Content)?.copy(
                    scenes = scenes,
                    hasMoreScenes = scenes.size < totalScenes,
                    isLoadingMoreScenes = false
                ) ?: return@launch
            } catch (e: Exception) {
                Timber.e(e, "❌ Error lazy loading scenes for tag $id")
                _state.value = (_state.value as? TagDetailState.Content)?.copy(isLoadingMoreScenes = false) ?: return@launch
            }
        }
    }

    fun loadMoreScenes() {
        val currentState = _state.value as? TagDetailState.Content ?: return
        val id = currentTagId ?: return
        if (currentState.isLoadingMoreScenes || !currentState.hasMoreScenes) return

        _state.value = currentState.copy(isLoadingMoreScenes = true)

        viewModelScope.launch {
            try {
                val nextPage = scenesPage + 1
                val (newScenes, count) = sceneRepository.fetchScenesForTag(id, nextPage)
                scenesPage = nextPage
                totalScenes = count

                _state.value = (_state.value as? TagDetailState.Content)?.copy(
                    scenes = currentState.scenes + newScenes,
                    hasMoreScenes = (currentState.scenes.size + newScenes.size) < totalScenes,
                    isLoadingMoreScenes = false
                ) ?: return@launch
            } catch (e: Exception) {
                Timber.e(e, "❌ Error loading more scenes")
                _state.value = (_state.value as? TagDetailState.Content)?.copy(isLoadingMoreScenes = false) ?: return@launch
            }
        }
    }

    private fun loadMarkersForTag() {
        val currentState = _state.value as? TagDetailState.Content ?: return
        val id = currentTagId ?: return
        if (isMarkersLoaded || currentState.isLoadingMoreMarkers) return

        Timber.d("📍 Lazy loading markers for tag $id")
        _state.value = currentState.copy(isLoadingMoreMarkers = true)

        viewModelScope.launch {
            try {
                // fetchSceneMarkersForTag returns markers and count
                val (markers, mCount) = sceneRepository.fetchSceneMarkersForTag(id, 1)
                totalMarkers = mCount
                markersPage = 1
                isMarkersLoaded = true

                _state.value = (_state.value as? TagDetailState.Content)?.copy(
                    markers = markers,
                    hasMoreMarkers = markers.size < totalMarkers,
                    isLoadingMoreMarkers = false
                ) ?: return@launch
            } catch (e: Exception) {
                Timber.e(e, "❌ Error lazy loading markers for tag $id")
                _state.value = (_state.value as? TagDetailState.Content)?.copy(isLoadingMoreMarkers = false) ?: return@launch
            }
        }
    }

    fun loadMoreMarkers() {
        val currentState = _state.value as? TagDetailState.Content ?: return
        val id = currentTagId ?: return
        if (currentState.isLoadingMoreMarkers || !currentState.hasMoreMarkers) return

        _state.value = currentState.copy(isLoadingMoreMarkers = true)

        viewModelScope.launch {
            try {
                val nextPage = markersPage + 1
                val (newMarkers, count) = sceneRepository.fetchSceneMarkersForTag(id, nextPage)
                markersPage = nextPage
                totalMarkers = count

                _state.value = (_state.value as? TagDetailState.Content)?.copy(
                    markers = currentState.markers + newMarkers,
                    hasMoreMarkers = (currentState.markers.size + newMarkers.size) < totalMarkers,
                    isLoadingMoreMarkers = false
                ) ?: return@launch
            } catch (e: Exception) {
                Timber.e(e, "❌ Error loading more markers")
                _state.value = (_state.value as? TagDetailState.Content)?.copy(isLoadingMoreMarkers = false) ?: return@launch
            }
        }
    }

    private fun loadPerformersForTag() {
        val currentState = _state.value as? TagDetailState.Content ?: return
        val id = currentTagId ?: return
        if (isPerformersLoaded || currentState.isLoadingMorePerformers) return

        Timber.d("🎭 Lazy loading performers for tag $id")
        _state.value = currentState.copy(isLoadingMorePerformers = true)

        viewModelScope.launch {
            try {
                val (performers, count) = performerRepository.fetchPerformersForTag(id, 1)
                totalPerformers = count
                performersPage = 1
                isPerformersLoaded = true

                _state.value = (_state.value as? TagDetailState.Content)?.copy(
                    performers = performers,
                    hasMorePerformers = performers.size < totalPerformers,
                    isLoadingMorePerformers = false
                ) ?: return@launch
            } catch (e: Exception) {
                Timber.e(e, "❌ Error lazy loading performers for tag $id")
                _state.value = (_state.value as? TagDetailState.Content)?.copy(isLoadingMorePerformers = false) ?: return@launch
            }
        }
    }

    fun loadMorePerformers() {
        val currentState = _state.value as? TagDetailState.Content ?: return
        val id = currentTagId ?: return
        if (currentState.isLoadingMorePerformers || !currentState.hasMorePerformers) return

        _state.value = currentState.copy(isLoadingMorePerformers = true)

        viewModelScope.launch {
            try {
                val nextPage = performersPage + 1
                val (newPerformers, count) = performerRepository.fetchPerformersForTag(id, nextPage)
                performersPage = nextPage
                totalPerformers = count

                _state.value = (_state.value as? TagDetailState.Content)?.copy(
                    performers = currentState.performers + newPerformers,
                    hasMorePerformers = (currentState.performers.size + newPerformers.size) < totalPerformers,
                    isLoadingMorePerformers = false
                ) ?: return@launch
            } catch (e: Exception) {
                Timber.e(e, "❌ Error loading more performers")
                _state.value = (_state.value as? TagDetailState.Content)?.copy(isLoadingMorePerformers = false) ?: return@launch
            }
        }
    }

    private fun loadStudiosForTag() {
        val currentState = _state.value as? TagDetailState.Content ?: return
        val id = currentTagId ?: return
        if (isStudiosLoaded || currentState.isLoadingMoreStudios) return

        Timber.d("🏢 Lazy loading studios for tag $id")
        _state.value = currentState.copy(isLoadingMoreStudios = true)

        viewModelScope.launch {
            try {
                val (studios, count) = studioRepository.fetchStudiosForTag(id, 1)
                totalStudios = count
                studiosPage = 1
                isStudiosLoaded = true

                _state.value = (_state.value as? TagDetailState.Content)?.copy(
                    studios = studios,
                    studioCount = count,
                    hasMoreStudios = studios.size < totalStudios,
                    isLoadingMoreStudios = false
                ) ?: return@launch
            } catch (e: Exception) {
                Timber.e(e, "❌ Error lazy loading studios for tag $id")
                _state.value = (_state.value as? TagDetailState.Content)?.copy(isLoadingMoreStudios = false) ?: return@launch
            }
        }
    }

    fun loadMoreStudios() {
        val currentState = _state.value as? TagDetailState.Content ?: return
        val id = currentTagId ?: return
        if (currentState.isLoadingMoreStudios || !currentState.hasMoreStudios) return

        _state.value = currentState.copy(isLoadingMoreStudios = true)

        viewModelScope.launch {
            try {
                val nextPage = studiosPage + 1
                val (newStudios, count) = studioRepository.fetchStudiosForTag(id, nextPage)
                studiosPage = nextPage
                totalStudios = count

                _state.value = (_state.value as? TagDetailState.Content)?.copy(
                    studios = currentState.studios + newStudios,
                    studioCount = count,
                    hasMoreStudios = (currentState.studios.size + newStudios.size) < totalStudios,
                    isLoadingMoreStudios = false
                ) ?: return@launch
            } catch (e: Exception) {
                Timber.e(e, "❌ Error loading more studios")
                _state.value = (_state.value as? TagDetailState.Content)?.copy(isLoadingMoreStudios = false) ?: return@launch
            }
        }
    }

    fun toggleFavorite() {
        val currentState = _state.value as? TagDetailState.Content ?: return
        val currentTag = currentState.tag
        val newFavorite = !(currentTag.favorite ?: false)

        viewModelScope.launch {
            // Update server and local DB
            tagRepository.updateTag(
                id = currentTag.id,
                favorite = newFavorite
            )
        }
    }

    fun toggleFollow() {
        val tagId = currentTagId ?: return
        viewModelScope.launch {
            // Logic handled by repository, we just trigger it.
            // isFollowed flow will update automatically.
            val followedIds = tagRepository.followedTagIds.first()
            if (followedIds.contains(tagId)) {
                tagRepository.removeFollowedTag(tagId)
            } else {
                tagRepository.addFollowedTag(tagId)
            }
        }
    }

    fun deleteTag() {
        val id = currentTagId ?: return
        viewModelScope.launch {
            val success = tagRepository.deleteTag(id)
            if (success) {
                _deleteSuccess.value = true
            }
        }
    }

    suspend fun getStreamUrl(streamPath: String?): String? {
        return urlFormatter.resolveUrl(streamPath)
    }
}
