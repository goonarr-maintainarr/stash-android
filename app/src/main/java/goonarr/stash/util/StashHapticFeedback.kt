package goonarr.stash.util

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.staticCompositionLocalOf
import timber.log.Timber

enum class StashHapticFeedbackType {
    Light,
    Medium,
    Heavy,
    Success,
    Error,
    Selection
}

class StashHapticFeedback(private val view: View?) {
    fun perform(type: StashHapticFeedbackType) {
        if (view == null) return
        Timber.d("StashHapticFeedback: Performing $type")
        val feedbackConstant = when (type) {
            StashHapticFeedbackType.Light -> HapticFeedbackConstants.CLOCK_TICK
            StashHapticFeedbackType.Medium -> HapticFeedbackConstants.VIRTUAL_KEY
            StashHapticFeedbackType.Heavy -> HapticFeedbackConstants.LONG_PRESS
            StashHapticFeedbackType.Success -> HapticFeedbackConstants.CONFIRM
            StashHapticFeedbackType.Error -> HapticFeedbackConstants.REJECT
            StashHapticFeedbackType.Selection -> HapticFeedbackConstants.CONTEXT_CLICK
        }
        // Fallback for older APIs if needed, but these are generally safe or ignored
        view.performHapticFeedback(feedbackConstant)
    }
}

val LocalStashHapticFeedback = staticCompositionLocalOf {
    StashHapticFeedback(null)
}
