package goonarr.stash.repositories

import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.database.dao.StudioDao
import goonarr.stash.core.database.entity.StudioEntity
import goonarr.stash.core.model.Studio
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

class StudioRepositoryTest {

    private val studioDao = mockk<StudioDao>(relaxed = true)
    private val client = mockk<StashClient>(relaxed = true)
    private val settingsStore = mockk<SettingsStore>(relaxed = true)

    private lateinit var repository: StudioRepository

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        every { settingsStore.serverUrl } returns flowOf("http://test")
        every { settingsStore.apiKey } returns flowOf("key")

        // Default mocks
        every { studioDao.getById(any()) } returns flowOf(null)

        repository = StudioRepository(
            studioDao = studioDao,
            client = client,
            settingsStore = settingsStore,
            ioDispatcher = UnconfinedTestDispatcher()
        )
    }

    @Test
    fun `getStudio returns studio from DB`() = runTest {
        val studioId = "1"
        val studioEntity = StudioEntity(
            id = studioId, name = "DB Studio", imagePath = null, sceneCount = 0,
            imageCount = 0, galleryCount = 0, performerCount = 0, groupCount = 0,
            details = null, rating100 = null, favorite = false, oCounter = null,
            ignoreAutoTag = false, aliases = null, urls = null, stashIds = null,
            createdAt = null, updatedAt = null, parentStudioId = null
        )
        every { studioDao.getById(studioId) } returns flowOf(studioEntity)

        val result = repository.getStudio(studioId).first()

        assertNotNull(result)
        assertEquals("DB Studio", result?.name)
    }

    @Test
    fun `fetchStudio queries API and inserts to DB`() = runTest {
        val studioId = "1"
        val mockStudio = Studio(id = studioId, name = "API Studio")
        coEvery { client.findStudio(any(), any(), studioId) } returns mockStudio

        val result = repository.fetchStudio(studioId)

        assertNotNull(result)
        assertEquals("API Studio", result?.name)
        coVerify { studioDao.insertAll(any()) }
    }

    @Test
    fun `fetchStudios returns list from API and resolves URLs`() = runTest {
        val mockStudios = listOf(Studio(id = "1", name = "Studio 1", imagePath = "image.jpg"))
        coEvery { client.findStudios(any(), any(), any(), any(), any(), any(), any(), any()) } returns Pair(mockStudios, 1)

        val result = repository.fetchStudios(searchText = "test")

        assertEquals(1, result.first.size)
        assertEquals("http://test/image.jpg", result.first[0].imagePath)
    }

    @Test
    fun `fetchStudiosForTag returns list from API`() = runTest {
        val tagId = "t1"
        val mockStudios = listOf(Studio(id = "1", name = "Studio 1"))
        coEvery { client.findStudios(any(), any(), any(), any(), any(), any(), any(), tagId) } returns Pair(mockStudios, 1)

        val result = repository.fetchStudiosForTag(tagId)

        assertEquals(1, result.first.size)
        assertEquals("Studio 1", result.first[0].name)
    }

    @Test
    fun `updateStudio calls API and updates DB on success`() = runTest {
        val studioId = "1"
        val updatedStudio = Studio(id = studioId, name = "Updated Name")
        coEvery {
            client.updateStudio(any(), any(), studioId, name = "Updated Name", any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns updatedStudio

        val result = repository.updateStudio(id = studioId, name = "Updated Name")

        assertNotNull(result)
        assertEquals("Updated Name", result?.name)
        coVerify { studioDao.insertAll(any()) }
    }

    @Test
    fun `deleteStudio calls API`() = runTest {
        val studioId = "1"
        coEvery {
            client.studioDestroy(any(), any(), studioId)
        } returns true // client returns Unit/void in some generated code, checking usage logic

        repository.deleteStudio(studioId)

        coVerify { client.studioDestroy(any(), any(), studioId) }
    }
}
