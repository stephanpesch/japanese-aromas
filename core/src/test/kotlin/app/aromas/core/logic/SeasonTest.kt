package app.aromas.core.logic

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SeasonTest {
    @Test
    fun `seasons partition the twelve calendar months without overlap`() {
        val all = Season.entries.flatMap { it.months }
        assertEquals(12, all.size)
        assertEquals((1..12).toSet(), all.toSet())
    }

    @Test
    fun `each season maps to its expected months`() {
        assertEquals(setOf(3, 4, 5), Season.SPRING.months)
        assertEquals(setOf(6, 7, 8), Season.SUMMER.months)
        assertEquals(setOf(9, 10, 11), Season.AUTUMN.months)
        assertEquals(setOf(12, 1, 2), Season.WINTER.months)
    }
}
