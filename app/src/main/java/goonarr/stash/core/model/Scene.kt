package goonarr.stash.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Scene(
    val id: String,
    val title: String? = null,
    val details: String? = null,
    val date: String? = null,
    @kotlinx.serialization.SerialName("created_at")
    val createdAt: String? = null,
    @kotlinx.serialization.SerialName("updated_at")
    val updatedAt: String? = null,
    @kotlinx.serialization.SerialName("last_played_at")
    val lastPlayedAt: String? = null,
    @kotlinx.serialization.SerialName("rating100")
    val rating100: Int? = null,
    @kotlinx.serialization.SerialName("o_counter")
    val oCounter: Int? = null,
    @kotlinx.serialization.SerialName("resume_time")
    val resumeTime: Double? = null,
    val paths: ScenePaths? = null,
    val files: List<SceneFile>? = null,
    val performers: List<Performer>? = null,
    val tags: List<Tag>? = null,
    val studio: Studio? = null,
    @kotlinx.serialization.SerialName("stash_ids")
    val stashIds: List<StashID>? = null,
    @kotlinx.serialization.SerialName("scene_markers")
    val sceneMarkers: List<SceneMarker>? = null,
    @kotlinx.serialization.SerialName("o_history")
    val oHistory: List<String>? = null,
    @kotlinx.serialization.SerialName("play_history")
    val playHistory: List<String>? = null,
    @kotlinx.serialization.SerialName("play_duration")
    val playDuration: Double? = null,
    val director: String? = null,
    val code: String? = null,
    val url: String? = null,
    val urls: List<String>? = null,
    @kotlinx.serialization.SerialName("sort_order")
    val sortOrder: Int = 0
)

@Serializable
data class ScenePaths(
    val screenshot: String? = null,
    val preview: String? = null,
    val stream: String? = null,
    val sprite: String? = null,
    val vtt: String? = null
)

@Serializable
data class SceneFile(
    val path: String? = null,
    val size: Long? = null,
    val duration: Double? = null,
    @kotlinx.serialization.SerialName("video_codec")
    val videoCodec: String? = null,
    @kotlinx.serialization.SerialName("audio_codec")
    val audioCodec: String? = null,
    val width: Int? = null,
    val height: Int? = null
)
