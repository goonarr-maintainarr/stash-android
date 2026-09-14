package goonarr.stash.features.settings

import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.model.GenerationOptions
import goonarr.stash.core.model.ScanOptions
import goonarr.stash.core.network.StashClient
import goonarr.stash.core.network.stashdb.StashDBClient
import goonarr.stash.core.services.StashSubscriptionService
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val settingsStore = mockk<SettingsStore>(relaxed = true)
    private val client = mockk<StashClient>(relaxed = true)
    private val stashDBClient = mockk<StashDBClient>(relaxed = true)
    private val subscriptionService = mockk<StashSubscriptionService>(relaxed = true)
    private lateinit var viewModel: SettingsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Default mocks for flows
        every { settingsStore.serverUrl } returns flowOf("http://localhost:9999/graphql")
        every { settingsStore.apiKey } returns flowOf("test-api-key")
        every { settingsStore.scanOptions } returns flowOf(ScanOptions())
        every { settingsStore.generationOptions } returns flowOf(GenerationOptions())
        every { settingsStore.hideBottomBarLabels } returns flowOf(false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SettingsViewModel {
        return SettingsViewModel(settingsStore, client, stashDBClient, subscriptionService)
    }

    @Test
    fun `setHideBottomBarLabels with true calls settings store`() = runTest {
        // Given
        viewModel = createViewModel()

        // When
        viewModel.setHideBottomBarLabels(true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { settingsStore.setHideBottomBarLabels(true) }
    }

    @Test
    fun `setHideBottomBarLabels with false calls settings store`() = runTest {
        // Given
        viewModel = createViewModel()

        // When
        viewModel.setHideBottomBarLabels(false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { settingsStore.setHideBottomBarLabels(false) }
    }

    @Test
    fun `setHideBottomBarLabels toggle sequence works correctly`() = runTest {
        // Given
        viewModel = createViewModel()

        // When - toggle on then off
        viewModel.setHideBottomBarLabels(true)
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.setHideBottomBarLabels(false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - both calls were made in order
        coVerify(ordering = io.mockk.Ordering.ORDERED) {
            settingsStore.setHideBottomBarLabels(true)
            settingsStore.setHideBottomBarLabels(false)
        }
    }
}
