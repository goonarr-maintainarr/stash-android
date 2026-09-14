package goonarr.stash.util

import java.net.URI
import java.util.Locale

/**
 * Utility object for string formatting operations.
 */
object StringFormatters {

    /**
     * Formats a byte size to human-readable format (B, KB, MB, GB, TB).
     *
     * @param bytes The size in bytes.
     * @return Formatted string with appropriate unit (e.g., "1.5 GB").
     */
    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format(Locale.US, "%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }

    /**
     * Formats a byte size to human-readable format (overload for Double).
     */
    fun formatFileSize(bytes: Double): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes) / Math.log10(1024.0)).toInt()
        return String.format(Locale.US, "%.1f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }

    /**
     * Formats an endpoint URL to a friendly name.
     *
     * Recognizes common metadata sources like StashDB and ThePornDB,
     * and extracts hostname for unknown endpoints.
     *
     * @param endpoint The full endpoint URL.
     * @return A human-readable name for the endpoint.
     */
    @Suppress("SpellCheckingInspection")
    fun formatEndpoint(endpoint: String): String {
        return when {
            endpoint.contains("stashdb.org") -> "StashDB"
            endpoint.contains("theporndb.net") || endpoint.contains("metadataapi.net") -> "ThePornDB"
            else -> {
                try {
                    URI(endpoint).host ?: "Unknown"
                } catch (_: Exception) {
                    "Unknown"
                }
            }
        }
    }

    /**
     * Formats seconds into a text description (e.g. "2d 4h 30m").
     */
    fun formatDurationText(seconds: Double): String {
        val totalSeconds = seconds.toLong()
        val days = totalSeconds / 86400
        val hours = (totalSeconds % 86400) / 3600
        val minutes = (totalSeconds % 3600) / 60

        val parts = mutableListOf<String>()
        if (days > 0) parts.add("${days}d")
        if (hours > 0) parts.add("${hours}h")
        if (minutes > 0) parts.add("${minutes}m")

        if (parts.isEmpty()) return "0m"
        return parts.joinToString(" ")
    }
}
