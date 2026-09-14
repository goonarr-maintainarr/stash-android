package goonarr.stash.features.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import goonarr.stash.LocalSetBottomBarVisibility
import timber.log.Timber

@Composable
fun Modifier.hideBottomNavOnScroll(): Modifier {
    val setBottomBarVisibility = LocalSetBottomBarVisibility.current
    val nestedScrollConnection = remember(setBottomBarVisibility) {
        BottomNavScrollConnection(setBottomBarVisibility)
    }
    return this.nestedScroll(nestedScrollConnection)
}

class BottomNavScrollConnection(
    private val setBottomBarVisibility: ((Boolean) -> Unit)?
) : NestedScrollConnection {
    var currentOffset = 0f
    val threshold = 750f // Pixels to scroll before triggering change

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        if (setBottomBarVisibility == null) return Offset.Zero

        val delta = available.y
        // Accumulate scroll distance
        currentOffset += delta

        // Hide on scroll down (negative delta)
        // available.y < 0 means scrolling down (content moving up)
        if (delta < 0) {
            // Reset positive accumulation if we switch direction
            if (currentOffset > 0) currentOffset = delta

            if (currentOffset < -threshold) {
                Timber.d("📜 Scroll down accumulated: $currentOffset < -$threshold. Hiding bottom bar.")
                setBottomBarVisibility.invoke(false)
                // Reset after triggering
                currentOffset = 0f
            }
        } else if (delta > 0) {
            if (currentOffset < 0) currentOffset = delta

            if (currentOffset > threshold) {
                Timber.d("📜 Scroll up accumulated: $currentOffset > $threshold. Showing bottom bar.")
                setBottomBarVisibility.invoke(true)
                // Reset after triggering
                currentOffset = 0f
            }
        }

        return Offset.Zero
    }
}
