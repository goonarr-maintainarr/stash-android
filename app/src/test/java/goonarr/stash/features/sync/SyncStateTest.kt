package goonarr.stash.features.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SyncStateTest {

    // SyncCategoryProgress Tests

    @Test
    fun `progress returns 0 when total is 0`() {
        val progress = SyncCategoryProgress(total = 0, synced = 0)
        assertEquals(0f, progress.progress, 0.001f)
    }

    @Test
    fun `progress returns correct ratio`() {
        val progress = SyncCategoryProgress(total = 100, synced = 50)
        assertEquals(0.5f, progress.progress, 0.001f)
    }

    @Test
    fun `progress returns 1 when all synced`() {
        val progress = SyncCategoryProgress(total = 100, synced = 100)
        assertEquals(1f, progress.progress, 0.001f)
    }

    @Test
    fun `progress handles partial sync`() {
        val progress = SyncCategoryProgress(total = 350, synced = 175)
        assertEquals(0.5f, progress.progress, 0.001f)
    }

    // SyncUiState Tests

    @Test
    fun `isComplete returns false when no categories complete`() {
        val state = SyncUiState()
        assertFalse(state.isComplete)
    }

    @Test
    fun `isComplete returns false when some categories complete`() {
        val state = SyncUiState(
            studios = SyncCategoryProgress(isComplete = true),
            tags = SyncCategoryProgress(isComplete = true),
            performers = SyncCategoryProgress(isComplete = false),
            scenes = SyncCategoryProgress(isComplete = false)
        )
        assertFalse(state.isComplete)
    }

    @Test
    fun `isComplete returns true when all categories complete`() {
        val state = SyncUiState(
            studios = SyncCategoryProgress(isComplete = true),
            tags = SyncCategoryProgress(isComplete = true),
            performers = SyncCategoryProgress(isComplete = true),
            scenes = SyncCategoryProgress(isComplete = true)
        )
        assertTrue(state.isComplete)
    }

    @Test
    fun `overallProgress returns 0 when no items`() {
        val state = SyncUiState()
        assertEquals(0f, state.overallProgress, 0.001f)
    }

    @Test
    fun `overallProgress returns correct ratio across categories`() {
        val state = SyncUiState(
            studios = SyncCategoryProgress(total = 100, synced = 100),
            tags = SyncCategoryProgress(total = 100, synced = 50),
            performers = SyncCategoryProgress(total = 100, synced = 0),
            scenes = SyncCategoryProgress(total = 100, synced = 0)
        )
        // 150 synced / 400 total = 0.375
        assertEquals(0.375f, state.overallProgress, 0.001f)
    }

    @Test
    fun `overallProgress returns 1 when all synced`() {
        val state = SyncUiState(
            studios = SyncCategoryProgress(total = 100, synced = 100, isComplete = true),
            tags = SyncCategoryProgress(total = 50, synced = 50, isComplete = true),
            performers = SyncCategoryProgress(total = 200, synced = 200, isComplete = true),
            scenes = SyncCategoryProgress(total = 1000, synced = 1000, isComplete = true)
        )
        assertEquals(1f, state.overallProgress, 0.001f)
    }
}
