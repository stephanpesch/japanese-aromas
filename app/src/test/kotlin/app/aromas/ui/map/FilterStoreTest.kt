package app.aromas.ui.map

import app.aromas.core.data.PlaceRepository
import app.aromas.core.logic.Season
import app.aromas.core.model.Availability
import app.aromas.core.model.Place
import app.aromas.core.model.PlaceCollection
import app.aromas.core.place
import app.aromas.visited.VisitedStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

private class FakeVisitedStore(
    initial: Set<String> = emptySet(),
) : VisitedStore {
    private val state = MutableStateFlow(initial)
    override val visited: StateFlow<Set<String>> = state.asStateFlow()

    override fun toggle(id: String) {
        state.value =
            state.value
                .toMutableSet()
                .apply { if (!add(id)) remove(id) }
                .toSet()
    }
}

// Pinned to mid-July so "now" means month 7.
private val JULY_CLOCK: Clock = Clock.fixed(Instant.parse("2026-07-15T00:00:00Z"), ZoneOffset.UTC)

class FilterStoreTest {
    private val flower = place(number = 1, categories = listOf("Blumen & Blüten"), months = listOf(6, 7, 8))
    private val coast = place(number = 2, categories = listOf("Meer & Küste"), months = listOf(3, 4, 5))
    private val tea = place(number = 3, categories = listOf("Tee"), months = emptyList(), yearRound = true)

    private fun store(
        places: List<Place> = listOf(flower, coast, tea),
        visited: Set<String> = emptySet(),
    ) = FilterStore(PlaceRepository(places), FakeVisitedStore(visited), JULY_CLOCK)

    private fun FilterStore.result(): List<Int> = apply(filter.value, visited.value).map { it.number }

    @Test
    fun `categories are the distinct dataset categories in order`() {
        assertEquals(listOf("Blumen & Blüten", "Meer & Küste", "Tee"), store().categoriesFor(emptySet()))
    }

    @Test
    fun `categories are scoped to the selected collection`() {
        val garden = place(number = 9, collection = PlaceCollection.SCENERY, categories = listOf("Garten"))
        val s = store(listOf(flower, garden))
        assertEquals(listOf("Blumen & Blüten", "Garten"), s.categoriesFor(emptySet()))
        assertEquals(listOf("Garten"), s.categoriesFor(setOf(PlaceCollection.SCENERY)))
    }

    @Test
    fun `no filter shows every place`() {
        assertEquals(listOf(1, 2, 3), store().result())
    }

    @Test
    fun `narrowing to another collection drops out-of-scope selected categories`() {
        val garden = place(number = 9, collection = PlaceCollection.SCENERY, categories = listOf("Garten"))
        val s = store(listOf(flower, garden))
        s.toggleCategory("Blumen & Blüten")
        s.toggleCollection(PlaceCollection.SCENERY)
        assertEquals(emptySet<String>(), s.filter.value.categories)
        assertEquals(setOf(PlaceCollection.SCENERY), s.filter.value.collections)
    }

    @Test
    fun `collection filter keeps only the selected collection`() {
        val scene =
            place(number = 9, collection = PlaceCollection.SCENERY, categories = emptyList(), months = emptyList())
        val s = store(listOf(flower, scene))
        assertEquals(listOf(PlaceCollection.AROMA, PlaceCollection.SCENERY), s.collections)
        s.toggleCollection(PlaceCollection.SCENERY)
        assertEquals(listOf(9), s.result())
    }

    @Test
    fun `category filter keeps only matching categories`() {
        val s = store()
        s.toggleCategory("Meer & Küste")
        assertEquals(listOf(2), s.result())
    }

    @Test
    fun `season filter matches months and always keeps year-round`() {
        val s = store()
        s.toggleSeason(Season.SPRING)
        // coast is a spring aroma; tea is year-round; flower (summer) drops out.
        assertEquals(listOf(2, 3), s.result())
    }

    @Test
    fun `now filter keeps places available this month plus year-round`() {
        val s = store() // clock pinned to July
        s.toggleNowOnly()
        assertEquals(listOf(1, 3), s.result())
    }

    @Test
    fun `now filter respects structured day-range availability`() {
        val nowFestival =
            place(number = 10, months = emptyList(), availability = listOf(Availability("07-10", "07-20")))
        val augustFestival =
            place(number = 11, months = emptyList(), availability = listOf(Availability("08-02", "08-07")))
        val s = store(listOf(nowFestival, augustFestival))
        s.toggleNowOnly()
        assertEquals(listOf(10), s.result())
    }

    @Test
    fun `visited filter cycles all - only - hide`() {
        val s = store(visited = setOf(coast.id))
        assertEquals(VisitedMode.ALL, s.filter.value.visited)
        assertEquals(listOf(1, 2, 3), s.result())
        s.cycleVisited()
        assertEquals(VisitedMode.ONLY_VISITED, s.filter.value.visited)
        assertEquals(listOf(2), s.result())
        s.cycleVisited()
        assertEquals(VisitedMode.HIDE_VISITED, s.filter.value.visited)
        assertEquals(listOf(1, 3), s.result())
        s.cycleVisited()
        assertEquals(VisitedMode.ALL, s.filter.value.visited)
    }

    @Test
    fun `clear removes all active filters`() {
        val s = store()
        s.toggleCategory("Tee")
        s.toggleNowOnly()
        assertEquals(listOf(3), s.result())
        s.clear()
        assertEquals(MapFilter(), s.filter.value)
        assertEquals(listOf(1, 2, 3), s.result())
    }
}
