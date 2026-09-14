package goonarr.stash.core.util

import goonarr.stash.util.DateFormatters
import goonarr.stash.util.StringFormatters
import org.junit.Assert.assertEquals
import org.junit.Test

class FormatterTest {

    @Test
    fun `formatFileSize handles zero and negative`() {
        assertEquals("0 B", StringFormatters.formatFileSize(0))
        assertEquals("0 B", StringFormatters.formatFileSize(-100))
    }

    @Test
    fun `formatFileSize formats correctly`() {
        assertEquals("1.0 KB", StringFormatters.formatFileSize(1024))
        assertEquals("1.0 MB", StringFormatters.formatFileSize(1024 * 1024))
        assertEquals("1.5 GB", StringFormatters.formatFileSize((1.5 * 1024 * 1024 * 1024).toLong()))
    }

    @Test
    fun `formatEndpoint recognizes known endpoints`() {
        assertEquals("StashDB", StringFormatters.formatEndpoint("https://stashdb.org/graphql"))
        assertEquals("ThePornDB", StringFormatters.formatEndpoint("https://theporndb.net"))
        assertEquals("ThePornDB", StringFormatters.formatEndpoint("https://metadataapi.net/graphql"))
    }

    @Test
    fun `formatEndpoint extracts hostname for unknown`() {
        assertEquals("example.com", StringFormatters.formatEndpoint("https://example.com/api"))
        assertEquals("Unknown", StringFormatters.formatEndpoint("not-a-url"))
    }

    @Test
    fun `formatDateString handles various formats`() {
        // ISO Date Time
        assertEquals("Jan 1st, 2024", DateFormatters.formatDateString("2024-01-01T12:00:00Z"))
        // ISO Local Date
        assertEquals("Jan 15th, 2024", DateFormatters.formatDateString("2024-01-15"))
        // YYYY-MM
        assertEquals("Feb 1st, 2024", DateFormatters.formatDateString("2024-02"))
        // YYYY
        assertEquals("Jan 1st, 2023", DateFormatters.formatDateString("2023"))
    }

    @Test
    fun `formatDateString handles ordinal suffixes`() {
        assertEquals("Jan 1st, 2024", DateFormatters.formatDateString("2024-01-01"))
        assertEquals("Jan 2nd, 2024", DateFormatters.formatDateString("2024-01-02"))
        assertEquals("Jan 3rd, 2024", DateFormatters.formatDateString("2024-01-03"))
        assertEquals("Jan 4th, 2024", DateFormatters.formatDateString("2024-01-04"))
        assertEquals("Jan 11th, 2024", DateFormatters.formatDateString("2024-01-11"))
        assertEquals("Jan 21st, 2024", DateFormatters.formatDateString("2024-01-21"))
    }

    @Test
    fun `formatTimestamp formats correctly`() {
        assertEquals("Jan 15, 2024 at 12:00 PM", DateFormatters.formatTimestamp("2024-01-15T12:00:00"))
    }

    @Test
    fun `calculateAge handles birth and death dates`() {
        val birth = "1990-01-01"
        val death = "2020-01-01"
        assertEquals(30, DateFormatters.calculateAge(birth, death))
    }

    @Test
    fun `formatDuration formats correctly`() {
        assertEquals("41:00", DateFormatters.formatDuration(2460.0))
        assertEquals("00:30", DateFormatters.formatDuration(30.0))
        assertEquals("1:00:00", DateFormatters.formatDuration(3600.0))
    }
}
