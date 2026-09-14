package goonarr.stash.features.tags

import goonarr.stash.core.model.Tag
import goonarr.stash.features.tags.list.TagListViewModel
import goonarr.stash.repositories.TagRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
class TagListViewModelTest {

    private val tagRepository = mockk<TagRepository>(relaxed = true)
    private lateinit var viewModel: TagListViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val mockTags = listOf(
        Tag(id = "t1", name = "Action"),
        Tag(id = "t2", name = "Drama"),
        Tag(id = "t3", name = "Comedy"),
        Tag(id = "t4", name = "Documentary")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { tagRepository.followedTagIds } returns flowOf(emptyList())
        every { tagRepository.getTags(any<List<String>>()) } returns flowOf(emptyList())
        coEvery { tagRepository.getAllTags() } returns mockTags
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): TagListViewModel {
        return TagListViewModel(tagRepository).also {
            testDispatcher.scheduler.advanceUntilIdle()
        }
    }

    @Test
    fun `init loads all tags from repository`() = runTest {
        // When
        viewModel = createViewModel()

        // Then
        coVerify { tagRepository.getAllTags() }
    }

    @Test
    fun `isLoading is true initially then false after load`() = runTest {
        // Given
        viewModel = TagListViewModel(tagRepository)

        // Initially loading
        assertTrue(viewModel.isLoading.value)

        // After load completes
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `onSearchQueryChanged updates searchQuery state`() = runTest {
        // Given
        viewModel = createViewModel()

        // When
        viewModel.onSearchQueryChanged("test query")

        // Then
        assertEquals("test query", viewModel.searchQuery.value)
    }

    @Test
    fun `toggleFollow adds tag when not followed`() = runTest {
        // Given
        every { tagRepository.followedTagIds } returns flowOf(emptyList())
        viewModel = createViewModel()
        val tag = Tag(id = "t1", name = "Test Tag")

        // When
        viewModel.toggleFollow(tag)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { tagRepository.addFollowedTag("t1") }
        coVerify(exactly = 0) { tagRepository.removeFollowedTag(any()) }
    }

    @Test
    fun `toggleFollow removes tag when already followed`() = runTest {
        // Given
        every { tagRepository.followedTagIds } returns flowOf(listOf("t1"))
        viewModel = createViewModel()
        val tag = Tag(id = "t1", name = "Test Tag")

        // When
        viewModel.toggleFollow(tag)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { tagRepository.removeFollowedTag("t1") }
        coVerify(exactly = 0) { tagRepository.addFollowedTag(any()) }
    }

    @Test
    fun `isFollowed returns true when tag is followed`() = runTest {
        // Given
        every { tagRepository.followedTagIds } returns flowOf(listOf("t1", "t2"))
        viewModel = createViewModel()

        // When
        val isFollowed = viewModel.isFollowed("t1").first()

        // Then
        assertTrue(isFollowed)
    }

    @Test
    fun `isFollowed returns false when tag is not followed`() = runTest {
        // Given
        every { tagRepository.followedTagIds } returns flowOf(listOf("t2", "t3"))
        viewModel = createViewModel()

        // When
        val isFollowed = viewModel.isFollowed("t1").first()

        // Then
        assertFalse(isFollowed)
    }

    @Test
    fun `followedTagIds exposes repository flow`() = runTest {
        // Given
        val expectedIds = listOf("t1", "t2", "t3")
        every { tagRepository.followedTagIds } returns flowOf(expectedIds)
        viewModel = createViewModel()

        // When
        val ids = viewModel.followedTagIds.first()

        // Then
        assertEquals(expectedIds, ids)
    }

    @Test
    fun `error during load does not crash and sets loading false`() = runTest {
        // Given
        coEvery { tagRepository.getAllTags() } throws Exception("Network error")

        // When
        viewModel = TagListViewModel(tagRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - no crash and loading is complete
        assertFalse(viewModel.isLoading.value)
    }
}
