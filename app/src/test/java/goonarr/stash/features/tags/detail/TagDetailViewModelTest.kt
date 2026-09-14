package goonarr.stash.features.tags.detail

import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.Scene
import goonarr.stash.core.model.SceneMarker
import goonarr.stash.core.model.Studio
import goonarr.stash.core.model.Tag
import goonarr.stash.features.tags.detail.TagDetailState.Content
import goonarr.stash.repositories.StudioRepository
import goonarr.stash.repositories.TagRepository
import goonarr.stash.repositories.performers.PerformerRepository
import goonarr.stash.repositories.scenes.SceneRepository
import goonarr.stash.util.StashUrlFormatter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TagDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val tagRepository = mockk<TagRepository>() // Not relaxed
    private val sceneRepository = mockk<SceneRepository>(relaxed = true)
    private val performerRepository = mockk<PerformerRepository>(relaxed = true)
    private val studioRepository = mockk<StudioRepository>(relaxed = true)
    private val urlFormatter = mockk<StashUrlFormatter>(relaxed = true)

    private lateinit var viewModel: TagDetailViewModel

    private val mockTagId = "1"
    private val mockTag = Tag(id = mockTagId, name = "Mock Tag")
    private val mockScenes = listOf(Scene(id = "s1", title = "Scene 1"))
    private val mockPerformers = listOf(Performer(id = "p1", name = "Performer 1"))
    private val mockStudios = listOf(Studio(id = "st1", name = "Studio 1"))

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() {
        viewModel = TagDetailViewModel(
            tagRepository = tagRepository,
            sceneRepository = sceneRepository,
            performerRepository = performerRepository,
            studioRepository = studioRepository,
            urlFormatter = urlFormatter
        )
    }

    @Test
    fun `loadTag loads tag, scenes, performers, and studios`() = runTest {
        // Given
        val tagFlow = flowOf(mockTag)
        every { tagRepository.getTag(mockTagId) } returns tagFlow
        coEvery { tagRepository.fetchTag(mockTagId) } returns mockTag
        every { tagRepository.followedTagIds } returns MutableStateFlow(emptyList())
        coEvery { sceneRepository.fetchScenesForTag(mockTagId, 1) } returns Pair(mockScenes, 10)
        coEvery { performerRepository.fetchPerformersForTag(mockTagId, 1) } returns Pair(mockPerformers, 5)
        coEvery { studioRepository.fetchStudiosForTag(mockTagId, 1) } returns Pair(mockStudios, 3)

        createViewModel()

        // When
        viewModel.loadTag(mockTagId)

        // With UnconfinedTestDispatcher, coroutines run immediately, no need to advance

        // Then
        coVerify(exactly = 1) { tagRepository.fetchTag(mockTagId) }

        val state = viewModel.state.value
        assertTrue("State was $state", state is Content)

        val content = state as Content
        assertEquals(mockTag, content.tag)
        assertEquals(emptyList<Scene>(), content.scenes)
        assertEquals(mockPerformers, content.performers)
        assertEquals(mockStudios, content.studios)
        assertEquals(3, content.studioCount)
    }

    @Test
    fun `selectTab loads specific content`() = runTest {
        // Given
        val tagFlow = flowOf(mockTag)
        every { tagRepository.getTag(mockTagId) } returns tagFlow
        coEvery { tagRepository.fetchTag(mockTagId) } returns mockTag
        every { tagRepository.followedTagIds } returns MutableStateFlow(emptyList())
        coEvery { sceneRepository.fetchScenesForTag(mockTagId, 1) } returns Pair(mockScenes, 10)
        coEvery { performerRepository.fetchPerformersForTag(mockTagId, 1) } returns Pair(mockPerformers, 5)
        coEvery { studioRepository.fetchStudiosForTag(mockTagId, 1) } returns Pair(mockStudios, 3)

        createViewModel()
        viewModel.loadTag(mockTagId)

        // Mock marker loading
        val mockMarkers = listOf(SceneMarker(id = "m1", title = "Marker 1", seconds = 10.0))
        coEvery { sceneRepository.fetchSceneMarkersForTag(mockTagId, 1) } returns Pair(mockMarkers, 2)

        // When
        viewModel.selectTab(TagDetailTab.MARKERS)

        // Then
        val state = viewModel.state.value as Content
        assertEquals(TagDetailTab.MARKERS, viewModel.selectedTab.value)
        assertEquals(mockMarkers, state.markers)
    }

    @Test
    fun `loadMorePerformers fetches next page`() = runTest {
        // Given
        val tagFlow = flowOf(mockTag)
        every { tagRepository.getTag(mockTagId) } returns tagFlow
        coEvery { tagRepository.fetchTag(mockTagId) } returns mockTag
        every { tagRepository.followedTagIds } returns MutableStateFlow(emptyList())
        coEvery { sceneRepository.fetchScenesForTag(mockTagId, 1) } returns Pair(mockScenes, 10)
        coEvery { studioRepository.fetchStudiosForTag(mockTagId, 1) } returns Pair(mockStudios, 3)

        // Initial load returns 5 performers, count 10
        val initialPerformers = (1..5).map { Performer(id = "p$it", name = "P$it") }
        coEvery { performerRepository.fetchPerformersForTag(mockTagId, 1) } returns Pair(initialPerformers, 10)

        // Next page
        val nextPerformers = (6..10).map { Performer(id = "p$it", name = "P$it") }
        coEvery { performerRepository.fetchPerformersForTag(mockTagId, 2) } returns Pair(nextPerformers, 10)

        createViewModel()
        viewModel.loadTag(mockTagId)

        // When
        viewModel.loadMorePerformers()

        // Then
        val state = viewModel.state.value as Content
        assertEquals(10, state.performers.size)
        assertEquals("P6", state.performers[5].name)
    }

    @Test
    fun `loadMoreStudios fetches next page`() = runTest {
        // Given
        val tagFlow = flowOf(mockTag)
        every { tagRepository.getTag(mockTagId) } returns tagFlow
        coEvery { tagRepository.fetchTag(mockTagId) } returns mockTag
        every { tagRepository.followedTagIds } returns MutableStateFlow(emptyList())
        coEvery { sceneRepository.fetchScenesForTag(mockTagId, 1) } returns Pair(mockScenes, 10)
        coEvery { performerRepository.fetchPerformersForTag(mockTagId, 1) } returns Pair(mockPerformers, 5)

        // Initial load returns 5 studios, count 10
        val initialStudios = (1..5).map { Studio(id = "s$it", name = "S$it") }
        coEvery { studioRepository.fetchStudiosForTag(mockTagId, 1) } returns Pair(initialStudios, 10)

        // Next page
        val nextStudios = (6..10).map { Studio(id = "s$it", name = "S$it") }
        coEvery { studioRepository.fetchStudiosForTag(mockTagId, 2) } returns Pair(nextStudios, 10)

        createViewModel()
        viewModel.loadTag(mockTagId)

        // When
        viewModel.loadMoreStudios()

        // Then
        val state = viewModel.state.value as Content
        assertEquals(10, state.studios.size)
        assertEquals("S6", state.studios[5].name)
    }
}
