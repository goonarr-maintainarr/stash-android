package goonarr.stash.core.model

import kotlinx.serialization.Serializable

@Serializable
data class ScanOptions(
    // Newline separated for UI
    val paths: String = "",
    val rescan: Boolean = false,
    val scanGenerateCovers: Boolean = false,
    val scanGeneratePreviews: Boolean = false,
    val scanGenerateImagePreviews: Boolean = false,
    val scanGenerateSprites: Boolean = false,
    val scanGeneratePhashes: Boolean = false,
    val scanGenerateThumbnails: Boolean = false,
    val scanGenerateClipPreviews: Boolean = false
) {
    val pathsArray: List<String>
        get() = paths.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
}
