package goonarr.stash.core.network

import android.graphics.BitmapFactory
import android.util.Base64
import coil.ImageLoader
import coil.decode.DataSource
import coil.decode.ImageSource
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.request.Options
import okio.Buffer
import timber.log.Timber

/**
 * Custom Coil Fetcher that handles data: URIs (Base64-encoded images).
 *
 * Note: SmartImage composable in SceneCard.kt provides the primary Base64 handling.
 * This fetcher is registered as a fallback for any AsyncImage calls with data: URIs.
 */
class DataUriFetcher(
    private val data: String,
    private val options: Options
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val base64Data = data.substringAfter("base64,")
        if (base64Data.isEmpty()) return null

        return try {
            val cleanBase64 = base64Data.replace("\\s".toRegex(), "")
            val decoded = Base64.decode(cleanBase64, Base64.DEFAULT)

            val bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
                ?: return null

            val buffer = Buffer().write(decoded)
            SourceResult(
                source = ImageSource(buffer, options.context),
                mimeType = "image/jpeg",
                dataSource = DataSource.MEMORY
            )
        } catch (e: Exception) {
            Timber.w(e, "DataUriFetcher: Failed to decode Base64 image")
            null
        }
    }

    class Factory : Fetcher.Factory<String> {
        override fun create(data: String, options: Options, imageLoader: ImageLoader): Fetcher? {
            if (!data.startsWith("data:")) return null
            return DataUriFetcher(data, options)
        }
    }
}

/**
 * Coil Keyer that generates unique cache keys for data: URIs.
 */
class DataUriKeyer : coil.key.Keyer<String> {
    override fun key(data: String, options: Options): String? {
        if (!data.startsWith("data:")) return null
        return "data_uri_${data.hashCode()}"
    }
}
