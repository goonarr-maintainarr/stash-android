package goonarr.stash.core.util

import goonarr.stash.util.DateFormatters
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DateFormattersTest {

    @Test
    fun `formatDateString handles ISO format`() {
        val input = "2024-01-01T12:00:00Z"
        val expected = "Jan 1st, 2024"
        assertEquals(expected, DateFormatters.formatDateString(input))
    }

    @Test
    fun `formatDateString handles YMD format`() {
        val input = "2024-01-02"
        val expected = "Jan 2nd, 2024"
        assertEquals(expected, DateFormatters.formatDateString(input))
    }

    @Test
    fun `formatDateString handles ordinal suffixes`() {
        assertEquals("Jan 1st, 2024", DateFormatters.formatDate(LocalDate.of(2024, 1, 1)))
        assertEquals("Jan 2nd, 2024", DateFormatters.formatDate(LocalDate.of(2024, 1, 2)))
        assertEquals("Jan 3rd, 2024", DateFormatters.formatDate(LocalDate.of(2024, 1, 3)))
        assertEquals("Jan 4th, 2024", DateFormatters.formatDate(LocalDate.of(2024, 1, 4)))
        assertEquals("Jan 21st, 2024", DateFormatters.formatDate(LocalDate.of(2024, 1, 21)))
        assertEquals("Jan 22nd, 2024", DateFormatters.formatDate(LocalDate.of(2024, 1, 22)))
        assertEquals("Jan 23rd, 2024", DateFormatters.formatDate(LocalDate.of(2024, 1, 23)))
    }

    @Test
    fun `calculateAge returns correct age for alive person`() {
        val birthdate = "1990-01-01"
        // We can't easily mock LocalDate.now() in unit tests without extra libraries or refactoring.
        // So we'll test the logic by providing a "deathDate" (end date) which acts as "today" for the calculation logic.
        // Wait, calculateAge uses LocalDate.now() if deathDate is null.
        // Let's rely on the deathDate parameter to test the Period calculation logic deterministically.

        val fixedNow = "2024-01-01"
        val age = DateFormatters.calculateAge(birthdate, fixedNow)
        assertEquals(34, age)
    }

    @Test
    fun `calculateAge returns null for invalid input`() {
        assertNull(DateFormatters.calculateAge(null))
        assertNull(DateFormatters.calculateAge("invalid"))
    }

    @Test
    fun `formatTimestamp returns formatted string`() {
        val input = "2024-01-01T15:30:00Z"
        val expected = "Jan 1, 2024 at 3:30 PM"
        assertEquals(expected, DateFormatters.formatTimestamp(input))
    }

    @Test
    fun `formatDuration returns correct format`() {
        // Less than an hour
        assertEquals("05:00", DateFormatters.formatDuration(300.0)) // 5 mins
        assertEquals("36:08", DateFormatters.formatDuration(2168.0)) // 36m 8s

        // More than an hour
        assertEquals("1:01:01", DateFormatters.formatDuration(3661.0)) // 1h 1m 1s
    }
}
