package goonarr.stash.core.network.dto

import goonarr.stash.core.model.Scene
import goonarr.stash.core.model.SceneFile
import goonarr.stash.core.model.SceneMarker
import goonarr.stash.core.model.ScenePaths
import goonarr.stash.core.model.SimpleStudio
import goonarr.stash.core.model.Studio
import goonarr.stash.core.model.Tag
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SceneDto(
    val id: String,
    val title: String? = null,
    val details: String? = null,
    val date: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val rating100: Int? = null,
    @SerialName("o_counter") val oCounter: Int? = null,
    @SerialName("last_played_at") val lastPlayedAt: String? = null,
    @SerialName("resume_time") val resumeTime: Double? = null,
    val paths: ScenePathsDto? = null,
    val files: List<SceneFileDto>? = null,
    val performers: List<PerformerDto>? = null,
    val tags: List<TagDto>? = null,
    val studio: StudioDto? = null,
    @SerialName("stash_ids") val stashIds: List<StashIDDto>? = null,
    @SerialName("scene_markers") val sceneMarkers: List<SceneMarkerDto>? = null,
    @SerialName("o_history") val oHistory: List<String>? = null,
    @SerialName("play_history") val playHistory: List<String>? = null,
    @SerialName("play_duration") val playDuration: Double? = null,
    val director: String? = null,
    val code: String? = null,
    val urls: List<String>? = null,
    val url: String? = null
)

@Serializable
data class ScenePathsDto(
    val screenshot: String? = null,
    val preview: String? = null,
    val stream: String? = null,
    val sprite: String? = null,
    val vtt: String? = null
)

@Serializable
data class SceneFileDto(
    val path: String? = null,
    val size: Long? = null,
    val duration: Double? = null,
    @SerialName("video_codec") val videoCodec: String? = null,
    @SerialName("audio_codec") val audioCodec: String? = null,
    val width: Int? = null,
    val height: Int? = null
)

@Serializable
data class TagDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val favorite: Boolean? = null,
    @SerialName("image_path") val imagePath: String? = null,
    val aliases: List<String>? = null,
    @SerialName("ignore_auto_tag") val ignoreAutoTag: Boolean? = null,
    @SerialName("scene_count") val sceneCount: Int? = null,
    @SerialName("scene_marker_count") val sceneMarkerCount: Int? = null,
    @SerialName("image_count") val imageCount: Int? = null,
    @SerialName("gallery_count") val galleryCount: Int? = null,
    @SerialName("performer_count") val performerCount: Int? = null,
    @SerialName("parent_count") val parentCount: Int? = null,
    @SerialName("child_count") val childCount: Int? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("stash_ids") val stashIds: List<StashIDDto>? = null,
    val parents: List<TagDto>? = null,
    val children: List<TagDto>? = null
)

@Serializable
data class StudioDto(
    val id: String,
    val name: String,
    val details: String? = null,
    val url: String? = null,
    @SerialName("image_path") val imagePath: String? = null,
    val favorite: Boolean? = null,
    @SerialName("scene_count") val sceneCount: Int? = null,
    @SerialName("image_count") val imageCount: Int? = null,
    @SerialName("gallery_count") val galleryCount: Int? = null,
    @SerialName("performer_count") val performerCount: Int? = null,
    @SerialName("group_count") val groupCount: Int? = null,
    @SerialName("o_counter") val oCounter: Int? = null,
    @SerialName("parent_studio") val parentStudio: SimpleStudioDto? = null,
    @SerialName("child_studios") val childStudios: List<SimpleStudioDto>? = null,
    val aliases: List<String>? = null,
    val tags: List<TagDto>? = null,
    @SerialName("ignore_auto_tag") val ignoreAutoTag: Boolean? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val urls: List<String>? = null,
    val rating100: Int? = null,
    @SerialName("stash_ids") val stashIds: List<StashIDDto>? = null
)

@Serializable
data class SimpleStudioDto(
    val id: String,
    val name: String,
    @SerialName("image_path") val imagePath: String? = null
)

@Serializable
data class SceneMarkerDto(
    val id: String,
    val title: String,
    val seconds: Double,
    @SerialName("end_seconds") val endSeconds: Double? = null,
    @SerialName("primary_tag") val primaryTag: TagDto? = null,
    val tags: List<TagDto>? = null,
    val stream: String? = null,
    val preview: String? = null,
    @SerialName("screenshot_path") val screenshotPath: String? = null,
    @SerialName("vtt_path") val vttPath: String? = null,
    val scene: SceneDto? = null
)

fun SceneDto.toDomain(): Scene = Scene(
    id = id,
    title = title,
    details = details,
    date = date,
    createdAt = createdAt,
    updatedAt = updatedAt,
    lastPlayedAt = lastPlayedAt ?: playHistory?.maxOrNull(),
    rating100 = rating100,
    oCounter = oCounter,
    resumeTime = resumeTime,
    paths = paths?.toDomain(),
    files = files?.map { it.toDomain() },
    performers = performers?.map { it.toDomain() },
    tags = tags?.map { it.toDomain() },
    studio = studio?.toDomain(),
    stashIds = stashIds?.map { it.toDomain() },
    sceneMarkers = sceneMarkers?.map { it.toDomain() },
    oHistory = oHistory,
    playHistory = playHistory,
    playDuration = playDuration,
    director = director,
    code = code,
    url = url,
    urls = urls
)

fun ScenePathsDto.toDomain(): ScenePaths = ScenePaths(
    screenshot = screenshot,
    preview = preview,
    stream = stream,
    sprite = sprite,
    vtt = vtt
)

fun SceneFileDto.toDomain(): SceneFile = SceneFile(
    path = path,
    size = size,
    duration = duration,
    videoCodec = videoCodec,
    audioCodec = audioCodec,
    width = width,
    height = height
)

fun TagDto.toDomain(): Tag = Tag(
    id = id,
    name = name,
    description = description,
    favorite = favorite,
    imagePath = imagePath,
    aliases = aliases,
    ignoreAutoTag = ignoreAutoTag,
    sceneCount = sceneCount,
    sceneMarkerCount = sceneMarkerCount,
    imageCount = imageCount,
    galleryCount = galleryCount,
    performerCount = performerCount,
    parentCount = parentCount,
    childCount = childCount,
    createdAt = createdAt,
    updatedAt = updatedAt,
    stashIds = stashIds?.map { it.toDomain() },
    parents = parents?.map { it.toDomain() },
    children = children?.map { it.toDomain() }
)

fun StudioDto.toDomain(): Studio = Studio(
    id = id,
    name = name,
    details = details,
    url = url,
    imagePath = imagePath,
    favorite = favorite,
    sceneCount = sceneCount,
    imageCount = imageCount,
    galleryCount = galleryCount,
    performerCount = performerCount,
    groupCount = groupCount,
    oCounter = oCounter,
    parentStudio = parentStudio?.toDomain(),
    childStudios = childStudios?.map { it.toDomain() },
    aliases = aliases,
    tags = tags?.map { it.toDomain() },
    ignoreAutoTag = ignoreAutoTag,
    createdAt = createdAt,
    updatedAt = updatedAt,
    urls = urls,
    rating100 = rating100,
    stashIds = stashIds?.map { it.toDomain() }
)

fun SimpleStudioDto.toDomain(): SimpleStudio = SimpleStudio(
    id = id,
    name = name,
    imagePath = imagePath
)

fun SceneMarkerDto.toDomain(): SceneMarker = SceneMarker(
    id = id,
    title = title,
    seconds = seconds,
    endSeconds = endSeconds,
    primaryTag = primaryTag?.toDomain(),
    tags = tags?.map { it.toDomain() },
    stream = stream,
    preview = preview,
    scene = scene?.toDomain()
)
