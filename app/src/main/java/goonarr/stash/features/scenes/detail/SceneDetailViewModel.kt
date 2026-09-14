package goonarr.stash.features.scenes.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import goonarr.stash.core.model.Scene
import goonarr.stash.core.model.Tag
import goonarr.stash.repositories.scenes.SceneRepository
import goonarr.stash.util.StashUrlFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

sealed interface SceneDetailUiState {
    data object Loading : SceneDetailUiState
    data class Success(val scene: Scene, val allTags: List<Tag> = emptyList()) : SceneDetailUiState
    data class Error(val message: String) : SceneDetailUiState
    data object Deleted : SceneDetailUiState
}

@HiltViewModel
class SceneDetailViewModel @Inject constructor(
    private val sceneRepository: SceneRepository,
    private val urlFormatter: StashUrlFormatter,
    val spriteManager: goonarr.stash.features.scenes.utils.SpriteManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<SceneDetailUiState>(SceneDetailUiState.Loading)
    val uiState: StateFlow<SceneDetailUiState> = _uiState.asStateFlow()

    suspend fun getStreamUrl(streamPath: String?): String? {
        Timber.d("🎥 Resolving stream URL for path: $streamPath")
        val url = urlFormatter.resolveUrl(streamPath)
        Timber.d("🎥 Resolved stream URL: $url")
        return url
    }

    fun loadScene(id: String, forceRefresh: Boolean = false) {
        val currentState = _uiState.value
        if (!forceRefresh && currentState is SceneDetailUiState.Success && currentState.scene.id == id) {
            Timber.d("📱 Scene $id already loaded, skipping reset")
            return
        }

        Timber.d("📱 Loading scene details for id: $id")
        _uiState.value = SceneDetailUiState.Loading

        // 1. Observe database changes for this scene
        viewModelScope.launch {
            sceneRepository.getSceneById(id).collect { scene ->
                if (scene != null) {
                    Timber.d("📦 Scene update from DB: ${scene.title}")
                    _uiState.value = SceneDetailUiState.Success(scene)

                    // Initialize sprite manager if needed
                    val vttUrl = urlFormatter.resolveUrl(scene.paths?.vtt)
                    val spriteUrl = urlFormatter.resolveUrl(scene.paths?.sprite)
                    if (vttUrl != null && spriteUrl != null) {
                        spriteManager.initialize(vttUrl, spriteUrl)
                    }
                }
            }
        }

        // 2. Background fetch from API to get fresh data with tags, markers, stash_ids
        viewModelScope.launch {
            try {
                Timber.d("🌐 Background fetching full scene details from API for id: $id")
                val fullScene = sceneRepository.fetchSceneFromApi(id)
                if (fullScene != null) {
                    Timber.d("✅ Got full scene from API: tags=${fullScene.tags?.size ?: 0}, markers=${fullScene.sceneMarkers?.size ?: 0}")
                    fullScene.files?.forEachIndexed { index, file ->
                        Timber.d("📁 File[$index]: path=${file.path}, size=${file.size}, duration=${file.duration}")
                    }
                    _uiState.value = SceneDetailUiState.Success(fullScene)

                    // Initialize sprite manager from fresh API data
                    val vttUrl = urlFormatter.resolveUrl(fullScene.paths?.vtt)
                    val spriteUrl = urlFormatter.resolveUrl(fullScene.paths?.sprite)
                    if (vttUrl != null && spriteUrl != null) {
                        spriteManager.initialize(vttUrl, spriteUrl)
                    }
                } else if (_uiState.value is SceneDetailUiState.Loading) {
                    // Only show error if we have nothing to display
                    _uiState.value = SceneDetailUiState.Error("Scene not found")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Error fetching scene from API")
                // Only show error if we have nothing to display (no cached data)
                if (_uiState.value is SceneDetailUiState.Loading) {
                    _uiState.value = SceneDetailUiState.Error(e.message ?: "Unknown error")
                }
            }
        }
    }
    fun rateScene(rating: Int) {
        val currentState = _uiState.value
        if (currentState is SceneDetailUiState.Success) {
            val sceneId = currentState.scene.id
            val stashRating = rating * 20

            // Optimistic update
            _uiState.value = SceneDetailUiState.Success(
                currentState.scene.copy(rating100 = stashRating)
            )

            viewModelScope.launch {
                Timber.d("📱 SceneDetailViewModel: Rating scene $sceneId with $rating stars ($stashRating)")
                sceneRepository.rateScene(sceneId, stashRating)
                // Repository refreshScene will update cache/DB, but optimistic update handles immediate UI
            }
        }
    }

    fun incrementOCounter() {
        val currentState = _uiState.value
        if (currentState is SceneDetailUiState.Success) {
            val sceneId = currentState.scene.id
            val currentCount = currentState.scene.oCounter ?: 0

            // Optimistic update
            _uiState.value = SceneDetailUiState.Success(
                currentState.scene.copy(oCounter = currentCount + 1)
            )

            viewModelScope.launch {
                Timber.d("📱 SceneDetailViewModel: Incrementing O-counter for scene $sceneId")
                sceneRepository.incrementOCounter(sceneId)
                // Repository refreshScene will update cache/DB, but optimistic update handles immediate UI
            }
        }
    }

    fun refresh() {
        if (_uiState.value is SceneDetailUiState.Success) {
            val id = (_uiState.value as SceneDetailUiState.Success).scene.id
            viewModelScope.launch {
                sceneRepository.refreshScene(id)
            }
        }
    }

    fun deleteScene(deleteFile: Boolean, deleteGenerated: Boolean) {
        val currentState = _uiState.value
        if (currentState is SceneDetailUiState.Success) {
            val id = currentState.scene.id
            viewModelScope.launch {
                Timber.d("🗑️ Deleting scene $id (file=$deleteFile, generated=$deleteGenerated)")
                _uiState.value = SceneDetailUiState.Loading // Or keep current UI and show progress dialog

                try {
                    val success = sceneRepository.deleteScene(id, deleteFile, deleteGenerated)
                    if (success) {
                        Timber.d("✅ Scene $id deleted successfully")
                        _uiState.value = SceneDetailUiState.Deleted
                    } else {
                        Timber.e("❌ Failed to delete scene $id")
                        _uiState.value = SceneDetailUiState.Error("Failed to delete scene")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "❌ Exception deleting scene $id")
                    _uiState.value = SceneDetailUiState.Error(e.message ?: "Failed to delete scene")
                }
            }
        }
    }

    /**
     * Triggers the screenshot generation for the current scene at a specific timestamp.
     * This calls the repository which in turn notifies the server and refreshes local data.
     * @param sceneId The scene to generate the screenshot for.
     * @param timestamp The time in seconds where the screenshot is taken.
     */
    fun generateScreenshot(sceneId: String, timestamp: Double) {
        viewModelScope.launch {
            Timber.d("📸 Generating screenshot for scene $sceneId at ${timestamp}s")
            try {
                val success = sceneRepository.generateSceneScreenshot(sceneId, timestamp)
                if (success) {
                    Timber.d("✅ Screenshot generated successfully for scene $sceneId")
                    // Note: sceneRepository.refreshScene is called within generateSceneScreenshot
                } else {
                    Timber.e("❌ Failed to generate screenshot for scene $sceneId")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Exception generating screenshot for scene $sceneId")
            }
        }
    }

    /**
     * Triggers a targeted rescan for the scene's file on the server.
     * @param path The file path of the scene to rescan.
     */
    fun rescanScene(path: String) {
        viewModelScope.launch {
            Timber.d("🔄 Requesting rescan for path: $path")
            try {
                val success = sceneRepository.rescanScene(path)
                if (success) {
                    Timber.d("✅ Rescan queued for path: $path")
                } else {
                    Timber.e("❌ Failed to queue rescan for path: $path")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Exception queuing rescan for path: $path")
            }
        }
    }

    /**
     * Triggers a general generation job for the scene using the user's current settings.
     * @param sceneId The scene ID to generate assets for.
     */
    fun generate(sceneId: String) {
        viewModelScope.launch {
            Timber.d("🪄 Requesting general generation for scene: $sceneId")
            try {
                val success = sceneRepository.generateScene(sceneId)
                if (success) {
                    Timber.d("✅ General generation queued for scene: $sceneId")
                } else {
                    Timber.e("❌ Failed to queue general generation for scene: $sceneId")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Exception queuing general generation for scene: $sceneId")
            }
        }
    }

    /**
     * Triggers a targeted generation of the default thumbnail (cover) for the scene.
     * @param sceneId The scene ID to generate the thumbnail for.
     */
    fun generateDefaultThumbnail(sceneId: String) {
        viewModelScope.launch {
            Timber.d("🖼️ Requesting targeted thumbnail generation for scene: $sceneId")
            try {
                val success = sceneRepository.generateDefaultThumbnail(sceneId)
                if (success) {
                    Timber.d("✅ Targeted thumbnail generation queued for scene: $sceneId")
                } else {
                    Timber.e("❌ Failed to queue targeted thumbnail generation for scene: $sceneId")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Exception queuing targeted thumbnail generation for scene: $sceneId")
            }
        }
    }
}
