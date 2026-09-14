package goonarr.stash.features.performers.detail

import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.Scene
import goonarr.stash.repositories.performers.PerformerRepository
import goonarr.stash.repositories.scenes.SceneRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PerformerDetailViewModelTest {

    private lateinit var viewModel: PerformerDetailViewModel
    private val performerRepository: PerformerRepository = mockk(relaxed = true)
    private val sceneRepository: SceneRepository = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Default mocks for scene repository methods used in loadPerformer
        coEvery { sceneRepository.getScenesForPerformer(any(), any(), any()) } returns emptyList()
        coEvery { sceneRepository.getScenesCountForPerformer(any()) } returns 0

        viewModel = PerformerDetailViewModel(performerRepository, sceneRepository, testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadPerformer shows cached data immediately then updates with full API data and local scenes`() = runTest {
        // Given
        val performerId = "1"
        val cachedPerformer = Performer(id = performerId, name = "Cached Performer")
        val fullApiPerformer = Performer(id = performerId, name = "Full API Performer", country = "USA")
        val scenes = listOf(Scene(id = "s1", title = "Scene 1"))

        coEvery { performerRepository.getPerformer(performerId) } returns flowOf(cachedPerformer)
        coEvery { performerRepository.fetchPerformerFromApi(performerId) } returns fullApiPerformer
        // Now uses local DB instead of API for scenes
        coEvery { sceneRepository.getScenesForPerformer(performerId, any(), any()) } returns scenes
        coEvery { sceneRepository.getScenesCountForPerformer(performerId) } returns 10

        // When
        viewModel.loadPerformer(performerId)

        // Then - initial state is loading
        assertEquals(PerformerDetailUiState.Loading, viewModel.uiState.value)

        // Advance to check cache
        advanceUntilIdle()

        // Should eventually settle on API performer with local scenes
        val state = viewModel.uiState.value
        assertTrue(state is PerformerDetailUiState.Success)
        val successState = state as PerformerDetailUiState.Success

        assertEquals("Full API Performer", successState.performer.name)
        assertEquals(1, successState.scenes.size)
        // All local scenes loaded at once, so hasMoreScenes is false
        assertEquals(false, successState.hasMoreScenes)

        // Verify interactions
        coVerify { performerRepository.getPerformer(performerId) }
        coVerify { performerRepository.fetchPerformerFromApi(performerId) }
        coVerify { sceneRepository.getScenesForPerformer(performerId, any(), any()) }
    }

    @Test
    fun `loadPerformer shows cached data when API fails`() = runTest {
        // Given
        val performerId = "1"
        val cachedPerformer = Performer(id = performerId, name = "Cached Performer")

        coEvery { performerRepository.getPerformer(performerId) } returns flowOf(cachedPerformer)
        coEvery { performerRepository.fetchPerformerFromApi(performerId) } throws Exception("Network Error")
        // Local DB should still work even if API fails
        coEvery { sceneRepository.getScenesForPerformer(performerId, any(), any()) } returns emptyList()
        coEvery { sceneRepository.getScenesCountForPerformer(performerId) } returns 0

        // When
        viewModel.loadPerformer(performerId)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is PerformerDetailUiState.Success)
        assertEquals("Cached Performer", (state as PerformerDetailUiState.Success).performer.name)
    }

    @Test
    fun `loadPerformer shows API data when cache returns null`() = runTest {
        // Given
        val performerId = "1"
        val apiPerformer = Performer(id = performerId, name = "API Performer")

        every { performerRepository.getPerformer(performerId) } returns flowOf(null)
        coEvery { performerRepository.fetchPerformerFromApi(performerId) } returns apiPerformer
        coEvery { sceneRepository.getScenesForPerformer(performerId, any(), any()) } returns emptyList()
        coEvery { sceneRepository.getScenesCountForPerformer(performerId) } returns 0

        // When
        viewModel.loadPerformer(performerId)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is PerformerDetailUiState.Success)
        assertEquals("API Performer", (state as PerformerDetailUiState.Success).performer.name)
    }

    @Test
    fun `loadPerformer emits Error when both cache and API fail`() = runTest {
        // Given
        val performerId = "1"

        coEvery { performerRepository.getPerformer(performerId) } returns flowOf(null)
        coEvery { performerRepository.fetchPerformerFromApi(performerId) } throws Exception("Major Failure")

        // When
        viewModel.loadPerformer(performerId)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is PerformerDetailUiState.Error)
        assertEquals("Major Failure", (state as PerformerDetailUiState.Error).message)
    }

    @Test
    fun `loadPerformer loads all local scenes at once without pagination`() = runTest {
        // Given
        val performerId = "1"
        val performer = Performer(id = performerId, name = "P1")
        val allScenes = listOf(
            Scene(id = "s1", title = "Scene 1"),
            Scene(id = "s2", title = "Scene 2"),
            Scene(id = "s3", title = "Scene 3")
        )

        coEvery { performerRepository.getPerformer(performerId) } returns flowOf(performer)
        coEvery { performerRepository.fetchPerformerFromApi(performerId) } returns performer
        // Local DB returns all scenes at once
        coEvery { sceneRepository.getScenesForPerformer(performerId, any(), any()) } returns allScenes
        coEvery { sceneRepository.getScenesCountForPerformer(performerId) } returns 3

        // When
        viewModel.loadPerformer(performerId)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is PerformerDetailUiState.Success)
        val successState = state as PerformerDetailUiState.Success

        // All scenes loaded at once
        assertEquals(3, successState.scenes.size)
        assertEquals("Scene 1", successState.scenes[0].title)
        assertEquals("Scene 2", successState.scenes[1].title)
        assertEquals("Scene 3", successState.scenes[2].title)

        // No more scenes to load (all loaded from local DB)
        assertEquals(false, successState.hasMoreScenes)

        coVerify { sceneRepository.getScenesForPerformer(performerId, any(), any()) }
    }

    @Test
    fun `deletePerformer updates state to Deleted on success`() = runTest {
        // Given
        val performerId = "1"
        val performer = Performer(id = performerId, name = "P1")
        coEvery { performerRepository.getPerformer(performerId) } returns flowOf(performer)
        coEvery { performerRepository.fetchPerformerFromApi(performerId) } returns performer
        coEvery { performerRepository.deletePerformer(performerId) } returns true

        viewModel.loadPerformer(performerId)
        advanceUntilIdle()

        // When
        viewModel.deletePerformer()
        advanceUntilIdle()

        // Then
        assertEquals(PerformerDetailUiState.Deleted, viewModel.uiState.value)
        coVerify { performerRepository.deletePerformer(performerId) }
    }

    @Test
    fun `deletePerformer updates state to Error on failure`() = runTest {
        // Given
        val performerId = "1"
        val performer = Performer(id = performerId, name = "P1")
        coEvery { performerRepository.getPerformer(performerId) } returns flowOf(performer)
        coEvery { performerRepository.fetchPerformerFromApi(performerId) } returns performer
        coEvery { performerRepository.deletePerformer(performerId) } returns false

        viewModel.loadPerformer(performerId)
        advanceUntilIdle()

        // When
        viewModel.deletePerformer()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is PerformerDetailUiState.Error)
        assertEquals("Failed to delete performer", (state as PerformerDetailUiState.Error).message)
        coVerify { performerRepository.deletePerformer(performerId) }
    }

    @Test
    fun `deletePerformer updates state to Error on exception`() = runTest {
        // Given
        val performerId = "1"
        val performer = Performer(id = performerId, name = "P1")
        coEvery { performerRepository.getPerformer(performerId) } returns flowOf(performer)
        coEvery { performerRepository.fetchPerformerFromApi(performerId) } returns performer
        coEvery { performerRepository.deletePerformer(performerId) } throws Exception("Delete failed")

        viewModel.loadPerformer(performerId)
        advanceUntilIdle()

        // When
        viewModel.deletePerformer()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is PerformerDetailUiState.Error)
        assertEquals("Delete failed", (state as PerformerDetailUiState.Error).message)
        coVerify { performerRepository.deletePerformer(performerId) }
    }
}
