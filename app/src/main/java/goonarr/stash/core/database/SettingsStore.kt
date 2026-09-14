package goonarr.stash.core.database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import goonarr.stash.core.model.GenerationOptions
import goonarr.stash.core.model.ScanOptions
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import timber.log.Timber

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore
    private val json = Json { ignoreUnknownKeys = true }

    // Keys
    companion object {
        val SERVER_URL = stringPreferencesKey("serverUrl")
        val API_KEY = stringPreferencesKey("apiKey")

        val WHISPARR_URL = stringPreferencesKey("whisparrUrl")
        val WHISPARR_API_KEY = stringPreferencesKey("whisparrApiKey")
        val WHISPARR_QUALITY_PROFILE_ID = intPreferencesKey("whisparrQualityProfileId")
        val WHISPARR_ROOT_FOLDER_PATH = stringPreferencesKey("whisparrRootFolderPath")

        val STASHDB_URL = stringPreferencesKey("stashDBUrl")
        val STASHDB_API_KEY = stringPreferencesKey("stashDBApiKey")
        val EXCLUDE_VR_FROM_STASHDB = booleanPreferencesKey("excludeVRFromStashDB")
        val EXCLUDE_COMPILATIONS_FROM_STASHDB = booleanPreferencesKey("excludeCompilationsFromStashDB")
        val STASHDB_FAVORITES_CACHE = stringPreferencesKey("stashDBFavoritesCache")

        val FOLLOWED_TAG_IDS = stringPreferencesKey("followedTagIds")
        val SYNC_SAVED_FILTERS = booleanPreferencesKey("syncSavedFilters")
        val SYNC_HOME_LAYOUT = booleanPreferencesKey("syncHomeLayout")
        val PREFETCH_ON_SCROLL = booleanPreferencesKey("prefetchOnScroll")
        val PREFETCH_HOME_SCENE_DETAILS = booleanPreferencesKey("prefetchHomeSceneDetails")
        val SHOW_SCENE_PREVIEWS = booleanPreferencesKey("showScenePreviews")
        val MUTE_SCENE_PREVIEWS = booleanPreferencesKey("muteScenePreviews")
        val SHOW_MARKER_PREVIEWS = booleanPreferencesKey("showMarkerPreviews")
        val BLUR_NSFW = booleanPreferencesKey("blurNsfw")

        val GENERATION_OPTIONS = stringPreferencesKey("generationOptions")
        val SCAN_OPTIONS = stringPreferencesKey("scanOptions")

        val VERBOSE_LOGGING = booleanPreferencesKey("verboseLogging")

        val PERFORMER_SORT_TYPE = stringPreferencesKey("performerSortType")
        val PERFORMER_SORT_DIRECTION = stringPreferencesKey("performerSortDirection")
        val SCENE_SORT_TYPE = stringPreferencesKey("sceneSortType")
        val SCENE_SORT_DIRECTION = stringPreferencesKey("sceneSortDirection")
        val STUDIO_SORT_TYPE = stringPreferencesKey("studioSortType")
        val STUDIO_SORT_DIRECTION = stringPreferencesKey("studioSortDirection")
        val PERFORMER_VIEW_TYPE = stringPreferencesKey("performerViewType")
        val SCENE_VIEW_TYPE = stringPreferencesKey("sceneViewType")
        val STUDIO_VIEW_TYPE = stringPreferencesKey("studioViewType")

        val HOME_SECTION_ORDER = stringPreferencesKey("homeSectionOrder")
        val HOME_SECTION_HIDDEN = stringPreferencesKey("homeSectionHidden")
        val SHOW_DEFAULT_ROWS = booleanPreferencesKey("showDefaultRows")
        val HIDE_BOTTOM_BAR_LABELS = booleanPreferencesKey("hideBottomBarLabels")
        val THEME = stringPreferencesKey("theme")
    }

    // Default Values
    private val defaultServerUrl = ""
    private val defaultApiKey = ""

    // Flows
    val serverUrl: Flow<String> = dataStore.data.map { it[SERVER_URL] ?: defaultServerUrl }
    val apiKey: Flow<String> = dataStore.data.map { it[API_KEY] ?: defaultApiKey }

    val whisparrUrl: Flow<String> = dataStore.data.map { it[WHISPARR_URL] ?: "" }
    val whisparrApiKey: Flow<String> = dataStore.data.map { it[WHISPARR_API_KEY] ?: "" }
    val whisparrQualityProfileId: Flow<Int> = dataStore.data.map { it[WHISPARR_QUALITY_PROFILE_ID] ?: 4 }
    val whisparrRootFolderPath: Flow<String> = dataStore.data.map { it[WHISPARR_ROOT_FOLDER_PATH] ?: "" }

    val stashDBUrl: Flow<String> = dataStore.data.map { it[STASHDB_URL] ?: "https://stashdb.org/graphql" }
    val stashDBApiKey: Flow<String> = dataStore.data.map { it[STASHDB_API_KEY] ?: "" }
    val excludeVRFromStashDB: Flow<Boolean> = dataStore.data.map { it[EXCLUDE_VR_FROM_STASHDB] ?: false }
    val excludeCompilationsFromStashDB: Flow<Boolean> = dataStore.data.map {
        it[EXCLUDE_COMPILATIONS_FROM_STASHDB] ?: false
    }
    val stashDBFavoritesCache: Flow<String> = dataStore.data.map { it[STASHDB_FAVORITES_CACHE] ?: "" }

    val followedTagIds: Flow<List<String>> = dataStore.data.map {
        val raw = it[FOLLOWED_TAG_IDS] ?: ""
        val ids = raw.split(",").filter { id -> id.isNotEmpty() }
        Timber.d("⚙️ SettingsStore.followedTagIds: Raw='$raw', Parsed=${ids.size} IDs: $ids")
        ids
    }

    val syncSavedFilters: Flow<Boolean> = dataStore.data.map { it[SYNC_SAVED_FILTERS] ?: false }
    val syncHomeLayout: Flow<Boolean> = dataStore.data.map { it[SYNC_HOME_LAYOUT] ?: false }

    val prefetchOnScroll: Flow<Boolean> = dataStore.data.map { it[PREFETCH_ON_SCROLL] ?: true }
    val prefetchHomeSceneDetails: Flow<Boolean> = dataStore.data.map { it[PREFETCH_HOME_SCENE_DETAILS] ?: true }
    val showScenePreviews: Flow<Boolean> = dataStore.data.map { it[SHOW_SCENE_PREVIEWS] ?: false }
    val muteScenePreviews: Flow<Boolean> = dataStore.data.map { it[MUTE_SCENE_PREVIEWS] ?: true }
    val showMarkerPreviews: Flow<Boolean> = dataStore.data.map { it[SHOW_MARKER_PREVIEWS] ?: false }
    val blurNsfw: Flow<Boolean> = dataStore.data.map { it[BLUR_NSFW] ?: false }

    val generationOptions: Flow<GenerationOptions> = dataStore.data.map {
        val jsonString = it[GENERATION_OPTIONS] ?: "{}"
        try {
            json.decodeFromString<GenerationOptions>(jsonString)
        } catch (e: Exception) {
            GenerationOptions()
        }
    }

    val scanOptions: Flow<ScanOptions> = dataStore.data.map {
        val jsonString = it[SCAN_OPTIONS] ?: "{}"
        try {
            json.decodeFromString<ScanOptions>(jsonString)
        } catch (e: Exception) {
            ScanOptions()
        }
    }

    val performerSortType: Flow<String> = dataStore.data.map { it[PERFORMER_SORT_TYPE] ?: "name" }
    val performerSortDirection: Flow<String> = dataStore.data.map { it[PERFORMER_SORT_DIRECTION] ?: "ASC" }
    val sceneSortType: Flow<String> = dataStore.data.map { it[SCENE_SORT_TYPE] ?: "date" }
    val sceneSortDirection: Flow<String> = dataStore.data.map { it[SCENE_SORT_DIRECTION] ?: "DESC" }
    val studioSortType: Flow<String> = dataStore.data.map { it[STUDIO_SORT_TYPE] ?: "name" }
    val studioSortDirection: Flow<String> = dataStore.data.map { it[STUDIO_SORT_DIRECTION] ?: "ASC" }
    val performerViewType: Flow<String> = dataStore.data.map { it[PERFORMER_VIEW_TYPE] ?: "GRID_LARGE" }
    val sceneViewType: Flow<String> = dataStore.data.map { it[SCENE_VIEW_TYPE] ?: "LIST" }
    val studioViewType: Flow<String> = dataStore.data.map { it[STUDIO_VIEW_TYPE] ?: "GRID" }

    val homeSectionOrder: Flow<List<String>> = dataStore.data.map {
        (it[HOME_SECTION_ORDER] ?: "").split(",").filter { id -> id.isNotEmpty() }
    }

    val homeSectionHidden: Flow<List<String>> = dataStore.data.map {
        (it[HOME_SECTION_HIDDEN] ?: "").split(",").filter { id -> id.isNotEmpty() }
    }

    val showDefaultRows: Flow<Boolean> = dataStore.data.map { it[SHOW_DEFAULT_ROWS] ?: true }

    val hideBottomBarLabels: Flow<Boolean> = dataStore.data.map { it[HIDE_BOTTOM_BAR_LABELS] ?: false }

    val theme: Flow<String> = dataStore.data.map { it[THEME] ?: "goonarr" }

    // Updates
    suspend fun setServerUrl(url: String) {
        dataStore.edit { it[SERVER_URL] = url }
    }

    suspend fun setApiKey(key: String) {
        dataStore.edit { it[API_KEY] = key }
    }

    suspend fun setWhisparrUrl(url: String) {
        dataStore.edit { it[WHISPARR_URL] = url }
    }

    suspend fun setWhisparrApiKey(key: String) {
        dataStore.edit { it[WHISPARR_API_KEY] = key }
    }

    suspend fun setWhisparrQualityProfileId(id: Int) {
        dataStore.edit { it[WHISPARR_QUALITY_PROFILE_ID] = id }
    }

    suspend fun setWhisparrRootFolderPath(path: String) {
        dataStore.edit { it[WHISPARR_ROOT_FOLDER_PATH] = path }
    }

    suspend fun setStashDBApiKey(key: String) {
        dataStore.edit { it[STASHDB_API_KEY] = key }
    }

    suspend fun setExcludeVRFromStashDB(exclude: Boolean) {
        dataStore.edit { it[EXCLUDE_VR_FROM_STASHDB] = exclude }
    }

    suspend fun setExcludeCompilationsFromStashDB(exclude: Boolean) {
        dataStore.edit { it[EXCLUDE_COMPILATIONS_FROM_STASHDB] = exclude }
    }

    suspend fun setStashDBFavoritesCache(jsonCache: String) {
        dataStore.edit { it[STASHDB_FAVORITES_CACHE] = jsonCache }
    }

    suspend fun addFollowedTag(id: String) {
        Timber.d("⚙️ SettingsStore.addFollowedTag: Adding tag id='$id'")
        dataStore.edit { preferences ->
            val current = (preferences[FOLLOWED_TAG_IDS] ?: "").split(",").filter { it.isNotEmpty() }.toMutableList()
            Timber.d("⚙️ SettingsStore.addFollowedTag: Current list has ${current.size} tags: $current")
            if (!current.contains(id)) {
                current.add(id)
                preferences[FOLLOWED_TAG_IDS] = current.joinToString(",")
                Timber.d("⚙️ SettingsStore.addFollowedTag: ✅ Added '$id'. New list: $current")
            } else {
                Timber.d("⚙️ SettingsStore.addFollowedTag: ⚠️ Tag '$id' already in list, skipping")
            }
        }
    }

    suspend fun removeFollowedTag(id: String) {
        Timber.d("⚙️ SettingsStore.removeFollowedTag: Removing tag id='$id'")
        dataStore.edit { preferences ->
            val current = (preferences[FOLLOWED_TAG_IDS] ?: "").split(",").filter { it.isNotEmpty() }.toMutableList()
            Timber.d("⚙️ SettingsStore.removeFollowedTag: Current list has ${current.size} tags: $current")
            if (current.remove(id)) {
                preferences[FOLLOWED_TAG_IDS] = current.joinToString(",")
                Timber.d("⚙️ SettingsStore.removeFollowedTag: ✅ Removed '$id'. New list: $current")
            } else {
                Timber.d("⚙️ SettingsStore.removeFollowedTag: ⚠️ Tag '$id' not in list, skipping")
            }
        }
    }

    suspend fun setBlurNsfw(enabled: Boolean) {
        dataStore.edit { it[BLUR_NSFW] = enabled }
    }

    suspend fun setSyncSavedFilters(enabled: Boolean) {
        Timber.d("⚙️ SettingsStore.setSyncSavedFilters: $enabled")
        dataStore.edit { it[SYNC_SAVED_FILTERS] = enabled }
    }

    suspend fun setSyncHomeLayout(enabled: Boolean) {
        Timber.d("⚙️ SettingsStore.setSyncHomeLayout: $enabled")
        dataStore.edit { it[SYNC_HOME_LAYOUT] = enabled }
    }

    suspend fun setPrefetchOnScroll(enabled: Boolean) {
        dataStore.edit { it[PREFETCH_ON_SCROLL] = enabled }
    }

    suspend fun setShowScenePreviews(enabled: Boolean) {
        dataStore.edit { it[SHOW_SCENE_PREVIEWS] = enabled }
    }

    suspend fun setGenerationOptions(options: GenerationOptions) {
        dataStore.edit {
            it[GENERATION_OPTIONS] = json.encodeToString(GenerationOptions.serializer(), options)
        }
    }

    suspend fun setScanOptions(options: ScanOptions) {
        dataStore.edit {
            it[SCAN_OPTIONS] = json.encodeToString(ScanOptions.serializer(), options)
        }
    }

    suspend fun setPerformerSort(type: String, direction: String) {
        dataStore.edit {
            it[PERFORMER_SORT_TYPE] = type
            it[PERFORMER_SORT_DIRECTION] = direction
        }
    }

    suspend fun setSceneSort(type: String, direction: String) {
        dataStore.edit {
            it[SCENE_SORT_TYPE] = type
            it[SCENE_SORT_DIRECTION] = direction
        }
    }

    suspend fun setStudioSort(type: String, direction: String) {
        dataStore.edit {
            it[STUDIO_SORT_TYPE] = type
            it[STUDIO_SORT_DIRECTION] = direction
        }
    }

    suspend fun setPerformerViewType(viewType: String) {
        dataStore.edit { it[PERFORMER_VIEW_TYPE] = viewType }
    }

    suspend fun setSceneViewType(viewType: String) {
        dataStore.edit { it[SCENE_VIEW_TYPE] = viewType }
    }

    suspend fun setStudioViewType(viewType: String) {
        dataStore.edit { it[STUDIO_VIEW_TYPE] = viewType }
    }

    suspend fun setHomeSectionOrder(order: List<String>) {
        dataStore.edit { it[HOME_SECTION_ORDER] = order.joinToString(",") }
    }

    suspend fun setHomeSectionHidden(hidden: List<String>) {
        dataStore.edit { it[HOME_SECTION_HIDDEN] = hidden.joinToString(",") }
    }

    suspend fun setShowDefaultRows(enabled: Boolean) {
        dataStore.edit { it[SHOW_DEFAULT_ROWS] = enabled }
    }

    suspend fun setHideBottomBarLabels(hide: Boolean) {
        dataStore.edit { it[HIDE_BOTTOM_BAR_LABELS] = hide }
    }

    suspend fun setTheme(theme: String) {
        dataStore.edit { it[THEME] = theme }
    }

    suspend fun setHomeLayoutConfig(order: List<String>, hidden: List<String>) {
        dataStore.edit {
            it[HOME_SECTION_ORDER] = order.joinToString(",")
            it[HOME_SECTION_HIDDEN] = hidden.joinToString(",")
        }
    }
}
