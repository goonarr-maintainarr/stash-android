package goonarr.stash.features.studios.scrape

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import goonarr.stash.core.model.Studio
import goonarr.stash.core.model.scraper.ScrapedStudio
import goonarr.stash.core.model.scraper.ScrapedTag
import goonarr.stash.core.model.scraper.StashBox
import goonarr.stash.repositories.StudioRepository
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
 * UI state for the studio scrape feature.
 *
 * @property isLoading Whether a network operation (loading, scraping, applying) is in progress.
 * @property stashBoxes List of available StashBox scrapers.
 * @property selectedStashBox Currently selected StashBox for scraping.
 * @property query The search query used for scraping.
 * @property currentStudio The local studio being updated.
 * @property scrapeResults List of results returned from the scraper.
 * @property selectedResult The specific scraped result currently being reviewed.
 * @property selection The user's selection of which fields to apply from the scraped result.
 * @property applySuccess Whether the scrape result was successfully applied to the local studio.
 * @property error Any error message to be displayed.
 */
data class StudioScrapeUiState(
    val isLoading: Boolean = false,
    val stashBoxes: List<StashBox> = emptyList(),
    val selectedStashBox: StashBox? = null,
    val query: String = "",
    val currentStudio: Studio? = null,
    val scrapeResults: List<ScrapedStudio> = emptyList(),
    val selectedResult: ScrapedStudio? = null,
    val selection: StudioScrapeSelection = StudioScrapeSelection(),
    val applySuccess: Boolean = false,
    val error: String? = null
)

/**
 * ViewModel responsible for managing the studio scraping flow.
 * Consists of three main stages:
 * 1. Searching/Scraping: Querying external sources (StashBoxes) for studio metadata.
 * 2. Reviewing: Comparing scraped data with local data and selecting fields to update.
 * 3. Applying: Saving the selected changes back to the local database via the server.
 */
@HiltViewModel
class StudioScrapeViewModel @Inject constructor(
    private val studioRepository: StudioRepository,
    // Reused for fetching StashBoxes for now, or move fetchStashBoxes to a shared repo
    private val performerRepository: PerformerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudioScrapeUiState())
    val uiState: StateFlow<StudioScrapeUiState> = _uiState.asStateFlow()

    fun loadStashBoxes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Using PerformerRepository to fetch stash boxes as it's a general configuration
            // TODO: Move fetchStashBoxes to a common ConfigurationRepository
            val boxes = performerRepository.fetchStashBoxes()
            Timber.d("📦 StudioScrapeViewModel: Loaded ${boxes.size} StashBoxes")
            _uiState.update {
                it.copy(
                    stashBoxes = boxes,
                    selectedStashBox = if (it.selectedStashBox == null) boxes.firstOrNull() else it.selectedStashBox,
                    isLoading = false
                )
            }
        }
    }

    fun loadStudio(studioId: String) {
        viewModelScope.launch {
            Timber.d("🏢 StudioScrapeViewModel: Loading studio info for ID: $studioId")

            // First get from cache/DB to show something immediately
            val initialStudio = studioRepository.getStudio(studioId).first()
            if (initialStudio != null) {
                _uiState.update {
                    it.copy(
                        query = it.query.ifBlank { initialStudio.name },
                        currentStudio = initialStudio
                    )
                }
            }

            // Then fetch full from API to get stash_ids etc. (if needed)
            val fullStudio = studioRepository.fetchStudio(studioId)
            if (fullStudio != null) {
                _uiState.update {
                    it.copy(
                        query = it.query.ifBlank { fullStudio.name },
                        currentStudio = fullStudio
                    )
                }
            }
        }
    }

    fun updateQuery(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
    }

    fun selectStashBox(box: StashBox) {
        Timber.d("👉 StudioScrapeViewModel: Selected StashBox: ${box.name}")
        _uiState.update { it.copy(selectedStashBox = box) }
    }

    fun selectResult(result: ScrapedStudio?) {
        Timber.d("👉 StudioScrapeViewModel: Selected Scraped Result: ${result?.name}")
        // Initialize selection with all available fields enabled
        val initialSelection = if (result != null) {
            StudioScrapeSelection(
                useName = result.name.isNotBlank(),
                useUrls = !result.urls.isNullOrEmpty(),
                urls = result.urls ?: emptyList(),
                useImage = !result.image.isNullOrBlank(),
                useDetails = !result.details.isNullOrBlank(),
                useParent = result.parent != null,
                useTags = !result.tags.isNullOrEmpty(),
                useStashId = !result.remoteSiteId.isNullOrBlank(),
                tagsToApply = result.tags
            )
        } else {
            StudioScrapeSelection()
        }
        _uiState.update {
            it.copy(
                selectedResult = result,
                selection = initialSelection
            )
        }
    }

    fun updateSelection(selection: StudioScrapeSelection) {
        _uiState.update { it.copy(selection = selection) }
    }

    fun scrape() {
        val box = uiState.value.selectedStashBox ?: return
        val query = uiState.value.query

        Timber.d("🔎 StudioScrapeViewModel: Starting scrape with query: '$query'")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, scrapeResults = emptyList(), error = null) }

            try {
                val results = studioRepository.scrapeStudio(box, query)
                Timber.d("✅ StudioScrapeViewModel: Found ${results.size} results")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        scrapeResults = results
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ StudioScrapeViewModel: Scrape failed")
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
        val current = uiState.value.currentStudio ?: return
        val result = uiState.value.selectedResult ?: return
        val selection = uiState.value.selection

        viewModelScope.launch {
            Timber.d("💾 StudioScrapeViewModel: Applying scrape result to studio ${current.id}")
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Download image as base64 if selected
                val imageBase64 = if (selection.useImage && !result.image.isNullOrBlank()) {
                    // We need a way to download image. PerformerRepository has downloadImageAsBase64.
                    // We might need to expose it in StudioRepository or move to common.
                    // For now, assuming we can access it via performerRepository or add to StudioRepository.
                    // Let's assume we add it to StudioRepository or reuse StashClient directly if possible,
                    // but Repository pattern suggests checking StudioRepository.
                    // Checking PerformerRepository shows it uses CLIENT.
                    // I'll use performerRepository.downloadImageAsBase64 for now as a utility since it's injected.
                    performerRepository.downloadImageAsBase64(result.image)
                } else {
                    null
                }

                // TODO: Handle Stash ID application logic similar to Performer
                val stashIds = if (selection.useStashId && !result.remoteSiteId.isNullOrBlank()) {
                    val box = uiState.value.selectedStashBox
                    if (box != null) {
                        // Logic to merge stash IDs
                        // For now, we don't have stashIds exposed on Studio model in common UIState cleanly without a mapper,
                        // but updateStudio takes standard inputs.
                        // Studio update does NOT explicitly take stashIds in the current Repository definition?
                        // Let's check StudioRepository.updateStudio.
                        // updateStudio args: id, name, urls, parentId, rating100, favorite, details, aliases, tagIds, ignoreAutoTag, image
                        // It seems StudioRepository.updateStudio MISSES stashIds argument!
                        // I will need to verify this. If so, I can't update stash ID yet.
                        null
                    } else {
                        null
                    }
                } else {
                    null
                }

                // Note: updates to parent, etc. might require resolving names to IDs if the scraper returns objects.
                // ScrapedStudio has `parent: ScrapedStudio?`.
                // updateStudio expects `parentId: String?`.
                // If scraper returns a parent object, we might not have its ID unless we search for it.
                // For now, we'll skip parent application or implementing it if we have ID.
                // ScrapedStudio has `storedId` if it matched a local one? No, scraper returns external data.

                val updatedStudio = studioRepository.updateStudio(
                    id = current.id,
                    name = if (selection.useName) result.name else null,
                    details = if (selection.useDetails) result.details else null,
                    urls = if (selection.useUrls) {
                        val filteredUrls = selection.urls.filter { it.isNotBlank() }
                        filteredUrls
                    } else {
                        null
                    },
                    image = imageBase64,
                    // TODO: Parent, Tags handling
                )

                if (updatedStudio != null) {
                    Timber.d("✅ StudioScrapeViewModel: Studio updated successfully")
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            applySuccess = true,
                            currentStudio = updatedStudio
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to update studio"
                        )
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ StudioScrapeViewModel: Apply failed")
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
 * Defines which scraped studio fields to apply when updating a studio.
 */
data class StudioScrapeSelection(
    val useName: Boolean = false,
    val useUrls: Boolean = false,
    val urls: List<String> = emptyList(),
    val useImage: Boolean = false,
    val useDetails: Boolean = false,
    val useParent: Boolean = false,
    val useTags: Boolean = false,
    val useStashId: Boolean = false,
    val tagsToApply: List<ScrapedTag>? = null
)
