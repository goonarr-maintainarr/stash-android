package goonarr.stash.core.network

import coil.intercept.Interceptor
import coil.request.ImageResult
import goonarr.stash.core.database.SettingsStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import timber.log.Timber

/**
 * Coil Interceptor that resolves relative Stash image paths to absolute URLs
 * and appends the API key for authentication.
 */
@Singleton
class StashUrlInterceptor @Inject constructor(
    private val settingsStore: SettingsStore
) : Interceptor {

    override suspend fun intercept(chain: Interceptor.Chain): coil.request.ImageResult {
        val request = chain.request
        val originalData = request.data

        val dataStr = when (originalData) {
            is String -> originalData
            is android.net.Uri -> originalData.toString()
            is okhttp3.HttpUrl -> originalData.toString()
            else -> null
        }

        if (dataStr == null) return chain.proceed(request)
        if (dataStr.startsWith("data:")) return chain.proceed(request) // Skip base64

        val serverUrl = settingsStore.serverUrl.first()
        val apiKey = settingsStore.apiKey.first()

        val serverHttpUrl = serverUrl.toHttpUrlOrNull() ?: return chain.proceed(request)
        val baseUrl = if (serverHttpUrl.pathSegments.lastOrNull() == "graphql") {
            serverHttpUrl.newBuilder().removePathSegment(serverHttpUrl.pathSize - 1).build()
        } else {
            serverHttpUrl
        }

        var finalUrl: okhttp3.HttpUrl? = null

        if (dataStr.startsWith("http")) {
            finalUrl = dataStr.toHttpUrlOrNull()
        } else {
            // Relative path - could be "/scene/..." or "scene/..."
            try {
                finalUrl = baseUrl.newBuilder()
                    .addPathSegments(dataStr.trimStart('/'))
                    .build()
            } catch (e: Exception) {
                Timber.e(e, "🌐 Failed to construct URL for $dataStr")
            }
        }

        if (finalUrl == null) return chain.proceed(request)

        // Append API Key if missing
        var authenticatedUrl = finalUrl
        if (apiKey.isNotEmpty() && authenticatedUrl.queryParameter("apikey") == null) {
            authenticatedUrl = authenticatedUrl.newBuilder()
                .addQueryParameter("apikey", apiKey)
                .build()
        }

        if (authenticatedUrl.toString() != dataStr) {
            val newRequest = request.newBuilder()
                .data(authenticatedUrl.toString())
                .build()
            return chain.proceed(newRequest)
        }

        return chain.proceed(request)
    }
}
