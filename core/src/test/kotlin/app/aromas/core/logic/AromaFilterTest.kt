package app.aromas.core.logic

import app.aromas.core.aroma
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AromaFilterTest {
    private val flowers = aroma(nummer = 1, kategorien = listOf("Blumen & Blüten"), monate = listOf(4, 5))
    private val sea = aroma(nummer = 2, kategorien = listOf("Meer & Küste"), ganzjaehrig = true, monate = emptyList())
    private val both = aroma(nummer = 3, kategorien = listOf("Meer & Küste", "Blumen & Blüten"), monate = listOf(6))

    private val all = listOf(flowers, sea, both)

    @Test
    fun `category filter keeps any matching category`() {
        val result = AromaFilter.filter(all, emptySet(), includeYearRound = true, setOf("Meer & Küste"))
        assertEquals(listOf(2, 3), result.map { it.nummer })
    }

    @Test
    fun `season and category combine with AND`() {
        val result = AromaFilter.filter(all, setOf(6), includeYearRound = false, setOf("Blumen & Blüten"))
        assertEquals(listOf(3), result.map { it.nummer })
    }

    @Test
    fun `sort by distance orders nearest first`() {
        val near = aroma(nummer = 10, lat = 35.0, lon = 135.0)
        val far = aroma(nummer = 11, lat = 43.0, lon = 143.0)
        val sorted = AromaFilter.sortedByDistance(listOf(far, near), 35.0, 135.0)
        assertEquals(listOf(10, 11), sorted.map { it.nummer })
    }
}
