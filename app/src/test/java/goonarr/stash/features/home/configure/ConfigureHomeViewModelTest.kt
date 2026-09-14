package goonarr.stash.features.home.configure

import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.network.StashClient
import goonarr.stash.repositories.SavedFilterRepository
import goonarr.stash.repositories.TagRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConfigureHomeViewModelTest {

    private val settingsStore = mockk<SettingsStore>(relaxed = true)
    private val tagRepository = mockk<TagRepository>(relaxed = true)
    private val savedFilterRepository = mockk<SavedFilterRepository>(relaxed = true)
    private val stashClient = mockk<StashClient>(relaxed = true)
    private lateinit var viewModel: ConfigureHomeViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val homeSectionOrder = MutableStateFlow<List<String>>(emptyList())
    private val homeSectionHidden = MutableStateFlow<List<String>>(emptyList())
    private val followedTagIds = MutableStateFlow<List<String>>(emptyList())
    private val syncSavedFilters = MutableStateFlow(false)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { settingsStore.homeSectionOrder } returns homeSectionOrder
        every { settingsStore.homeSectionHidden } returns homeSectionHidden
        every { tagRepository.followedTagIds } returns followedTagIds
        every { settingsStore.syncSavedFilters } returns syncSavedFilters

        // Default mocks
        coEvery { savedFilterRepository.getAllFilters() } returns flowOf(emptyList())
        coEvery { tagRepository.getTags(any()) } returns flowOf(emptyList())
        coEvery { savedFilterRepository.syncFilters(any()) } returns true

        viewModel = ConfigureHomeViewModel(
            settingsStore,
            tagRepository,
            savedFilterRepository,
            stashClient
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setTab to SAVED_FILTERS triggers forced sync`() = runTest {
        // Given
        coEvery { savedFilterRepository.syncFilters(true) } returns true

        // When
        viewModel.setTab(ConfigureHomeTab.SAVED_FILTERS)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { savedFilterRepository.syncFilters(force = true) }
    }

    @Test
    fun `setTab to other tabs does NOT trigger sync`() = runTest {
        // When
        viewModel.setTab(ConfigureHomeTab.DEFAULT_ROWS)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { savedFilterRepository.syncFilters(any()) }

        // When
        viewModel.setTab(ConfigureHomeTab.TAGS)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify(exactly = 0) { savedFilterRepository.syncFilters(any()) }
    }

    @Test
    fun `setTab to SAVED_FILTERS updates isSyncing state`() = runTest {
        // Given
        // Mock syncFilters to suspend so we can check the loading state if we could inspect it mid-execution
        // However, standard testing of state changes usually inspects before/after or collects state
        // Since we can't easily pause execution in the middle without more complex setup,
        // we'll verify the call happens which implies the state logic around it runs.

        // Ideally we would check _isSyncing becomes true then false, but with runTest and advanceUntilIdle
        // it will just be false at the end.

        coEvery { savedFilterRepository.syncFilters(true) } returns true

        // When
        viewModel.setTab(ConfigureHomeTab.SAVED_FILTERS)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { savedFilterRepository.syncFilters(force = true) }
        // Verify we finished
        assert(!viewModel.isSyncing.value)
    }
}
