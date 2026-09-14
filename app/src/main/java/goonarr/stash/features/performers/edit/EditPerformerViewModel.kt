package goonarr.stash.features.performers.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.Tag
import goonarr.stash.features.components.SharedEditUiState
import goonarr.stash.repositories.TagRepository
import goonarr.stash.repositories.performers.PerformerRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class EditPerformerViewModel @Inject constructor(
    private val performerRepository: PerformerRepository,
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SharedEditUiState>(SharedEditUiState.Loading)
    val uiState: StateFlow<SharedEditUiState> = _uiState.asStateFlow()

    private var performerId: String? = null
    private var currentPerformer: Performer? = null

    // Basic Info
    val name = MutableStateFlow("")
    val disambiguation = MutableStateFlow("")
    val gender = MutableStateFlow("")
    val country = MutableStateFlow("")
    val ethnicity = MutableStateFlow("")
    val birthdate = MutableStateFlow("")
    val deathDate = MutableStateFlow("")
    val favorite = MutableStateFlow(false)

    // Physical
    val heightCm = MutableStateFlow("")
    val weight = MutableStateFlow("")
    val measurements = MutableStateFlow("")
    val eyeColor = MutableStateFlow("")
    val hairColor = MutableStateFlow("")
    val fakeTits = MutableStateFlow("")
    val penisLength = MutableStateFlow("")
    val circumcised = MutableStateFlow("")

    // Career
    val careerLength = MutableStateFlow("")
    val aliases = MutableStateFlow<List<String>>(emptyList())
    val urls = MutableStateFlow<List<String>>(emptyList())

    // Body Mods
    val tattoos = MutableStateFlow("")
    val piercings = MutableStateFlow("")

    // Bio
    val details = MutableStateFlow("")
    val rating = MutableStateFlow<Int?>(null)
    private val _imageUrl = MutableStateFlow<String?>(null)
    val imageUrl: StateFlow<String?> = _imageUrl.asStateFlow()

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

    fun loadPerformer(id: String) {
        if (performerId == id) return
        performerId = id
        _uiState.value = SharedEditUiState.Loading

        viewModelScope.launch {
            // Optimistically load cached performer details for immediate background rendering
            performerRepository.getPerformer(id).collect { performer ->
                if (performer != null && _imageUrl.value == null) {
                    _imageUrl.value = performer.imagePath
                }
            }
        }

        viewModelScope.launch {
            try {
                val performer = performerRepository.fetchPerformerFromApi(id)
                if (performer != null) {
                    currentPerformer = performer
                    // Basic Info
                    name.value = performer.name ?: ""
                    disambiguation.value = performer.disambiguation ?: ""
                    gender.value = performer.gender ?: ""
                    country.value = performer.country ?: ""
                    ethnicity.value = performer.ethnicity ?: ""
                    birthdate.value = performer.birthdate ?: ""
                    deathDate.value = performer.deathDate ?: ""
                    favorite.value = performer.favorite ?: false

                    // Physical
                    heightCm.value = performer.heightCm?.toString() ?: ""
                    weight.value = performer.weight?.toString() ?: ""
                    measurements.value = performer.measurements ?: ""
                    eyeColor.value = performer.eyeColor ?: ""
                    hairColor.value = performer.hairColor ?: ""
                    fakeTits.value = performer.fakeTits ?: ""
                    penisLength.value = performer.penisLength?.toString() ?: ""
                    circumcised.value = performer.circumcised ?: ""

                    // Career
                    careerLength.value = performer.careerLength ?: ""
                    aliases.value = performer.aliasList ?: emptyList()
                    urls.value = performer.urls ?: emptyList()

                    // Body Mods
                    tattoos.value = performer.tattoos ?: ""
                    piercings.value = performer.piercings ?: ""

                    // Bio
                    details.value = performer.details ?: ""
                    rating.value = performer.rating100
                    _imageUrl.value = performer.imagePath

                    // Tags
                    tags.value = performer.tags?.map { Tag(id = it.id, name = it.name) } ?: emptyList()

                    _uiState.value = SharedEditUiState.Success
                } else {
                    _uiState.value = SharedEditUiState.Error("Performer not found")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to load performer for editing")
                _uiState.value = SharedEditUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun save() {
        val id = performerId ?: return
        _uiState.value = SharedEditUiState.Saving

        viewModelScope.launch {
            try {
                val success = performerRepository.updatePerformer(
                    id = id,
                    name = name.value.takeIf { it.isNotBlank() },
                    disambiguation = disambiguation.value.takeIf { it.isNotBlank() },
                    gender = gender.value.takeIf { it.isNotBlank() },
                    country = country.value.takeIf { it.isNotBlank() },
                    ethnicity = ethnicity.value.takeIf { it.isNotBlank() },
                    birthdate = birthdate.value.takeIf { it.isNotBlank() },
                    deathDate = deathDate.value.takeIf { it.isNotBlank() },
                    favorite = favorite.value,

                    heightCm = heightCm.value.toIntOrNull(),
                    weight = weight.value.toIntOrNull(),
                    measurements = measurements.value.takeIf { it.isNotBlank() },
                    eyeColor = eyeColor.value.takeIf { it.isNotBlank() },
                    hairColor = hairColor.value.takeIf { it.isNotBlank() },
                    fakeTits = fakeTits.value.takeIf { it.isNotBlank() },
                    penisLength = penisLength.value.toDoubleOrNull(),
                    circumcised = circumcised.value.takeIf { it.isNotBlank() },

                    careerLength = careerLength.value.takeIf { it.isNotBlank() },
                    aliases = aliases.value.filter { it.isNotBlank() }.joinToString(", ").takeIf { it.isNotBlank() },
                    urls = urls.value.filter { it.isNotBlank() }.takeIf { it.isNotEmpty() },

                    tattoos = tattoos.value.takeIf { it.isNotBlank() },
                    piercings = piercings.value.takeIf { it.isNotBlank() },

                    details = details.value.takeIf { it.isNotBlank() },
                    rating = rating.value,
                    tagIds = tags.value.map { it.id }
                )

                if (success != null) {
                    _uiState.value = SharedEditUiState.Saved
                } else {
                    _uiState.value = SharedEditUiState.Error("Failed to save performer changes")
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Error saving performer")
                _uiState.value = SharedEditUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun clearError() {
        if (_uiState.value is SharedEditUiState.Error) {
            _uiState.value = SharedEditUiState.Success
        }
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
