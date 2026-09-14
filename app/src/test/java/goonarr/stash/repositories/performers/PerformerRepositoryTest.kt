package goonarr.stash.repositories.performers

import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.database.StashDatabase
import goonarr.stash.core.database.dao.PerformerDao
import goonarr.stash.core.database.entity.PerformerEntity
import goonarr.stash.core.model.Performer
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

class PerformerRepositoryTest {

    private val db = mockk<StashDatabase>(relaxed = true)
    private val performerDao = mockk<PerformerDao>(relaxed = true)
    private val client = mockk<StashClient>(relaxed = true)
    private val settingsStore = mockk<SettingsStore>(relaxed = true)

    private lateinit var repository: PerformerRepository

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        every { db.performerDao() } returns performerDao
        every { settingsStore.serverUrl } returns flowOf("http://test")
        every { settingsStore.apiKey } returns flowOf("key")

        // Default mocks to prevent NoSuchElementException when calling .first()
        every { performerDao.getById(any()) } returns flowOf(null)
        coEvery { performerDao.getSortOrder(any()) } returns null

        repository = PerformerRepository(db, client, settingsStore, UnconfinedTestDispatcher())
    }

    @Test
    fun `getById returns performer from DB`() = runTest {
        val performerId = "123"
        val performerEntity = PerformerEntity(
            id = performerId, name = "DB Performer", imagePath = null, sceneCount = 0,
            oCounter = null, disambiguation = null, gender = null, birthdate = null,
            deathDate = null, country = null, ethnicity = null, eyeColor = null,
            hairColor = null, heightCm = null, weight = null, measurements = null,
            fakeTits = null, careerLength = null, tattoos = null, piercings = null,
            aliasList = null, favorite = null, details = null, rating100 = null,
            createdAt = null, updatedAt = null, stashIds = null, urls = null, tags = null, sortOrder = 0
        )
        every { performerDao.getById(performerId) } returns flowOf(performerEntity)

        val result = repository.getPerformer(performerId).first()

        assertNotNull(result)
        assertEquals("DB Performer", result?.name)
    }

    @Test
    fun `fetchFromApi returns performer data`() = runTest {
        val performerId = "123"
        val mockPerformer = Performer(id = performerId, name = "API Performer")
        coEvery { client.findPerformer(any(), any(), performerId) } returns mockPerformer

        val result = repository.fetchPerformerFromApi(performerId)

        assertNotNull(result)
        assertEquals("API Performer", result?.name)
    }

    @Test
    fun `refreshById fetches and inserts into DB when performer exists`() = runTest {
        val performerId = "123"
        val mockPerformer = Performer(id = performerId, name = "Test Performer")
        coEvery { client.findPerformer(any(), any(), performerId) } returns mockPerformer
        // Performer exists in DB
        coEvery { performerDao.getSortOrder(performerId) } returns 1

        repository.refreshPerformer(performerId)

        coVerify { performerDao.insertAll(any()) }
    }

    @Test
    fun `refreshById handles exceptions gracefully`() = runTest {
        val performerId = "123"
        coEvery { client.findPerformer(any(), any(), performerId) } throws Exception("API Error")

        repository.refreshPerformer(performerId)

        coVerify(exactly = 0) { performerDao.insertAll(any()) }
    }

    @Test
    fun `savePerformer inserts entity into DB`() = runTest {
        val performer = Performer(id = "1", name = "Test")
        repository.savePerformer(performer)
        coVerify { performerDao.insertAll(any()) }
    }

    @Test
    fun `savePerformer preserves existing sortOrder`() = runTest {
        val performerId = "123"
        val performer = Performer(id = performerId, name = "Test")
        coEvery { performerDao.getSortOrder(performerId) } returns 42

        repository.savePerformer(performer)

        coVerify {
            performerDao.insertAll(
                match {
                    it.first().sortOrder == 42
                }
            )
        }
    }

    @Test
    fun `cachePerformer saves to DB when performer exists in paginated list`() = runTest {
        val performerId = "123"
        val performer = Performer(id = performerId, name = "Test Performer")
        // Performer exists in DB (has a sortOrder)
        coEvery { performerDao.getSortOrder(performerId) } returns 5

        repository.cachePerformer(performer)

        // Should save to DB
        coVerify { performerDao.insertAll(any()) }
    }

    @Test
    fun `cachePerformer stores in memory only when performer not in paginated list`() = runTest {
        val performerId = "new-performer"
        val performer = Performer(id = performerId, name = "New Performer")
        // Performer does NOT exist in DB
        coEvery { performerDao.getSortOrder(performerId) } returns null

        repository.cachePerformer(performer)

        // Should NOT save to DB
        coVerify(exactly = 0) { performerDao.insertAll(any()) }

        // But should be retrievable from memory cache
        val cached = repository.getPerformer(performerId).first()
        assertEquals("New Performer", cached?.name)
    }

    @Test
    fun `fetchStashBoxes returns results from client`() = runTest {
        val mockBoxes = listOf(goonarr.stash.core.model.scraper.StashBox("1", "Box 1", "http://box1"))
        coEvery { client.fetchStashBoxes(any(), any()) } returns mockBoxes

        val result = repository.fetchStashBoxes()

        assertEquals(mockBoxes, result)
        coVerify { client.fetchStashBoxes("http://test", "key") }
    }

    @Test
    fun `scrapePerformer returns results from client`() = runTest {
        val box = goonarr.stash.core.model.scraper.StashBox("1", "Box 1", "http://box1")
        val query = "performer name"
        val mockResults = listOf(goonarr.stash.core.model.scraper.ScrapedPerformer(name = "Scraped Performer"))
        coEvery { client.scrapePerformer(any(), any(), box, query) } returns mockResults

        val result = repository.scrapePerformer(box, query)

        assertEquals(mockResults, result)
        coVerify { client.scrapePerformer("http://test", "key", box, query) }
    }

    @Test
    fun `fetchPerformersForTag returns list from API`() = runTest {
        val tagId = "t1"
        val mockPerformers = listOf(Performer(id = "1", name = "P1"))
        coEvery { client.findPerformers(any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns Pair(mockPerformers, 1)

        val result = repository.fetchPerformersForTag(tagId)

        assertEquals(1, result.first.size)
        assertEquals("P1", result.first[0].name)
    }

    @Test
    fun `fetchPerformersForStudio returns list from API`() = runTest {
        val studioId = "s1"
        val mockPerformers = listOf(Performer(id = "1", name = "P1"))
        coEvery { client.findPerformers(any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns Pair(mockPerformers, 1)

        val result = repository.fetchPerformersForStudio(studioId)

        assertEquals(1, result.first.size)
        assertEquals("P1", result.first[0].name)
    }

    @Test
    fun `updatePerformer calls API and caches result`() = runTest {
        val performerId = "p1"
        val updatedPerformer = Performer(id = performerId, name = "Updated Name")
        coEvery {
            client.updatePerformer(any(), any(), performerId, name = "Updated Name", any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns updatedPerformer
        // Mock DB checks for caching logic
        coEvery { performerDao.getSortOrder(performerId) } returns 1

        val result = repository.updatePerformer(id = performerId, name = "Updated Name")

        assertEquals("Updated Name", result?.name)
        coVerify { performerDao.insertAll(any()) }
    }

    @Test
    fun `deletePerformer calls API and deletes from DB`() = runTest {
        val performerId = "p1"
        coEvery { client.performerDestroy(any(), any(), performerId) } returns true

        repository.deletePerformer(performerId)

        coVerify { client.performerDestroy(any(), any(), performerId) }
        coVerify { performerDao.deleteById(performerId) }
    }
}
