package goonarr.stash.features.home.discovery

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import goonarr.stash.core.model.Scene
import goonarr.stash.features.scenes.SceneSortType
import goonarr.stash.repositories.scenes.SceneRepository
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
class SceneDiscoveryViewModelTest {

    private val repository = mockk<SceneRepository>(relaxed = true)
    private lateinit var viewModel: SceneDiscoveryViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initializes with values from SavedStateHandle`() = runTest {
        // Given
        val savedStateHandle = SavedStateHandle(
            mapOf(
                "sort" to "rating",
                "direction" to "ASC"
            )
        )
        viewModel = SceneDiscoveryViewModel(repository, savedStateHandle)

        // Then
        assertEquals(SceneSortType.RATING, viewModel.sortType.value)
        assertEquals("ASC", viewModel.sortDirection.value)
    }

    @Test
    fun `scenes flow fetches from repository with initial values`() = runTest {
        // Given
        val savedStateHandle = SavedStateHandle(
            mapOf(
                "sort" to "rating",
                "direction" to "ASC"
            )
        )
        val pagingData = PagingData.from(listOf(Scene(id = "1", title = "Discovery Scene")))
        every { repository.getPagedScenes(any(), any(), any()) } returns flowOf(pagingData)

        viewModel = SceneDiscoveryViewModel(repository, savedStateHandle)

        // When
        val result = viewModel.scenes.first()

        // Then
        assertNotNull(result)
        verify { repository.getPagedScenes("rating", "ASC", "") }
    }

    @Test
    fun `updateSortType does NOT affect settings store (ephemeral)`() = runTest {
        // Given
        val savedStateHandle = SavedStateHandle()
        viewModel = SceneDiscoveryViewModel(repository, savedStateHandle)

        // When
        viewModel.updateSortType(SceneSortType.RATING)

        // Then
        assertEquals(SceneSortType.RATING, viewModel.sortType.value)
        // No interaction with SettingsStore should occur as it's not even a dependency here
    }

    @Test
    fun `search query triggers repository call with debounce`() = runTest {
        // Given
        val savedStateHandle = SavedStateHandle()
        val pagingData = PagingData.from(listOf(Scene(id = "1", title = "Discovery Scene")))
        every { repository.getPagedScenes(any(), any(), any()) } returns flowOf(pagingData)

        viewModel = SceneDiscoveryViewModel(repository, savedStateHandle)

        // When
        viewModel.updateSearchQuery("find me")
        testDispatcher.scheduler.advanceTimeBy(350)
        testDispatcher.scheduler.runCurrent()

        viewModel.scenes.first()

        // Then
        verify { repository.getPagedScenes(any(), any(), "find me") }
    }
}
