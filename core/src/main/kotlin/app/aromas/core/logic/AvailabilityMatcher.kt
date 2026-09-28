package app.aromas.core.logic

import app.aromas.core.model.Availability
import app.aromas.core.model.Place
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Decides whether a place is available "now" — on the current day and at the
 * current time of day.
 *
 * A place with structured [Place.availability] windows is available when the
 * moment falls inside any one of them (each window matches independently). A
 * place without any window falls back to the coarse month/year-round season
 * data, and is then treated as available all day: that keeps the ~81 places
 * whose season is only known to the month honest — day- and time-precision are
 * applied only where they are real (festivals, venue opening hours).
 */
object AvailabilityMatcher {
    fun isAvailableNow(
        place: Place,
        now: LocalDateTime,
    ): Boolean =
        if (place.availability.isEmpty()) {
            availableThisMonth(place, now.monthValue)
        } else {
            place.availability.any { matches(it, place, now) }
        }

    /** Fallback for places with no structured window: month/year-round only. */
    private fun availableThisMonth(
        place: Place,
        month: Int,
    ): Boolean = place.yearRound || place.months.isEmpty() || month in place.months

    private fun matches(
        window: Availability,
        place: Place,
        now: LocalDateTime,
    ): Boolean = dateInRange(window, place, now.toLocalDate()) && timeInRange(window, now.toLocalTime())

    private fun dateInRange(
        window: Availability,
        place: Place,
        today: LocalDate,
    ): Boolean {
        val from = parseMonthDay(window.from)
        val to = parseMonthDay(window.to)
        // A window with no explicit day range (e.g. an opening-hours-only window)
        // still respects the place's season, so a time window never overrides it.
        if (from == null || to == null) return availableThisMonth(place, today.monthValue)
        val day = MonthDay(today.monthValue, today.dayOfMonth)
        // A range whose start is after its end wraps the year end (e.g. Dec–Feb).
        return if (from <= to) day in from..to else day >= from || day <= to
    }

    private fun timeInRange(
        window: Availability,
        now: LocalTime,
    ): Boolean {
        val from = parseTime(window.fromTime)
        val to = parseTime(window.toTime)
        if (from == null || to == null) return true // unbounded time window
        // A window whose start is after its end wraps midnight (e.g. 22:00–02:00).
        return if (!from.isAfter(to)) {
            !now.isBefore(from) && !now.isAfter(to)
        } else {
            !now.isBefore(from) ||
                !now.isAfter(to)
        }
    }

    private fun parseMonthDay(value: String?): MonthDay? {
        val parts = value?.split("-")?.takeIf { it.size == MONTH_DAY_PARTS } ?: return null
        val month = parts[0].toIntOrNull()
        val day = parts[1].toIntOrNull()
        return if (month != null && day != null) MonthDay(month, day) else null
    }

    private fun parseTime(value: String?): LocalTime? {
        val parts = value?.split(":")?.takeIf { it.size == TIME_PARTS } ?: return null
        val hour = parts[0].toIntOrNull()
        val minute = parts[1].toIntOrNull()
        return if (hour != null && minute != null) runCatching { LocalTime.of(hour, minute) }.getOrNull() else null
    }

    /** Month/day as a comparable pair, so date ranges ignore the year. */
    private data class MonthDay(
        val month: Int,
        val day: Int,
    ) : Comparable<MonthDay> {
        override fun compareTo(other: MonthDay): Int =
            if (month != other.month) month - other.month else day - other.day
    }

    private const val MONTH_DAY_PARTS = 2
    private const val TIME_PARTS = 2
}
