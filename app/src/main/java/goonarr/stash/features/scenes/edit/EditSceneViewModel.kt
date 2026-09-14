package goonarr.stash.features.scenes.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.SceneMarker
import goonarr.stash.core.model.StashID
import goonarr.stash.core.model.Tag
import goonarr.stash.core.network.StashClient
import goonarr.stash.features.components.SharedEditUiState
import goonarr.stash.repositories.scenes.SceneRepository
import goonarr.stash.util.StashUrlFormatter
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class EditSceneViewModel @Inject constructor(
    private val sceneRepository: SceneRepository,
    private val stashClient: StashClient,
    private val settingsStore: SettingsStore,
    private val urlFormatter: StashUrlFormatter
) : ViewModel() {

    private val _uiState = MutableStateFlow<SharedEditUiState>(SharedEditUiState.Loading)
    val uiState: StateFlow<SharedEditUiState> = _uiState.asStateFlow()

    suspend fun getStreamUrl(streamPath: String?): String? {
        return urlFormatter.resolveUrl(streamPath)
    }

    // Form fields
    val title = MutableStateFlow("")
    val details = MutableStateFlow("")
    val director = MutableStateFlow("")
    val code = MutableStateFlow("")
    val url = MutableStateFlow("")
    val rating = MutableStateFlow<Int?>(null)

    // Relationships
    private val _currentPerformers = MutableStateFlow<List<Performer>>(emptyList())
    val currentPerformers = _currentPerformers.asStateFlow()

    private val _currentTags = MutableStateFlow<List<Tag>>(emptyList())
    val currentTags = _currentTags.asStateFlow()

    private val _oHistory = MutableStateFlow<List<String>>(emptyList())
    val oHistory = _oHistory.asStateFlow()

    private val _playHistory = MutableStateFlow<List<String>>(emptyList())
    val playHistory = _playHistory.asStateFlow()

    private val _currentMarkers = MutableStateFlow<List<SceneMarker>>(emptyList())
    val currentMarkers = _currentMarkers.asStateFlow()

    private val _screenshotUrl = MutableStateFlow<String?>(null)
    val screenshotUrl = _screenshotUrl.asStateFlow()

    private val _stashIds = MutableStateFlow<List<StashID>>(emptyList())
    val stashIds = _stashIds.asStateFlow()

    // Removal tracking
    private val removedOHistory = mutableListOf<String>()
    private val removedPlayHistory = mutableListOf<String>()
    private val removedMarkers = mutableListOf<String>()
    private var isCoverDeleted = false

    // Search
    private val _performerSearchText = MutableStateFlow("")
    val performerSearchText = _performerSearchText.asStateFlow()

    private val _performerSearchResults = MutableStateFlow<List<Performer>>(emptyList())
    val performerSearchResults = _performerSearchResults.asStateFlow()

    private val _isSearchingPerformers = MutableStateFlow(false)
    val isSearchingPerformers = _isSearchingPerformers.asStateFlow()

    private val _tagSearchText = MutableStateFlow("")
    val tagSearchText = _tagSearchText.asStateFlow()

    private val _tagSearchResults = MutableStateFlow<List<Tag>>(emptyList())
    val tagSearchResults = _tagSearchResults.asStateFlow()

    private val _isSearchingTags = MutableStateFlow(false)
    val isSearchingTags = _isSearchingTags.asStateFlow()

    private var sceneId: String? = null

    init {
        setupPerformerSearch()
        setupTagSearch()
    }

    fun loadScene(id: String) {
        if (sceneId == id) return
        sceneId = id
        _uiState.value = SharedEditUiState.Loading

        viewModelScope.launch {
            // Optimistically load cached scene details for immediate background rendering
            sceneRepository.getSceneById(id).collect { scene ->
                if (scene != null && _screenshotUrl.value == null) {
                    _screenshotUrl.value = scene.paths?.screenshot
                }
            }
        }

        viewModelScope.launch {
            try {
                val scene = sceneRepository.fetchSceneFromApi(id)
                if (scene != null) {
                    title.value = scene.title ?: ""
                    details.value = scene.details ?: ""
                    director.value = scene.director ?: ""
                    code.value = scene.code ?: ""
                    url.value = scene.url ?: ""
                    rating.value = scene.rating100
                    _currentPerformers.value = scene.performers ?: emptyList()
                    _currentTags.value = scene.tags ?: emptyList()
                    _oHistory.value = scene.oHistory ?: emptyList()
                    _playHistory.value = scene.playHistory ?: emptyList()
                    _currentMarkers.value = scene.sceneMarkers ?: emptyList()
                    _screenshotUrl.value = scene.paths?.screenshot
                    _stashIds.value = scene.stashIds ?: emptyList()

                    _uiState.value = SharedEditUiState.Success
                } else {
                    _uiState.value = SharedEditUiState.Error("Scene not found")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to load scene for editing")
                _uiState.value = SharedEditUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun setupPerformerSearch() {
        viewModelScope.launch {
            _performerSearchText
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _performerSearchResults.value = emptyList()
                        return@collectLatest
                    }

                    _isSearchingPerformers.value = true
                    try {
                        val serverUrl = settingsStore.serverUrl.first()
                        val apiKey = settingsStore.apiKey.first()
                        val (results, _) = stashClient.findPerformers(serverUrl, apiKey, query, page = 1)
                        _performerSearchResults.value = results.filter { p ->
                            currentPerformers.value.none { it.id == p.id }
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Error searching performers")
                    } finally {
                        _isSearchingPerformers.value = false
                    }
                }
        }
    }

    @OptIn(FlowPreview::class)
    private fun setupTagSearch() {
        viewModelScope.launch {
            _tagSearchText
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _tagSearchResults.value = emptyList()
                        return@collectLatest
                    }

                    _isSearchingTags.value = true
                    try {
                        val serverUrl = settingsStore.serverUrl.first()
                        val apiKey = settingsStore.apiKey.first()
                        val (results, _) = stashClient.findTags(serverUrl, apiKey, query, page = 1)
                        _tagSearchResults.value = results.filter { t ->
                            currentTags.value.none { it.id == t.id }
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Error searching tags")
                    } finally {
                        _isSearchingTags.value = false
                    }
                }
        }
    }

    fun updateTitle(text: String) {
        title.value = text
    }

    fun updateDetails(text: String) {
        details.value = text
    }

    fun updateDirector(text: String) {
        director.value = text
    }

    fun updateCode(text: String) {
        code.value = text
    }

    fun updateUrl(text: String) {
        url.value = text
    }

    fun updatePerformerSearch(text: String) {
        _performerSearchText.value = text
    }

    fun updateTagSearch(text: String) {
        _tagSearchText.value = text
    }

    fun addPerformer(performer: Performer) {
        if (_currentPerformers.value.none { it.id == performer.id }) {
            _currentPerformers.value = _currentPerformers.value + performer
            _performerSearchText.value = ""
            _performerSearchResults.value = emptyList()
        }
    }

    fun removePerformer(id: String) {
        _currentPerformers.value = _currentPerformers.value.filter { it.id != id }
    }

    fun addTag(tag: Tag) {
        if (_currentTags.value.none { it.id == tag.id }) {
            _currentTags.value = _currentTags.value + tag
            _tagSearchText.value = ""
            _tagSearchResults.value = emptyList()
        }
    }

    fun removeTag(id: String) {
        _currentTags.value = _currentTags.value.filter { it.id != id }
    }

    fun removeStashId(stashId: String) {
        _stashIds.value = _stashIds.value.filter { it.stashId != stashId }
    }

    fun removeOHistory(timestamp: String) {
        _oHistory.value = _oHistory.value.filter { it != timestamp }
        removedOHistory.add(timestamp)
    }

    fun removePlayHistory(timestamp: String) {
        _playHistory.value = _playHistory.value.filter { it != timestamp }
        removedPlayHistory.add(timestamp)
    }

    fun deleteMarker(markerId: String) {
        val id = sceneId ?: return
        // Optimistic update - remove from list immediately and queue for deletion
        _currentMarkers.value = _currentMarkers.value.filter { it.id != markerId }
        removedMarkers.add(markerId)
    }

    fun save() {
        val id = sceneId ?: return
        _uiState.value = SharedEditUiState.Saving

        viewModelScope.launch {
            try {
                // 1. Delete removed history
                if (removedOHistory.isNotEmpty()) {
                    sceneRepository.deleteOHistory(id, removedOHistory)
                }

                if (removedPlayHistory.isNotEmpty()) {
                    sceneRepository.deletePlayHistory(id, removedPlayHistory)
                }

                // Delete removed markers
                removedMarkers.forEach { markerId ->
                    sceneRepository.deleteMarker(markerId, id)
                }

                // 2. Update metadata
                val success = sceneRepository.updateScene(
                    id = id,
                    title = title.value,
                    details = details.value,
                    performerIds = _currentPerformers.value.map { it.id },
                    tagIds = _currentTags.value.map { it.id },
                    director = director.value.takeIf { it.isNotBlank() },
                    code = code.value.takeIf { it.isNotBlank() },
                    url = url.value.takeIf { it.isNotBlank() },
                    rating = rating.value,
                    coverImage = if (isCoverDeleted) "" else null,
                    stashIds = _stashIds.value.map { it.endpoint to it.stashId }
                )

                if (success != null) {
                    _uiState.value = SharedEditUiState.Saved
                } else {
                    _uiState.value = SharedEditUiState.Error("Failed to save scene changes")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Error saving scene")
                _uiState.value = SharedEditUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun clearError() {
        if (_uiState.value is SharedEditUiState.Error) {
            _uiState.value = SharedEditUiState.Success
        }
    }

    fun deleteCover() {
        // Optimistic update
        _screenshotUrl.value = null
        isCoverDeleted = true
    }
}
