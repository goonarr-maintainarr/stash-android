package goonarr.stash.features.tags.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.model.Tag
import goonarr.stash.core.network.StashBoxService
import goonarr.stash.core.network.StashBoxTag
import goonarr.stash.features.components.SharedEditUiState
import goonarr.stash.repositories.TagRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class EditTagViewModel @Inject constructor(
    private val tagRepository: TagRepository,
    private val settingsStore: SettingsStore,
    private val stashBoxService: StashBoxService
) : ViewModel() {

    private val _uiState = MutableStateFlow<SharedEditUiState>(SharedEditUiState.Loading)
    val uiState: StateFlow<SharedEditUiState> = _uiState.asStateFlow()

    private var tagId: String? = null

    // Form Fields
    val name = MutableStateFlow("")
    val details = MutableStateFlow("") // Description
    val aliases = MutableStateFlow<List<String>>(emptyList())
    val favorite = MutableStateFlow(false)
    val ignoreAutoTag = MutableStateFlow(false)
    val imageUrl = MutableStateFlow<String?>(null)

    // Tag Relationships
    val parentTags = MutableStateFlow<List<Tag>>(emptyList())
    val subTags = MutableStateFlow<List<Tag>>(emptyList())

    // Tag Search
    val parentTagSearchText = MutableStateFlow("")
    val parentTagSearchResults = MutableStateFlow<List<Tag>>(emptyList())
    val subTagSearchText = MutableStateFlow("")
    val subTagSearchResults = MutableStateFlow<List<Tag>>(emptyList())
    val isSearching = MutableStateFlow(false)

    // StashBox Search
    val stashBoxSearchText = MutableStateFlow("")
    val stashBoxSearchResults = MutableStateFlow<List<StashBoxTag>>(emptyList())
    val isSearchingStashBox = MutableStateFlow(false)

    // Image Handling
    private var isImageDeleted = false

    fun loadTag(id: String) {
        if (tagId == id) return
        tagId = id
        _uiState.value = SharedEditUiState.Loading

        viewModelScope.launch {
            try {
                val tag = tagRepository.fetchTag(id)
                if (tag != null) {
                    name.value = tag.name
                    details.value = tag.description ?: ""
                    aliases.value = tag.aliases ?: emptyList()
                    favorite.value = tag.favorite ?: false
                    ignoreAutoTag.value = tag.ignoreAutoTag ?: false
                    imageUrl.value = tag.imagePath
                    parentTags.value = tag.parents ?: emptyList()
                    subTags.value = tag.children ?: emptyList()

                    _uiState.value = SharedEditUiState.Success
                } else {
                    _uiState.value = SharedEditUiState.Error("Tag not found")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load tag")
                _uiState.value = SharedEditUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun searchParentTags(query: String) {
        parentTagSearchText.value = query
        if (query.length < 2) {
            parentTagSearchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            isSearching.value = true
            // Exclude self, already selected parents, and tags that are already sub tags
            val results = tagRepository.searchTags(query)
                .filter { tag ->
                    tag.id != tagId &&
                        parentTags.value.none { p -> p.id == tag.id } &&
                        subTags.value.none { s -> s.id == tag.id }
                }
            parentTagSearchResults.value = results
            isSearching.value = false
        }
    }

    fun addParentTag(tag: Tag) {
        // Don't add if already a parent
        if (parentTags.value.any { it.id == tag.id }) return

        // Remove from sub tags if present (can't be both parent and sub)
        if (subTags.value.any { it.id == tag.id }) {
            removeSubTag(tag.id)
        }

        val current = parentTags.value.toMutableList()
        current.add(tag)
        parentTags.value = current
        parentTagSearchText.value = ""
        parentTagSearchResults.value = emptyList()
    }

    fun removeParentTag(id: String) {
        val current = parentTags.value.toMutableList()
        current.removeAll { it.id == id }
        parentTags.value = current
    }

    fun searchSubTags(query: String) {
        subTagSearchText.value = query
        if (query.length < 2) {
            subTagSearchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            isSearching.value = true
            // Exclude self, already selected children, and tags that are already parent tags
            val results = tagRepository.searchTags(query)
                .filter { tag ->
                    tag.id != tagId &&
                        subTags.value.none { s -> s.id == tag.id } &&
                        parentTags.value.none { p -> p.id == tag.id }
                }
            subTagSearchResults.value = results
            isSearching.value = false
        }
    }

    fun addSubTag(tag: Tag) {
        // Don't add if already a sub tag
        if (subTags.value.any { it.id == tag.id }) return

        // Remove from parent tags if present (can't be both parent and sub)
        if (parentTags.value.any { it.id == tag.id }) {
            removeParentTag(tag.id)
        }

        val current = subTags.value.toMutableList()
        current.add(tag)
        subTags.value = current
        subTagSearchText.value = ""
        subTagSearchResults.value = emptyList()
    }

    fun removeSubTag(id: String) {
        val current = subTags.value.toMutableList()
        current.removeAll { it.id == id }
        subTags.value = current
    }

    fun searchStashBox(query: String) {
        stashBoxSearchText.value = query
        if (query.isBlank()) {
            stashBoxSearchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            isSearchingStashBox.value = true
            // Ensure the endpoint includes the GraphQL path
            val baseUrl = settingsStore.stashDBUrl.first().trimEnd('/')
            val endpoint = if (baseUrl.endsWith("/graphql")) baseUrl else "$baseUrl/graphql"
            val apiKey = settingsStore.stashDBApiKey.first()
            if (apiKey.isBlank()) {
                Timber.w("StashBox API key is missing; cannot perform search")
                stashBoxSearchResults.value = emptyList()
                isSearchingStashBox.value = false
                return@launch
            }
            val results = stashBoxService.searchTags(query, endpoint, apiKey)
            stashBoxSearchResults.value = results
            isSearchingStashBox.value = false
        }
    }

    fun onStashBoxTagSelected(stashBoxTag: StashBoxTag) {
        val id = tagId ?: return
        viewModelScope.launch {
            try {
                val baseUrl = settingsStore.stashDBUrl.first().trimEnd('/')
                val endpoint = if (baseUrl.endsWith("/graphql")) baseUrl else "$baseUrl/graphql"
                val stashIds = listOf(Pair(endpoint, stashBoxTag.id))

                val result = tagRepository.updateTag(
                    id = id,
                    stashIds = stashIds
                )

                if (result != null) {
                    // Refresh tag data
                    loadTag(id)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error linking StashBox tag")
            }
        }
    }

    fun save() {
        val id = tagId ?: return
        _uiState.value = SharedEditUiState.Saving

        viewModelScope.launch {
            try {
                val result = tagRepository.updateTag(
                    id = id,
                    name = name.value.takeIf { it.isNotBlank() },
                    description = details.value,
                    aliases = aliases.value.filter { it.isNotBlank() },
                    favorite = favorite.value,
                    ignoreAutoTag = ignoreAutoTag.value,
                    parentIds = parentTags.value.map { it.id },
                    childIds = subTags.value.map { it.id },
                    image = if (isImageDeleted) "" else null
                    // sortName? Check if API supports updating sort name specifically or if it's just handled via backend logic
                )

                if (result != null) {
                    _uiState.value = SharedEditUiState.Saved
                } else {
                    _uiState.value = SharedEditUiState.Error("Failed to save tag changes")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error saving tag")
                _uiState.value = SharedEditUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun clearError() {
        if (_uiState.value is SharedEditUiState.Error) {
            _uiState.value = SharedEditUiState.Success
        }
    }

    fun deleteImage() {
        imageUrl.value = null
        isImageDeleted = true
    }
}
