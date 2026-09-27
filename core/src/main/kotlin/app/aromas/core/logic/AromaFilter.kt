package app.aromas.core.logic

import app.aromas.core.model.Aroma

/** Combines the season and category filters and distance sorting. */
object AromaFilter {
    fun filter(
        all: List<Aroma>,
        selectedMonths: Set<Int>,
        includeYearRound: Boolean,
        selectedCategories: Set<String>,
    ): List<Aroma> =
        all.filter { aroma ->
            val categoryOk =
                selectedCategories.isEmpty() ||
                    aroma.categories.any { it in selectedCategories }
            categoryOk && SeasonMatcher.matches(aroma, selectedMonths, includeYearRound)
        }

    fun sortedByDistance(
        list: List<Aroma>,
        lat: Double,
        lon: Double,
    ): List<Aroma> = list.sortedBy { DistanceCalculator.distanceKm(it, lat, lon) }
}
