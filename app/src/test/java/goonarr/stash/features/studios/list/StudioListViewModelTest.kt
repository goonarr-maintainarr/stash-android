package goonarr.stash.features.studios.list

import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.model.Studio
import goonarr.stash.features.studios.StudioSortType
import goonarr.stash.features.studios.StudioViewType
import goonarr.stash.repositories.StudioRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StudioListViewModelTest {

    private val repository = mockk<StudioRepository>(relaxed = true)
    private val settingsStore = mockk<SettingsStore>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: StudioListViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Default settings
        every { settingsStore.studioSortType } returns flowOf("name")
        every { settingsStore.studioSortDirection } returns flowOf("ASC")
        every { settingsStore.studioViewType } returns flowOf("GRID")

        // Mock default repository response
        coEvery { repository.fetchStudios(any(), any(), any(), any()) } returns (emptyList<Studio>() to 0)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading and then Empty if no studios`() = runTest {
        // Given
        viewModel = StudioListViewModel(repository, settingsStore, testDispatcher)

        // Then
        assertTrue(viewModel.state.value is StudioListState.Loading)

        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.state.value is StudioListState.Empty)
    }

    @Test
    fun `state is Content when repository returns studios`() = runTest {
        // Given
        val mockStudios = listOf(Studio(id = "1", name = "Studio 1"))
        coEvery { repository.fetchStudios(any(), any(), any(), any()) } returns (mockStudios to 1)

        // When
        viewModel = StudioListViewModel(repository, settingsStore, testDispatcher)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.state.value is StudioListState.Content)
        val content = viewModel.state.value as StudioListState.Content
        assertEquals(1, content.studios.size)
        assertEquals("Studio 1", content.studios[0].name)
    }

    @Test
    fun `onSearchTextChange triggers refresh`() = runTest {
        // Given
        viewModel = StudioListViewModel(repository, settingsStore, testDispatcher)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.onSearchTextChange("search")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { repository.fetchStudios("search", 1, "name", "ASC") }
    }

    @Test
    fun `updateSortType updates settings store`() = runTest {
        // Given
        viewModel = StudioListViewModel(repository, settingsStore, testDispatcher)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.updateSortType(StudioSortType.SCENE_COUNT)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { settingsStore.setStudioSort("scene_count", "ASC") }
    }

    @Test
    fun `updateViewType updates settings store`() = runTest {
        // Given
        viewModel = StudioListViewModel(repository, settingsStore, testDispatcher)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.updateViewType(StudioViewType.LIST)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { settingsStore.setStudioViewType("LIST") }
    }

    @Test
    fun `loadMore appends studios and increments page`() = runTest {
        // Given
        val page1 = listOf(Studio(id = "1", name = "S1"))
        val page2 = listOf(Studio(id = "2", name = "S2"))
        coEvery { repository.fetchStudios(any(), 1, any(), any()) } returns (page1 to 2)
        coEvery { repository.fetchStudios(any(), 2, any(), any()) } returns (page2 to 2)

        viewModel = StudioListViewModel(repository, settingsStore, testDispatcher)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.loadMore()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val content = viewModel.state.value as StudioListState.Content
        assertEquals(2, content.studios.size)
        assertEquals("S2", content.studios[1].name)
        coVerify { repository.fetchStudios(any(), 2, any(), any()) }
    }

    @Test
    fun `error in repository sets Error state`() = runTest {
        // Given
        coEvery { repository.fetchStudios(any(), any(), any(), any()) } throws RuntimeException("Network Error")

        // When
        viewModel = StudioListViewModel(repository, settingsStore, testDispatcher)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertTrue(viewModel.state.value is StudioListState.Error)
        assertEquals("Network Error", (viewModel.state.value as StudioListState.Error).message)
    }
}
