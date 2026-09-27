package app.aromas.core.logic

import app.aromas.core.model.Aroma
import java.time.LocalDate

/** Season logic mirrored from the web app: match by month set, plus year-round. */
object SeasonMatcher {
    private const val MONTHS_PER_YEAR = 12

    /** Months (1-12) touched by the inclusive date range (order preserved). */
    fun monthsInRange(from: LocalDate, to: LocalDate): Set<Int> {
        val start = minOf(from, to)
        val end = maxOf(from, to)
        val months = linkedSetOf<Int>()
        var cursor = start.withDayOfMonth(1)
        while (!cursor.isAfter(end) && months.size < MONTHS_PER_YEAR) {
            months.add(cursor.monthValue)
            cursor = cursor.plusMonths(1)
        }
        return months
    }

    /**
     * An aroma is "in season" when no month filter is set, when it is year-round
     * (and those are included), or when its months intersect the selection.
     */
    fun matches(aroma: Aroma, selectedMonths: Set<Int>, includeYearRound: Boolean): Boolean {
        if (selectedMonths.isEmpty()) return true
        return (includeYearRound && aroma.yearRound) ||
            aroma.months.any { it in selectedMonths }
    }
}
