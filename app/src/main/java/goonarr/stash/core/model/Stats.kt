package goonarr.stash.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Stats(
    @SerialName("scene_count") val sceneCount: Int,
    @SerialName("scenes_size") val scenesSize: Double,
    @SerialName("scenes_duration") val scenesDuration: Double,
    @SerialName("image_count") val imageCount: Int,
    @SerialName("images_size") val imagesSize: Double,
    @SerialName("gallery_count") val galleryCount: Int,
    @SerialName("performer_count") val performerCount: Int,
    @SerialName("studio_count") val studioCount: Int,
    @SerialName("group_count") val groupCount: Int,
    @SerialName("movie_count") val movieCount: Int,
    @SerialName("tag_count") val tagCount: Int,
    @SerialName("total_o_count") val totalOCount: Int,
    @SerialName("total_play_duration") val totalPlayDuration: Double,
    @SerialName("total_play_count") val totalPlayCount: Int,
    @SerialName("scenes_played") val scenesPlayed: Int
)
