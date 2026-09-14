package goonarr.stash.core.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class SavedFilterMappingTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `SavedFilterDto parses valid mode strings`() {
        val validModes = listOf(
            "SCENES",
            "PERFORMERS",
            "STUDIOS",
            "TAGS",
            "GALLERIES",
            "IMAGES",
            "SCENE_MARKERS",
            "MOVIES",
            "GROUPS"
        )

        validModes.forEach { mode ->
            val dto = SavedFilterDto(
                id = "test",
                mode = mode,
                name = "Test",
                find_filter = null,
                object_filter = null,
                ui_options = null
            )
            assertEquals(mode, dto.mode)
        }
    }

    @Test
    fun `FindFilterDto contains all required fields`() {
        val dto = FindFilterDto(
            q = "search",
            page = 1,
            per_page = 25,
            sort = "date",
            direction = "DESC"
        )

        assertEquals("search", dto.q)
        assertEquals(1, dto.page)
        assertEquals(25, dto.per_page)
        assertEquals("date", dto.sort)
        assertEquals("DESC", dto.direction)
    }

    @Test
    fun `FindFilterDto has correct default values`() {
        val dto = FindFilterDto()

        assertNull(dto.q)
        assertEquals(1, dto.page)
        assertEquals(25, dto.per_page)
        assertEquals("date", dto.sort)
        assertEquals("DESC", dto.direction)
    }

    @Test
    fun `SavedFilterDto handles null find_filter`() {
        val dto = SavedFilterDto(
            id = "test",
            mode = "SCENES",
            name = "Test",
            find_filter = null,
            object_filter = null,
            ui_options = null
        )

        assertNull(dto.find_filter)
    }

    @Test
    fun `SavedFilterDto handles complex object_filter JSON`() {
        val objectFilterJson = buildJsonObject {
            put(
                "tags",
                buildJsonObject {
                    put("value", Json.encodeToJsonElement(listOf("tag1", "tag2")))
                    put("modifier", "INCLUDES")
                }
            )
            put(
                "performers",
                buildJsonObject {
                    put("value", Json.encodeToJsonElement(listOf("perf1")))
                    put("modifier", "INCLUDES_ALL")
                }
            )
        }

        val dto = SavedFilterDto(
            id = "test",
            mode = "SCENES",
            name = "Test",
            find_filter = null,
            object_filter = objectFilterJson,
            ui_options = null
        )

        assertNotNull(dto.object_filter)
        assertEquals(2, dto.object_filter?.jsonObject?.size)
    }

    @Test
    fun `SavedFilterDto handles complex ui_options JSON`() {
        val uiOptionsJson = buildJsonObject {
            put("displayMode", "grid")
            put("sortBy", "name")
            put("zoom", "100")
        }

        val dto = SavedFilterDto(
            id = "test",
            mode = "SCENES",
            name = "Test",
            find_filter = null,
            object_filter = null,
            ui_options = uiOptionsJson
        )

        assertNotNull(dto.ui_options)
        assertEquals(3, dto.ui_options?.jsonObject?.size)
    }

    @Test
    fun `SavedFilterDto with all fields populated`() {
        val findFilter = FindFilterDto(
            q = "complex search",
            page = 5,
            per_page = 100,
            sort = "updated_at",
            direction = "ASC"
        )

        val objectFilter = buildJsonObject {
            put(
                "tags",
                buildJsonObject {
                    put("value", Json.encodeToJsonElement(listOf("tag1", "tag2", "tag3")))
                    put("modifier", "INCLUDES_ALL")
                    put("depth", 2)
                }
            )
        }

        val uiOptions = buildJsonObject {
            put("displayMode", "list")
        }

        val dto = SavedFilterDto(
            id = "complex-filter-1",
            mode = "SCENES",
            name = "Complex Filter",
            find_filter = findFilter,
            object_filter = objectFilter,
            ui_options = uiOptions
        )

        assertEquals("complex-filter-1", dto.id)
        assertEquals("SCENES", dto.mode)
        assertEquals("Complex Filter", dto.name)
        assertNotNull(dto.find_filter)
        assertNotNull(dto.object_filter)
        assertNotNull(dto.ui_options)
        assertEquals("complex search", dto.find_filter?.q)
        assertEquals(5, dto.find_filter?.page)
    }

    @Test
    fun `SavedFilterDto name can contain special characters`() {
        val specialNames = listOf(
            "Filter with spaces",
            "Filter-with-dashes",
            "Filter_with_underscores",
            "Filter (with parens)",
            "Filter \"with quotes\"",
            "Filter & Symbols!"
        )

        specialNames.forEach { name ->
            val dto = SavedFilterDto(
                id = "test",
                mode = "SCENES",
                name = name,
                find_filter = null,
                object_filter = null,
                ui_options = null
            )
            assertEquals(name, dto.name)
        }
    }

    @Test
    fun `FindFilterDto handles null search query`() {
        val dto = FindFilterDto(q = null)
        assertNull(dto.q)
    }

    @Test
    fun `FindFilterDto handles empty search query`() {
        val dto = FindFilterDto(q = "")
        assertEquals("", dto.q)
    }

    @Test
    fun `FindFilterDto handles various sort values`() {
        val sortValues = listOf(
            "date",
            "created_at",
            "updated_at",
            "name",
            "rating",
            "o_counter",
            "play_duration"
        )

        sortValues.forEach { sort ->
            val dto = FindFilterDto(sort = sort)
            assertEquals(sort, dto.sort)
        }
    }

    @Test
    fun `FindFilterDto handles both direction values`() {
        val ascDto = FindFilterDto(direction = "ASC")
        assertEquals("ASC", ascDto.direction)

        val descDto = FindFilterDto(direction = "DESC")
        assertEquals("DESC", descDto.direction)
    }

    @Test
    fun `FindFilterDto handles various page sizes`() {
        val pageSizes = listOf(10, 25, 50, 100, 250)

        pageSizes.forEach { size ->
            val dto = FindFilterDto(per_page = size)
            assertEquals(size, dto.per_page)
        }
    }
}
