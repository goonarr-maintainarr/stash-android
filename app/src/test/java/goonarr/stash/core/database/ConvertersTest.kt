package goonarr.stash.core.database

import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.SceneMarker
import goonarr.stash.util.Converters
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `sceneMarkerList serialization functions correctly`() {
        // Given
        val markers = listOf(
            SceneMarker(id = "1", title = "Marker 1", seconds = 10.0, stream = "http://stream1"),
            SceneMarker(id = "2", title = "Marker 2", seconds = 20.0)
        )

        // When
        val json = converters.fromSceneMarkerList(markers)
        val result = converters.toSceneMarkerList(json)

        // Then
        assertEquals(markers, result)
    }

    @Test
    fun `toSceneMarkerList handles empty string`() {
        val result = converters.toSceneMarkerList("")
        assertEquals(emptyList<SceneMarker>(), result)
    }

    @Test
    fun `toSceneMarkerList handles invalid json`() {
        val result = converters.toSceneMarkerList("{invalid json}")
        assertEquals(emptyList<SceneMarker>(), result)
    }

    @Test
    fun `performerList serialization preserves IDs and names`() {
        // Given
        val performers = listOf(
            Performer(id = "perf-123", name = "Performer One"),
            Performer(id = "perf-456", name = "Performer Two"),
            Performer(id = "perf-789", name = "Performer Three")
        )

        // When
        val json = converters.fromPerformerList(performers)
        val result = converters.toPerformerList(json)

        // Then
        assertEquals(performers.size, result.size)
        assertEquals("perf-123", result[0].id)
        assertEquals("Performer One", result[0].name)
        assertEquals("perf-456", result[1].id)
        assertEquals("Performer Two", result[1].name)
        assertEquals("perf-789", result[2].id)
        assertEquals("Performer Three", result[2].name)
    }

    @Test
    fun `performerList serialization handles empty list`() {
        // Given
        val performers = emptyList<Performer>()

        // When
        val json = converters.fromPerformerList(performers)
        val result = converters.toPerformerList(json)

        // Then
        assertEquals(emptyList<Performer>(), result)
    }

    @Test
    fun `performerList serialization handles null`() {
        // When
        val json = converters.fromPerformerList(null)
        val result = converters.toPerformerList(json)

        // Then
        assertEquals(emptyList<Performer>(), result)
    }

    @Test
    fun `toPerformerList handles empty string`() {
        val result = converters.toPerformerList("")
        assertEquals(emptyList<Performer>(), result)
    }

    @Test
    fun `toPerformerList handles invalid json`() {
        val result = converters.toPerformerList("{invalid json}")
        assertEquals(emptyList<Performer>(), result)
    }

    @Test
    fun `performerList preserves all performer fields`() {
        // Given - performer with additional fields
        val performers = listOf(
            Performer(
                id = "perf-full",
                name = "Full Performer",
                country = "USA",
                sceneCount = 42,
                oCounter = 10,
                birthdate = "1990-01-15"
            )
        )

        // When
        val json = converters.fromPerformerList(performers)
        val result = converters.toPerformerList(json)

        // Then
        assertEquals(1, result.size)
        val resultPerformer = result[0]
        assertEquals("perf-full", resultPerformer.id)
        assertEquals("Full Performer", resultPerformer.name)
        assertEquals("USA", resultPerformer.country)
        assertEquals(42, resultPerformer.sceneCount)
        assertEquals(10, resultPerformer.oCounter)
        assertEquals("1990-01-15", resultPerformer.birthdate)
    }
}
