package goonarr.stash.features.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import org.junit.Assert.assertEquals
import org.junit.Test

class BottomNavScrollConnectionTest {

    @Test
    fun `scroll down hides bottom bar after threshold`() {
        var isVisible: Boolean? = null
        val connection = BottomNavScrollConnection { visible -> isVisible = visible }

        // Scroll down (negative y)
        // Threshold is 750f.

        // 1. Scroll a bit, not enough
        connection.onPreScroll(Offset(0f, -100f), NestedScrollSource.Drag)
        assertEquals(null, isVisible)
        assertEquals(-100f, connection.currentOffset, 0.01f)

        // 2. Scroll more, total -400
        connection.onPreScroll(Offset(0f, -300f), NestedScrollSource.Drag)
        assertEquals(null, isVisible)
        assertEquals(-400f, connection.currentOffset, 0.01f)

        // 3. Scroll past threshold (-750)
        // Current -400. Need -351 more.
        connection.onPreScroll(Offset(0f, -400f), NestedScrollSource.Drag)
        // Total accumulated would be -800, which is < -750
        assertEquals(false, isVisible)
        assertEquals(0f, connection.currentOffset, 0.01f)
    }

    @Test
    fun `scroll up shows bottom bar after threshold`() {
        var isVisible: Boolean? = null
        val connection = BottomNavScrollConnection { visible -> isVisible = visible }

        // Scroll up (positive y)

        // 1. Scroll a bit
        connection.onPreScroll(Offset(0f, 100f), NestedScrollSource.Drag)
        assertEquals(null, isVisible)
        assertEquals(100f, connection.currentOffset, 0.01f)

        // 2. Scroll past threshold (750)
        connection.onPreScroll(Offset(0f, 700f), NestedScrollSource.Drag)
        // Total 800 > 750
        assertEquals(true, isVisible)
        assertEquals(0f, connection.currentOffset, 0.01f)
    }

    @Test
    fun `switching direction resets accumulation`() {
        var isVisible: Boolean? = null
        val connection = BottomNavScrollConnection { visible -> isVisible = visible }

        // 1. Scroll down a bit (-300)
        connection.onPreScroll(Offset(0f, -300f), NestedScrollSource.Drag)
        assertEquals(-300f, connection.currentOffset, 0.01f)

        // 2. Switch to scroll up (+50)
        connection.onPreScroll(Offset(0f, 50f), NestedScrollSource.Drag)
        // Should reset accum to new delta because signs changed
        assertEquals(50f, connection.currentOffset, 0.01f)
        assertEquals(null, isVisible)
    }
}
