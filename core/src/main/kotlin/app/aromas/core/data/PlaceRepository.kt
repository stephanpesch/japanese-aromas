package app.aromas.core.data

import app.aromas.core.model.Place
import app.aromas.core.model.PlaceCollection

/** Read-only in-memory access to the bundled places (all collections). */
class PlaceRepository(
    private val places: List<Place>,
) {
    fun all(): List<Place> = places

    fun byId(id: String): Place? = places.firstOrNull { it.id == id }

    fun byCollection(collection: PlaceCollection): List<Place> = places.filter { it.collection == collection }

    /** Distinct aroma categories across the places, in first-seen order. */
    fun categories(): List<String> = places.flatMap { it.categories }.distinct()
}
