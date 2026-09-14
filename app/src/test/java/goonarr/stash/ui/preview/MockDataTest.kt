package goonarr.stash.ui.preview

import goonarr.stash.util.MockData
import org.junit.Assert.*
import org.junit.Test

class MockDataTest {

    @Test
    fun mockData_scene_hasRequiredFields() {
        val scene = MockData.scene

        assertNotNull("Scene should not be null", scene)
        assertNotNull("Scene ID should not be null", scene.id)
        assertNotNull("Scene title should not be null", scene.title)
        assertNotNull("Scene studio should not be null", scene.studio)
        assertNotNull("Scene date should not be null", scene.date)
    }

    @Test
    fun mockData_scene_hasHistoryData() {
        val scene = MockData.scene

        assertNotNull("oHistory should not be null", scene.oHistory)
        assertTrue("oHistory should have entries", scene.oHistory!!.isNotEmpty())

        assertNotNull("playHistory should not be null", scene.playHistory)
        assertTrue("playHistory should have entries", scene.playHistory!!.isNotEmpty())
    }

    @Test
    fun mockData_scene_hasStashIds() {
        val scene = MockData.scene

        assertNotNull("stashIds should not be null", scene.stashIds)
        assertTrue("stashIds should have entries", scene.stashIds!!.isNotEmpty())

        // Verify StashID structure
        val firstStashId = scene.stashIds!!.first()
        assertNotNull("StashID endpoint should not be null", firstStashId.endpoint)
        assertNotNull("StashID stashId should not be null", firstStashId.stashId)
    }

    @Test
    fun mockData_scene_hasAdditionalMetadata() {
        val scene = MockData.scene

        assertNotNull("director should not be null", scene.director)
        assertNotNull("code should not be null", scene.code)
        assertNotNull("url should not be null", scene.url)
        assertNotNull("createdAt should not be null", scene.createdAt)
        assertNotNull("updatedAt should not be null", scene.updatedAt)
    }

    @Test
    fun mockData_scene_hasFileInfo() {
        val scene = MockData.scene

        assertNotNull("files should not be null", scene.files)
        assertTrue("files should have at least one entry", scene.files!!.isNotEmpty())

        val file = scene.files!!.first()
        assertNotNull("file path should not be null", file.path)
        assertNotNull("file width should not be null", file.width)
        assertNotNull("file height should not be null", file.height)
        assertNotNull("file videoCodec should not be null", file.videoCodec)
        assertNotNull("file size should not be null", file.size)
    }

    @Test
    fun mockData_scene_hasTags() {
        val scene = MockData.scene

        assertNotNull("tags should not be null", scene.tags)
        assertTrue("tags should have entries", scene.tags!!.isNotEmpty())

        val firstTag = scene.tags!!.first()
        assertNotNull("Tag ID should not be null", firstTag.id)
        assertNotNull("Tag name should not be null", firstTag.name)
    }

    @Test
    fun mockData_scene_hasMarkers() {
        val scene = MockData.scene

        assertNotNull("sceneMarkers should not be null", scene.sceneMarkers)
        assertTrue("sceneMarkers should have entries", scene.sceneMarkers!!.isNotEmpty())

        val firstMarker = scene.sceneMarkers!!.first()
        assertNotNull("Marker ID should not be null", firstMarker.id)
        assertNotNull("Marker title should not be null", firstMarker.title)
    }

    @Test
    fun mockData_performer_hasRequiredFields() {
        val performer = MockData.performer

        assertNotNull("Performer should not be null", performer)
        assertNotNull("Performer ID should not be null", performer.id)
        assertNotNull("Performer name should not be null", performer.name)
        assertNotNull("Performer country should not be null", performer.country)
        assertNotNull("Performer birthdate should not be null", performer.birthdate)
    }

    @Test
    fun mockData_performers_hasMultipleEntries() {
        val performers = MockData.performers

        assertNotNull("Performers list should not be null", performers)
        assertTrue("Performers list should have multiple entries", performers.size >= 2)
    }

    @Test
    fun mockData_scenes_hasMultipleEntries() {
        val scenes = MockData.scenes

        assertNotNull("Scenes list should not be null", scenes)
        assertTrue("Scenes list should have multiple entries", scenes.size >= 2)
    }
}
