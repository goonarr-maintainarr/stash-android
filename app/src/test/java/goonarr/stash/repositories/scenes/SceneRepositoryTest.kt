package goonarr.stash.repositories.scenes

import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.database.StashDatabase
import goonarr.stash.core.database.dao.SceneDao
import goonarr.stash.core.model.GenerationOptions
import goonarr.stash.core.model.ScanOptions
import goonarr.stash.core.model.Scene
import goonarr.stash.core.model.SceneMarker
import goonarr.stash.core.model.Tag
import goonarr.stash.core.network.StashClient
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class SceneRepositoryTest {

    private val db = mockk<StashDatabase>(relaxed = true)
    private val sceneDao = mockk<SceneDao>(relaxed = true)
    private val client = mockk<StashClient>(relaxed = true)
    private val settingsStore = mockk<SettingsStore>(relaxed = true)

    private lateinit var repository: SceneRepository

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        every { db.sceneDao() } returns sceneDao
        every { settingsStore.serverUrl } returns flowOf("http://test")
        every { settingsStore.apiKey } returns flowOf("key")
        every { settingsStore.generationOptions } returns flowOf(GenerationOptions())

        // Default mocks to prevent NoSuchElementException when calling .first()
        every { sceneDao.getById(any()) } returns flowOf(null)
        coEvery { sceneDao.getSortOrder(any()) } returns null
        every { sceneDao.getAll() } returns flowOf(emptyList())

        repository = SceneRepository(db, client, settingsStore, UnconfinedTestDispatcher())
    }

    @Test
    fun `getById returns scene from DB`() = runTest {
        val sceneId = "123"
        val sceneEntity = mockk<goonarr.stash.core.database.entity.SceneEntity>(relaxed = true) {
            every { id } returns sceneId
            every { title } returns "DB Scene"
        }
        every { sceneDao.getById(sceneId) } returns flowOf(sceneEntity)

        val result = repository.getSceneById(sceneId).first()

        assertNotNull(result)
        assertEquals("DB Scene", result?.title)
    }

    @Test
    fun `fetchFromApi returns scene data`() = runTest {
        val sceneId = "123"
        val mockScene = Scene(id = sceneId, title = "API Scene", tags = listOf(Tag(id = "1", name = "Tag")))
        coEvery { client.findScene(any(), any(), sceneId) } returns mockScene

        val result = repository.fetchSceneFromApi(sceneId)

        assertNotNull(result)
        assertEquals("API Scene", result?.title)
    }

    @Test
    fun `refreshById fetches and inserts into DB when scene exists`() = runTest {
        val sceneId = "123"
        val mockScene = Scene(id = sceneId, title = "Test Scene")
        coEvery { client.findScene(any(), any(), sceneId) } returns mockScene
        // Scene exists in DB
        coEvery { sceneDao.getSortOrder(sceneId) } returns 1

        repository.refreshScene(sceneId)

        coVerify { sceneDao.insertAll(any()) }
    }

    @Test
    fun `refreshById handles exceptions gracefully`() = runTest {
        val sceneId = "123"
        coEvery { client.findScene(any(), any(), sceneId) } throws Exception("API Error")

        repository.refreshScene(sceneId)

        coVerify(exactly = 0) { sceneDao.insertAll(any()) }
    }

    @Test
    fun `saveScene inserts entity into DB`() = runTest {
        val scene = Scene(id = "1", title = "Test")
        repository.saveScene(scene)
        coVerify { sceneDao.insertAll(any()) }
    }

    @Test
    fun `saveScene preserves scene markers when mapping to entity`() = runTest {
        val markers = listOf(SceneMarker(id = "m1", title = "M1", seconds = 10.0))
        val scene = Scene(id = "1", title = "Test", sceneMarkers = markers)

        repository.saveScene(scene)

        coVerify {
            sceneDao.insertAll(
                match {
                    it.first().sceneMarkers?.size == 1 &&
                        it.first().sceneMarkers?.first()?.id == "m1"
                }
            )
        }
    }

    @Test
    fun `fetchScenesForPerformer fetches from API and returns scenes and count with sort params`() = runTest {
        val performerId = "p1"
        val mockScenes = listOf(Scene(id = "s1", title = "S1"))

        coEvery { client.findScenes(any(), any(), any(), any(), any(), any(), any(), performerId) } returns Pair(mockScenes, 10)

        // Test default sort
        val (scenes, count) = repository.fetchScenesForPerformer(performerId)

        assertEquals(mockScenes, scenes)
        assertEquals(10, count)
        coVerify { client.findScenes(any(), any(), "", 1, 100, "date", "DESC", performerId) }

        // Test explicit sort
        repository.fetchScenesForPerformer(performerId, 1, "rating", "ASC")
        coVerify { client.findScenes(any(), any(), "", 1, 100, "rating", "ASC", performerId) }
    }

    @Test
    fun `rateScene calls client sceneUpdate and refreshes scene`() = runTest {
        val sceneId = "123"
        val rating = 80
        val mockScene = Scene(id = sceneId, title = "Updated Scene")

        coEvery {
            client.updateScene(any(), any(), sceneId, any(), any(), any(), any(), any(), any(), any(), any(), rating, any(), any(), any())
        } returns mockScene
        coEvery { client.findScene(any(), any(), sceneId) } returns mockScene
        // Mock getSortOrder to return non-null so cacheScene calls saveScene
        coEvery { sceneDao.getSortOrder(sceneId) } returns 1

        repository.rateScene(sceneId, rating)

        coVerify {
            client.updateScene(any(), any(), sceneId, null, null, null, null, null, null, null, null, rating, null, null, null)
        }
        coVerify { client.findScene(any(), any(), sceneId) }
        coVerify { sceneDao.insertAll(any()) }
    }

    @Test
    fun `saveScene preserves existing sortOrder`() = runTest {
        val sceneId = "123"
        val scene = Scene(id = sceneId, title = "Test")
        coEvery { sceneDao.getSortOrder(sceneId) } returns 123

        repository.saveScene(scene)

        coVerify {
            sceneDao.insertAll(
                match {
                    it.first().sortOrder == 123
                }
            )
        }
    }

    @Test
    fun `incrementOCounter calls client and refreshes scene`() = runTest {
        val sceneId = "123"
        val mockCount = 5
        val mockScene = Scene(id = sceneId, title = "Updated Scene")

        coEvery { client.incrementOCounter(any(), any(), any()) } returns mockCount
        coEvery { client.findScene(any(), any(), any()) } returns mockScene

        coEvery { sceneDao.getSortOrder(sceneId) } returns 1
        every { sceneDao.getById(sceneId) } returns flowOf(null)

        val result = repository.incrementOCounter(sceneId)

        assertEquals(5, result)
        coVerify { client.incrementOCounter(any(), any(), any()) }
        coVerify { client.findScene(any(), any(), any()) }
        coVerify { sceneDao.insertAll(any()) }
    }

    @Test
    fun `cacheScene saves to DB when scene exists in paginated list`() = runTest {
        val sceneId = "123"
        val scene = Scene(id = sceneId, title = "Test Scene")
        // Scene exists in DB (has a sortOrder)
        coEvery { sceneDao.getSortOrder(sceneId) } returns 5

        repository.cacheScene(scene)

        // Should save to DB
        coVerify { sceneDao.insertAll(any()) }
    }

    @Test
    fun `cacheScene stores in memory only when scene not in paginated list`() = runTest {
        val sceneId = "new-scene"
        val scene = Scene(id = sceneId, title = "New Scene")
        // Scene does NOT exist in DB
        coEvery { sceneDao.getSortOrder(sceneId) } returns null

        repository.cacheScene(scene)

        // Should NOT save to DB
        coVerify(exactly = 0) { sceneDao.insertAll(any()) }

        // But should be retrievable from memory cache
        val cached = repository.getSceneById(sceneId).first()
        assertEquals("New Scene", cached?.title)
    }

    @Test
    fun `getHomeScenes fetches from local DB with correct sort`() = runTest {
        val mockSceneEntity = mockk<goonarr.stash.core.database.entity.SceneEntity>(relaxed = true) {
            every { id } returns "s1"
            every { title } returns "S1"
        }
        coEvery { sceneDao.getHomeScenes(any()) } returns flowOf(listOf(mockSceneEntity))

        val result = repository.getHomeScenes(sort = "rating", limit = 5)

        assertEquals(1, result.size)
        assertEquals("S1", result[0].title)
        coVerify { sceneDao.getHomeScenes(any()) }
    }

    @Test
    fun `getScenesForTag fetches from local DB with tagId`() = runTest {
        val tagId = "tag123"
        val mockSceneEntity = mockk<goonarr.stash.core.database.entity.SceneEntity>(relaxed = true) {
            every { id } returns "s1"
            every { title } returns "Tag Scene"
        }
        coEvery { sceneDao.getByTagId(tagId, 10) } returns flowOf(listOf(mockSceneEntity))

        val result = repository.getScenesForTag(tagId, limit = 10)

        assertEquals(1, result.size)
        assertEquals("Tag Scene", result[0].title)
        coVerify { sceneDao.getByTagId(tagId, 10) }
    }

    @Test
    fun `getScenesForTag uses default limit of 10`() = runTest {
        val tagId = "tag456"
        coEvery { sceneDao.getByTagId(tagId, 10) } returns flowOf(emptyList())

        repository.getScenesForTag(tagId)

        coVerify { sceneDao.getByTagId(tagId, 10) }
    }

    @Test
    fun `getScenesForPerformer fetches from local DB with performerId filter`() = runTest {
        val performerId = "perf123"
        val mockSceneEntity = mockk<goonarr.stash.core.database.entity.SceneEntity>(relaxed = true) {
            every { id } returns "s1"
            every { title } returns "Performer Scene"
        }

        coEvery { sceneDao.getHomeScenes(any()) } returns flowOf(listOf(mockSceneEntity))

        val result = repository.getScenesForPerformer(performerId)

        assertEquals(1, result.size)
        assertEquals("Performer Scene", result[0].title)
    }

    @Test
    fun `getScenesForPerformer uses default sort params`() = runTest {
        val performerId = "perf456"
        coEvery { sceneDao.getHomeScenes(any()) } returns flowOf(emptyList())

        repository.getScenesForPerformer(performerId)

        // Verify that the query was constructed with default sort params
        coVerify { sceneDao.getHomeScenes(any()) }
    }

    @Test
    fun `getScenesCountForPerformer returns count from local DB`() = runTest {
        val performerId = "perf789"

        coEvery { sceneDao.getScenesCount(any()) } returns flowOf(42)

        val result = repository.getScenesCountForPerformer(performerId)

        assertEquals(42, result)
    }

    @Test
    fun `scrapeScene calls client with correct parameters`() = runTest {
        val box = goonarr.stash.core.model.scraper.StashBox("Box", "endpoint", "key")
        val scene = Scene(id = "1", title = "Scene")
        val query = "query"
        val results =
            listOf(goonarr.stash.core.model.scraper.ScrapedScene(title = "Result", details = null, date = null, studio = null, performers = null, tags = null, image = null, remoteSiteId = null, director = null, code = null, url = null, urls = null))

        coEvery { client.scrapeScene(any(), any(), box, any(), any(), any(), any(), any(), any(), query) } returns results

        val actual = repository.scrapeScene(box, scene, query)

        assertEquals(results, actual)
        coVerify {
            client.scrapeScene(
                url = "http://test",
                apiKey = "key",
                stashBox = box,
                sceneTitle = "Scene",
                sceneCode = null,
                sceneDetails = null,
                sceneDirector = null,
                sceneUrl = null,
                sceneDate = null,
                query = query
            )
        }
    }

    @Test
    fun `scrapeSceneByFragment calls client with correct parameters`() = runTest {
        val box = goonarr.stash.core.model.scraper.StashBox("Box", "endpoint", "key")
        val scene = Scene(id = "1", title = "Scene", code = "123")
        val results =
            listOf(goonarr.stash.core.model.scraper.ScrapedScene(title = "Result", details = null, date = null, studio = null, performers = null, tags = null, image = null, remoteSiteId = null, director = null, code = null, url = null, urls = null))

        coEvery { client.scrapeSceneByFragment(any(), any(), box, any(), any(), any(), any(), any(), any()) } returns results

        val actual = repository.scrapeSceneByFragment(box, scene)

        assertEquals(results, actual)
        coVerify {
            client.scrapeSceneByFragment(
                url = "http://test",
                apiKey = "key",
                stashBox = box,
                sceneTitle = "Scene",
                sceneCode = "123",
                sceneDetails = null,
                sceneDirector = null,
                sceneUrl = null,
                sceneDate = null
            )
        }
    }

    @Test
    fun `generateSceneScreenshot calls client and refreshes scene`() = runTest {
        val sceneId = "123"
        val timestamp = 45.0
        val mockScene = Scene(id = sceneId, title = "Refreshed Scene")

        coEvery { client.sceneGenerateScreenshot(any(), any(), any(), any()) } returns true
        coEvery { client.findScene(any(), any(), any()) } returns mockScene
        coEvery { sceneDao.getSortOrder(sceneId) } returns 1
        every { sceneDao.getById(sceneId) } returns flowOf(null)

        val result = repository.generateSceneScreenshot(sceneId, timestamp)

        assertEquals(true, result)
        coVerify { client.sceneGenerateScreenshot(any(), any(), any(), any()) }
        coVerify { client.findScene(any(), any(), any()) }
        coVerify { sceneDao.insertAll(any()) }
    }

    @Test
    fun `rescanScene calls requestScan with correct options`() = runTest {
        val path = "/media/scenes/test_scene.mp4"

        coEvery { client.requestScan(any<String>(), any<String>(), any<goonarr.stash.core.model.ScanOptions>()) } returns true

        val result = repository.rescanScene(path)

        assertEquals(true, result)
        coVerify {
            client.requestScan(
                any<String>(),
                any<String>(),
                match { it.paths == path && it.rescan }
            )
        }
    }

    @Test
    fun `generateScene calls requestGeneration with sceneId and settings options`() = runTest {
        val sceneId = "scene-456"
        val mockScene = Scene(id = sceneId, title = "Test Scene")
        val settingsOptions = GenerationOptions(covers = true, sprites = false, previews = true)

        every { settingsStore.generationOptions } returns flowOf(settingsOptions)
        coEvery { client.requestGeneration(any<String>(), any<String>(), any<GenerationOptions>()) } returns true
        coEvery { client.findScene(any<String>(), any<String>(), sceneId) } returns mockScene

        val result = repository.generateScene(sceneId)

        assertEquals(true, result)
        coVerify {
            client.requestGeneration(
                any<String>(),
                any<String>(),
                match {
                    it.sceneIds == listOf(sceneId) &&
                        it.covers == settingsOptions.covers &&
                        it.sprites == settingsOptions.sprites &&
                        it.previews == settingsOptions.previews &&
                        it.interactiveHeatmapsSpeeds == settingsOptions.interactiveHeatmapsSpeeds
                }
            )
        }
        coVerify { client.findScene(any<String>(), any<String>(), sceneId) }
    }

    @Test
    fun `generateDefaultThumbnail calls requestGeneration with fixed targeted options`() = runTest {
        val sceneId = "scene-targeted"
        val mockScene = Scene(id = sceneId, title = "Targeted Scene")

        coEvery { client.requestGeneration(any<String>(), any<String>(), any<GenerationOptions>()) } returns true
        coEvery { client.findScene(any<String>(), any<String>(), sceneId) } returns mockScene

        val result = repository.generateDefaultThumbnail(sceneId)

        assertEquals(true, result)
        coVerify {
            client.requestGeneration(
                any<String>(),
                any<String>(),
                match {
                    it.sceneIds == listOf(sceneId) &&
                        it.covers == true &&
                        it.sprites == false &&
                        it.previews == false
                }
            )
        }
        coVerify { client.findScene(any<String>(), any<String>(), sceneId) }
    }
}
