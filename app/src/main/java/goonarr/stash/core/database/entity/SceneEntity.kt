package goonarr.stash.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.Scene
import goonarr.stash.core.model.SceneFile
import goonarr.stash.core.model.SceneMarker
import goonarr.stash.core.model.ScenePaths
import goonarr.stash.core.model.Studio
import goonarr.stash.core.model.Tag

@Entity(tableName = "scenes")
data class SceneEntity(
    @PrimaryKey val id: String,
    val title: String?,
    val details: String?,
    val date: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val lastPlayedAt: String?,
    val rating100: Int?,
    val oCounter: Int?,
    val resumeTime: Double?,
    val director: String?,
    val code: String?,
    val url: String?,
    val duration: Double?,

    // Storing critical display paths as flat strings
    val screenshotPath: String?,
    val streamPath: String?,
    val spritePath: String?,
    val vttPath: String?,
    val previewPath: String?,

    // Simplified fields for list view display
    val studio: Studio?,
    val performers: List<Performer>?,

    // Store lists as JSON strings via Converters
    val sceneMarkers: List<SceneMarker>?,
    val tags: List<Tag>?,
    val oHistory: List<String>?,
    val playHistory: List<String>?,
    val playDuration: Double?,
    val stashIds: List<goonarr.stash.core.model.StashID>?,
    val urls: List<String>?,
    val videoCodec: String?,
    val audioCodec: String?,
    val filePath: String?,
    val fileSize: Long?,
    val sortOrder: Int = 0
)

fun SceneEntity.toDomain(): Scene {
    return Scene(
        id = id,
        title = title,
        details = details,
        date = date,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastPlayedAt = lastPlayedAt,
        rating100 = rating100,
        oCounter = oCounter,
        resumeTime = resumeTime,
        director = director,
        code = code,
        url = url,
        urls = urls,
        paths = ScenePaths(
            screenshot = screenshotPath,
            stream = streamPath,
            sprite = spritePath,
            vtt = vttPath,
            preview = previewPath
        ),
        files = duration?.let {
            listOf(
                SceneFile(
                    duration = it,
                    videoCodec = videoCodec,
                    audioCodec = audioCodec,
                    path = filePath,
                    size = fileSize
                )
            )
        },
        studio = studio,
        performers = performers,
        tags = tags,
        sceneMarkers = sceneMarkers,
        oHistory = oHistory,
        playHistory = playHistory,
        playDuration = playDuration,
        stashIds = stashIds,
        sortOrder = sortOrder
    )
}

fun Scene.toEntity(sortOrder: Int = 0): SceneEntity {
    return SceneEntity(
        id = id,
        title = title,
        details = details,
        date = date,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastPlayedAt = lastPlayedAt,
        rating100 = rating100,
        oCounter = oCounter,
        resumeTime = resumeTime,
        director = director,
        code = code,
        url = url,
        urls = urls,
        duration = files?.firstOrNull()?.duration,
        videoCodec = files?.firstOrNull()?.videoCodec,
        audioCodec = files?.firstOrNull()?.audioCodec,
        filePath = files?.firstOrNull()?.path,
        fileSize = files?.firstOrNull()?.size,
        screenshotPath = paths?.screenshot,
        streamPath = paths?.stream,
        spritePath = paths?.sprite,
        vttPath = paths?.vtt,
        previewPath = paths?.preview,
        studio = studio,
        performers = performers,
        tags = tags,
        sceneMarkers = sceneMarkers,
        oHistory = oHistory,
        playHistory = playHistory,
        playDuration = playDuration,
        stashIds = stashIds,
        sortOrder = sortOrder
    )
}
