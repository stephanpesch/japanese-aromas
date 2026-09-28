package app.aromas.core.logic

import app.aromas.core.model.Availability
import app.aromas.core.model.Place
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Decides whether a place is available within a time interval — the engine behind
 * every "when" filter (now, soon, today, this week, the next 30 days, a custom
 * date range).
 *
 * A place with structured [Place.availability] windows is available when the
 * interval overlaps any one of them (each window matches independently, on both
 * its day range and its time-of-day range). A place without any window falls back
 * to the coarse month/year-round season data, and is then treated as available
 * all day: that keeps the places whose season is only known to the month honest —
 * day- and time-precision are applied only where they are real (festivals, venue
 * opening hours).
 */
object AvailabilityMatcher {
    /** Whether the place is available at the single instant [now]. */
    fun isAvailableNow(
        place: Place,
        now: LocalDateTime,
    ): Boolean = isAvailableInRange(place, now, now)

    /** Whether the place is available at some instant in the inclusive interval [from]..[to]. */
    fun isAvailableInRange(
        place: Place,
        from: LocalDateTime,
        to: LocalDateTime,
    ): Boolean {
        if (to.isBefore(from)) return false
        val firstDay = from.toLocalDate()
        val lastDay = to.toLocalDate()
        return generateSequence(firstDay) { it.plusDays(1) }
            .takeWhile { !it.isAfter(lastDay) }
            .take(MAX_RANGE_DAYS)
            .any { day ->
                val dayStart = if (day == firstDay) from.toLocalTime() else LocalTime.MIN
                val dayEnd = if (day == lastDay) to.toLocalTime() else LocalTime.MAX
                availableOnDay(place, day, dayStart, dayEnd)
            }
    }

    /** Whether the place is available on [day] at some time in [fromTime]..[toTime]. */
    private fun availableOnDay(
        place: Place,
        day: LocalDate,
        fromTime: LocalTime,
        toTime: LocalTime,
    ): Boolean =
        if (place.availability.isEmpty()) {
            availableThisMonth(place, day.monthValue)
        } else {
            place.availability.any { dateInRange(it, place, day) && timeOverlaps(it, fromTime, toTime) }
        }

    /** Fallback for places with no structured window: month/year-round only. */
    private fun availableThisMonth(
        place: Place,
        month: Int,
    ): Boolean = place.yearRound || place.months.isEmpty() || month in place.months

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

    /** Whether the window's daily opening time range overlaps the query interval [fromTime]..[toTime]. */
    private fun timeOverlaps(
        window: Availability,
        fromTime: LocalTime,
        toTime: LocalTime,
    ): Boolean {
        val open = parseTime(window.fromTime)
        val close = parseTime(window.toTime)
        if (open == null || close == null) return true // unbounded time window
        // A window whose start is after its end wraps midnight (e.g. 22:00–02:00).
        return if (!open.isAfter(close)) {
            !fromTime.isAfter(close) && !toTime.isBefore(open)
        } else {
            !toTime.isBefore(open) || !fromTime.isAfter(close)
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
    private const val MAX_RANGE_DAYS = 400
}
