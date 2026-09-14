package goonarr.stash.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoField

/**
 * Utility object for date formatting and age calculation.
 *
 * Provides methods to parse various date string formats (ISO, YMD, etc.) and
 * formatting them into user-friendly strings (e.g., "Jan 1st, 2024").
 */
object DateFormatters {

    // Formatters for parsing
    private val isoFormatter = DateTimeFormatter.ISO_DATE_TIME
    private val isoDateOnlyFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val ymdFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val parsingFormatters = listOf(
        ymdFormatter,
        DateTimeFormatter.ofPattern("yyyy-MM"),
        DateTimeFormatter.ofPattern("yyyy")
    )

    // Formatters for display
    private val displayMonthDayFormatter = DateTimeFormatter.ofPattern("MMM d")
    private val displayYearFormatter = DateTimeFormatter.ofPattern("yyyy")
    private val mediumDateShortTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a")

    /**
     * Formats a date string to "MMM dth, yyyy" format with ordinal suffix (e.g., "Aug 8th, 2025")
     * Handles multiple input formats: ISO8601, yyyy-MM-dd, etc.
     *
     * @param input The date string to parse.
     * @return Formatted date string, or the original input if parsing fails.
     */
    fun formatDateString(input: String?): String {
        if (input.isNullOrEmpty()) return ""

        // 1. Try ISO variants first
        try {
            val date = LocalDateTime.parse(input, isoFormatter).toLocalDate()
            return formatDate(date)
        } catch (e: DateTimeParseException) {
            // Continue
        }

        try {
            val date = LocalDate.parse(input, isoDateOnlyFormatter)
            return formatDate(date)
        } catch (e: DateTimeParseException) {
            // Continue
        }

        // 2. Try common patterns
        for (formatter in parsingFormatters) {
            try {
                val accessor = formatter.parse(input)
                val year = if (accessor.isSupported(ChronoField.YEAR)) accessor.get(ChronoField.YEAR) else 0
                val month = if (accessor.isSupported(ChronoField.MONTH_OF_YEAR)) accessor.get(ChronoField.MONTH_OF_YEAR) else 1
                val day = if (accessor.isSupported(ChronoField.DAY_OF_MONTH)) accessor.get(ChronoField.DAY_OF_MONTH) else 1

                if (year != 0) {
                    val date = LocalDate.of(year, month, day)
                    return formatDate(date)
                }
            } catch (e: DateTimeParseException) {
                // Continue
            }
        }

        return input
    }

    /**
     * Formats a LocalDate to "MMM dth, yyyy" format with ordinal suffix (e.g., "Aug 8th, 2025")
     *
     * @param date The LocalDate object.
     * @return Formatted date string.
     */
    fun formatDate(date: LocalDate): String {
        val monthDay = date.format(displayMonthDayFormatter)
        val year = date.format(displayYearFormatter)
        val day = date.dayOfMonth
        val suffix = ordinalSuffix(day)

        return "$monthDay$suffix, $year"
    }

    /**
     * Formats an ISO8601 timestamp to medium date + short time (e.g., "Jan 15, 2024 at 3:45 PM")
     *
     * @param isoString The ISO8601 timestamp string.
     * @return Formatted timestamp string, or original string if parsing fails.
     */
    fun formatTimestamp(isoString: String?): String {
        if (isoString.isNullOrEmpty()) return ""

        try {
            // 1. Try LocalDateTime (no offset)
            try {
                val dateTime = LocalDateTime.parse(isoString, isoFormatter)
                return dateTime.format(mediumDateShortTimeFormatter)
            } catch (e: DateTimeParseException) {
                // Continue
            }

            // 2. Try OffsetDateTime (has offset like +01:00 or Z)
            try {
                val offsetDateTime = OffsetDateTime.parse(isoString)
                return offsetDateTime.format(mediumDateShortTimeFormatter)
            } catch (e: DateTimeParseException) {
                // Continue
            }

            // 3. Try Instant (often what Stash returns/stores)
            try {
                val instant = Instant.parse(isoString)
                val dateTime = LocalDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
                return dateTime.format(mediumDateShortTimeFormatter)
            } catch (e: DateTimeParseException) {
                // Continue
            }

            return isoString
        } catch (e: Exception) {
            // Broadest catch to ensure Preview doesn't crash on invalid data
            return isoString ?: ""
        }
    }

    /**
     * Calculates age from birthdate string (yyyy-MM-dd).
     *
     * @param birthdate The birthdate string.
     * @param deathDate Optional death date string. If provided, age is calculated at death.
     * @return The age in years, or null if birthdate is invalid.
     */
    fun calculateAge(birthdate: String?, deathDate: String? = null): Int? {
        if (birthdate.isNullOrEmpty()) return null

        try {
            val birthDateParsed = LocalDate.parse(birthdate, ymdFormatter)

            val endDate: LocalDate = if (!deathDate.isNullOrEmpty()) {
                try {
                    LocalDate.parse(deathDate, ymdFormatter)
                } catch (e: DateTimeParseException) {
                    LocalDate.now()
                }
            } else {
                LocalDate.now()
            }

            return Period.between(birthDateParsed, endDate).years
        } catch (e: DateTimeParseException) {
            return null
        }
    }

    private fun ordinalSuffix(day: Int): String {
        return when (day) {
            1, 21, 31 -> "st"
            2, 22 -> "nd"
            3, 23 -> "rd"
            else -> "th"
        }
    }

    /**
     * Formats duration in seconds to "H:mm:ss" or "m:ss" or just "0:ss" format.
     */
    fun formatDuration(seconds: Double): String {
        val totalSeconds = seconds.toLong()
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val secs = totalSeconds % 60

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, secs)
        } else {
            String.format("%02d:%02d", minutes, secs)
        }
    }

    /**
     * Formats duration for loop buttons (e.g., "17 second" or "1m 43s").
     */
    fun formatLoopDuration(seconds: Double): String {
        val totalSeconds = kotlin.math.round(seconds).toLong()
        val minutes = totalSeconds / 60
        val secs = totalSeconds % 60

        return if (minutes > 0) {
            if (secs > 0) {
                "${minutes}m ${secs}s"
            } else {
                "${minutes}m"
            }
        } else {
            "${totalSeconds}s"
        }
    }
}
