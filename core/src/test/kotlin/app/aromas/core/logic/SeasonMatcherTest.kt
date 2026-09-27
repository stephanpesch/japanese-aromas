package app.aromas.core.logic

import app.aromas.core.aroma
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

class SeasonMatcherTest {
    @Test
    fun `no month filter matches everything`() {
        val a = aroma(monate = listOf(6, 7), ganzjaehrig = false)
        assertTrue(SeasonMatcher.matches(a, emptySet(), includeYearRound = true))
    }

    @Test
    fun `overlapping month matches`() {
        val a = aroma(monate = listOf(6, 7, 8))
        assertTrue(SeasonMatcher.matches(a, setOf(5, 6), includeYearRound = false))
    }

    @Test
    fun `disjoint months do not match`() {
        val a = aroma(monate = listOf(6, 7, 8))
        assertFalse(SeasonMatcher.matches(a, setOf(1, 2), includeYearRound = false))
    }

    @Test
    fun `year-round respects the include flag`() {
        val a = aroma(monate = emptyList(), ganzjaehrig = true)
        assertTrue(SeasonMatcher.matches(a, setOf(3), includeYearRound = true))
        assertFalse(SeasonMatcher.matches(a, setOf(3), includeYearRound = false))
    }

    @Test
    fun `months in range covers the span`() {
        val months = SeasonMatcher.monthsInRange(LocalDate.of(2026, 5, 3), LocalDate.of(2026, 6, 12))
        assertEquals(setOf(5, 6), months)
    }

    @Test
    fun `months in range spans the year boundary and accepts reversed args`() {
        val months = SeasonMatcher.monthsInRange(LocalDate.of(2027, 2, 10), LocalDate.of(2026, 12, 20))
        assertEquals(setOf(12, 1, 2), months)
    }
}
