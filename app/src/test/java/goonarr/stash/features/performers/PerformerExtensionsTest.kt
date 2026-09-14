package goonarr.stash.features.performers

import org.junit.Assert.assertEquals
import org.junit.Test

class PerformerExtensionsTest {

    @Test
    fun `formatHeight converts cm to feet and inches correctly`() {
        assertEquals("5'8\" (173cm)", formatHeight(173))
        assertEquals("6'0\" (183cm)", formatHeight(183))
        assertEquals("5'0\" (152cm)", formatHeight(152))
    }

    @Test
    fun `formatWeight converts kg to lbs correctly`() {
        assertEquals("121 lbs (55kg)", formatWeight(55))
        assertEquals("154 lbs (70kg)", formatWeight(70))
        assertEquals("220 lbs (100kg)", formatWeight(100))
    }
}
