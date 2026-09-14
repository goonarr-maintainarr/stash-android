package goonarr.stash.features.scenes.list

import androidx.paging.PagingData
import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.model.Scene
import goonarr.stash.features.scenes.SceneSortType
import goonarr.stash.repositories.scenes.SceneRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SceneListViewModelTest {

    private val repository = mockk<SceneRepository>(relaxed = true)
    private val settingsStore = mockk<SettingsStore>(relaxed = true)
    private lateinit var viewModel: SceneListViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { settingsStore.sceneSortType } returns flowOf("date")
        every { settingsStore.sceneSortDirection } returns flowOf("DESC")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `scenes flow fetches from repository`() = runTest {
        // Given
        val pagingData = PagingData.from(listOf(Scene(id = "1", title = "Test Scene")))
        every { repository.getPagedScenes(any(), any(), any()) } returns flowOf(pagingData)
        viewModel = SceneListViewModel(repository, settingsStore, testDispatcher)

        // When
        val result = viewModel.scenes.first()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertNotNull(result)
        verify { repository.getPagedScenes("date", "DESC", "") }
    }

    @Test
    fun `totalCount flow exposure`() = runTest {
        // Given
        val totalFlow = kotlinx.coroutines.flow.MutableStateFlow(100)
        every { repository.totalScenesCount } returns totalFlow
        viewModel = SceneListViewModel(repository, settingsStore, testDispatcher)

        // When
        val result = viewModel.totalCount.first()

        // Then
        assertEquals(100, result)
    }

    @Test
    fun `updateSortType updates settings store`() = runTest {
        // Given
        viewModel = SceneListViewModel(repository, settingsStore, testDispatcher)

        // When
        viewModel.updateSortType(SceneSortType.RATING)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { settingsStore.setSceneSort("rating", "DESC") }
    }

    @Test
    fun `updateSortDirection updates settings store`() = runTest {
        // Given
        viewModel = SceneListViewModel(repository, settingsStore, testDispatcher)

        // When
        viewModel.updateSortDirection("ASC")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { settingsStore.setSceneSort("date", "ASC") }
    }

    @Test
    fun `searchQuery initial value is empty`() = runTest {
        // Given
        viewModel = SceneListViewModel(repository, settingsStore, testDispatcher)

        // Then
        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `updateSearchQuery updates search state`() = runTest {
        // Given
        viewModel = SceneListViewModel(repository, settingsStore, testDispatcher)

        // When
        viewModel.updateSearchQuery("test query")

        // Then
        assertEquals("test query", viewModel.searchQuery.value)
    }

    @Test
    fun `search query triggers repository call with query parameter`() = runTest {
        // Given
        val pagingData = PagingData.from(listOf(Scene(id = "1", title = "Test Scene")))
        every { repository.getPagedScenes(any(), any(), any()) } returns flowOf(pagingData)
        viewModel = SceneListViewModel(repository, settingsStore, testDispatcher)

        // When - update search query and wait for debounce
        viewModel.updateSearchQuery("action")
        testDispatcher.scheduler.advanceTimeBy(350) // Wait for 300ms debounce + buffer
        testDispatcher.scheduler.runCurrent()

        // Collect the flow to trigger the flatMapLatest
        viewModel.scenes.first()

        // Then
        verify { repository.getPagedScenes("date", "DESC", "action") }
    }
}
