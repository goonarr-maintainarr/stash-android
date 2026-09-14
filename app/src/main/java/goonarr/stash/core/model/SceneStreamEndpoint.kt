package goonarr.stash.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SceneStreamEndpoint(
    val url: String,
    val label: String? = null,
    @SerialName("mime_type") val mimeType: String? = null
) {
    val id: String get() = url

    val displayLabel: String
        get() = label ?: mimeType ?: "Stream"
}
