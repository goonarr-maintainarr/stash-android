package goonarr.stash.core.model.scraper

import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.Scene
import goonarr.stash.core.model.SceneFile
import goonarr.stash.core.model.Studio
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaggerQueryServiceTest {

    // region AUTO mode tests

    @Test
    fun `prepareQueryString AUTO mode uses metadata when date and studio available`() {
        val scene = Scene(
            id = "1",
            title = "Test Scene",
            date = "2024-01-15",
            studio = Studio(id = "s1", name = "Test Studio")
        )

        val query = TaggerQueryService.prepareQueryString(scene, ParseMode.AUTO)

        assertTrue(query.contains("2024-01-15"))
        assertTrue(query.contains("Test Studio"))
    }

    @Test
    fun `prepareQueryString AUTO mode falls back to title when no metadata`() {
        val scene = Scene(
            id = "1",
            title = "My Amazing Scene"
        )

        val query = TaggerQueryService.prepareQueryString(scene, ParseMode.AUTO)

        assertTrue(query.contains("My Amazing Scene"))
    }

    @Test
    fun `prepareQueryString AUTO mode uses filename when no metadata but has file`() {
        val scene = Scene(
            id = "1",
            title = null,
            date = null,
            studio = null,
            files = listOf(SceneFile(path = "/videos/my.scene.file.mp4"))
        )

        val query = TaggerQueryService.prepareQueryString(scene, ParseMode.AUTO)

        // Filename should be cleaned: "my.scene.file" -> "my scene file"
        assertTrue(query.contains("my scene file"))
    }

    // endregion

    // region METADATA mode tests

    @Test
    fun `prepareQueryString METADATA mode includes date studio performers title`() {
        val scene = Scene(
            id = "1",
            title = "Scene Title",
            date = "2023-05-20",
            studio = Studio(id = "s1", name = "Studio Name"),
            performers = listOf(
                Performer(id = "p1", name = "Performer One"),
                Performer(id = "p2", name = "Performer Two")
            )
        )

        val query = TaggerQueryService.prepareQueryString(scene, ParseMode.METADATA)

        assertTrue(query.contains("2023-05-20"))
        assertTrue(query.contains("Studio Name"))
        assertTrue(query.contains("Performer One"))
        assertTrue(query.contains("Performer Two"))
        assertTrue(query.contains("Scene Title"))
    }

    @Test
    fun `prepareQueryString METADATA mode cleans special characters from title`() {
        val scene = Scene(
            id = "1",
            title = "Scene! @With# \$Special% ^Characters&"
        )

        val query = TaggerQueryService.prepareQueryString(scene, ParseMode.METADATA)

        assertEquals("Scene With Special Characters", query)
    }

    // endregion

    // region FILENAME mode tests

    @Test
    fun `prepareQueryString FILENAME mode extracts filename without extension`() {
        val scene = Scene(
            id = "1",
            files = listOf(SceneFile(path = "/media/videos/my.awesome.scene.mp4"))
        )

        val query = TaggerQueryService.prepareQueryString(scene, ParseMode.FILENAME)

        assertEquals("my awesome scene", query)
    }

    @Test
    fun `prepareQueryString FILENAME mode returns empty when no files`() {
        val scene = Scene(id = "1", files = null)

        val query = TaggerQueryService.prepareQueryString(scene, ParseMode.FILENAME)

        assertEquals("", query)
    }

    // endregion

    // region PATH mode tests

    @Test
    fun `prepareQueryString PATH mode includes all path components`() {
        val scene = Scene(
            id = "1",
            files = listOf(SceneFile(path = "/media/videos/studio/scene.file.mp4"))
        )

        val query = TaggerQueryService.prepareQueryString(scene, ParseMode.PATH)

        assertTrue(query.contains("media"))
        assertTrue(query.contains("videos"))
        assertTrue(query.contains("studio"))
        assertTrue(query.contains("scene file"))
    }

    // endregion

    // region DIRECTORY mode tests

    @Test
    fun `prepareQueryString DIRECTORY mode uses parent directory name`() {
        val scene = Scene(
            id = "1",
            files = listOf(SceneFile(path = "/media/videos/StudioName/scene.mp4"))
        )

        val query = TaggerQueryService.prepareQueryString(scene, ParseMode.DIRECTORY)

        assertEquals("StudioName", query)
    }

    // endregion

    // region Blacklist tests

    @Test
    fun `prepareQueryString applies blacklist patterns`() {
        val scene = Scene(
            id = "1",
            title = "Scene with XXX unwanted YYY content"
        )
        val blacklist = listOf("XXX", "YYY")

        val query = TaggerQueryService.prepareQueryString(scene, ParseMode.METADATA, blacklist)

        assertTrue(!query.contains("XXX"))
        assertTrue(!query.contains("YYY"))
        assertTrue(query.contains("Scene with"))
        assertTrue(query.contains("unwanted"))
    }

    @Test
    fun `prepareQueryString applies regex blacklist patterns`() {
        val scene = Scene(
            id = "1",
            title = "Scene 1080p HD quality 720p"
        )
        val blacklist = listOf("\\d{3,4}p")

        val query = TaggerQueryService.prepareQueryString(scene, ParseMode.METADATA, blacklist)

        assertTrue(!query.contains("1080p"))
        assertTrue(!query.contains("720p"))
        assertTrue(query.contains("Scene"))
        assertTrue(query.contains("HD quality"))
    }

    @Test
    fun `prepareQueryString handles invalid regex patterns gracefully`() {
        val scene = Scene(
            id = "1",
            title = "Test Scene"
        )
        val blacklist = listOf("[invalid(regex")

        // Should not throw exception
        val query = TaggerQueryService.prepareQueryString(scene, ParseMode.METADATA, blacklist)

        assertEquals("Test Scene", query)
    }

    // endregion

    // region Edge cases

    @Test
    fun `prepareQueryString collapses multiple spaces`() {
        val scene = Scene(
            id = "1",
            title = "Scene   with    multiple     spaces"
        )

        val query = TaggerQueryService.prepareQueryString(scene, ParseMode.METADATA)

        assertEquals("Scene with multiple spaces", query)
    }

    @Test
    fun `prepareQueryString trims whitespace`() {
        val scene = Scene(
            id = "1",
            title = "  Padded Title  "
        )

        val query = TaggerQueryService.prepareQueryString(scene, ParseMode.METADATA)

        assertEquals("Padded Title", query)
    }

    // endregion
}
