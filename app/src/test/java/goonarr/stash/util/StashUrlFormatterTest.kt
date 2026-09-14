package goonarr.stash.util

import goonarr.stash.core.database.SettingsStore
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class StashUrlFormatterTest {

    private lateinit var settingsStore: SettingsStore
    private lateinit var formatter: StashUrlFormatter

    @Before
    fun setup() {
        settingsStore = mockk()
        formatter = StashUrlFormatter(settingsStore)
    }

    @Test
    fun `resolveUrl appends base url and api key to relative path`() = runTest {
        // Given
        every { settingsStore.serverUrl } returns flowOf("https://stash.example.com:9999")
        every { settingsStore.apiKey } returns flowOf("secret-key")

        // When
        val result = formatter.resolveUrl("/stream/123")

        // Then
        assertEquals("https://stash.example.com:9999/stream/123?apikey=secret-key", result)
    }

    @Test
    fun `resolveUrl appends api key to absolute url`() = runTest {
        // Given
        every {
            settingsStore.serverUrl
        } returns flowOf("https://stash.example.com:9999")
        every { settingsStore.apiKey } returns flowOf("secret-key")

        // When
        val result = formatter.resolveUrl("http://external-server.com/video.mp4")

        // Then
        assertEquals("http://external-server.com/video.mp4?apikey=secret-key", result)
    }

    @Test
    fun `resolveUrl handles existing query params correctly`() = runTest {
        // Given
        every { settingsStore.serverUrl } returns flowOf("http://server")
        every { settingsStore.apiKey } returns flowOf("secret-key")

        // When
        val result = formatter.resolveUrl("/stream?format=mp4")

        // Then
        assertEquals("http://server/stream?format=mp4&apikey=secret-key", result)
    }

    @Test
    fun `resolveUrl does not append api key if empty`() = runTest {
        // Given
        every { settingsStore.serverUrl } returns flowOf("http://server")
        every { settingsStore.apiKey } returns flowOf("")

        // When
        val result = formatter.resolveUrl("/stream")

        // Then
        assertEquals("http://server/stream", result)
    }

    @Test
    fun `resolveUrl handles null path`() = runTest {
        val result = formatter.resolveUrl(null)
        assertEquals(null, result)
    }
}
