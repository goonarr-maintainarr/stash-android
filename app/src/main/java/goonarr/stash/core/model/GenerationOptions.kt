package goonarr.stash.core.model

import kotlinx.serialization.Serializable

@Serializable
data class GenerationOptions(
    val covers: Boolean = true,
    val sprites: Boolean = true,
    val previews: Boolean = true,
    val imagePreviews: Boolean = true,
    val markers: Boolean = true,
    val markerImagePreviews: Boolean = true,
    val markerScreenshots: Boolean = true,
    val transcodes: Boolean = true,
    val forceTranscodes: Boolean = false,
    val phashes: Boolean = true,
    val interactiveHeatmapsSpeeds: Boolean = true,
    val imageThumbnails: Boolean = true,
    val clipPreviews: Boolean = true,
    val overwrite: Boolean = false,
    val sceneIds: List<String>? = null
)
