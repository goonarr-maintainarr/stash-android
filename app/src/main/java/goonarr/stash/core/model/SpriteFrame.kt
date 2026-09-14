package goonarr.stash.core.model

/**
 * Represents a single frame in a sprite sheet with timing and coordinates.
 */
data class SpriteFrame(
    val startTime: Double,
    val endTime: Double,
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)
