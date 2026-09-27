package app.aromas.core.logic

import app.aromas.core.aroma
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AromaFilterTest {
    private val flowers = aroma(number = 1, categories = listOf("Blumen & Blüten"), months = listOf(4, 5))
    private val sea = aroma(number = 2, categories = listOf("Meer & Küste"), yearRound = true, months = emptyList())
    private val both = aroma(number = 3, categories = listOf("Meer & Küste", "Blumen & Blüten"), months = listOf(6))

    private val all = listOf(flowers, sea, both)

    @Test
    fun `category filter keeps any matching category`() {
        val result = AromaFilter.filter(all, emptySet(), includeYearRound = true, setOf("Meer & Küste"))
        assertEquals(listOf(2, 3), result.map { it.number })
    }

    @Test
    fun `season and category combine with AND`() {
        val result = AromaFilter.filter(all, setOf(6), includeYearRound = false, setOf("Blumen & Blüten"))
        assertEquals(listOf(3), result.map { it.number })
    }

    @Test
    fun `sort by distance orders nearest first`() {
        val near = aroma(number = 10, lat = 35.0, lon = 135.0)
        val far = aroma(number = 11, lat = 43.0, lon = 143.0)
        val sorted = AromaFilter.sortedByDistance(listOf(far, near), 35.0, 135.0)
        assertEquals(listOf(10, 11), sorted.map { it.number })
    }
}
