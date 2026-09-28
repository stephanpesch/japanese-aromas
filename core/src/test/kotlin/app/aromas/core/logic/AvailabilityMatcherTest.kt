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
    fun `a time-only opening-hours window still respects the place season`() {
        // A plum garden: open daytime, but the scent is only there in season (Dec-Mar).
        val plumGarden =
            place(
                months = listOf(12, 1, 2, 3),
                yearRound = false,
                availability = listOf(Availability(fromTime = "06:00", toTime = "19:00")),
            )
        assertTrue(AvailabilityMatcher.isAvailableNow(plumGarden, at(2, 15, 10))) // in season, open
        assertFalse(AvailabilityMatcher.isAvailableNow(plumGarden, at(7, 15, 10))) // out of season
        assertFalse(AvailabilityMatcher.isAvailableNow(plumGarden, at(2, 15, 22))) // in season, closed
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

    // --- isAvailableInRange ---

    @Test
    fun `a full-day range includes a venue open at some point that day`() {
        val temple = place(yearRound = true, availability = listOf(Availability(fromTime = "09:00", toTime = "17:00")))
        // "Today" (whole day) overlaps the opening hours, so it is available today...
        assertTrue(AvailabilityMatcher.isAvailableInRange(temple, at(3, 3, 0, 0), at(3, 3, 23, 59)))
        // ...even though the single late-evening instant is not.
        assertFalse(AvailabilityMatcher.isAvailableInRange(temple, at(3, 3, 22, 0), at(3, 3, 22, 0)))
    }

    @Test
    fun `a multi-day range catches a festival falling within it`() {
        val awa = place(months = emptyList(), availability = listOf(Availability(from = "08-12", to = "08-15")))
        assertTrue(AvailabilityMatcher.isAvailableInRange(awa, at(8, 1, 0, 0), at(8, 20, 23, 59)))
        assertFalse(AvailabilityMatcher.isAvailableInRange(awa, at(9, 1, 0, 0), at(9, 30, 23, 59)))
    }

    @Test
    fun `a short soon-range catches a venue that opens later within it`() {
        val temple = place(yearRound = true, availability = listOf(Availability(fromTime = "09:00", toTime = "17:00")))
        // 07:00 looking ahead to 10:00 reaches the 09:00 opening.
        assertTrue(AvailabilityMatcher.isAvailableInRange(temple, at(3, 3, 7, 0), at(3, 3, 10, 0)))
        // 03:00 to 06:00 is still before opening.
        assertFalse(AvailabilityMatcher.isAvailableInRange(temple, at(3, 3, 3, 0), at(3, 3, 6, 0)))
    }

    @Test
    fun `a range respects the season for places without windows`() {
        val summer = place(months = listOf(6, 7, 8), yearRound = false)
        assertTrue(AvailabilityMatcher.isAvailableInRange(summer, at(7, 1, 0, 0), at(7, 7, 23, 59)))
        assertFalse(AvailabilityMatcher.isAvailableInRange(summer, at(11, 1, 0, 0), at(11, 7, 23, 59)))
    }

    @Test
    fun `a reversed interval is never available`() {
        assertFalse(AvailabilityMatcher.isAvailableInRange(place(yearRound = true), at(3, 3, 12, 0), at(3, 3, 11, 0)))
    }
}
