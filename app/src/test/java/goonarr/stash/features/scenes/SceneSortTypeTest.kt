package goonarr.stash.features.scenes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SceneSortTypeTest {

    @Test
    fun `fromApiValue returns correct type`() {
        assertEquals(SceneSortType.DATE, SceneSortType.fromApiValue("date"))
        assertEquals(SceneSortType.TITLE, SceneSortType.fromApiValue("title"))
        assertEquals(SceneSortType.RATING, SceneSortType.fromApiValue("rating"))
        assertEquals(SceneSortType.CREATED_AT, SceneSortType.fromApiValue("created_at"))
        assertEquals(SceneSortType.O_COUNTER, SceneSortType.fromApiValue("o_counter"))
        assertEquals(SceneSortType.UPDATED_AT, SceneSortType.fromApiValue("updated_at"))
        assertEquals(SceneSortType.RANDOM, SceneSortType.fromApiValue("random"))
    }

    @Test
    fun `fromApiValue returns DATE for unknown value`() {
        assertEquals(SceneSortType.DATE, SceneSortType.fromApiValue("invalid"))
    }

    @Test
    fun `all enum values have mandatory fields`() {
        SceneSortType.entries.forEach { type ->
            assertNotNull("Display name should not be null for ${type.name}", type.displayName)
            assertNotNull("API value should not be null for ${type.name}", type.apiValue)
            val hasIcon = type.icon != null || type.iconRes != null
            assertEquals("Enum ${type.name} should have either icon or iconRes", true, hasIcon)
        }
    }
}
