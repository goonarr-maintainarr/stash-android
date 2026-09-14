package goonarr.stash.features.scenes.edit

import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.model.Scene
import goonarr.stash.core.model.SceneMarker
import goonarr.stash.core.model.ScenePaths
import goonarr.stash.core.network.StashClient
import goonarr.stash.repositories.scenes.SceneRepository
import goonarr.stash.util.StashUrlFormatter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditSceneViewModelTest {

    private lateinit var repository: SceneRepository
    private lateinit var client: StashClient
    private lateinit var settingsStore: SettingsStore
    private lateinit var urlFormatter: StashUrlFormatter
    private lateinit var viewModel: EditSceneViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        client = mockk(relaxed = true)
        settingsStore = mockk(relaxed = true)
        urlFormatter = mockk(relaxed = true)

        coEvery { settingsStore.serverUrl } returns flowOf("http://localhost:9999")
        coEvery { settingsStore.apiKey } returns flowOf("apikey")

        viewModel = EditSceneViewModel(repository, client, settingsStore, urlFormatter)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `deleteMarker queues deletion and updates local state only`() = runTest {
        // Given
        val marker = SceneMarker(id = "marker1", title = "Test Marker", seconds = 10.0)
        val scene = Scene(id = "1", title = "Test Scene", sceneMarkers = listOf(marker))

        coEvery { repository.fetchSceneFromApi("1") } returns scene

        viewModel.loadScene("1")
        testDispatcher.scheduler.advanceUntilIdle() // Ensure scene loads

        assertEquals(1, viewModel.currentMarkers.value.size)

        // When
        viewModel.deleteMarker("marker1")

        // Then
        // Local state should be empty
        assertTrue(viewModel.currentMarkers.value.isEmpty())

        // Repository should NOT have been called yet
        coVerify(exactly = 0) { repository.deleteMarker(any(), any()) }
    }

    @Test
    fun `deleteCover queues deletion and updates local state only`() = runTest {
        // Given
        val scene = Scene(id = "1", title = "Test Scene")
        // We can't easily mock the screenshotUrl internal state directly from loadScene unless we mock the path,
        // but let's assume valid scene load.
        // Actually, screenshotUrl is derived from scene.paths.screenshot in loadScene.
        // Let's mock a scene with a screenshot path.
        // We'll trust the ViewModel's optimistic update clears it.

        coEvery { repository.fetchSceneFromApi("1") } returns scene
        viewModel.loadScene("1")
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.deleteCover()

        // Then
        assertNull(viewModel.screenshotUrl.value)

        // Repository should NOT have been updated yet
        // updateScene uses Named Arguments, so we verify specifically that updateScene wasn't called with coverImage=""
        coVerify(exactly = 0) { repository.updateScene(any(), coverImage = any()) }
    }

    @Test
    fun `save executes queued marker deletions`() = runTest {
        // Given
        val marker = SceneMarker(id = "marker1", title = "Test Marker", seconds = 10.0)
        val scene = Scene(id = "1", title = "Test Scene", sceneMarkers = listOf(marker))

        coEvery { repository.fetchSceneFromApi("1") } returns scene
        coEvery { repository.updateScene(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns scene

        viewModel.loadScene("1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteMarker("marker1")

        // When
        viewModel.save()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { repository.deleteMarker("marker1", "1") }
    }

    @Test
    fun `save executes queued cover deletion`() = runTest {
        // Given
        val scene = Scene(id = "1", title = "Test Scene")
        coEvery { repository.fetchSceneFromApi("1") } returns scene
        coEvery { repository.updateScene(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns scene

        viewModel.loadScene("1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteCover()

        // When
        viewModel.save()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify {
            repository.updateScene(
                id = "1",
                title = any(),
                details = any(),
                performerIds = any(),
                tagIds = any(),
                director = any(),
                code = any(),
                url = any(),
                rating = any(),
                coverImage = "",
                stashIds = any()
            )
        }
    }

    @Test
    fun `loadScene loads cached data optimistically`() = runTest {
        // Given
        val cachedScene = Scene(id = "1", title = "Cached Title", paths = ScenePaths(screenshot = "cached_url"))
        val freshScene = Scene(id = "1", title = "Fresh Title", paths = ScenePaths(screenshot = "fresh_url"))

        val apiDeferred = CompletableDeferred<Scene>()

        coEvery { repository.getSceneById("1") } returns flowOf(cachedScene)
        coEvery { repository.fetchSceneFromApi("1") } coAnswers { apiDeferred.await() }

        // When
        viewModel.loadScene("1")

        // Then
        // Let optimistic load launches run
        testDispatcher.scheduler.advanceUntilIdle()

        // Should show cached screenshot
        assertEquals("cached_url", viewModel.screenshotUrl.value)
        // Title should still be empty (not loaded optimistically)
        assertEquals("", viewModel.title.value)

        // Now complete the API fetch
        apiDeferred.complete(freshScene)
        testDispatcher.scheduler.advanceUntilIdle()

        // After full load, should show fresh data
        assertEquals("Fresh Title", viewModel.title.value)
        assertEquals("fresh_url", viewModel.screenshotUrl.value)
    }
}
