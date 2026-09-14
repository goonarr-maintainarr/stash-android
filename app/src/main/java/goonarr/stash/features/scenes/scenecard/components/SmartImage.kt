package goonarr.stash.features.scenes.scenecard.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import timber.log.Timber

/**
 * Smart image composable that handles both URL images (via Coil) and data: URI images (via direct bitmap decoding).
 * Use this instead of AsyncImage when the image source might be a base64 data URI.
 */
@Composable
fun SmartImage(
    model: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    if (model == null) {
        Box(
            modifier = modifier.background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            // Empty placeholder
        }
        return
    }

    if (model.startsWith("data:")) {
        // Decode base64 directly
        val bitmap = remember(model) {
            decodeDataUriToBitmap(model)
        }
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        } else {
            Box(
                modifier = modifier.background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text("Failed", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
            }
        }
    } else {
        // Use Coil for URL images
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(model)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}

/**
 * Decodes a data: URI (base64-encoded image) into a Bitmap.
 */
private fun decodeDataUriToBitmap(dataUri: String): Bitmap? {
    return try {
        val base64Data = dataUri.substringAfter("base64,")
        if (base64Data.isEmpty()) return null

        val cleanBase64 = base64Data.replace("\\s".toRegex(), "")
        val decoded = Base64.decode(cleanBase64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decoded, 0, decoded.size)
    } catch (e: Exception) {
        Timber.e(e, "Failed to decode data URI to bitmap")
        null
    }
}
