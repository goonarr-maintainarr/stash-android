package goonarr.stash.features.home

import goonarr.stash.core.database.SettingsStore
import goonarr.stash.core.model.HomeSectionIds
import goonarr.stash.core.model.Scene
import goonarr.stash.core.model.Tag
import goonarr.stash.features.sync.SyncRepository
import goonarr.stash.repositories.SavedFilterRepository
import goonarr.stash.repositories.StudioRepository
import goonarr.stash.repositories.TagRepository
import goonarr.stash.repositories.performers.PerformerRepository
import goonarr.stash.repositories.scenes.SceneRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class HomeViewModelTest {

    private val sceneRepository = mockk<SceneRepository>(relaxed = true)
    private val performerRepository = mockk<PerformerRepository>(relaxed = true)
    private val studioRepository = mockk<StudioRepository>(relaxed = true)
    private val tagRepository = mockk<TagRepository>(relaxed = true)
    private val savedFilterRepository = mockk<SavedFilterRepository>(relaxed = true)
    private val settingsStore = mockk<SettingsStore>(relaxed = true)
    private val syncRepository = mockk<SyncRepository>(relaxed = true)
    private lateinit var viewModel: HomeViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val homeSectionOrder = MutableStateFlow<List<String>>(emptyList())
    private val homeSectionHidden = MutableStateFlow<List<String>>(emptyList())
    private val followedTagIds = MutableStateFlow<List<String>>(emptyList())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { settingsStore.homeSectionOrder } returns homeSectionOrder
        every { settingsStore.homeSectionHidden } returns homeSectionHidden
        every { settingsStore.followedTagIds } returns followedTagIds
        every { tagRepository.followedTagIds } returns followedTagIds

        coEvery { sceneRepository.getHomeScenes(any(), any(), any()) } returns emptyList()
        coEvery { savedFilterRepository.getAllFilters() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() = runTest {
        // Given
        viewModel = HomeViewModel(
            sceneRepository, performerRepository, studioRepository, tagRepository,
            savedFilterRepository, settingsStore, syncRepository, testDispatcher
        )

        // Then
        assertEquals(HomeUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun `loadHomeData updates state to Success when repository returns data`() = runTest {
        // Given
        val scenes = listOf(Scene(id = "1", title = "Test Scene"))
        coEvery { sceneRepository.getHomeScenes(any(), any(), any()) } returns scenes

        // When
        viewModel = HomeViewModel(
            sceneRepository, performerRepository, studioRepository, tagRepository,
            savedFilterRepository, settingsStore, syncRepository, testDispatcher
        )

        // Start collecting to activate the Eagerly flow
        val collectJob = launch { viewModel.uiState.collect {} }

        testDispatcher.scheduler.advanceTimeBy(1000)
        testDispatcher.scheduler.runCurrent()

        // Then
        val state = viewModel.uiState.value
        assertTrue("State should be Success but was $state", state is HomeUiState.Success)

        collectJob.cancel()
    }

    @Test
    fun `reordering sections does not trigger re-fetch`() = runTest {
        // Given
        val scenes = listOf(Scene(id = "1", title = "Test Scene"))
        coEvery { sceneRepository.getHomeScenes(any(), any(), any()) } returns scenes

        viewModel = HomeViewModel(
            sceneRepository, performerRepository, studioRepository, tagRepository,
            savedFilterRepository, settingsStore, syncRepository, testDispatcher
        )
        val collectJob = launch { viewModel.uiState.collect {} }

        testDispatcher.scheduler.advanceTimeBy(1000)
        testDispatcher.scheduler.runCurrent()

        // Initial fetch happened
        coVerify(exactly = 6) { sceneRepository.getHomeScenes(any(), any(), any()) }

        // When: Change order
        homeSectionOrder.value = listOf(HomeSectionIds.RECENTLY_ADDED, HomeSectionIds.RANDOM)
        testDispatcher.scheduler.runCurrent()

        // Then: uiState reflects new order
        val state = viewModel.uiState.value
        assertTrue("State should still be Success but was $state", state is HomeUiState.Success)
        val successState = state as HomeUiState.Success
        assertEquals(HomeSectionIds.RECENTLY_ADDED, successState.sections[0].id)
        assertEquals(HomeSectionIds.RANDOM, successState.sections[1].id)

        // And: NO additional re-fetch happened (still exactly 6 calls from the beginning)
        coVerify(exactly = 6) { sceneRepository.getHomeScenes(any(), any(), any()) }

        collectJob.cancel()
    }

    @Test
    fun `unhiding a section triggers a fetch`() = runTest {
        // Given: Hide all initially
        homeSectionHidden.value = listOf(
            HomeSectionIds.RANDOM,
            HomeSectionIds.RECENTLY_RELEASED,
            HomeSectionIds.RECENTLY_ADDED,
            HomeSectionIds.TOP_RATED,
            HomeSectionIds.MOST_WATCHED,
            HomeSectionIds.RECENTLY_WATCHED
        )

        viewModel = HomeViewModel(
            sceneRepository, performerRepository, studioRepository, tagRepository,
            savedFilterRepository, settingsStore, syncRepository, testDispatcher
        )
        val collectJob = launch { viewModel.uiState.collect {} }

        testDispatcher.scheduler.advanceTimeBy(1000)
        testDispatcher.scheduler.runCurrent()

        // No fetches should have happened because all were hidden
        coVerify(exactly = 0) { sceneRepository.getHomeScenes(any(), any(), any()) }

        // When: Unhide RANDOM
        homeSectionHidden.value = emptyList() // All visible now
        testDispatcher.scheduler.advanceTimeBy(1000)
        testDispatcher.scheduler.runCurrent()

        // Then: Fetches are triggered
        coVerify(atLeast = 1) { sceneRepository.getHomeScenes("random", any(), any()) }

        collectJob.cancel()
    }

    @Test
    fun `loadHomeData includes tag sections when tags are followed`() = runTest {
        // Given
        val tag1 = Tag(id = "t1", name = "Tag 1")
        val scenesForTag1 = listOf(Scene(id = "s1", title = "Scene for Tag 1"))

        followedTagIds.value = listOf("t1")
        every { tagRepository.getTags(listOf("t1")) } returns flowOf(listOf(tag1))
        coEvery { sceneRepository.getScenesForTag("t1", any()) } returns scenesForTag1

        // When
        viewModel = HomeViewModel(
            sceneRepository, performerRepository, studioRepository, tagRepository,
            savedFilterRepository, settingsStore, syncRepository, testDispatcher
        )
        val collectJob = launch { viewModel.uiState.collect {} }

        testDispatcher.scheduler.advanceTimeBy(1000)
        testDispatcher.scheduler.runCurrent()

        // Then
        val state = viewModel.uiState.value
        assertTrue("State should be Success but was $state", state is HomeUiState.Success)
        val successState = state as HomeUiState.Success

        val tagSectionId = HomeSectionIds.getTagSectionId("t1")
        val tagSection = successState.sections.find { it.id == tagSectionId } as? HomeSectionData.SceneSection

        assertNotNull(tagSection)
        assertEquals("Tag 1", tagSection?.title)
        assertEquals(scenesForTag1, tagSection?.scenes)

        collectJob.cancel()
    }
}
