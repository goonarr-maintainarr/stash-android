package goonarr.stash.features.studios

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class StudioViewTypeTest {

    @Test
    fun `fromString returns correct type`() {
        assertEquals(StudioViewType.GRID, StudioViewType.fromString("GRID"))
        assertEquals(StudioViewType.LIST, StudioViewType.fromString("LIST"))
    }

    @Test
    fun `fromString returns GRID for unknown value`() {
        assertEquals(StudioViewType.GRID, StudioViewType.fromString("INVALID"))
        assertEquals(StudioViewType.GRID, StudioViewType.fromString(""))
    }

    @Test
    fun `all enum values have mandatory fields`() {
        StudioViewType.entries.forEach { type ->
            assertNotNull("Display name should not be null for ${type.name}", type.displayName)
            assertNotNull("Icon should not be null for ${type.name}", type.icon)
        }
    }
}
