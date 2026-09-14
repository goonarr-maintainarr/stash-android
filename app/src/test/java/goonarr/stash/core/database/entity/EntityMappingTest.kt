package goonarr.stash.core.database.entity

import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.Scene
import org.junit.Assert.assertEquals
import org.junit.Test

class EntityMappingTest {

    @Test
    fun `Performer to PerformerEntity mapping`() {
        val performer = Performer(
            id = "1",
            name = "Test",
            details = "Bio",
            favorite = true
        )

        val entity = performer.toEntity()

        assertEquals(performer.id, entity.id)
        assertEquals(performer.name, entity.name)
        assertEquals(performer.details, entity.details)
        assertEquals(performer.favorite, entity.favorite)
    }

    @Test
    fun `PerformerEntity to Performer mapping`() {
        val entity = PerformerEntity(
            id = "1",
            name = "Test",
            imagePath = null,
            sceneCount = 5,
            oCounter = 2,
            disambiguation = null,
            gender = "female",
            birthdate = null,
            deathDate = null,
            country = null,
            ethnicity = null,
            eyeColor = null,
            hairColor = null,
            heightCm = null,
            weight = null,
            measurements = null,
            fakeTits = null,
            careerLength = null,
            tattoos = null,
            piercings = null,
            aliasList = null,
            favorite = true,
            details = "Details",
            rating100 = 80,
            createdAt = null,
            updatedAt = null,
            stashIds = null,
            urls = null,
            tags = null
        )

        val domain = entity.toDomain()

        assertEquals(entity.id, domain.id)
        assertEquals(entity.name, domain.name)
        assertEquals(entity.favorite, domain.favorite)
        assertEquals(entity.details, domain.details)
    }

    @Test
    fun `Scene to SceneEntity mapping`() {
        val scene = Scene(
            id = "1",
            title = "Title",
            details = "Details",
            rating100 = 90
        )

        val entity = scene.toEntity()

        assertEquals(scene.id, entity.id)
        assertEquals(scene.title, entity.title)
        assertEquals(scene.details, entity.details)
        assertEquals(scene.rating100, entity.rating100)
    }
}
