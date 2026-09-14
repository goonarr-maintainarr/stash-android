package goonarr.stash.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.model.GenerationOptions
import goonarr.stash.core.model.Job
import goonarr.stash.core.model.ScanOptions
import goonarr.stash.core.network.StashClient
import goonarr.stash.core.network.WebSocketState
import goonarr.stash.core.network.stashdb.StashDBClient
import goonarr.stash.core.services.StashSubscriptionService
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for the Settings screen.
 *
 * Manages the connection state, server URL, API key, and background job list.
 * It also coordinates triggering library scans and content generation using currently stored options.
 *
 * @property settingsStore The persistent storage for application settings.
 * @property client The API client for communicating with the Stash server.
 * @property subscriptionService The service handling real-time job updates via WebSocket.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsStore: SettingsStore,
    private val client: StashClient,
    private val stashDBClient: StashDBClient,
    private val subscriptionService: StashSubscriptionService
) : ViewModel() {

    // Connection State
    val serverUrl = settingsStore.serverUrl.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val apiKey = settingsStore.apiKey.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _isTestingConnection = MutableStateFlow(false)
    val isTestingConnection = _isTestingConnection.asStateFlow()

    private val _connectionTestMessage = MutableStateFlow<String?>(null)
    val connectionTestMessage = _connectionTestMessage.asStateFlow()

    private val _connectionTestSuccess = MutableStateFlow(false)
    val connectionTestSuccess = _connectionTestSuccess.asStateFlow()

    // Jobs State - from subscription service
    val jobs: StateFlow<List<Job>> = subscriptionService.jobs

    // WebSocket connection state
    val webSocketState: StateFlow<WebSocketState> = subscriptionService.connectionState

    // Scan and Generation Options - exposed for options screens
    val scanOptions = settingsStore.scanOptions.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ScanOptions()
    )

    val generationOptions = settingsStore.generationOptions.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        GenerationOptions()
    )

    // Appearance Settings

    val hideBottomBarLabels = settingsStore.hideBottomBarLabels.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    val theme = settingsStore.theme.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "goonarr"
    )

    // StashDB Settings
    val stashDBApiKey = settingsStore.stashDBApiKey.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ""
    )

    val excludeVRFromStashDB = settingsStore.excludeVRFromStashDB.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    val excludeCompilationsFromStashDB = settingsStore.excludeCompilationsFromStashDB.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        false
    )

    private val _isTestingStashDB = MutableStateFlow(false)
    val isTestingStashDB = _isTestingStashDB.asStateFlow()

    private val _stashDBTestMessage = MutableStateFlow<String?>(null)
    val stashDBTestMessage = _stashDBTestMessage.asStateFlow()

    private val _stashDBTestSuccess = MutableStateFlow(false)
    val stashDBTestSuccess = _stashDBTestSuccess.asStateFlow()

    init {
        // Connect the subscription service when ViewModel is created
        subscriptionService.connect()
    }

    // Connection Methods
    fun updateServerUrl(url: String) {
        viewModelScope.launch {
            settingsStore.setServerUrl(url)
            resetConnectionTest()
            // Reconnect with new URL
            subscriptionService.disconnect()
            subscriptionService.connect()
        }
    }

    fun updateApiKey(key: String) {
        viewModelScope.launch {
            settingsStore.setApiKey(key)
            resetConnectionTest()
            // Reconnect with new API key
            subscriptionService.disconnect()
            subscriptionService.connect()
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _isTestingConnection.value = true
            _connectionTestMessage.value = null

            val url = serverUrl.value
            val key = apiKey.value

            try {
                val success = client.testConnection(url, key)
                _connectionTestSuccess.value = success
                _connectionTestMessage.value = if (success) "Connection successful!" else "Connection failed"
            } catch (e: Exception) {
                Timber.e(e, "Connection test error")
                _connectionTestSuccess.value = false
                _connectionTestMessage.value = "Error: ${e.message}"
            } finally {
                _isTestingConnection.value = false
            }
        }
    }

    private fun resetConnectionTest() {
        _connectionTestMessage.value = null
        _connectionTestSuccess.value = false
    }

    fun stopJob(jobId: String) {
        viewModelScope.launch {
            val url = serverUrl.value
            val key = apiKey.value
            client.stopJob(url, key, jobId)
        }
    }

    fun stopAllJobs() {
        viewModelScope.launch {
            val url = serverUrl.value
            val key = apiKey.value
            client.stopAllJobs(url, key)
        }
    }

    // Options Update Methods
    fun updateScanOptions(options: ScanOptions) {
        viewModelScope.launch {
            settingsStore.setScanOptions(options)
        }
    }

    fun updateGenerationOptions(options: GenerationOptions) {
        viewModelScope.launch {
            settingsStore.setGenerationOptions(options)
        }
    }

    fun setHideBottomBarLabels(hide: Boolean) {
        viewModelScope.launch {
            settingsStore.setHideBottomBarLabels(hide)
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            settingsStore.setTheme(theme)
        }
    }

    // StashDB Settings Methods
    fun updateStashDBApiKey(key: String) {
        viewModelScope.launch {
            settingsStore.setStashDBApiKey(key)
            resetStashDBConnectionTest()
        }
    }

    fun setExcludeVRFromStashDB(exclude: Boolean) {
        viewModelScope.launch {
            settingsStore.setExcludeVRFromStashDB(exclude)
        }
    }

    fun setExcludeCompilationsFromStashDB(exclude: Boolean) {
        viewModelScope.launch {
            settingsStore.setExcludeCompilationsFromStashDB(exclude)
        }
    }

    fun testStashDBConnection() {
        viewModelScope.launch {
            _isTestingStashDB.value = true
            _stashDBTestMessage.value = null

            val url = settingsStore.stashDBUrl.first()
            val key = stashDBApiKey.value

            try {
                val success = stashDBClient.testConnection(url, key)
                _stashDBTestSuccess.value = success
                _stashDBTestMessage.value = if (success) "Connection successful!" else "Connection failed"
            } catch (e: Exception) {
                Timber.e(e, "StashDB connection test error")
                _stashDBTestSuccess.value = false
                _stashDBTestMessage.value = "Error: ${e.message}"
            } finally {
                _isTestingStashDB.value = false
            }
        }
    }

    private fun resetStashDBConnectionTest() {
        _stashDBTestMessage.value = null
        _stashDBTestSuccess.value = false
    }

    // Task Triggers - now use stored options
    fun triggerScan() {
        viewModelScope.launch {
            val url = serverUrl.value
            val key = apiKey.value
            val options = settingsStore.scanOptions.first()
            Timber.d("Triggering scan with options: $options")
            client.requestScan(url, key, options)
        }
    }

    fun triggerGeneration() {
        viewModelScope.launch {
            val url = serverUrl.value
            val key = apiKey.value
            val options = settingsStore.generationOptions.first()
            Timber.d("Triggering generation with options: $options")
            client.requestGeneration(url, key, options)
        }
    }
}
