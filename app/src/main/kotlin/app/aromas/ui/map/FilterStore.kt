package app.aromas.ui.map

import app.aromas.core.data.PlaceRepository
import app.aromas.core.logic.AvailabilityMatcher
import app.aromas.core.logic.PlaceFilter
import app.aromas.core.logic.Season
import app.aromas.core.model.Place
import app.aromas.core.model.PlaceCollection
import app.aromas.visited.VisitedStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/** How the visited set narrows the results. */
enum class VisitedMode {
    ALL,
    ONLY_VISITED,
    HIDE_VISITED,
}

/** The active filter: collections, categories (OR), seasons, the "now" toggle and the visited mode. */
data class MapFilter(
    val collections: Set<PlaceCollection> = emptySet(),
    val categories: Set<String> = emptySet(),
    val seasons: Set<Season> = emptySet(),
    val nowOnly: Boolean = false,
    val visited: VisitedMode = VisitedMode.ALL,
)

/**
 * The single source of truth for the place filter, shared across the map, the
 * nearby list and anywhere else that shows places, so a filter toggled on one
 * screen applies everywhere. The derived list is computed by [apply]; each
 * consumer turns it into its own [StateFlow] in its own scope.
 */
@Singleton
class FilterStore
    @Inject
    constructor(
        repository: PlaceRepository,
        visitedStore: VisitedStore,
        private val clock: Clock,
    ) {
        val all: List<Place> = repository.all()

        /** All collections present in the dataset, in first-seen order (for the chips). */
        val collections: List<PlaceCollection> = all.map { it.collection }.distinct()

        private val filterState = MutableStateFlow(MapFilter())
        val filter: StateFlow<MapFilter> = filterState.asStateFlow()

        /** Places the user has marked as visited. */
        val visited: StateFlow<Set<String>> = visitedStore.visited

        fun toggleCollection(collection: PlaceCollection) {
            filterState.update {
                val collections = it.collections.toggle(collection)
                // Drop selected categories the new collection scope no longer offers,
                // so no category chip becomes an unreachable "orphan" that empties results.
                val inScope = categoriesFor(collections).toSet()
                it.copy(collections = collections, categories = it.categories intersect inScope)
            }
        }

        fun toggleCategory(category: String) {
            filterState.update { it.copy(categories = it.categories.toggle(category)) }
        }

        fun toggleSeason(season: Season) {
            filterState.update { it.copy(seasons = it.seasons.toggle(season)) }
        }

        fun toggleNowOnly() {
            filterState.update { it.copy(nowOnly = !it.nowOnly) }
        }

        /** Cycles the visited filter: all -> only visited -> hide visited -> all. */
        fun cycleVisited() {
            filterState.update {
                it.copy(
                    visited =
                        when (it.visited) {
                            VisitedMode.ALL -> VisitedMode.ONLY_VISITED
                            VisitedMode.ONLY_VISITED -> VisitedMode.HIDE_VISITED
                            VisitedMode.HIDE_VISITED -> VisitedMode.ALL
                        },
                )
            }
        }

        fun clear() {
            filterState.value = MapFilter()
        }

        /** Category chips available for the given collection scope (all collections' if empty). */
        fun categoriesFor(collections: Set<PlaceCollection>): List<String> {
            val scope = if (collections.isEmpty()) all else all.filter { it.collection in collections }
            return scope.flatMap { it.categories }.distinct()
        }

        /** Applies [active] and the [visited] set to the dataset, returning the matching places. */
        fun apply(
            active: MapFilter,
            visited: Set<String>,
        ): List<Place> {
            val base =
                PlaceFilter.filter(
                    all = all,
                    selectedMonths = active.seasons.flatMap { it.months }.toSet(),
                    includeYearRound = true,
                    selectedCategories = active.categories,
                    selectedCollections = active.collections,
                )
            val timed =
                if (active.nowOnly) {
                    val now = LocalDateTime.now(clock)
                    base.filter { AvailabilityMatcher.isAvailableNow(it, now) }
                } else {
                    base
                }
            return when (active.visited) {
                VisitedMode.ALL -> timed
                VisitedMode.ONLY_VISITED -> timed.filter { it.id in visited }
                VisitedMode.HIDE_VISITED -> timed.filterNot { it.id in visited }
            }
        }
    }

private fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item
