package goonarr.stash.repositories

import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.database.StashDatabase
import goonarr.stash.core.database.dao.SavedFilterDao
import goonarr.stash.core.database.entity.SavedFilterEntity
import goonarr.stash.core.model.FilterCriterion
import goonarr.stash.core.model.FilterMode
import goonarr.stash.core.model.FilterModifier
import goonarr.stash.core.model.FindFilter
import goonarr.stash.core.model.SavedFilter
import goonarr.stash.core.network.FindFilterDto
import goonarr.stash.core.network.SavedFilterDto
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SavedFilterRepositoryTest {

    private val db = mockk<StashDatabase>(relaxed = true)
    private val savedFilterDao = mockk<SavedFilterDao>(relaxed = true)
    private val client = mockk<StashClient>(relaxed = true)
    private val settingsStore = mockk<SettingsStore>(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var repository: SavedFilterRepository

    @Before
    fun setUp() {
        every { db.savedFilterDao() } returns savedFilterDao
        every { settingsStore.serverUrl } returns flowOf("http://test")
        every { settingsStore.apiKey } returns flowOf("key")
        every { settingsStore.syncSavedFilters } returns flowOf(true)

        // Default mocks
        every { savedFilterDao.getAll() } returns flowOf(emptyList())
        every { savedFilterDao.getById(any()) } returns flowOf(null)
        every { savedFilterDao.getByMode(any()) } returns flowOf(emptyList())

        repository = SavedFilterRepository(db, client, settingsStore, testDispatcher)
    }

    @Test
    fun `getAllFilters returns filters from DAO`() = runTest {
        // Given
        val filterEntity = SavedFilterEntity(
            id = "filter1",
            mode = FilterMode.SCENES,
            name = "My Filter",
            findFilter = FindFilter(sort = "date"),
            objectFilter = mapOf("tags" to FilterCriterion(value = listOf("tag1"))),
            uiOptions = null,
            sortOrder = 0
        )
        every { savedFilterDao.getAll() } returns flowOf(listOf(filterEntity))

        // When
        val result = repository.getAllFilters().first()

        // Then
        assertEquals(1, result.size)
        assertEquals("filter1", result[0].id)
        assertEquals("My Filter", result[0].name)
    }

    @Test
    fun `getFiltersByMode filters by mode`() = runTest {
        // Given
        val filterEntity = SavedFilterEntity(
            id = "filter1",
            mode = FilterMode.PERFORMERS,
            name = "Performer Filter",
            findFilter = null,
            objectFilter = null,
            uiOptions = null,
            sortOrder = 0
        )
        every { savedFilterDao.getByMode(FilterMode.PERFORMERS) } returns flowOf(listOf(filterEntity))

        // When
        val result = repository.getFiltersByMode(FilterMode.PERFORMERS).first()

        // Then
        assertEquals(1, result.size)
        assertEquals(FilterMode.PERFORMERS, result[0].mode)
    }

    @Test
    fun `getFilter returns single filter by ID`() = runTest {
        // Given
        val filterEntity = SavedFilterEntity(
            id = "filter1",
            mode = FilterMode.TAGS,
            name = "Tag Filter",
            findFilter = null,
            objectFilter = null,
            uiOptions = null,
            sortOrder = 0
        )
        every { savedFilterDao.getById("filter1") } returns flowOf(filterEntity)

        // When
        val result = repository.getFilter("filter1").first()

        // Then
        assertNotNull(result)
        assertEquals("filter1", result?.id)
        assertEquals("Tag Filter", result?.name)
    }

    @Test
    fun `syncFilters skips when sync is disabled`() = runTest {
        // Given
        every { settingsStore.syncSavedFilters } returns flowOf(false)
        repository = SavedFilterRepository(db, client, settingsStore, testDispatcher)

        // When
        val result = repository.syncFilters()

        // Then
        assertFalse(result)
        coVerify(exactly = 0) { client.findSavedFilters(any(), any(), any()) }
    }

    @Test
    fun `syncFilters fetches and stores filters when enabled`() = runTest {
        // Given
        val filterDto = SavedFilterDto(
            id = "filter1",
            mode = "SCENES",
            name = "My Scenes",
            find_filter = FindFilterDto(sort = "date", direction = "DESC"),
            object_filter = null,
            ui_options = null
        )
        coEvery {
            client.findSavedFilters("http://test", "key", null)
        } returns listOf(filterDto)
        coEvery { savedFilterDao.insertAll(any()) } returns Unit

        // When
        val result = repository.syncFilters()

        // Then
        assertTrue(result)
        coVerify { savedFilterDao.insertAll(any()) }
    }

    @Test
    fun `syncFilters handles errors gracefully`() = runTest {
        // Given
        coEvery {
            client.findSavedFilters(any(), any(), any())
        } throws Exception("Network error")

        // When
        val result = repository.syncFilters()

        // Then
        assertFalse(result)
    }

    @Test
    fun `extractFilterIds extracts tag IDs from object_filter`() {
        // Given
        val filter = SavedFilter(
            id = "filter1",
            mode = FilterMode.SCENES,
            name = "Tagged Scenes",
            findFilter = null,
            objectFilter = mapOf(
                "tags" to FilterCriterion(
                    value = listOf("tag1", "tag2", "tag3"),
                    modifier = FilterModifier.INCLUDES
                )
            ),
            uiOptions = null
        )

        // When
        val result = repository.extractFilterIds(filter)

        // Then
        assertEquals(3, result.tagIds.size)
        assertEquals(listOf("tag1", "tag2", "tag3"), result.tagIds)
        assertEquals(0, result.performerIds.size)
        assertEquals(0, result.studioIds.size)
    }

    @Test
    fun `extractFilterIds extracts performer IDs from object_filter`() {
        // Given
        val filter = SavedFilter(
            id = "filter1",
            mode = FilterMode.SCENES,
            name = "Performer Scenes",
            findFilter = null,
            objectFilter = mapOf(
                "performers" to FilterCriterion(
                    value = listOf("perf1", "perf2"),
                    modifier = FilterModifier.INCLUDES
                )
            ),
            uiOptions = null
        )

        // When
        val result = repository.extractFilterIds(filter)

        // Then
        assertEquals(2, result.performerIds.size)
        assertEquals(listOf("perf1", "perf2"), result.performerIds)
        assertEquals(0, result.tagIds.size)
    }

    @Test
    fun `extractFilterIds extracts studio IDs from object_filter`() {
        // Given
        val filter = SavedFilter(
            id = "filter1",
            mode = FilterMode.SCENES,
            name = "Studio Scenes",
            findFilter = null,
            objectFilter = mapOf(
                "studios" to FilterCriterion(
                    value = listOf("studio1"),
                    modifier = FilterModifier.INCLUDES
                )
            ),
            uiOptions = null
        )

        // When
        val result = repository.extractFilterIds(filter)

        // Then
        assertEquals(1, result.studioIds.size)
        assertEquals(listOf("studio1"), result.studioIds)
    }

    @Test
    fun `extractFilterIds extracts multiple filter types`() {
        // Given
        val filter = SavedFilter(
            id = "filter1",
            mode = FilterMode.SCENES,
            name = "Complex Filter",
            findFilter = null,
            objectFilter = mapOf(
                "tags" to FilterCriterion(value = listOf("tag1", "tag2")),
                "performers" to FilterCriterion(value = listOf("perf1")),
                "studios" to FilterCriterion(value = listOf("studio1"))
            ),
            uiOptions = null
        )

        // When
        val result = repository.extractFilterIds(filter)

        // Then
        assertEquals(2, result.tagIds.size)
        assertEquals(1, result.performerIds.size)
        assertEquals(1, result.studioIds.size)
    }

    @Test
    fun `extractFilterIds returns empty lists when object_filter is null`() {
        // Given
        val filter = SavedFilter(
            id = "filter1",
            mode = FilterMode.SCENES,
            name = "Simple Filter",
            findFilter = FindFilter(q = "search term"),
            objectFilter = null,
            uiOptions = null
        )

        // When
        val result = repository.extractFilterIds(filter)

        // Then
        assertEquals(0, result.tagIds.size)
        assertEquals(0, result.performerIds.size)
        assertEquals(0, result.studioIds.size)
    }

    @Test
    fun `extractFilterIds returns empty lists when criteria values are null`() {
        // Given
        val filter = SavedFilter(
            id = "filter1",
            mode = FilterMode.SCENES,
            name = "Empty Criteria",
            findFilter = null,
            objectFilter = mapOf(
                "tags" to FilterCriterion(value = null, modifier = FilterModifier.IS_NULL)
            ),
            uiOptions = null
        )

        // When
        val result = repository.extractFilterIds(filter)

        // Then
        assertEquals(0, result.tagIds.size)
    }
}
