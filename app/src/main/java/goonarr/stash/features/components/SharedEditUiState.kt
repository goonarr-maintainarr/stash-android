package goonarr.stash.features.components

/**
 * Unified UI state for all Edit screens.
 * Replaces individual EditXUiState interfaces to reduce duplication.
 */
sealed interface SharedEditUiState {
    /** Loading data from the server. */
    data object Loading : SharedEditUiState

    /** Data loaded successfully and ready for editing. */
    data object Success : SharedEditUiState

    /** An error occurred while loading or saving. */
    data class Error(val message: String) : SharedEditUiState

    /** Currently saving changes to the server. */
    data object Saving : SharedEditUiState

    /** Changes saved successfully. */
    data object Saved : SharedEditUiState
}
