package goonarr.stash.features.performers.edit

import goonarr.stash.core.model.Performer
import goonarr.stash.core.model.Tag
import goonarr.stash.repositories.TagRepository
import goonarr.stash.repositories.performers.PerformerRepository
import io.mockk.coEvery
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditPerformerViewModelTest {

    private val performerRepository = mockk<PerformerRepository>(relaxed = true)
    private val tagRepository = mockk<TagRepository>(relaxed = true)
    private lateinit var viewModel: EditPerformerViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val mockPerformer = Performer(
        id = "1",
        name = "Jane Doe",
        tags = emptyList()
    )

    private val mockTag = Tag(id = "t1", name = "Test Tag")

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { performerRepository.getPerformer("1") } returns flowOf(mockPerformer)
        viewModel = EditPerformerViewModel(performerRepository, tagRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadPerformer updates tags state`() = runTest {
        // Given
        val performerWithTags = mockPerformer.copy(
            tags = listOf(goonarr.stash.core.model.PerformerTag("t1", "Test Tag"))
        )
        every { performerRepository.getPerformer("1") } returns flowOf(performerWithTags)
        coEvery { performerRepository.fetchPerformerFromApi("1") } returns performerWithTags

        // When
        viewModel.loadPerformer("1")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.tags.value.size)
        assertEquals("t1", viewModel.tags.value[0].id)
        assertEquals("Test Tag", viewModel.tags.value[0].name)
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
        assertFalse(viewModel.isSearchingTags.value)
    }

    @Test
    fun `addTag adds tag to list`() = runTest {
        // Given
        val tag = Tag(id = "t1", name = "New Tag")

        // When
        viewModel.addTag(tag)

        // Then
        assertTrue(viewModel.tags.value.contains(tag))
        assertEquals("", viewModel.tagSearchText.value)
        assertEquals(emptyList<Tag>(), viewModel.tagSearchResults.value)
    }

    @Test
    fun `removeTag removes tag from list`() = runTest {
        // Given
        val tag1 = Tag(id = "t1", name = "Tag 1")
        val tag2 = Tag(id = "t2", name = "Tag 2")
        viewModel.addTag(tag1)
        viewModel.addTag(tag2)

        // When
        viewModel.removeTag("t1")

        // Then
        assertFalse(viewModel.tags.value.contains(tag1))
        assertTrue(viewModel.tags.value.contains(tag2))
    }

    @Test
    fun `save calls performerRepository update with tag ids`() = runTest {
        // Given
        coEvery { performerRepository.fetchPerformerFromApi("1") } returns mockPerformer
        viewModel.loadPerformer("1")
        testDispatcher.scheduler.advanceUntilIdle()
        viewModel.addTag(mockTag)
        coEvery {
            performerRepository.updatePerformer(
                id = any(),
                name = any(),
                disambiguation = any(),
                url = any(),
                gender = any(),
                birthdate = any(),
                ethnicity = any(),
                country = any(),
                eyeColor = any(),
                heightCm = any(),
                measurements = any(),
                fakeTits = any(),
                careerLength = any(),
                tattoos = any(),
                piercings = any(),
                aliases = any(),
                details = any(),
                deathDate = any(),
                hairColor = any(),
                weight = any(),
                rating = any(),
                favorite = any(),
                urls = any(),
                penisLength = any(),
                circumcised = any(),
                image = any(),
                tagIds = any(),
                stashIds = any()
            )
        } returns mockPerformer

        // When
        viewModel.save()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify {
            performerRepository.updatePerformer(
                id = "1",
                tagIds = listOf("t1"),
                name = any(),
                disambiguation = any(),
                url = any(),
                gender = any(),
                birthdate = any(),
                ethnicity = any(),
                country = any(),
                eyeColor = any(),
                hairColor = any(),
                heightCm = any(),
                weight = any(),
                measurements = any(),
                fakeTits = any(),
                careerLength = any(),
                tattoos = any(),
                piercings = any(),
                details = any(),
                rating = any(),
                favorite = any(),
                urls = any(),
                aliases = any(),
                deathDate = any(),
                penisLength = any(),
                circumcised = any(),
                image = any(),
                stashIds = any()
            )
        }
    }
}
