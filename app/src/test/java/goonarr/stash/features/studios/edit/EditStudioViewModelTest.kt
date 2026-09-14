package goonarr.stash.features.studios.edit

import goonarr.stash.core.model.Studio
import goonarr.stash.core.model.Tag
import goonarr.stash.repositories.StudioRepository
import goonarr.stash.repositories.TagRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditStudioViewModelTest {

    private val studioRepository = mockk<StudioRepository>(relaxed = true)
    private val tagRepository = mockk<TagRepository>(relaxed = true)
    private lateinit var viewModel: EditStudioViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val mockStudio = Studio(
        id = "1",
        name = "Nebula Productions",
        tags = emptyList()
    )

    private val mockTag = Tag(id = "t1", name = "Test Tag")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        coEvery { studioRepository.fetchStudio("1") } returns mockStudio
        viewModel = EditStudioViewModel(studioRepository, tagRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadStudio updates tags state`() = runTest {
        // Given
        val studioWithTags = mockStudio.copy(
            tags = listOf(Tag(id = "t1", name = "Test Tag"))
        )
        coEvery { studioRepository.fetchStudio("1") } returns studioWithTags

        // When
        viewModel.loadStudio("1")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.tags.value.size)
        assertEquals("t1", viewModel.tags.value[0].id)
    }

    @Test
    fun `searchTags updates results state`() = runTest {
        // Given
        val tagResults = listOf(Tag(id = "t1", name = "Test Tag"))
        coEvery { tagRepository.searchTags("test") } returns tagResults

        // When
        viewModel.tagSearchText.value = "test"
        testDispatcher.scheduler.advanceTimeBy(350)
        testDispatcher.scheduler.runCurrent()

        // Then
        assertEquals(tagResults, viewModel.tagSearchResults.value)
        coVerify { tagRepository.searchTags("test") }
    }

    @Test
    fun `addTag adds tag to list`() = runTest {
        // Given
        val tag = Tag(id = "t1", name = "New Tag")

        // When
        viewModel.addTag(tag)

        // Then
        assertTrue(viewModel.tags.value.contains(tag))
    }

    @Test
    fun `removeTag removes tag from list`() = runTest {
        // Given
        val tag1 = Tag(id = "t1", name = "Tag 1")
        viewModel.addTag(tag1)

        // When
        viewModel.removeTag("t1")

        // Then
        assertFalse(viewModel.tags.value.contains(tag1))
    }

    @Test
    fun `save calls studioRepository update with tag ids`() = runTest {
        // Given
        viewModel.loadStudio("1")
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.addTag(mockTag)
        coEvery {
            studioRepository.updateStudio(
                id = any(),
                name = any(),
                urls = any(),
                parentId = any(),
                rating100 = any(),
                favorite = any(),
                details = any(),
                aliases = any(),
                tagIds = any(),
                ignoreAutoTag = any(),
                image = any()
            )
        } returns mockStudio

        // When
        viewModel.save()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify {
            studioRepository.updateStudio(
                id = "1",
                tagIds = listOf("t1"),
                name = any(),
                details = any(),
                rating100 = any(),
                urls = any(),
                parentId = any(),
                favorite = any(),
                ignoreAutoTag = any(),
                aliases = any(),
                image = any()
            )
        }
    }
}
