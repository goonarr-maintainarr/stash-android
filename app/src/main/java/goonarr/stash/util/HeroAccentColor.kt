package goonarr.stash.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

object HeroAccentColor {
    private val colorCache = mutableMapOf<String, Color>()

    /**
     * Extracts a hero accent color from the image data (URL, File, Res ID, etc).
     * Results are cached to avoid redundant processing.
     */
    suspend fun extract(context: Context, data: Any?, imageLoader: ImageLoader? = null): Color? =
        withContext(Dispatchers.Default) {
            if (data == null) return@withContext null

            val cacheKey = data.toString()
            colorCache[cacheKey]?.let { return@withContext it }

            try {
                val logData = if (cacheKey.length > 50) cacheKey.take(50) + "..." else cacheKey
                Timber.d("🌐 Fetching image for hero color: $logData")
                val loader = imageLoader ?: context.imageLoader
                val request = ImageRequest.Builder(context)
                    .data(data)
                    .allowHardware(false)
                    .build()

                val result = loader.execute(request)
                if (result is SuccessResult) {
                    val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                    if (bitmap != null) {
                        val color = extractPalette(bitmap)
                        if (color != null) {
                            colorCache[cacheKey] = color
                            return@withContext color
                        }
                    } else {
                        Timber.w("🖼️ Success result but bitmap was null for $logData")
                    }
                } else if (result is coil.request.ErrorResult) {
                    Timber.w("❌ Coil loading failed for $logData: ${result.throwable.message}")
                }
            } catch (e: IllegalStateException) {
                Timber.w("⚠️ Failed to decode valid bitmap for $cacheKey (likely empty/invalid image source)")
            } catch (e: Exception) {
                Timber.e(e, "🔥 Exception during hero color extraction for $cacheKey")
            }

            return@withContext null
        }

    /**
     * Extracts a hero accent color directly from a [Bitmap].
     */
    suspend fun extractFromBitmap(bitmap: Bitmap): Color? =
        withContext(Dispatchers.Default) {
            return@withContext extractPalette(bitmap)
        }

    /**
     * Common helper to extract a dominant or vibrant color from a bitmap.
     */
    private fun extractPalette(bitmap: Bitmap): Color? {
        try {
            Timber.d("🎨 Extracting palette from ${bitmap.width}x${bitmap.height} bitmap")
            val palette = Palette.from(bitmap).generate()

            // Comprehensive swatch selection
            val swatch = palette.vibrantSwatch
                ?: palette.lightVibrantSwatch
                ?: palette.darkVibrantSwatch
                ?: palette.mutedSwatch
                ?: palette.lightMutedSwatch
                ?: palette.darkMutedSwatch
                ?: palette.swatches.maxByOrNull { it.population }

            if (swatch != null) {
                val color = Color(swatch.rgb)
                Timber.d("✅ Hero color found: $color (population: ${swatch.population})")
                return color
            } else {
                Timber.w("⚠️ No swatches found in palette")
            }
        } catch (e: Exception) {
            Timber.e(e, "🔥 Exception during palette extraction")
        }
        return null
    }

    /**
     * Clears the color cache.
     */
    fun clearCache() {
        colorCache.clear()
    }
}
