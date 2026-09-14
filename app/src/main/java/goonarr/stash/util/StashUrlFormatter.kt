package goonarr.stash.util

import goonarr.stash.core.database.SettingsStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/**
 * Utility for resolving relative paths to absolute URLs using the base URL from settings.
 * Needed for non-Coil loaders like ExoPlayer.
 */
@Singleton
class StashUrlFormatter @Inject constructor(
    private val settingsStore: SettingsStore
) {
    /**
     * Resolves a path (e.g., "/stream/xyz") to a full URL (e.g., "http://server:9999/stream/xyz").
     * Appends API key if available.
     */
    suspend fun resolveUrl(path: String?): String? {
        if (path.isNullOrBlank()) return null

        val baseUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()

        var fullUrl = if (path.startsWith("http")) {
            path
        } else {
            val cleanBase = baseUrl.trimEnd('/')
            val cleanPath = if (path.startsWith("/")) path else "/$path"
            "$cleanBase$cleanPath"
        }

        if (apiKey.isNotBlank()) {
            val separator = if (fullUrl.contains("?")) "&" else "?"
            fullUrl += "${separator}apikey=$apiKey"
        }

        return fullUrl
    }
}
