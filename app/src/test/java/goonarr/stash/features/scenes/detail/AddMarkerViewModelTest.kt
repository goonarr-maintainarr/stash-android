package goonarr.stash.features.scenes.detail

import androidx.lifecycle.SavedStateHandle
import goonarr.stash.core.model.Scene
import goonarr.stash.core.model.SceneMarker
import goonarr.stash.core.model.Tag
import goonarr.stash.features.scenes.utils.SpriteManager
import goonarr.stash.repositories.TagRepository
import goonarr.stash.repositories.scenes.SceneRepository
import goonarr.stash.util.StashUrlFormatter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class AddMarkerViewModelTest {

    private lateinit var sceneRepository: SceneRepository
    private lateinit var tagRepository: TagRepository
    private lateinit var urlFormatter: StashUrlFormatter
    private lateinit var spriteManager: SpriteManager
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: AddMarkerViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        sceneRepository = mockk(relaxed = true)
        tagRepository = mockk(relaxed = true)
        urlFormatter = mockk(relaxed = true)
        spriteManager = mockk(relaxed = true)
        savedStateHandle = mockk(relaxed = true)

        every { savedStateHandle.get<String>("sceneId") } returns "1"
        every { savedStateHandle.get<String>("initialSeconds") } returns "30.0"

        // viewModel init calls loadScene which is async, but UnconfinedTestDispatcher executes it immediately
    }

    private fun createViewModel() {
        viewModel = AddMarkerViewModel(
            sceneRepository,
            tagRepository,
            urlFormatter,
            spriteManager,
            savedStateHandle
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadScene success updates uiState to Success`() = runTest {
        // Given
        val scene = Scene(id = "1", title = "Test Scene")
        val tags = listOf(mockk<Tag>())
        coEvery { sceneRepository.fetchSceneFromApi("1") } returns scene
        coEvery { tagRepository.getAllTags() } returns tags

        // When
        createViewModel()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is SceneDetailUiState.Success)
        assertEquals(scene, (state as SceneDetailUiState.Success).scene)
        assertEquals(tags, state.allTags)
    }

    @Test
    fun `loadScene failure updates uiState to Error`() = runTest {
        // Given
        coEvery { sceneRepository.fetchSceneFromApi("1") } throws Exception("Network Error")

        // When
        createViewModel()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state is SceneDetailUiState.Error)
        assertEquals("Network Error", (state as SceneDetailUiState.Error).message)
    }

    @Test
    fun `createMarker calls repository and triggers onSuccess`() = runTest {
        // Given
        val scene = Scene(id = "1", title = "Test Scene")
        coEvery { sceneRepository.fetchSceneFromApi("1") } returns scene
        createViewModel()

        var successTriggered = false
        coEvery {
            sceneRepository.createMarker(any(), any(), any(), any(), any(), any())
        } returns mockk<SceneMarker>()

        // When
        viewModel.createMarker(
            title = "Marker 1",
            seconds = 10.0,
            endSeconds = 15.0,
            primaryTagId = "t1",
            additionalTagIds = listOf("t2"),
            onSuccess = { successTriggered = true }
        )

        // Then
        assertTrue(successTriggered)
        coVerify {
            sceneRepository.createMarker(
                sceneId = "1",
                title = "Marker 1",
                seconds = 10.0,
                endSeconds = 15.0,
                primaryTagId = "t1",
                tagIds = listOf("t2")
            )
        }
    }

    @Test
    fun `initialSeconds is correctly parsed from savedStateHandle`() {
        // Given
        every { savedStateHandle.get<String>("initialSeconds") } returns "45.5"

        // When
        createViewModel()

        // Then
        assertEquals(45.5, viewModel.initialSeconds, 0.001)
    }
}
