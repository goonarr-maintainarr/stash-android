package goonarr.stash.data.local

import goonarr.stash.core.model.JobStatus
import goonarr.stash.util.Converters
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun testStringListConversion() {
        val list = listOf("A", "B", "C")
        val json = converters.fromStringList(list)
        assertEquals("[\"A\",\"B\",\"C\"]", json)
        assertEquals(list, converters.toStringList(json))
    }

    @Test
    fun testEmptyStringList() {
        val list = emptyList<String>()
        val json = converters.fromStringList(list)
        assertEquals("[]", json)
        assertEquals(list, converters.toStringList(json))
    }

    @Test
    fun testNullStringList() {
        val json = converters.fromStringList(null)
        assertEquals("[]", json)
        assertEquals(emptyList<String>(), converters.toStringList(json))
    }

    @Test
    fun testIntListConversion() {
        val list = listOf(1, 2, 3)
        val json = converters.fromIntList(list)
        assertEquals("[1,2,3]", json)
        assertEquals(list, converters.toIntList(json))
    }

    @Test
    fun testJobStatusConversion() {
        val status = JobStatus.RUNNING
        val string = converters.fromJobStatus(status)
        assertEquals("RUNNING", string)
        assertEquals(status, converters.toJobStatus(string))
    }

    @Test
    fun testInvalidJobStatusFallback() {
        val invalidStatus = "INVALID_STATUS_XYZ"
        assertEquals(JobStatus.FAILED, converters.toJobStatus(invalidStatus))
    }
}
