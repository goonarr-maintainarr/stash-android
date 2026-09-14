package goonarr.stash.features.performers.scrape

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.scraper.ScrapedPerformer
import goonarr.stash.core.model.scraper.ScrapedTag
import goonarr.stash.core.model.scraper.StashBox
import goonarr.stash.repositories.performers.PerformerRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * UI state for the performer scrape feature.
 *
 * @property isLoading Whether a network operation (loading, scraping, applying) is in progress.
 * @property stashBoxes List of available StashBox scrapers.
 * @property selectedStashBox Currently selected StashBox for scraping.
 * @property query The search query used for scraping.
 * @property currentPerformer The local performer being updated.
 * @property scrapeResults List of results returned from the scraper.
 * @property selectedResult The specific scraped result currently being reviewed.
 * @property selection The user's selection of which fields to apply from the scraped result.
 * @property applySuccess Whether the scrape result was successfully applied to the local performer.
 * @property error Any error message to be displayed.
 */
data class PerformerScrapeUiState(
    val isLoading: Boolean = false,
    val stashBoxes: List<StashBox> = emptyList(),
    val selectedStashBox: StashBox? = null,
    val query: String = "",
    val currentPerformer: Performer? = null,
    val scrapeResults: List<ScrapedPerformer> = emptyList(),
    val selectedResult: ScrapedPerformer? = null,
    val selection: PerformerScrapeSelection = PerformerScrapeSelection(),
    val applySuccess: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel responsible for managing the performer scraping flow.
 * Consists of three main stages:
 * 1. Searching/Scraping: Querying external sources (StashBoxes) for performer metadata.
 * 2. Reviewing: Comparing scraped data with local data and selecting fields to update.
 * 3. Applying: Saving the selected changes back to the local database via the server.
 */
@HiltViewModel
class PerformerScrapeViewModel @Inject constructor(
    private val performerRepository: PerformerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerformerScrapeUiState())
    val uiState: StateFlow<PerformerScrapeUiState> = _uiState.asStateFlow()

    fun loadStashBoxes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val boxes = performerRepository.fetchStashBoxes()
            Timber.d("📦 PerformerScrapeViewModel: Loaded ${boxes.size} StashBoxes")
            _uiState.update {
                it.copy(
                    stashBoxes = boxes,
                    selectedStashBox = if (it.selectedStashBox == null) boxes.firstOrNull() else it.selectedStashBox,
                    isLoading = false
                )
            }
        }
    }

    fun loadPerformer(performerId: String) {
        viewModelScope.launch {
            Timber.d("🎭 PerformerScrapeViewModel: Loading performer info for ID: $performerId")

            // First get from cache/DB to show something immediately
            val initialPerformer = performerRepository.getPerformer(performerId).first()
            if (initialPerformer != null) {
                _uiState.update {
                    it.copy(
                        query = it.query.ifBlank { initialPerformer.name ?: "" },
                        currentPerformer = initialPerformer
                    )
                }
            }

            // Then fetch full from API to get stash_ids etc.
            val fullPerformer = performerRepository.fetchPerformerFromApi(performerId)
            if (fullPerformer != null) {
                _uiState.update {
                    it.copy(
                        query = it.query.ifBlank { fullPerformer.name ?: "" },
                        currentPerformer = fullPerformer
                    )
                }
            }
        }
    }

    fun updateQuery(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
    }

    fun selectStashBox(box: StashBox) {
        Timber.d("👉 PerformerScrapeViewModel: Selected StashBox: ${box.name}")
        _uiState.update { it.copy(selectedStashBox = box) }
    }

    fun selectResult(result: ScrapedPerformer?) {
        Timber.d("👉 PerformerScrapeViewModel: Selected Scraped Result: ${result?.name}")
        // Initialize selection with all available fields enabled
        val initialSelection = if (result != null) {
            PerformerScrapeSelection(
                useName = result.name.isNotBlank(),
                useDisambiguation = !result.disambiguation.isNullOrBlank(),
                useGender = !result.gender.isNullOrBlank(),
                useBirthdate = !result.birthdate.isNullOrBlank(),
                useDeathDate = !result.deathDate.isNullOrBlank(),
                useCountry = !result.country.isNullOrBlank(),
                useEthnicity = !result.ethnicity.isNullOrBlank(),
                useHeight = !result.height.isNullOrBlank(),
                useWeight = !result.weight.isNullOrBlank(),
                useMeasurements = !result.measurements.isNullOrBlank(),
                useEyeColor = !result.eyeColor.isNullOrBlank(),
                useHairColor = !result.hairColor.isNullOrBlank(),
                useFakeTits = !result.fakeTits.isNullOrBlank(),
                usePenisLength = !result.penisLength.isNullOrBlank(),
                useCircumcised = !result.circumcised.isNullOrBlank(),
                useCareerLength = !result.careerLength.isNullOrBlank(),
                useTattoos = !result.tattoos.isNullOrBlank(),
                usePiercings = !result.piercings.isNullOrBlank(),
                useAliases = !result.aliases.isNullOrBlank(),
                useDetails = !result.details.isNullOrBlank(),
                useUrls = !result.urls.isNullOrEmpty(),
                urls = result.urls ?: emptyList(),
                useImage = !result.images.isNullOrEmpty(),
                selectedImageIndex = 0,
                useTags = !result.tags.isNullOrEmpty(),
                useStashId = !result.remoteSiteId.isNullOrBlank(),
                tagsToApply = result.tags
            )
        } else {
            PerformerScrapeSelection()
        }
        _uiState.update {
            it.copy(
                selectedResult = result,
                selection = initialSelection
            )
        }
    }

    fun updateSelection(selection: PerformerScrapeSelection) {
        _uiState.update { it.copy(selection = selection) }
    }

    fun scrape() {
        val box = uiState.value.selectedStashBox ?: return
        val query = uiState.value.query

        Timber.d("🔎 PerformerScrapeViewModel: Starting scrape with query: '$query'")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, scrapeResults = emptyList(), error = null) }

            try {
                val results = performerRepository.scrapePerformer(box, query)
                Timber.d("✅ PerformerScrapeViewModel: Found ${results.size} results")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        scrapeResults = results
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ PerformerScrapeViewModel: Scrape failed")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Scrape failed"
                    )
                }
            }
        }
    }

    fun applyScrapeResult() {
        val current = uiState.value.currentPerformer ?: return
        val result = uiState.value.selectedResult ?: return
        val selection = uiState.value.selection

        viewModelScope.launch {
            Timber.d("💾 PerformerScrapeViewModel: Applying scrape result to performer ${current.id}")
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Parse height and weight from strings if available
                val heightCm = if (selection.useHeight && result.height != null) {
                    result.height.filter { it.isDigit() }.toIntOrNull()
                } else {
                    null
                }

                val weightValue = if (selection.useWeight && result.weight != null) {
                    result.weight.filter { it.isDigit() }.toIntOrNull()
                } else {
                    null
                }

                // Get selected image from carousel and download as base64
                val imageBase64 = if (selection.useImage && !result.images.isNullOrEmpty()) {
                    val imageUrl = result.images.getOrNull(selection.selectedImageIndex)
                    if (imageUrl != null) {
                        performerRepository.downloadImageAsBase64(imageUrl)
                    } else {
                        null
                    }
                } else {
                    null
                }

                val circumcisedValue = if (selection.useCircumcised && !result.circumcised.isNullOrBlank()) {
                    when (result.circumcised.uppercase()) {
                        "CUT", "YES", "TRUE" -> "CUT"
                        "UNCUT", "NO", "FALSE" -> "UNCUT"
                        else -> null
                    }
                } else {
                    null
                }

                Timber.d(
                    "🔍 Performer Update Payload: gender=${
                        if (selection.useGender) {
                            result.gender?.uppercase()
                        } else {
                            null
                        }
                    }, circumcised=$circumcisedValue, country=${
                        if (selection.useCountry) {
                            result.country
                        } else {
                            null
                        }
                    }"
                )

                val stashIds = if (selection.useStashId && !result.remoteSiteId.isNullOrBlank()) {
                    val box = uiState.value.selectedStashBox
                    if (box != null) {
                        val existing = current.stashIds?.map { it.endpoint to it.stashId }?.toMutableList()
                            ?: mutableListOf()
                        val newId = box.endpoint to result.remoteSiteId!!
                        if (existing.none { it.second == newId.second && it.first == newId.first }) {
                            existing.add(newId)
                        }
                        existing
                    } else {
                        null
                    }
                } else {
                    null
                }

                val updatedPerformer = performerRepository.updatePerformer(
                    id = current.id,
                    name = if (selection.useName) result.name else null,
                    disambiguation = if (selection.useDisambiguation) result.disambiguation else null,
                    gender = if (selection.useGender) result.gender?.uppercase() else null,
                    birthdate = if (selection.useBirthdate) result.birthdate else null,
                    deathDate = if (selection.useDeathDate) result.deathDate else null,
                    country = if (selection.useCountry) result.country else null,
                    ethnicity = if (selection.useEthnicity) result.ethnicity else null,
                    heightCm = heightCm,
                    weight = weightValue,
                    measurements = if (selection.useMeasurements) result.measurements else null,
                    eyeColor = if (selection.useEyeColor) result.eyeColor else null,
                    hairColor = if (selection.useHairColor) result.hairColor else null,
                    fakeTits = if (selection.useFakeTits) result.fakeTits else null,
                    careerLength = if (selection.useCareerLength) result.careerLength else null,
                    tattoos = if (selection.useTattoos) result.tattoos else null,
                    piercings = if (selection.usePiercings) result.piercings else null,
                    aliases = if (selection.useAliases) result.aliases else null,
                    details = if (selection.useDetails) result.details else null,
                    urls = if (selection.useUrls) {
                        Timber.d("🔍 Applying scrape result. Raw URLs: ${selection.urls}")
                        val filteredUrls = selection.urls.filter { it.isNotBlank() }
                        Timber.d("🔍 Filtered URLs: $filteredUrls")
                        filteredUrls
                    } else {
                        null
                    },
                    penisLength = if (selection.usePenisLength && result.penisLength != null) {
                        result.penisLength.filter { it.isDigit() || it == '.' }.toDoubleOrNull()
                    } else {
                        null
                    },
                    circumcised = circumcisedValue,
                    image = imageBase64,
                    stashIds = stashIds
                )

                if (updatedPerformer != null) {
                    Timber.d("✅ PerformerScrapeViewModel: Performer updated successfully")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            applySuccess = true,
                            currentPerformer = updatedPerformer
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to update performer"
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ PerformerScrapeViewModel: Apply failed")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Apply failed"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetApplySuccess() {
        _uiState.update { it.copy(applySuccess = false) }
    }
}

/**
 * Defines which scraped performer fields to apply when updating a performer.
 */
data class PerformerScrapeSelection(
    val useName: Boolean = false,
    val useDisambiguation: Boolean = false,
    val useGender: Boolean = false,
    val useBirthdate: Boolean = false,
    val useDeathDate: Boolean = false,
    val useCountry: Boolean = false,
    val useEthnicity: Boolean = false,
    val useHeight: Boolean = false,
    val useWeight: Boolean = false,
    val useMeasurements: Boolean = false,
    val useEyeColor: Boolean = false,
    val useHairColor: Boolean = false,
    val useFakeTits: Boolean = false,
    val usePenisLength: Boolean = false,
    val useCircumcised: Boolean = false,
    val useCareerLength: Boolean = false,
    val useTattoos: Boolean = false,
    val usePiercings: Boolean = false,
    val useAliases: Boolean = false,
    val useDetails: Boolean = false,
    val useUrls: Boolean = false,
    val urls: List<String> = emptyList(),
    val useImage: Boolean = false,
    // Which image from the carousel to apply
    val selectedImageIndex: Int = 0,
    val useTags: Boolean = false,
    val useStashId: Boolean = false,
    val tagsToApply: List<ScrapedTag>? = null
)
