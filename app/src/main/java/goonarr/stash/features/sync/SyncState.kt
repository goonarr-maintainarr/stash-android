package goonarr.stash.features.sync

data class SyncCategoryProgress(
    val total: Int = 0,
    val synced: Int = 0,
    val isComplete: Boolean = false
) {
    val progress: Float
        get() = if (total > 0) synced.toFloat() / total.toFloat() else 0f
}

data class SyncUiState(
    val isInitialSync: Boolean = false,
    val studios: SyncCategoryProgress = SyncCategoryProgress(),
    val tags: SyncCategoryProgress = SyncCategoryProgress(),
    val performers: SyncCategoryProgress = SyncCategoryProgress(),
    val scenes: SyncCategoryProgress = SyncCategoryProgress()
) {
    val isComplete: Boolean
        get() = studios.isComplete && tags.isComplete && performers.isComplete && scenes.isComplete

    val overallProgress: Float
        get() {
            val totalItems = studios.total + tags.total + performers.total + scenes.total
            val syncedItems = studios.synced + tags.synced + performers.synced + scenes.synced
            return if (totalItems > 0) syncedItems.toFloat() / totalItems.toFloat() else 0f
        }
}
