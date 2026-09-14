package goonarr.stash.repositories

import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.database.StashDatabase
import goonarr.stash.core.database.dao.TagDao
import goonarr.stash.core.database.entity.TagEntity
import goonarr.stash.core.network.StashClient
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

class TagRepositoryTest {

    private val db = mockk<StashDatabase>(relaxed = true)
    private val tagDao = mockk<TagDao>(relaxed = true)
    private val client = mockk<StashClient>(relaxed = true)
    private val settingsStore = mockk<SettingsStore>(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var repository: TagRepository

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        every { db.tagDao() } returns tagDao
        every { settingsStore.serverUrl } returns flowOf("http://test")
        every { settingsStore.apiKey } returns flowOf("key")
        every { settingsStore.followedTagIds } returns flowOf(emptyList())

        // Default mocks to prevent NoSuchElementException when calling .first()
        every { tagDao.getAll() } returns flowOf(emptyList())
        every { tagDao.getById(any()) } returns flowOf(null)

        repository = TagRepository(db, client, settingsStore, testDispatcher)
    }

    @Test
    fun `getById returns tag from DB`() = runTest {
        val tagId = "123"
        val tagEntity = mockk<TagEntity>(relaxed = true) {
            every { id } returns tagId
            every { name } returns "DB Tag"
        }
        every { tagDao.getById(tagId) } returns flowOf(tagEntity)

        val result = repository.getTag(tagId).first()

        assertNotNull(result)
        assertEquals("DB Tag", result?.name)
    }

    @Test
    fun `getTags by ids returns tags from DAO`() = runTest {
        // Given
        val ids = listOf("t1", "t2")
        val tagEntity1 = mockk<TagEntity>(relaxed = true) {
            every { id } returns "t1"
            every { name } returns "Tag 1"
        }
        val tagEntity2 = mockk<TagEntity>(relaxed = true) {
            every { id } returns "t2"
            every { name } returns "Tag 2"
        }
        every { tagDao.loadByIds(ids) } returns flowOf(listOf(tagEntity1, tagEntity2))

        // When
        val result = repository.getTags(ids).first()

        // Then
        assertEquals(2, result.size)
        assertEquals("t1", result[0].id)
        assertEquals("t2", result[1].id)
    }

    @Test
    fun `addFollowedTag delegates to settingsStore`() = runTest {
        // When
        repository.addFollowedTag("t1")

        // Then
        coVerify { settingsStore.addFollowedTag("t1") }
    }

    @Test
    fun `removeFollowedTag delegates to settingsStore`() = runTest {
        // When
        repository.removeFollowedTag("t1")

        // Then
        coVerify { settingsStore.removeFollowedTag("t1") }
    }

    @Test
    fun `followedTagIds exposes settingsStore flow`() = runTest {
        // Given
        val expectedIds = listOf("t1", "t2")
        @OptIn(ExperimentalCoroutinesApi::class)
        every { settingsStore.followedTagIds } returns flowOf(expectedIds)
        repository = TagRepository(db, client, settingsStore, testDispatcher)

        // When
        val result = repository.followedTagIds.first()

        // Then
        assertEquals(expectedIds, result)
    }
}
