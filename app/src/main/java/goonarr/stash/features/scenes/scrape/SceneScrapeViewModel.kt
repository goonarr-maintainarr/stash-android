package goonarr.stash.features.scenes.scrape

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import goonarr.stash.core.model.Scene
import goonarr.stash.core.model.scraper.ScrapedScene
import goonarr.stash.core.model.scraper.StashBox
import goonarr.stash.repositories.scenes.SceneRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * UI State for the Scene Scrape screen.
 *
 * @property isLoading Whether initial data (stashboxes, config) is being loaded.
 * @property isScraping Whether a scrape operation is currently in progress.
 * @property stashBoxes available StashBox sources.
 * @property selectedStashBox The StashBox currently selected for scraping.
 * @property taggerConfig The current tagger configuration (parse mode, etc).
 * @property query The search query string.
 * @property scrapedResults Results returned from the scraper.
 * @property selectedResult The specific result chosen for review.
 * @property currentScene The local scene being updated.
 */
data class SceneScrapeUiState(
    val isLoading: Boolean = false,
    val isScraping: Boolean = false,
    val stashBoxes: List<StashBox> = emptyList(),
    val selectedStashBox: StashBox? = null,
    val taggerConfig: goonarr.stash.core.model.scraper.TaggerConfig = goonarr.stash.core.model.scraper.TaggerConfig(),
    val query: String = "",
    val scrapedResults: List<ScrapedScene> = emptyList(),
    val selectedResult: ScrapedScene? = null,
    val currentScene: Scene? = null
)

/**
 * ViewModel for the Scene Scrape feature.
 * Facilitates searching for scene metadata from external StashBoxes,
 * managing tagger configuration, and applying selected metadata to local scenes.
 */
@HiltViewModel
class SceneScrapeViewModel @Inject constructor(
    private val sceneRepository: SceneRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SceneScrapeUiState())
    val uiState: StateFlow<SceneScrapeUiState> = _uiState.asStateFlow()

    private var currentScene: Scene? = null

    fun loadStashBoxes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Parallel load stash boxes and tagger config
            val boxesJob = viewModelScope.launch {
                val boxes = sceneRepository.fetchStashBoxes()
                Timber.d("📦 ViewModel: Loaded ${boxes.size} StashBoxes")
                _uiState.update {
                    it.copy(
                        stashBoxes = boxes,
                        selectedStashBox = if (it.selectedStashBox == null) boxes.firstOrNull() else it.selectedStashBox
                    )
                }
            }

            val configJob = viewModelScope.launch {
                val config = sceneRepository.fetchTaggerConfig()
                if (config != null) {
                    Timber.d("⚙️ ViewModel: Loaded TaggerConfig from API")
                    _uiState.update { it.copy(taggerConfig = config) }
                    // Update query if we just loaded config
                    uiState.value.currentScene?.let { updateQueryFromConfig() }
                }
            }

            boxesJob.join()
            configJob.join()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun loadScene(sceneId: String) {
        viewModelScope.launch {
            Timber.d("🎬 ViewModel: Loading full scene info for ID: $sceneId")

            // First get from cache/DB to show something immediately
            val initialScene = sceneRepository.getSceneById(sceneId).first()
            if (initialScene != null) {
                updateStateWithScene(initialScene)
            }

            // Then fetch full from API to get stash_ids etc.
            val fullScene = sceneRepository.fetchSceneFromApi(sceneId)
            if (fullScene != null) {
                updateStateWithScene(fullScene)
            }
        }
    }

    private fun updateStateWithScene(scene: goonarr.stash.core.model.Scene) {
        _uiState.update {
            it.copy(
                query = if (it.query.isBlank()) {
                    goonarr.stash.core.model.scraper.TaggerQueryService.prepareQueryString(
                        scene = scene,
                        mode = it.taggerConfig.mode,
                        blacklist = it.taggerConfig.blacklist
                    )
                } else {
                    it.query
                },
                currentScene = scene
            )
        }
    }

    fun updateQuery(newQuery: String) {
        _uiState.value = _uiState.value.copy(query = newQuery)
    }

    fun updateConfig(config: goonarr.stash.core.model.scraper.TaggerConfig) {
        Timber.d("⚙️ ViewModel: Tagger config updated locally")
        _uiState.update {
            it.copy(taggerConfig = config)
        }
        uiState.value.currentScene?.let { updateQueryFromConfig() }
    }

    fun saveConfig() {
        viewModelScope.launch {
            val config = uiState.value.taggerConfig
            Timber.d("💾 ViewModel: Saving TaggerConfig to API")
            val success = sceneRepository.saveTaggerConfig(config)
            if (success) {
                Timber.d("✅ ViewModel: TaggerConfig saved successfully")
            } else {
                Timber.e("❌ ViewModel: Failed to save TaggerConfig")
            }
        }
    }

    private fun updateQueryFromConfig() {
        val scene = uiState.value.currentScene ?: return
        val config = uiState.value.taggerConfig
        val newQuery = goonarr.stash.core.model.scraper.TaggerQueryService.prepareQueryString(
            scene = scene,
            mode = config.mode,
            blacklist = config.blacklist
        )
        Timber.d("🪄 ViewModel: Generated query: '$newQuery'")
        _uiState.value = _uiState.value.copy(query = newQuery)
    }

    fun selectStashBox(box: StashBox) {
        Timber.d("👉 ViewModel: Selected StashBox: ${box.name}")
        _uiState.value = _uiState.value.copy(selectedStashBox = box)
    }

    fun selectResult(result: ScrapedScene?) {
        Timber.d("👉 ViewModel: Selected Scraped Result: ${result?.title}")
        _uiState.value = _uiState.value.copy(selectedResult = result)
    }

    fun applyScrapeResult(result: ScrapedScene, selection: ScrapeResultSelection) {
        val current = uiState.value.currentScene ?: return

        viewModelScope.launch {
            Timber.d("💾 ViewModel: Applying scrape result to scene ${current.id}")
            _uiState.update { it.copy(isLoading = true) }

            // Map ScrapedScene to UpdateScene Input
            // In a real implementation, we would let user choose fields.
            // For now, we apply all non-null fields.

            // Resolve IDs from names for relations
            val performerIds = if (selection.usePerformers && !result.performers.isNullOrEmpty()) {
                result.performers.mapNotNull { p ->
                    // Attempt to find existing performer ID by name
                    val id = sceneRepository.findPerformerId(p.name)
                    if (id == null) {
                        Timber.w("⚠️ Could not find ID for performer '${p.name}'. Did you create it first?")
                    }
                    id
                }
            } else {
                null
            }

            val studioId = if (selection.useStudio && result.studio != null) {
                sceneRepository.findStudioId(result.studio.name).also {
                    if (it == null) Timber.w("⚠️ Could not find ID for studio '${result.studio.name}'")
                }
            } else {
                null
            }

            val tagIds = if (selection.useTags) {
                val tags = selection.tagsToApply ?: result.tags
                val newTagIds = tags?.mapNotNull { t ->
                    sceneRepository.findTagId(t.name).also {
                        if (it == null) Timber.w("⚠️ Could not find ID for tag '${t.name}'")
                    }
                } ?: emptyList()

                if (uiState.value.taggerConfig.tagOperation == goonarr.stash.core.model.scraper.TagOperation.ADD) {
                    val existingIds = current.tags?.map { it.id } ?: emptyList()
                    (existingIds + newTagIds).distinct()
                } else {
                    newTagIds
                }
            } else {
                null
            }

            sceneRepository.updateScene(
                id = current.id,
                title = if (selection.useTitle) result.title else null,
                details = if (selection.useDetails) result.details else null,
                date = if (selection.useDate) result.date else null,
                url = if (selection.useUrl) (result.url ?: result.urls?.firstOrNull()) else null,
                director = if (selection.useDirector) result.director else null,
                code = if (selection.useCode) result.code else null,
                performerIds = performerIds,
                studioId = studioId,
                tagIds = tagIds,
                coverImage = if (selection.useCover) result.image else null,
                stashIds = if (selection.useStashId && result.remoteSiteId != null) {
                    val endpoint = uiState.value.selectedStashBox?.endpoint
                    if (endpoint != null) {
                        val newId = endpoint to result.remoteSiteId!!
                        val existingIds = current.stashIds?.map { it.endpoint to it.stashId } ?: emptyList()
                        (existingIds + newId).distinct()
                    } else {
                        null
                    }
                } else {
                    null
                }
            )

            _uiState.update { it.copy(isLoading = false, isScraping = false, selectedResult = null) }
        }
    }

    suspend fun matchScrapedPerformers(names: List<String>): Map<String, goonarr.stash.core.model.Performer?> {
        return sceneRepository.matchPerformers(names)
    }

    suspend fun validateScrapedPerformers(names: List<String>): Set<String> {
        val matches = sceneRepository.matchPerformers(names)
        return matches.filterValues { it == null }.keys
    }

    fun createPerformer(
        name: String,
        image: String?,
        birthdate: String?,
        ethnicity: String?,
        country: String?,
        eyeColor: String?,
        heightCm: Int?,
        measurements: String?,
        fakeTits: String?,
        careerLength: String?,
        tattoos: String?,
        piercings: String?,
        aliases: String?,
        details: String?,
        deathDate: String?,
        hairColor: String?,
        weight: Int?,
        rating: Int?,
        gender: String? = null,
        urlParam: String? = null
    ) {
        viewModelScope.launch {
            sceneRepository.createPerformer(
                name = name,
                image = image,
                birthdate = birthdate,
                ethnicity = ethnicity,
                country = country,
                eyeColor = eyeColor,
                heightCm = heightCm,
                measurements = measurements,
                fakeTits = fakeTits,
                careerLength = careerLength,
                tattoos = tattoos,
                piercings = piercings,
                aliases = aliases,
                details = details,
                deathDate = deathDate,
                hairColor = hairColor,
                weight = weight,
                rating = rating,
                gender = gender,
                urlParam = urlParam
            )
        }
    }

    fun scrape() {
        val box = uiState.value.selectedStashBox ?: return
        val query = uiState.value.query

        Timber.d("🔎 ViewModel: Starting scrape...")

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isScraping = true,
                scrapedResults = emptyList()
            )
            val results = sceneRepository.scrapeScene(
                stashBox = box,
                scene = uiState.value.currentScene,
                query = query.ifBlank { null }
            )
            Timber.d("🏁 ViewModel: Scrape finished. Found ${results.size} matches")
            _uiState.value = _uiState.value.copy(
                scrapedResults = results,
                isScraping = false
            )
        }
    }

    fun scrapeByFragment() {
        val stashBox = uiState.value.selectedStashBox
        val scene = uiState.value.currentScene

        if (stashBox != null && scene != null) {
            Timber.d("🧩 ViewModel: Starting scrape by fragment...")
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isScraping = true, scrapedResults = emptyList())

                val results = sceneRepository.scrapeSceneByFragment(stashBox, scene)

                Timber.d("🏁 ViewModel: Fragment scrape finished. Found ${results.size} matches")

                _uiState.value = _uiState.value.copy(
                    scrapedResults = results,
                    isScraping = false
                )
            }
        }
    }
}

/**
 * Selection of which metadata fields to apply from a scraped result.
 */
data class ScrapeResultSelection(
    val useTitle: Boolean,
    val useDetails: Boolean,
    val useDate: Boolean,
    val useUrl: Boolean,
    val useStudio: Boolean,
    val useDirector: Boolean,
    val useCode: Boolean,
    val useCover: Boolean,
    val usePerformers: Boolean,
    val useTags: Boolean,
    val useStashId: Boolean,
    /** Optional list of tags to apply, overrides the full list from the scraped result if provided. */
    val tagsToApply: List<goonarr.stash.core.model.scraper.ScrapedTag>? = null
)
