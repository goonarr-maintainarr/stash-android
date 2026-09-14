package goonarr.stash.core.database.entity

import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.Scene
import goonarr.stash.core.model.SceneFile
import goonarr.stash.core.model.SceneMarker
import goonarr.stash.core.model.ScenePaths
import goonarr.stash.core.model.Studio
import goonarr.stash.core.model.Tag
import org.junit.Assert.assertEquals
import org.junit.Test

class MapperTest {

    @Test
    fun `Scene mapping is consistent`() {
        val scene = Scene(
            id = "1",
            title = "Test Scene",
            files = listOf(SceneFile(duration = 3600.0)),
            paths = ScenePaths(screenshot = "thumb.jpg", stream = "video.mp4"),
            oHistory = listOf("2024-01-01"),
            sceneMarkers = listOf(SceneMarker("m1", "Marker 1", 10.0)),
            rating100 = 80,
            oCounter = 5
        )

        // Domain -> Entity
        val entity = scene.toEntity()
        assertEquals("1", entity.id)
        assertEquals("thumb.jpg", entity.screenshotPath)
        assertEquals(3600.0, entity.duration!!, 0.0)
        assertEquals(1, entity.sceneMarkers?.size)
        assertEquals(80, entity.rating100)
        assertEquals(5, entity.oCounter)
        assertEquals("m1", entity.sceneMarkers?.get(0)?.id)

        // Entity -> Domain
        val domain = entity.toDomain()
        assertEquals("1", domain.id)
        assertEquals("thumb.jpg", domain.paths?.screenshot)
        assertEquals(3600.0, domain.files?.firstOrNull()?.duration!!, 0.0)
        assertEquals(80, domain.rating100)
        assertEquals(5, domain.oCounter)
        assertEquals(1, domain.sceneMarkers?.size)
        assertEquals("m1", domain.sceneMarkers?.get(0)?.id)
    }

    @Test
    fun `Scene performers preserve IDs through mapping`() {
        val performers = listOf(
            Performer(id = "perf-123", name = "Performer One"),
            Performer(id = "perf-456", name = "Performer Two"),
            Performer(id = "perf-789", name = "Performer Three")
        )
        val scene = Scene(
            id = "scene-1",
            title = "Scene with Performers",
            performers = performers
        )

        // Domain -> Entity
        val entity = scene.toEntity()
        assertEquals(3, entity.performers?.size)
        assertEquals("perf-123", entity.performers?.get(0)?.id)
        assertEquals("Performer One", entity.performers?.get(0)?.name)
        assertEquals("perf-456", entity.performers?.get(1)?.id)
        assertEquals("perf-789", entity.performers?.get(2)?.id)

        // Entity -> Domain
        val domain = entity.toDomain()
        assertEquals(3, domain.performers?.size)
        assertEquals("perf-123", domain.performers?.get(0)?.id)
        assertEquals("Performer One", domain.performers?.get(0)?.name)
        assertEquals("perf-456", domain.performers?.get(1)?.id)
        assertEquals("perf-789", domain.performers?.get(2)?.id)
    }

    @Test
    fun `Performer mapping is consistent`() {
        val performer = Performer(
            id = "1",
            name = "Test Performer",
            sceneCount = 5,
            aliasList = listOf("Alias 1"),
            oCounter = 10
        )

        // Domain -> Entity
        val entity = performer.toEntity()
        assertEquals("1", entity.id)
        assertEquals("Test Performer", entity.name)
        assertEquals(listOf("Alias 1"), entity.aliasList)
        assertEquals(10, entity.oCounter)

        // Entity -> Domain
        val domain = entity.toDomain()
        assertEquals("1", domain.id)
        assertEquals("Test Performer", domain.name)
        assertEquals(5, domain.sceneCount)
        assertEquals(10, domain.oCounter)
    }

    @Test
    fun `Studio mapping is consistent`() {
        val studio = Studio(
            id = "1",
            name = "Test Studio",
            sceneCount = 10
        )

        // Domain -> Entity
        val entity = studio.toEntity()
        assertEquals("1", entity.id)
        assertEquals("Test Studio", entity.name)

        // Entity -> Domain
        val domain = entity.toDomain()
        assertEquals("1", domain.id)
        assertEquals("Test Studio", domain.name)
        assertEquals(10, domain.sceneCount)
    }

    @Test
    fun `Tag mapping is consistent`() {
        val tag = Tag(
            id = "1",
            name = "Test Tag",
            sceneCount = 5
        )

        // Domain -> Entity
        val entity = tag.toEntity()
        assertEquals("1", entity.id)
        assertEquals("Test Tag", entity.name)

        // Entity -> Domain
        val domain = entity.toDomain()
        assertEquals("1", domain.id)
        assertEquals("Test Tag", domain.name)
        assertEquals(5, domain.sceneCount)
    }
}
