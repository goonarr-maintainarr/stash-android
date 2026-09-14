package goonarr.stash.util

import org.junit.Assert.assertEquals
import org.junit.Test

class StringFormattersTest {

    @Test
    fun formatFileSize_long() {
        assertEquals("0 B", StringFormatters.formatFileSize(0L))
        assertEquals("100.0 B", StringFormatters.formatFileSize(100L))
        assertEquals("1.0 KB", StringFormatters.formatFileSize(1024L))
        assertEquals("1.5 KB", StringFormatters.formatFileSize(1536L))
        assertEquals("1.0 MB", StringFormatters.formatFileSize(1024L * 1024))
        assertEquals("1.0 GB", StringFormatters.formatFileSize(1024L * 1024 * 1024))
    }

    @Test
    fun formatFileSize_double() {
        assertEquals("0 B", StringFormatters.formatFileSize(0.0))
        assertEquals("100.0 B", StringFormatters.formatFileSize(100.0))
        assertEquals("1.0 KB", StringFormatters.formatFileSize(1024.0))
        assertEquals("1.5 KB", StringFormatters.formatFileSize(1536.0))
        assertEquals("1.0 MB", StringFormatters.formatFileSize(1024.0 * 1024))
        assertEquals("1.0 GB", StringFormatters.formatFileSize(1024.0 * 1024 * 1024))
        // Test scientific notation implicitly via large double
        assertEquals("1.0 TB", StringFormatters.formatFileSize(1024.0 * 1024 * 1024 * 1024))
    }

    @Test
    fun formatDurationText() {
        assertEquals("0m", StringFormatters.formatDurationText(0.0))
        assertEquals("0m", StringFormatters.formatDurationText(59.0)) // Less than a minute
        assertEquals("1m", StringFormatters.formatDurationText(60.0))
        assertEquals("1h", StringFormatters.formatDurationText(3600.0))
        assertEquals("1h 30m", StringFormatters.formatDurationText(3600.0 + 1800.0))
        assertEquals("1d", StringFormatters.formatDurationText(86400.0))
        assertEquals("1d 1h 1m", StringFormatters.formatDurationText(86400.0 + 3600.0 + 60.0))

        // Test implicit cast and precision
        assertEquals("1h", StringFormatters.formatDurationText(3600.99))
    }
}
