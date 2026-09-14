package goonarr.stash.core.network.dto

import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkDtoMappingTest {

    @Test
    fun `PerformerDto maps to Performer correctly`() {
        val dto = PerformerDto(
            id = "1",
            name = "Test Performer",
            imagePath = "path/to/image",
            sceneCount = 10,
            oCounter = 5,
            details = "Some details",
            stashIds = listOf(StashIDDto(endpoint = "e1", stashId = "s1")),
            tags = listOf(PerformerTagDto(id = "t1", name = "tag1"))
        )

        val domain = dto.toDomain()

        assertEquals("1", domain.id)
        assertEquals("Test Performer", domain.name)
        assertEquals("path/to/image", domain.imagePath)
        assertEquals(10, domain.sceneCount)
        assertEquals(5, domain.oCounter)
        assertEquals("Some details", domain.details)
        assertEquals(1, domain.stashIds?.size)
        assertEquals("e1", domain.stashIds?.first()?.endpoint)
        assertEquals(1, domain.tags?.size)
        assertEquals("t1", domain.tags?.first()?.id)
    }

    @Test
    fun `SceneDto maps to Scene correctly`() {
        val dto = SceneDto(
            id = "101",
            title = "Test Scene",
            details = "Details",
            date = "2024-01-01",
            files = listOf(SceneFileDto(duration = 120.5)),
            paths = ScenePathsDto(screenshot = "ss", stream = "st"),
            studio = StudioDto(id = "st1", name = "Studio 1"),
            performers = listOf(PerformerDto(id = "p1", name = "P1")),
            sceneMarkers = listOf(SceneMarkerDto(id = "m1", title = "M1", seconds = 10.0)),
            tags = listOf(TagDto(id = "tg1", name = "Tag 1"))
        )

        val domain = dto.toDomain()

        assertEquals("101", domain.id)
        assertEquals("Test Scene", domain.title)
        assertEquals("2024-01-01", domain.date)
        assertEquals(120.5, domain.files?.first()?.duration ?: 0.0, 0.01)
        assertEquals("ss", domain.paths?.screenshot)
        assertEquals("Studio 1", domain.studio?.name)
        assertEquals("P1", domain.performers?.first()?.name)
        assertEquals(1, domain.sceneMarkers?.size)
        assertEquals("M1", domain.sceneMarkers?.first()?.title)
        assertEquals(1, domain.tags?.size)
        assertEquals("Tag 1", domain.tags?.first()?.name)
    }

    @Test
    fun `SceneMarkerDto handles null stream and preview`() {
        // Test that toDomain handles null stream/preview if they are returned as null from API
        val dto = SceneMarkerDto(
            id = "m1",
            title = "M1",
            seconds = 10.0,
            stream = null,
            preview = null
        )

        val domain = dto.toDomain()
        assertEquals("m1", domain.id)
        assertEquals(10.0, domain.seconds, 0.01)
    }
}
