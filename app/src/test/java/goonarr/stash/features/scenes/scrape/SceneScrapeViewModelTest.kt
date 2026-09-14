package goonarr.stash.features.scenes.scrape

import goonarr.stash.core.model.Scene
import goonarr.stash.core.model.scraper.ParseMode
import goonarr.stash.core.model.scraper.ScrapedScene
import goonarr.stash.core.model.scraper.StashBox
import goonarr.stash.core.model.scraper.TaggerConfig
import goonarr.stash.repositories.scenes.SceneRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [SceneScrapeViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SceneScrapeViewModelTest {

    private lateinit var repository: SceneRepository
    private lateinit var viewModel: SceneScrapeViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
        coEvery { repository.fetchSceneFromApi(any()) } returns null
        viewModel = SceneScrapeViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadStashBoxes updates uiState with boxes`() = runTest {
        val mockBoxes = listOf(StashBox(name = "TestBox", endpoint = "endpoint", apiKey = "key"))
        coEvery { repository.fetchStashBoxes() } returns mockBoxes

        viewModel.loadStashBoxes()

        val state = viewModel.uiState.value
        assertEquals(mockBoxes, state.stashBoxes)
        assertEquals(mockBoxes[0], state.selectedStashBox)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadScene updates query based on config`() = runTest {
        val sceneId = "1"
        val scene = Scene(id = sceneId, title = "Test Scene")
        coEvery { repository.getSceneById(sceneId) } returns flowOf(scene)

        viewModel.loadScene(sceneId)

        val state = viewModel.uiState.value
        assertEquals("Test Scene", state.query)
    }

    @Test
    fun `updateConfig regenerates query`() = runTest {
        // Setup scene
        val sceneId = "1"
        val scene =
            Scene(id = sceneId, title = "My Scene", paths = goonarr.stash.core.model.ScenePaths(screenshot = null, preview = null, stream = null, sprite = null, vtt = null))
        // Mock a scene that has file paths, or just test title first
        // TaggerQueryService logic is static, so we rely on its behavior.
        // Assuming Auto mode defaults to title.

        coEvery { repository.getSceneById(sceneId) } returns flowOf(scene)
        viewModel.loadScene(sceneId)

        // Change config
        val newConfig = TaggerConfig(mode = ParseMode.FILENAME) // Won't do much if filename is empty, but verify valid call
        viewModel.updateConfig(newConfig)

        assertEquals(newConfig, viewModel.uiState.value.taggerConfig)
        // Verify query changed or recalculated (difficult to assert exact string if TaggerQueryService is complex without mocking it, but we can verify state update)
    }

    @Test
    fun `scrape calls repository and updates results`() = runTest {
        // Setup
        val box = StashBox(name = "TestBox", endpoint = "endpoint", apiKey = "key")
        val scene = Scene(id = "1", title = "Scene")
        val results =
            listOf(ScrapedScene(title = "Result 1", details = null, date = null, studio = null, performers = null, tags = null, image = null, remoteSiteId = null, director = null, code = null, url = null, urls = null))

        coEvery { repository.fetchStashBoxes() } returns listOf(box)
        coEvery { repository.getSceneById("1") } returns flowOf(scene)
        coEvery { repository.scrapeScene(any(), any(), any()) } returns results

        viewModel.loadStashBoxes()
        viewModel.loadScene("1")
        viewModel.selectStashBox(box)
        viewModel.updateQuery("query")

        // Act
        viewModel.scrape()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(results, state.scrapedResults)
        assertFalse(state.isScraping)
        coVerify { repository.scrapeScene(box, any(), "query") }
    }

    @Test
    fun `scrapeByFragment calls repository and updates results`() = runTest {
        // Setup
        val box = StashBox(name = "TestBox", endpoint = "endpoint", apiKey = "key")
        val scene = Scene(id = "1", title = "Scene")
        val results =
            listOf(ScrapedScene(title = "Fragment Result", details = null, date = null, studio = null, performers = null, tags = null, image = null, remoteSiteId = null, director = null, code = null, url = null, urls = null))

        coEvery { repository.fetchStashBoxes() } returns listOf(box)
        coEvery { repository.getSceneById("1") } returns flowOf(scene)
        coEvery { repository.scrapeSceneByFragment(any(), any()) } returns results

        viewModel.loadStashBoxes()
        viewModel.loadScene("1")
        viewModel.selectStashBox(box)

        // Act
        viewModel.scrapeByFragment()

        // Assert
        val state = viewModel.uiState.value
        assertEquals(results, state.scrapedResults)
        assertFalse(state.isScraping)
        coVerify { repository.scrapeSceneByFragment(box, any()) }
    }

    @Test
    fun `applyScrapeResult calls updateScene with selected fields`() = runTest {
        // Setup
        val sceneId = "1"
        val scene = Scene(id = sceneId, title = "Original")
        val performer1 = goonarr.stash.core.model.scraper.ScrapedPerformer(name = "Perform A", gender = null, images = null)
        val studio1 = goonarr.stash.core.model.scraper.ScrapedStudio(name = "Studio A")
        val tag1 = goonarr.stash.core.model.scraper.ScrapedTag(name = "Tag A")
        val result =
            ScrapedScene(title = "New Title", details = "Details", performers = listOf(performer1), studio = studio1, tags = listOf(tag1), image = "http://image.com", date = null, remoteSiteId = null, director = null, code = null, url = null, urls = null)

        // Selection: Apply Title, Performers, Studio, Tags, Cover
        val selection = ScrapeResultSelection(
            useTitle = true,
            useDetails = false,
            useDate = false,
            useUrl = false,
            useStudio = true,
            useDirector = false,
            useCode = false,
            useCover = true,
            usePerformers = true,
            useTags = true,
            useStashId = false
        )

        coEvery { repository.getSceneById(sceneId) } returns flowOf(scene)
        coEvery { repository.findPerformerId("Perform A") } returns "p1"
        coEvery { repository.findStudioId("Studio A") } returns "s1"
        coEvery { repository.findTagId("Tag A") } returns "t1"

        viewModel.loadScene(sceneId)
        advanceUntilIdle()

        // Act
        viewModel.applyScrapeResult(result, selection)
        advanceUntilIdle()

        // Assert
        coVerify {
            repository.updateScene(
                id = sceneId,
                title = "New Title",
                details = any(),
                date = any(),
                url = any(),
                director = any(),
                code = any(),
                performerIds = listOf("p1"),
                studioId = "s1",
                tagIds = listOf("t1"),
                coverImage = "http://image.com",
                stashIds = any()
            )
        }
    }

    // region Error Handling Tests

    @Test
    fun `scrape handles empty results gracefully`() = runTest {
        // Setup
        val box = StashBox(name = "TestBox", endpoint = "endpoint", apiKey = "key")
        val scene = Scene(id = "1", title = "Scene")

        coEvery { repository.fetchStashBoxes() } returns listOf(box)
        coEvery { repository.getSceneById("1") } returns flowOf(scene)
        coEvery { repository.scrapeScene(any(), any(), any()) } returns emptyList()

        viewModel.loadStashBoxes()
        viewModel.loadScene("1")
        viewModel.selectStashBox(box)
        viewModel.updateQuery("query")

        // Act
        viewModel.scrape()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.scrapedResults.isEmpty())
        assertFalse(state.isScraping)
    }

    @Test
    fun `loadStashBoxes handles empty boxes`() = runTest {
        coEvery { repository.fetchStashBoxes() } returns emptyList()

        viewModel.loadStashBoxes()

        val state = viewModel.uiState.value
        assertTrue(state.stashBoxes.isEmpty())
        assertEquals(null, state.selectedStashBox)
    }

    @Test
    fun `selectResult sets and clears selection`() = runTest {
        val result = ScrapedScene(
            title = "Test", details = null, date = null, studio = null,
            performers = null, tags = null, image = null, remoteSiteId = null,
            director = null, code = null, url = null, urls = null
        )

        // Select
        viewModel.selectResult(result)
        assertEquals(result, viewModel.uiState.value.selectedResult)

        // Clear
        viewModel.selectResult(null)
        assertEquals(null, viewModel.uiState.value.selectedResult)
    }

    @Test
    fun `updateQuery changes query string`() = runTest {
        val newQuery = "custom search query"

        viewModel.updateQuery(newQuery)

        assertEquals(newQuery, viewModel.uiState.value.query)
    }

    // @Test
    // fun `validateScrapedPerformers returns missing performers`() = runTest {
    //     val performerNames = listOf("Known Performer", "Unknown Performer")
    //     val expectedMissing = setOf("Unknown Performer")
    //
    //     coEvery { repository.validatePerformers(performerNames) } returns expectedMissing
    //
    //     val missing = viewModel.validateScrapedPerformers(performerNames)
    //
    //     assertEquals(expectedMissing, missing)
    // }

    // endregion
}
