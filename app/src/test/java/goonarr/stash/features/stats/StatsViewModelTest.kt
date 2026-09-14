package goonarr.stash.features.stats

import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.model.Stats
import goonarr.stash.core.network.StashClient
import io.mockk.coEvery
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    private val client = mockk<StashClient>(relaxed = true)
    private val settingsStore = mockk<SettingsStore>(relaxed = true)

    private lateinit var viewModel: StatsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { settingsStore.serverUrl } returns flowOf("http://localhost:9999")
        every { settingsStore.apiKey } returns flowOf("test-api-key")
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadStats_success_updatesUiStateToSuccess() = runTest {
        val mockStats = Stats(
            sceneCount = 100,
            scenesSize = 1024.0,
            scenesDuration = 3600.0,
            imageCount = 20,
            imagesSize = 500.0,
            galleryCount = 5,
            performerCount = 10,
            studioCount = 2,
            tagCount = 50,
            groupCount = 1,
            movieCount = 1,
            totalOCount = 5,
            totalPlayDuration = 1000.0,
            totalPlayCount = 20,
            scenesPlayed = 10
        )

        coEvery { client.fetchStats(any(), any()) } returns mockStats

        viewModel = StatsViewModel(client, settingsStore, testDispatcher)

        // Advance dispatcher to execute loadStats
        testDispatcher.scheduler.advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assert(currentState is StatsUiState.Success)
        assertEquals(mockStats, (currentState as StatsUiState.Success).stats)
    }

    @Test
    fun loadStats_error_updatesUiStateToError() = runTest {
        coEvery { client.fetchStats(any(), any()) } throws RuntimeException("Network Error")

        viewModel = StatsViewModel(client, settingsStore, testDispatcher)

        testDispatcher.scheduler.advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assert(currentState is StatsUiState.Error)
        assertEquals("Network Error", (currentState as StatsUiState.Error).message)
    }
}
