package goonarr.stash.core.database

import goonarr.stash.core.model.FilterCriterion
import goonarr.stash.core.model.FilterMode
import goonarr.stash.core.model.FilterModifier
import goonarr.stash.core.model.FindFilter
import goonarr.stash.util.Converters
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SavedFilterConvertersTest {

    private val converters = Converters()

    // MARK: - FilterMode Tests

    @Test
    fun `fromFilterMode converts enum to string`() {
        val result = converters.fromFilterMode(FilterMode.SCENES)
        assertEquals("SCENES", result)
    }

    @Test
    fun `toFilterMode converts string to enum`() {
        val result = converters.toFilterMode("PERFORMERS")
        assertEquals(FilterMode.PERFORMERS, result)
    }

    @Test
    fun `toFilterMode handles invalid string with fallback`() {
        val result = converters.toFilterMode("INVALID_MODE")
        assertEquals(FilterMode.SCENES, result) // Default fallback
    }

    @Test
    fun `toFilterMode handles all valid modes`() {
        assertEquals(FilterMode.SCENES, converters.toFilterMode("SCENES"))
        assertEquals(FilterMode.PERFORMERS, converters.toFilterMode("PERFORMERS"))
        assertEquals(FilterMode.STUDIOS, converters.toFilterMode("STUDIOS"))
        assertEquals(FilterMode.TAGS, converters.toFilterMode("TAGS"))
        assertEquals(FilterMode.GALLERIES, converters.toFilterMode("GALLERIES"))
        assertEquals(FilterMode.IMAGES, converters.toFilterMode("IMAGES"))
        assertEquals(FilterMode.SCENE_MARKERS, converters.toFilterMode("SCENE_MARKERS"))
        assertEquals(FilterMode.MOVIES, converters.toFilterMode("MOVIES"))
        assertEquals(FilterMode.GROUPS, converters.toFilterMode("GROUPS"))
    }

    // MARK: - FindFilter Tests

    @Test
    fun `fromFindFilter serializes FindFilter to JSON`() {
        val findFilter = FindFilter(
            q = "search term",
            page = 2,
            perPage = 50,
            sort = "date",
            direction = "DESC"
        )

        val json = converters.fromFindFilter(findFilter)
        assertNotNull(json)
        assert(json!!.contains("\"q\":\"search term\""))
        assert(json.contains("\"page\":2"))
        assert(json.contains("\"perPage\":50"))
    }

    @Test
    fun `fromFindFilter handles null`() {
        val json = converters.fromFindFilter(null)
        assertNull(json)
    }

    @Test
    fun `toFindFilter deserializes JSON to FindFilter`() {
        val json = """{"q":"test","page":1,"perPage":25,"sort":"name","direction":"ASC"}"""
        val result = converters.toFindFilter(json)

        assertNotNull(result)
        assertEquals("test", result?.q)
        assertEquals(1, result?.page)
        assertEquals(25, result?.perPage)
        assertEquals("name", result?.sort)
        assertEquals("ASC", result?.direction)
    }

    @Test
    fun `toFindFilter handles null string`() {
        val result = converters.toFindFilter(null)
        assertNull(result)
    }

    @Test
    fun `toFindFilter handles invalid JSON`() {
        val result = converters.toFindFilter("{invalid json}")
        assertNull(result)
    }

    @Test
    fun `toFindFilter handles empty string`() {
        val result = converters.toFindFilter("")
        assertNull(result)
    }

    @Test
    fun `FindFilter roundtrip preserves all fields`() {
        val original = FindFilter(
            q = "complex search \"with\" quotes",
            page = 5,
            perPage = 100,
            sort = "updated_at",
            direction = "DESC"
        )

        val json = converters.fromFindFilter(original)
        val result = converters.toFindFilter(json)

        assertEquals(original.q, result?.q)
        assertEquals(original.page, result?.page)
        assertEquals(original.perPage, result?.perPage)
        assertEquals(original.sort, result?.sort)
        assertEquals(original.direction, result?.direction)
    }

    // MARK: - FilterCriterionMap Tests

    @Test
    fun `fromFilterCriterionMap serializes map to JSON`() {
        val map = mapOf(
            "tags" to FilterCriterion(
                value = listOf("tag1", "tag2"),
                modifier = FilterModifier.INCLUDES
            ),
            "performers" to FilterCriterion(
                value = listOf("perf1"),
                modifier = FilterModifier.INCLUDES_ALL
            )
        )

        val json = converters.fromFilterCriterionMap(map)
        assertNotNull(json)
        assert(json!!.contains("\"tags\""))
        assert(json.contains("\"performers\""))
    }

    @Test
    fun `fromFilterCriterionMap handles null`() {
        val json = converters.fromFilterCriterionMap(null)
        assertNull(json)
    }

    @Test
    fun `toFilterCriterionMap deserializes JSON to map`() {
        val json = """
            {
                "tags": {
                    "value": ["tag1", "tag2"],
                    "modifier": "INCLUDES",
                    "depth": null,
                    "excludes": null
                }
            }
        """.trimIndent()

        val result = converters.toFilterCriterionMap(json)
        assertNotNull(result)
        assertEquals(1, result?.size)
        assertEquals(2, result?.get("tags")?.value?.size)
        assertEquals(FilterModifier.INCLUDES, result?.get("tags")?.modifier)
    }

    @Test
    fun `toFilterCriterionMap handles null string`() {
        val result = converters.toFilterCriterionMap(null)
        assertNull(result)
    }

    @Test
    fun `toFilterCriterionMap handles invalid JSON`() {
        val result = converters.toFilterCriterionMap("{invalid}")
        assertNull(result)
    }

    @Test
    fun `toFilterCriterionMap handles empty string`() {
        val result = converters.toFilterCriterionMap("")
        assertNull(result)
    }

    @Test
    fun `FilterCriterionMap roundtrip preserves complex structures`() {
        val original = mapOf(
            "tags" to FilterCriterion(
                value = listOf("tag1", "tag2", "tag3"),
                modifier = FilterModifier.INCLUDES_ALL,
                depth = 2,
                excludes = listOf("excluded1")
            ),
            "performers" to FilterCriterion(
                value = listOf("perf1"),
                modifier = FilterModifier.EXCLUDES,
                depth = null,
                excludes = null
            ),
            "studios" to FilterCriterion(
                value = null,
                modifier = FilterModifier.IS_NULL,
                depth = null,
                excludes = null
            )
        )

        val json = converters.fromFilterCriterionMap(original)
        val result = converters.toFilterCriterionMap(json)

        assertEquals(3, result?.size)

        // Check tags
        assertEquals(3, result?.get("tags")?.value?.size)
        assertEquals(FilterModifier.INCLUDES_ALL, result?.get("tags")?.modifier)
        assertEquals(2, result?.get("tags")?.depth)
        assertEquals(1, result?.get("tags")?.excludes?.size)

        // Check performers
        assertEquals(1, result?.get("performers")?.value?.size)
        assertEquals(FilterModifier.EXCLUDES, result?.get("performers")?.modifier)
        assertNull(result?.get("performers")?.depth)

        // Check studios
        assertNull(result?.get("studios")?.value)
        assertEquals(FilterModifier.IS_NULL, result?.get("studios")?.modifier)
    }

    // MARK: - StringMap Tests

    @Test
    fun `fromStringMap serializes string map to JSON`() {
        val map = mapOf(
            "sortBy" to "name",
            "displayMode" to "grid"
        )

        val json = converters.fromStringMap(map)
        assertNotNull(json)
        assert(json!!.contains("\"sortBy\":\"name\""))
        assert(json.contains("\"displayMode\":\"grid\""))
    }

    @Test
    fun `fromStringMap handles null`() {
        val json = converters.fromStringMap(null)
        assertNull(json)
    }

    @Test
    fun `fromStringMap handles empty map`() {
        val json = converters.fromStringMap(emptyMap())
        assertNotNull(json)
        assertEquals("{}", json)
    }

    @Test
    fun `toStringMap deserializes JSON to string map`() {
        val json = """{"sortBy":"date","displayMode":"list"}"""
        val result = converters.toStringMap(json)

        assertNotNull(result)
        assertEquals(2, result?.size)
        assertEquals("date", result?.get("sortBy"))
        assertEquals("list", result?.get("displayMode"))
    }

    @Test
    fun `toStringMap handles null string`() {
        val result = converters.toStringMap(null)
        assertNull(result)
    }

    @Test
    fun `toStringMap handles invalid JSON`() {
        val result = converters.toStringMap("{not valid}")
        assertNull(result)
    }

    @Test
    fun `toStringMap handles empty string`() {
        val result = converters.toStringMap("")
        assertNull(result)
    }

    @Test
    fun `StringMap roundtrip preserves all key-value pairs`() {
        val original = mapOf(
            "key1" to "value1",
            "key2" to "value2",
            "key3" to "value with spaces and special chars: !@#"
        )

        val json = converters.fromStringMap(original)
        val result = converters.toStringMap(json)

        assertEquals(original, result)
    }

    @Test
    fun `StringMap handles special characters in keys and values`() {
        val original = mapOf(
            "key-with-dashes" to "value with \"quotes\"",
            "key_with_underscores" to "value\nwith\nnewlines"
        )

        val json = converters.fromStringMap(original)
        val result = converters.toStringMap(json)

        assertEquals(2, result?.size)
        assertEquals("value with \"quotes\"", result?.get("key-with-dashes"))
    }
}
