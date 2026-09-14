package goonarr.stash.core.services

import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.model.Job
import goonarr.stash.core.network.GraphQLWebSocketClient
import goonarr.stash.core.network.WebSocketState
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import timber.log.Timber

/**
 * A service that manages real-time job data streams over WebSocket.
 * Singleton that can be observed from any screen needing job status.
 */
@Singleton
class StashSubscriptionService @Inject constructor(
    private val webSocketClient: GraphQLWebSocketClient,
    private val settingsStore: SettingsStore,
    private val json: Json
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // Jobs State
    private val _jobs = MutableStateFlow<List<Job>>(emptyList())
    val jobs: StateFlow<List<Job>> = _jobs.asStateFlow()

    // Connection State - debounced to prevent flashing during transient drops
    @OptIn(FlowPreview::class)
    val connectionState: StateFlow<WebSocketState> = webSocketClient.state
        .debounce { state ->
            if (state is WebSocketState.Connected) 0L else 500L
        }
        .stateIn(scope, SharingStarted.Eagerly, webSocketClient.state.value)

    val isConnected: Boolean
        get() = webSocketClient.state.value == WebSocketState.Connected

    init {
        setupStateObservation()
    }

    private fun setupStateObservation() {
        scope.launch {
            webSocketClient.state.collect { state ->
                when (state) {
                    is WebSocketState.Connected -> {
                        Timber.d("🟢 WebSocket connected, subscribing to jobs")
                        subscribeToJobs()
                    }
                    is WebSocketState.Disconnected -> {
                        Timber.d("🔴 WebSocket disconnected")
                    }
                    is WebSocketState.Connecting -> {
                        Timber.d("🟡 WebSocket connecting...")
                    }
                    is WebSocketState.Reconnecting -> {
                        Timber.d("🟡 WebSocket reconnecting (attempt ${state.attempt})...")
                    }
                    is WebSocketState.Error -> {
                        Timber.e("❌ WebSocket error: ${state.message}")
                    }
                }
            }
        }
    }

    /**
     * Connects to the Stash server WebSocket.
     * Call this when the app starts or when settings change.
     */
    fun connect() {
        scope.launch {
            val url = settingsStore.serverUrl.first()
            val apiKey = settingsStore.apiKey.first()

            if (url.isNotEmpty()) {
                Timber.d("📡 Connecting to WebSocket at $url")
                webSocketClient.connect(url, apiKey)
            } else {
                Timber.w("⚠️ No server URL configured, skipping WebSocket connection")
            }
        }
    }

    /**
     * Disconnects from the WebSocket server.
     */
    fun disconnect() {
        webSocketClient.disconnect()
        _jobs.value = emptyList()
    }

    private fun subscribeToJobs() {
        val query = """
            subscription JobsSubscribe {
              jobsSubscribe {
                type
                job {
                  id
                  status
                  subTasks
                  description
                  progress
                  error
                  startTime
                  endTime
                }
              }
            }
        """.trimIndent()

        webSocketClient.subscribe("job-sub", query) { text ->
            scope.launch {
                handleJobMessage(text)
            }
        }
        Timber.d("📤 Subscribed to Jobs")
    }

    private fun handleJobMessage(text: String) {
        try {
            val response = json.decodeFromString<JobSubscriptionResponse>(text)
            val jobData = response.payload?.data?.jobsSubscribe?.job ?: return

            updateJob(jobData)
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse job message: $text")
        }
    }

    private fun updateJob(job: Job) {
        val currentJobs = _jobs.value.toMutableList()
        var isNewlyCompleted = false

        val existingIndex = currentJobs.indexOfFirst { it.id == job.id }

        if (existingIndex >= 0) {
            val oldJob = currentJobs[existingIndex]
            if (!oldJob.isCompleted && job.isCompleted) {
                isNewlyCompleted = true
            }
            currentJobs[existingIndex] = job
        } else {
            currentJobs.add(0, job)
            if (job.isCompleted) {
                isNewlyCompleted = true
            }
        }

        _jobs.value = currentJobs
        Timber.d("📥 Job updated: ${job.id} - ${job.status} - ${job.description}")

        if (isNewlyCompleted) {
            // Schedule removal after 5 seconds
            scope.launch {
                delay(5000)
                _jobs.value = _jobs.value.filter { it.id != job.id }
                Timber.d("🗑️ Removed completed job: ${job.id}")
            }
        }
    }
}

// MARK: - Response Models for Subscription

@Serializable
private data class JobSubscriptionResponse(
    val payload: JobSubscriptionPayload? = null
)

@Serializable
private data class JobSubscriptionPayload(
    val data: JobSubscriptionData? = null
)

@Serializable
private data class JobSubscriptionData(
    val jobsSubscribe: JobsSubscribeEvent? = null
)

@Serializable
private data class JobsSubscribeEvent(
    val type: String? = null,
    val job: Job? = null
)
