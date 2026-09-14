package goonarr.stash.features.scenes.utils

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import dagger.hilt.android.qualifiers.ApplicationContext
import goonarr.stash.core.model.SpriteFrame
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Manages sprite sheet image and provides thumbnail extraction for video scrubbing.
 */
@Singleton
class SpriteManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: HttpClient,
    private val vttParser: VTTParser
) {
    private var spriteBitmap: Bitmap? = null
    private var frames: List<SpriteFrame> = emptyList()

    // Simple LRU-like cache for cropped thumbnails could be added here if performance is an issue,
    // but typically we just crop on demand for scrubbing as 50-60 crops/sec is fine for modern phones.
    // We will cache the last cropped bitmap to avoid churn if multiple requests come for same time.
    private var lastFrameIndex: Int = -1
    private var lastCroppedBitmap: Bitmap? = null

    /**
     * Initialize with VTT and Sprite URLs.
     */
    suspend fun initialize(vttUrl: String?, spriteUrl: String?) = withContext(Dispatchers.IO) {
        if (vttUrl == null || spriteUrl == null) {
            Timber.w("SpriteManager: Missing VTT or Sprite URL")
            return@withContext
        }

        try {
            // 1. Fetch and Parse VTT
            Timber.d("Fetching VTT: $vttUrl")
            val vttContent = httpClient.get(vttUrl).bodyAsText()
            frames = vttParser.parse(vttContent)
            Timber.d("Parsed ${frames.size} frames from VTT")

            // 2. Fetch Sprite Image
            Timber.d("Fetching Sprite: $spriteUrl")
            // Use Coil to load the image efficiently
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(spriteUrl)
                .allowHardware(false) // Software bitmap required for Canvas drawing/cropping
                .build()

            val result = loader.execute(request)
            if (result is SuccessResult) {
                spriteBitmap = result.drawable.toBitmap()
                Timber.d("Loaded sprite bitmap: ${spriteBitmap?.width}x${spriteBitmap?.height}")
            } else {
                Timber.e("Failed to load sprite image")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error initializing SpriteManager")
        }
    }

    /**
     * Get the frame for a specific timestamp.
     */
    fun getFrame(time: Double): SpriteFrame? {
        if (frames.isEmpty()) return null

        // Find the frame that covers this time
        return frames.firstOrNull { time >= it.startTime && time < it.endTime }
            ?: frames.lastOrNull { time >= it.startTime } // Fallback to last frame if slightly past end
    }

    /**
     * Get the cropped thumbnail for a specific timestamp.
     */
    fun getThumbnail(time: Double): Bitmap? {
        val bitmap = spriteBitmap ?: return null
        val frame = getFrame(time) ?: return null

        // Return cached bitmap if index matches
        val frameIndex = frames.indexOf(frame)
        if (frameIndex == lastFrameIndex && lastCroppedBitmap != null) {
            return lastCroppedBitmap
        }

        try {
            // Validate coordinates
            if (frame.x + frame.width > bitmap.width || frame.y + frame.height > bitmap.height) {
                Timber.w("Frame coordinates out of bounds: $frame vs ${bitmap.width}x${bitmap.height}")
                return null
            }

            val cropped = Bitmap.createBitmap(
                bitmap,
                frame.x,
                frame.y,
                frame.width,
                frame.height
            )

            lastFrameIndex = frameIndex
            lastCroppedBitmap = cropped
            return cropped
        } catch (e: Exception) {
            Timber.e(e, "Error cropping sprite")
            return null
        }
    }

    fun clear() {
        // Recycle bitmaps if needed or just let GC handle it
        spriteBitmap = null
        lastCroppedBitmap = null
        frames = emptyList()
        lastFrameIndex = -1
    }
}
