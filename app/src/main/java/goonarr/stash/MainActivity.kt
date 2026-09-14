package goonarr.stash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import goonarr.stash.core.database.SettingsStore
import goonarr.stash.features.player.GlobalPlayerState
import goonarr.stash.features.player.LocalGlobalPlayerState
import goonarr.stash.features.sync.SyncLoadingScreen
import goonarr.stash.features.sync.SyncRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Main entry point for the Stash Android application.
 *
 * This activity uses:
 * - Hilt for dependency injection (@AndroidEntryPoint)
 * - Jetpack Compose for UI
 * - Material 3 theming
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsStore: SettingsStore

    @Inject
    lateinit var globalPlayerState: GlobalPlayerState

    @Inject
    lateinit var syncRepository: SyncRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        setContent {
            val blurNsfw by settingsStore.blurNsfw.collectAsState(initial = false)
            val themeString by settingsStore.theme.collectAsState(initial = "goonarr")
            val theme = when (themeString) {
                "stash" -> ThemeType.STASH
                "material_you" -> ThemeType.MATERIAL_YOU
                else -> ThemeType.GOONARR
            }
            var showSyncScreen by remember { mutableStateOf(false) }
            var hasCheckedSync by remember { mutableStateOf(false) }
            val syncUiState by syncRepository.syncUiState.collectAsState()

            // Check if we need to show sync screen on first launch
            LaunchedEffect(Unit) {
                val needsInitialSync = !syncRepository.hasCompletedInitialSync()
                val serverUrl = settingsStore.serverUrl.first()
                val apiKey = settingsStore.apiKey.first()

                if (needsInitialSync && serverUrl.isNotEmpty()) {
                    showSyncScreen = true
                    Timber.d("🚀 First launch: Starting initial sync")
                    lifecycleScope.launch {
                        syncRepository.syncAll(serverUrl, apiKey)
                    }
                } else if (serverUrl.isNotEmpty()) {
                    // Background sync for returning users
                    Timber.d("🔄 Background sync for returning user")
                    lifecycleScope.launch {
                        syncRepository.syncAll(serverUrl, apiKey)
                    }
                }
                hasCheckedSync = true
            }

            StashTheme(theme = theme, blurNsfw = blurNsfw) {
                CompositionLocalProvider(LocalGlobalPlayerState provides globalPlayerState) {
                    if (showSyncScreen && !syncUiState.isComplete) {
                        SyncLoadingScreen(
                            onSyncComplete = {
                                showSyncScreen = false
                            }
                        )
                    } else if (hasCheckedSync) {
                        MainScreen(globalPlayerState)
                    }
                }
            }
        }
    }
}
