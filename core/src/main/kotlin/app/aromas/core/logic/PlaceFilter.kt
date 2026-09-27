package app.aromas.core.logic

import app.aromas.core.model.Place
import app.aromas.core.model.PlaceCollection

/** Combines the category, season and collection filters and distance sorting. */
object PlaceFilter {
    fun filter(
        all: List<Place>,
        selectedMonths: Set<Int>,
        includeYearRound: Boolean,
        selectedCategories: Set<String>,
        selectedCollections: Set<PlaceCollection> = emptySet(),
    ): List<Place> =
        all.filter { place ->
            val collectionOk = selectedCollections.isEmpty() || place.collection in selectedCollections
            val categoryOk =
                selectedCategories.isEmpty() ||
                    place.categories.any { it in selectedCategories }
            collectionOk && categoryOk && SeasonMatcher.matches(place, selectedMonths, includeYearRound)
        }

    fun sortedByDistance(
        list: List<Place>,
        lat: Double,
        lon: Double,
    ): List<Place> = list.sortedBy { DistanceCalculator.distanceKm(it, lat, lon) }
}
