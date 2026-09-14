package goonarr.stash.features.performers.scrape

import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.scraper.ScrapedPerformer
import goonarr.stash.core.model.scraper.StashBox
import goonarr.stash.repositories.performers.PerformerRepository
import io.mockk.coEvery
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [PerformerScrapeViewModel].
 * Tests the three stages of scraping: search, review selection, and applying results.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PerformerScrapeViewModelTest {

    private val performerRepository = mockk<PerformerRepository>(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: PerformerScrapeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PerformerScrapeViewModel(performerRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadStashBoxes updates UI state with boxes`() = runTest {
        val mockBoxes = listOf(StashBox("1", "Box 1", "http://box1"))
        coEvery { performerRepository.fetchStashBoxes() } returns mockBoxes

        viewModel.loadStashBoxes()
        advanceUntilIdle()

        assertEquals(mockBoxes, viewModel.uiState.value.stashBoxes)
        assertEquals(mockBoxes.first(), viewModel.uiState.value.selectedStashBox)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadPerformer updates UI state with performer data`() = runTest {
        val performerId = "123"
        val mockPerformer = Performer(id = performerId, name = "Test Performer")
        every { performerRepository.getPerformer(performerId) } returns flowOf(mockPerformer)
        coEvery { performerRepository.fetchPerformerFromApi(performerId) } returns mockPerformer

        viewModel.loadPerformer(performerId)
        testDispatcher.scheduler.runCurrent()
        advanceUntilIdle()

        assertEquals(mockPerformer, viewModel.uiState.value.currentPerformer)
        assertEquals("Test Performer", viewModel.uiState.value.query)
    }

    @Test
    fun `scrape updates state with results on success`() = runTest {
        val box = StashBox("1", "Box 1", "http://box1")
        val query = "query"
        val mockResults = listOf(ScrapedPerformer(name = "Scraped"))

        viewModel.selectStashBox(box)
        viewModel.updateQuery(query)

        coEvery { performerRepository.scrapePerformer(box, query) } returns mockResults

        viewModel.scrape()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(mockResults, viewModel.uiState.value.scrapeResults)
    }

    @Test
    fun `scrape updates state with error on failure`() = runTest {
        val box = StashBox("1", "Box 1", "http://box1")
        viewModel.selectStashBox(box)

        coEvery { performerRepository.scrapePerformer(any(), any()) } throws Exception("Scrape Error")

        viewModel.scrape()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Scrape Error", viewModel.uiState.value.error)
    }

    @Test
    fun `selectResult initializes selection correctly`() = runTest {
        val result = ScrapedPerformer(
            name = "Scraped",
            gender = "Female",
            country = "USA"
        )

        viewModel.selectResult(result)

        val selection = viewModel.uiState.value.selection
        assertTrue(selection.useName)
        assertTrue(selection.useGender)
        assertTrue(selection.useCountry)
        assertFalse(selection.useBirthdate) // Not in result
        assertEquals(result, viewModel.uiState.value.selectedResult)
    }

    @Test
    fun `applyScrapeResult calls repository update and updates state on success`() = runTest {
        val current = Performer(id = "1", name = "Original")
        val result = ScrapedPerformer(name = "Scraped")
        val updated = Performer(id = "1", name = "Scraped")

        every { performerRepository.getPerformer(any()) } returns flowOf(current)
        coEvery { performerRepository.fetchPerformerFromApi(any()) } returns current
        coEvery {
            performerRepository.updatePerformer(id = "1", name = "Scraped", any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns updated

        // Setup state
        viewModel.loadPerformer("1")
        advanceUntilIdle()
        viewModel.selectResult(result)

        viewModel.applyScrapeResult()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.applySuccess)
        assertEquals(updated, viewModel.uiState.value.currentPerformer)
    }
}
