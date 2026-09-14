package goonarr.stash.features.performers.list

import androidx.paging.PagingData
import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.model.Performer
import goonarr.stash.features.performers.PerformerSortType
import goonarr.stash.repositories.performers.PerformerRepository
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
class PerformerListViewModelTest {

    private val repository = mockk<PerformerRepository>(relaxed = true)
    private val settingsStore = mockk<SettingsStore>(relaxed = true)
    private lateinit var viewModel: PerformerListViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { settingsStore.performerSortType } returns flowOf("name")
        every { settingsStore.performerSortDirection } returns flowOf("ASC")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `performers flow fetches from repository`() = runTest {
        // Given
        val pagingData = PagingData.from(listOf(Performer(id = "1", name = "Performer One")))
        every { repository.getPagedPerformers(any(), any(), any()) } returns flowOf(pagingData)
        viewModel = PerformerListViewModel(repository, settingsStore, testDispatcher)

        // When
        val result = viewModel.performers.first()

        // Then
        assertNotNull(result)
        verify { repository.getPagedPerformers("name", "ASC", "") }
    }

    @Test
    fun `updateSortType updates settings store`() = runTest {
        // Given
        viewModel = PerformerListViewModel(repository, settingsStore, testDispatcher)

        // When
        viewModel.updateSortType(PerformerSortType.SCENE_COUNT)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { settingsStore.setPerformerSort("scene_count", "ASC") }
    }

    @Test
    fun `updateSortDirection updates settings store`() = runTest {
        // Given
        viewModel = PerformerListViewModel(repository, settingsStore, testDispatcher)

        // When
        viewModel.updateSortDirection("DESC")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { settingsStore.setPerformerSort("name", "DESC") }
    }

    @Test
    fun `searchQuery initial value is empty`() = runTest {
        // Given
        viewModel = PerformerListViewModel(repository, settingsStore, testDispatcher)

        // Then
        assertEquals("", viewModel.searchQuery.value)
    }

    @Test
    fun `updateSearchQuery updates search state`() = runTest {
        // Given
        viewModel = PerformerListViewModel(repository, settingsStore, testDispatcher)

        // When
        viewModel.updateSearchQuery("jane")

        // Then
        assertEquals("jane", viewModel.searchQuery.value)
    }

    @Test
    fun `search query triggers repository call with query parameter`() = runTest {
        // Given
        val pagingData = PagingData.from(listOf(Performer(id = "1", name = "Jane Doe")))
        every { repository.getPagedPerformers(any(), any(), any()) } returns flowOf(pagingData)
        viewModel = PerformerListViewModel(repository, settingsStore, testDispatcher)

        // When - update search query and wait for debounce
        viewModel.updateSearchQuery("jane")
        testDispatcher.scheduler.advanceTimeBy(350) // Wait for 300ms debounce + buffer
        testDispatcher.scheduler.runCurrent()

        // Collect the flow to trigger the flatMapLatest
        viewModel.performers.first()

        // Then
        verify { repository.getPagedPerformers("name", "ASC", "jane") }
    }
}
