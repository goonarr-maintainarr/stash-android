package goonarr.stash.core.util

import goonarr.stash.util.StringFormatters
import org.junit.Assert.assertEquals
import org.junit.Test

class StringFormattersTest {

    @Test
    fun `formatFileSize formats bytes correctly`() {
        assertEquals("0 B", StringFormatters.formatFileSize(0))
        assertEquals("500.0 B", StringFormatters.formatFileSize(500))
        assertEquals("1.0 KB", StringFormatters.formatFileSize(1024))
        assertEquals("1.5 KB", StringFormatters.formatFileSize(1536))
        assertEquals("1.0 MB", StringFormatters.formatFileSize(1024 * 1024))
        assertEquals("1.5 GB", StringFormatters.formatFileSize((1.5 * 1024 * 1024 * 1024).toLong()))
        assertEquals("2.0 TB", StringFormatters.formatFileSize(2L * 1024 * 1024 * 1024 * 1024))
    }

    @Test
    fun `formatFileSize handles negative bytes`() {
        assertEquals("0 B", StringFormatters.formatFileSize(-100))
    }

    @Test
    fun `formatEndpoint recognizes StashDB`() {
        assertEquals("StashDB", StringFormatters.formatEndpoint("https://stashdb.org/graphql"))
        assertEquals("StashDB", StringFormatters.formatEndpoint("http://stashdb.org/api"))
    }

    @Test
    fun `formatEndpoint recognizes ThePornDB`() {
        assertEquals("ThePornDB", StringFormatters.formatEndpoint("https://theporndb.net/api"))
        assertEquals("ThePornDB", StringFormatters.formatEndpoint("https://metadataapi.net/graphql"))
    }

    @Test
    fun `formatEndpoint extracts hostname for unknown endpoints`() {
        assertEquals("example.com", StringFormatters.formatEndpoint("https://example.com/graphql"))
        assertEquals("api.custom.org", StringFormatters.formatEndpoint("http://api.custom.org/v1"))
    }

    @Test
    fun `formatEndpoint handles invalid URIs gracefully`() {
        assertEquals("Unknown", StringFormatters.formatEndpoint("not-a-valid-uri"))
        assertEquals("Unknown", StringFormatters.formatEndpoint(""))
    }
}
