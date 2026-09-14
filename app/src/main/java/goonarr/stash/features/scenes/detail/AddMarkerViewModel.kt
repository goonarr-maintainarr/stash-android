package goonarr.stash.features.scenes.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import goonarr.stash.features.scenes.utils.SpriteManager
import goonarr.stash.repositories.TagRepository
import goonarr.stash.repositories.scenes.SceneRepository
import goonarr.stash.util.StashUrlFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for the "Add Marker" flow.
 *
 * This ViewModel manages the state for creating new markers within a scene, including
 * loading scene details, fetching all available tags, and interacting with the repository
 * to persist the new marker.
 *
 * @property sceneRepository Repository for scene-related data and marker creation.
 * @property tagRepository Repository for fetching the global tag list.
 * @property urlFormatter Utility for resolving media stream URLs.
 * @property savedStateHandle Handle for retrieving navigation arguments (`sceneId`, `initialSeconds`).
 */

@HiltViewModel
class AddMarkerViewModel @Inject constructor(
    private val sceneRepository: SceneRepository,
    private val tagRepository: TagRepository,
    private val urlFormatter: StashUrlFormatter,
    val spriteManager: SpriteManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val sceneId: String = checkNotNull(savedStateHandle["sceneId"])
    val initialSeconds: Double = (savedStateHandle.get<String>("initialSeconds")?.toDoubleOrNull()) ?: 0.0

    private val _uiState = MutableStateFlow<SceneDetailUiState>(SceneDetailUiState.Loading)
    val uiState: StateFlow<SceneDetailUiState> = _uiState.asStateFlow()

    private val _screenshotUrl = MutableStateFlow<String?>(null)
    val screenshotUrl: StateFlow<String?> = _screenshotUrl.asStateFlow()

    init {
        // Optimistically load cached scene details for immediate background rendering
        viewModelScope.launch {
            sceneRepository.getSceneById(sceneId).collect { scene ->
                if (scene != null && _screenshotUrl.value == null) {
                    _screenshotUrl.value = scene.paths?.screenshot
                }
            }
        }
        loadScene()
    }

    private fun loadScene() {
        viewModelScope.launch {
            try {
                Timber.d("📍 AddMarkerViewModel: Loading scene $sceneId")
                // Fetch full scene details for tags
                val scene = sceneRepository.fetchSceneFromApi(sceneId)
                val allTags = tagRepository.getAllTags()

                if (scene != null) {
                    _uiState.value = SceneDetailUiState.Success(scene, allTags)

                    // Initialize sprite manager for scrubbing previews
                    val vttUrl = urlFormatter.resolveUrl(scene.paths?.vtt)
                    val spriteUrl = urlFormatter.resolveUrl(scene.paths?.sprite)
                    if (vttUrl != null && spriteUrl != null) {
                        spriteManager.initialize(vttUrl, spriteUrl)
                    }
                } else {
                    _uiState.value = SceneDetailUiState.Error("Scene not found")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Error loading scene for marker")
                _uiState.value = SceneDetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    suspend fun getStreamUrl(streamPath: String?): String? {
        return urlFormatter.resolveUrl(streamPath)
    }

    /**
     * Creates a new scene marker.
     *
     * @param title The display title for the marker.
     * @param seconds The start position of the marker in seconds.
     * @param endSeconds The optional end position of the marker in seconds.
     * @param primaryTagId The ID of the required primary tag.
     * @param additionalTagIds List of IDs for optional additional tags.
     * @param onSuccess Callback invoked when the marker is successfully created.
     */
    fun createMarker(
        title: String,
        seconds: Double,
        endSeconds: Double?,
        primaryTagId: String,
        additionalTagIds: List<String>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            Timber.d("📍 Creating marker: $title at $seconds")
            val marker = sceneRepository.createMarker(
                sceneId = sceneId,
                title = title,
                seconds = seconds,
                endSeconds = endSeconds,
                primaryTagId = primaryTagId,
                tagIds = additionalTagIds,
            )
            if (marker != null) {
                Timber.d("✅ Marker created successfully")
                onSuccess()
            }
        }
    }
}
