package goonarr.stash.features.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.di.IoDispatcher
import goonarr.stash.core.model.Stats
import goonarr.stash.core.network.StashClient
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

sealed class StatsUiState {
    data object Loading : StatsUiState()
    data class Success(val stats: Stats) : StatsUiState()
    data class Error(val message: String) : StatsUiState()
}

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val client: StashClient,
    private val settingsStore: SettingsStore,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatsUiState>(StatsUiState.Loading)
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = StatsUiState.Loading
            try {
                val url = settingsStore.serverUrl.first()
                val apiKey = settingsStore.apiKey.first()

                Timber.d("📊 Fetching stats from $url")
                val stats = client.fetchStats(url, apiKey)
                _uiState.value = StatsUiState.Success(stats)
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to load stats")
                _uiState.value = StatsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
