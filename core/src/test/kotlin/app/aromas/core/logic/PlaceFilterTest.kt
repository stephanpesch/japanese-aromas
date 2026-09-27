package app.aromas.core.logic

import app.aromas.core.model.PlaceCollection
import app.aromas.core.place
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PlaceFilterTest {
    private val flowers = place(number = 1, categories = listOf("Blumen & Blüten"), months = listOf(4, 5))
    private val sea = place(number = 2, categories = listOf("Meer & Küste"), yearRound = true, months = emptyList())
    private val both = place(number = 3, categories = listOf("Meer & Küste", "Blumen & Blüten"), months = listOf(6))

    private val all = listOf(flowers, sea, both)

    @Test
    fun `category filter keeps any matching category`() {
        val result = PlaceFilter.filter(all, emptySet(), includeYearRound = true, setOf("Meer & Küste"))
        assertEquals(listOf(2, 3), result.map { it.number })
    }

    @Test
    fun `season and category combine with AND`() {
        val result = PlaceFilter.filter(all, setOf(6), includeYearRound = false, setOf("Blumen & Blüten"))
        assertEquals(listOf(3), result.map { it.number })
    }

    @Test
    fun `collection filter keeps only the selected collections`() {
        val water =
            place(number = 4, collection = PlaceCollection.WATER, categories = emptyList(), months = emptyList())
        val result =
            PlaceFilter.filter(
                all + water,
                emptySet(),
                includeYearRound = true,
                emptySet(),
                setOf(PlaceCollection.WATER),
            )
        assertEquals(listOf(4), result.map { it.number })
    }

    @Test
    fun `sort by distance orders nearest first`() {
        val near = place(number = 10, lat = 35.0, lon = 135.0)
        val far = place(number = 11, lat = 43.0, lon = 143.0)
        val sorted = PlaceFilter.sortedByDistance(listOf(far, near), 35.0, 135.0)
        assertEquals(listOf(10, 11), sorted.map { it.number })
    }
}
