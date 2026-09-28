package app.aromas.core.logic

import app.aromas.core.model.Availability
import app.aromas.core.place
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class AvailabilityMatcherTest {
    private fun at(
        month: Int,
        day: Int,
        hour: Int = 12,
        minute: Int = 0,
    ) = LocalDateTime.of(2026, month, day, hour, minute)

    // --- month fallback (no structured windows) ---

    @Test
    fun `without windows a year-round place is always available`() {
        val p = place(yearRound = true, months = emptyList())
        assertTrue(AvailabilityMatcher.isAvailableNow(p, at(1, 15, 3)))
    }

    @Test
    fun `without windows a seasonal place follows its months`() {
        val p = place(months = listOf(6, 7, 8), yearRound = false)
        assertTrue(AvailabilityMatcher.isAvailableNow(p, at(7, 1)))
        assertFalse(AvailabilityMatcher.isAvailableNow(p, at(11, 1)))
    }

    @Test
    fun `without windows and without any season data a place stays available`() {
        val p = place(months = emptyList(), yearRound = false)
        assertTrue(AvailabilityMatcher.isAvailableNow(p, at(3, 3)))
    }

    // --- day range ---

    @Test
    fun `a festival day range matches only within its days`() {
        val awaOdori = place(availability = listOf(Availability(from = "08-12", to = "08-15")))
        assertTrue(AvailabilityMatcher.isAvailableNow(awaOdori, at(8, 13)))
        assertFalse(AvailabilityMatcher.isAvailableNow(awaOdori, at(8, 16)))
        assertFalse(AvailabilityMatcher.isAvailableNow(awaOdori, at(7, 30)))
    }

    @Test
    fun `a winter day range wraps the year boundary`() {
        val cranes = place(availability = listOf(Availability(from = "12-01", to = "02-28")))
        assertTrue(AvailabilityMatcher.isAvailableNow(cranes, at(1, 15)))
        assertTrue(AvailabilityMatcher.isAvailableNow(cranes, at(12, 20)))
        assertFalse(AvailabilityMatcher.isAvailableNow(cranes, at(6, 1)))
    }

    // --- time window ---

    @Test
    fun `an opening-hours window excludes times outside it`() {
        val temple = place(yearRound = true, availability = listOf(Availability(fromTime = "09:00", toTime = "17:00")))
        assertTrue(AvailabilityMatcher.isAvailableNow(temple, at(3, 3, 10)))
        assertFalse(AvailabilityMatcher.isAvailableNow(temple, at(3, 3, 22)))
    }

    @Test
    fun `a night window wraps midnight`() {
        val night = place(yearRound = true, availability = listOf(Availability(fromTime = "22:00", toTime = "02:00")))
        assertTrue(AvailabilityMatcher.isAvailableNow(night, at(3, 3, 23)))
        assertTrue(AvailabilityMatcher.isAvailableNow(night, at(3, 3, 1)))
        assertFalse(AvailabilityMatcher.isAvailableNow(night, at(3, 3, 12)))
    }

    @Test
    fun `a festival with date and time needs both to match`() {
        val nebuta =
            place(
                availability = listOf(Availability(from = "08-02", to = "08-07", fromTime = "19:00", toTime = "21:00")),
            )
        assertTrue(AvailabilityMatcher.isAvailableNow(nebuta, at(8, 3, 20)))
        assertFalse(AvailabilityMatcher.isAvailableNow(nebuta, at(8, 3, 12))) // right day, wrong time
        assertFalse(AvailabilityMatcher.isAvailableNow(nebuta, at(9, 3, 20))) // right time, wrong day
    }

    @Test
    fun `any one of several windows is enough`() {
        val p =
            place(
                months = emptyList(),
                availability =
                    listOf(
                        Availability(from = "01-01", to = "01-31"),
                        Availability(from = "08-01", to = "08-31"),
                    ),
            )
        assertTrue(AvailabilityMatcher.isAvailableNow(p, at(8, 15)))
        assertFalse(AvailabilityMatcher.isAvailableNow(p, at(5, 15)))
    }

    @Test
    fun `a malformed window is treated as unbounded`() {
        val p = place(months = emptyList(), availability = listOf(Availability(from = "bogus", to = "08-31")))
        assertTrue(AvailabilityMatcher.isAvailableNow(p, at(2, 2)))
    }
}
