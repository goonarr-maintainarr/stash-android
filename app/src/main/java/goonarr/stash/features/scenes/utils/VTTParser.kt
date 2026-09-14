package goonarr.stash.features.scenes.utils

import goonarr.stash.core.model.SpriteFrame
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VTTParser @Inject constructor() {

    fun parse(vttContent: String): List<SpriteFrame> {
        val frames = mutableListOf<SpriteFrame>()
        val lines = vttContent.lines()

        var currentStartTime: Double? = null
        var currentEndTime: Double? = null

        for (line in lines) {
            val trimmedLine = line.trim()

            // Skip empty, WEBVTT header, or comments
            if (trimmedLine.isEmpty() || trimmedLine.startsWith("WEBVTT") || trimmedLine.startsWith("NOTE")) {
                continue
            }

            // Parse timestamp line: "00:00:10.000 --> 00:00:20.000"
            if (trimmedLine.contains("-->")) {
                val times = trimmedLine.split("-->").map { it.trim() }
                if (times.size == 2) {
                    currentStartTime = parseTimestamp(times[0])
                    currentEndTime = parseTimestamp(times[1])
                }
            } else if (trimmedLine.contains("#xywh=") && currentStartTime != null && currentEndTime != null) {
                // Parse coordinate line: "/scene/123_sprite.jpg#xywh=160,0,160,90"
                val coords = parseCoordinates(trimmedLine)
                if (coords != null) {
                    frames.add(
                        SpriteFrame(
                            startTime = currentStartTime!!,
                            endTime = currentEndTime!!,
                            x = coords.x,
                            y = coords.y,
                            width = coords.width,
                            height = coords.height
                        )
                    )
                }
                currentStartTime = null
                currentEndTime = null
            }
        }

        return frames
    }

    private fun parseTimestamp(timestamp: String): Double? {
        // Format: HH:MM:SS.mmm or MM:SS.mmm
        val parts = timestamp.split(":")

        return try {
            if (parts.size == 3) {
                // HH:MM:SS.mmm
                val hours = parts[0].toDouble()
                val minutes = parts[1].toDouble()
                val seconds = parts[2].toDouble()
                hours * 3600 + minutes * 60 + seconds
            } else if (parts.size == 2) {
                // MM:SS.mmm
                val minutes = parts[0].toDouble()
                val seconds = parts[1].toDouble()
                minutes * 60 + seconds
            } else {
                null
            }
        } catch (e: NumberFormatException) {
            null
        }
    }

    private fun parseCoordinates(line: String): Coordinates? {
        // Extract substring after "#xywh="
        val substring = line.substringAfter("#xywh=", "")
        if (substring.isEmpty()) return null

        val values = substring.split(",").mapNotNull { it.toIntOrNull() }
        if (values.size != 4) return null

        return Coordinates(values[0], values[1], values[2], values[3])
    }

    private data class Coordinates(val x: Int, val y: Int, val width: Int, val height: Int)
}
