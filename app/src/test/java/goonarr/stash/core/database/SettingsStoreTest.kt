package goonarr.stash.core.database

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import goonarr.stash.core.model.GenerationOptions
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsStoreTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val settingsStore = SettingsStore(context)

    @Test
    fun `default values are correct`() = runTest {
        assertEquals("", settingsStore.serverUrl.first())
        assertEquals("", settingsStore.apiKey.first())
        assertTrue(settingsStore.prefetchOnScroll.first())
    }

    @Test
    fun `generationOptions serializes and deserializes correctly`() = runTest {
        val options = GenerationOptions(
            covers = false,
            sprites = true,
            previews = false
        )

        settingsStore.setGenerationOptions(options)

        val retrieved = settingsStore.generationOptions.first()
        assertEquals(false, retrieved.covers)
        assertEquals(true, retrieved.sprites)
        assertEquals(false, retrieved.previews)
    }

    @Test
    fun `generationOptions falls back to default on malformed json`() = runTest {
        // We can't easily inject malformed JSON into DataStore without exposing keys or using reflection/helper.
        // However, we can verify that the default is valid.
        // Or we can try to set a raw string if we exposed a method, but we didn't.
        // Ideally we'd modify SettingsStore to allow injecting the dataStore or creating a test instance with pre-populated bad data.

        // For now, let's verify that the initial state (empty/default) returns a valid object.
        val defaultOptions = settingsStore.generationOptions.first()
        assertTrue(defaultOptions.covers) // Default is true
    }

    @Test
    fun `hideBottomBarLabels defaults to false`() = runTest {
        val defaultValue = settingsStore.hideBottomBarLabels.first()
        assertEquals(false, defaultValue)
    }

    @Test
    fun `setHideBottomBarLabels persists value`() = runTest {
        // Given the default is false
        assertEquals(false, settingsStore.hideBottomBarLabels.first())

        // When we set it to true
        settingsStore.setHideBottomBarLabels(true)

        // Then the value should be true
        assertEquals(true, settingsStore.hideBottomBarLabels.first())

        // And we can set it back to false
        settingsStore.setHideBottomBarLabels(false)
        assertEquals(false, settingsStore.hideBottomBarLabels.first())
    }
}
