package goonarr.stash.features.studios.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import goonarr.stash.core.model.Studio
import goonarr.stash.core.model.Tag
import goonarr.stash.features.components.SharedEditUiState
import goonarr.stash.repositories.StudioRepository
import goonarr.stash.repositories.TagRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for editing studio information.
 *
 * Manages form state for all editable studio fields and handles
 * loading existing data and saving changes via [StudioRepository].
 *
 * @param studioRepository Repository for fetching and updating studio data.
 */
@HiltViewModel
class EditStudioViewModel @Inject constructor(
    private val studioRepository: StudioRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SharedEditUiState>(SharedEditUiState.Loading)

    /** Current UI state of the edit screen. */
    val uiState: StateFlow<SharedEditUiState> = _uiState.asStateFlow()

    private var studioId: String? = null
    private var currentStudio: Studio? = null

    // ========== Form Fields ==========

    /** Studio name. */
    val name = MutableStateFlow("")

    /** Studio URLs. */
    val urls = MutableStateFlow<List<String>>(emptyList())

    /** Studio description/details. */
    val details = MutableStateFlow("")

    /** Studio aliases. */
    val aliases = MutableStateFlow<List<String>>(emptyList())

    /** Studio rating (0-100). */
    val rating = MutableStateFlow<Int?>(null)

    /** Whether studio is marked as favorite. */
    val favorite = MutableStateFlow(false)

    /** Whether to ignore auto-tagging for this studio. */
    val ignoreAutoTag = MutableStateFlow(false)

    private val _imageUrl = MutableStateFlow<String?>(null)

    /** URL of the studio image for display. */
    val imageUrl: StateFlow<String?> = _imageUrl.asStateFlow()
    private var isImageDeleted = false

    // Tags
    val tags = MutableStateFlow<List<Tag>>(emptyList())
    val tagSearchText = MutableStateFlow("")
    val tagSearchResults = MutableStateFlow<List<Tag>>(emptyList())
    val isSearchingTags = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            tagSearchText
                .debounce(300)
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isNotBlank()) {
                        searchTags(query)
                    } else {
                        tagSearchResults.value = emptyList()
                    }
                }
        }
    }

    /**
     * Loads studio data for editing.
     *
     * @param id The studio ID to load.
     */
    fun loadStudio(id: String) {
        if (studioId == id) return
        studioId = id
        _uiState.value = SharedEditUiState.Loading
        Timber.d("📝 Loading studio $id for editing")

        viewModelScope.launch {
            try {
                val studio = studioRepository.fetchStudio(id)
                if (studio != null) {
                    currentStudio = studio
                    populateFormFields(studio)
                    Timber.d("✅ Loaded studio ${studio.name} for editing")
                    _uiState.value = SharedEditUiState.Success
                } else {
                    Timber.e("❌ Studio $id not found")
                    _uiState.value = SharedEditUiState.Error("Studio not found")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to load studio for editing")
                _uiState.value = SharedEditUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun populateFormFields(studio: Studio) {
        name.value = studio.name
        urls.value = studio.urls ?: emptyList()
        details.value = studio.details ?: ""
        aliases.value = studio.aliases ?: emptyList()
        rating.value = studio.rating100
        favorite.value = studio.favorite ?: false
        ignoreAutoTag.value = studio.ignoreAutoTag ?: false
        _imageUrl.value = studio.imagePath
        tags.value = studio.tags ?: emptyList()
    }

    /**
     * Saves the current form data to the server.
     *
     * On success, sets [uiState] to [EditStudioUiState.Saved].
     * On failure, sets [uiState] to [EditStudioUiState.Error].
     */
    fun save() {
        val id = studioId ?: return
        _uiState.value = SharedEditUiState.Saving
        Timber.d("💾 Saving studio $id")

        viewModelScope.launch {
            try {
                val result = studioRepository.updateStudio(
                    id = id,
                    name = name.value.takeIf { it.isNotBlank() },
                    urls = urls.value.filter { it.isNotBlank() },
                    details = details.value,
                    aliases = aliases.value.map { it.trim() }.filter { it.isNotBlank() },
                    rating100 = rating.value,
                    favorite = favorite.value,
                    ignoreAutoTag = ignoreAutoTag.value,
                    image = if (isImageDeleted) "" else null,
                    tagIds = tags.value.map { it.id }
                )

                if (result != null) {
                    Timber.d("✅ Successfully saved studio ${result.name}")
                    _uiState.value = SharedEditUiState.Saved
                } else {
                    Timber.e("❌ Failed to save studio changes")
                    _uiState.value = SharedEditUiState.Error("Failed to save studio changes")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Error saving studio")
                _uiState.value = SharedEditUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Clears the current error state and returns to success state.
     */
    fun clearError() {
        if (_uiState.value is SharedEditUiState.Error) {
            _uiState.value = SharedEditUiState.Success
        }
    }

    /**
     * Flags the studio image for deletion and clears the preview URL.
     */
    fun deleteImage() {
        Timber.d("🗑️ Marking studio image for deletion")
        _imageUrl.value = null
        isImageDeleted = true
    }

    fun searchTags(query: String) {
        viewModelScope.launch {
            isSearchingTags.value = true
            try {
                val results = tagRepository.searchTags(query)
                tagSearchResults.value = results.filter { result ->
                    tags.value.none { it.id == result.id }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error searching tags")
            } finally {
                isSearchingTags.value = false
            }
        }
    }

    fun addTag(tag: Tag) {
        val currentTags = tags.value.toMutableList()
        if (currentTags.none { it.id == tag.id }) {
            currentTags.add(tag)
            tags.value = currentTags
            tagSearchText.value = ""
            tagSearchResults.value = emptyList()
        }
    }

    fun removeTag(tagId: String) {
        val currentTags = tags.value.toMutableList()
        currentTags.removeAll { it.id == tagId }
        tags.value = currentTags
    }
}
