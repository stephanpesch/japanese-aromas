package app.aromas.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.aromas.core.data.PlaceRepository
import app.aromas.core.logic.PlaceFilter
import app.aromas.core.logic.Season
import app.aromas.core.model.Place
import app.aromas.core.model.PlaceCollection
import app.aromas.core.model.UserLocation
import app.aromas.location.LocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** The active map filter: selected collections, categories (OR) and seasons. */
data class MapFilter(
    val collections: Set<PlaceCollection> = emptySet(),
    val categories: Set<String> = emptySet(),
    val seasons: Set<Season> = emptySet(),
)

@HiltViewModel
class MapViewModel
    @Inject
    constructor(
        repository: PlaceRepository,
        private val locationProvider: LocationProvider,
    ) : ViewModel() {
        private val all = repository.all()

        /** The user's current location, for the "locate me" button; null if unavailable. */
        suspend fun currentLocation(): UserLocation? = locationProvider.currentLocation()

        /** All collections present in the dataset, in first-seen order (for the chips). */
        val collections: List<PlaceCollection> = all.map { it.collection }.distinct()

        private val filterState = MutableStateFlow(MapFilter())
        val filter: StateFlow<MapFilter> = filterState.asStateFlow()

        /** Category chips scoped to the selected collections (all collections' if none). */
        val categories: StateFlow<List<String>> =
            filterState
                .map { active -> categoriesFor(active.collections) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), categoriesFor(emptySet()))

        val filtered: StateFlow<List<Place>> =
            filterState
                .map { active -> apply(active) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), all)

        fun toggleCollection(collection: PlaceCollection) {
            filterState.update {
                val collections = it.collections.toggle(collection)
                // Drop selected categories that the new collection scope no longer offers,
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

        fun clear() {
            filterState.value = MapFilter()
        }

        private fun categoriesFor(collections: Set<PlaceCollection>): List<String> {
            val scope = if (collections.isEmpty()) all else all.filter { it.collection in collections }
            return scope.flatMap { it.categories }.distinct()
        }

        private fun apply(active: MapFilter): List<Place> =
            PlaceFilter.filter(
                all = all,
                selectedMonths = active.seasons.flatMap { it.months }.toSet(),
                includeYearRound = true,
                selectedCategories = active.categories,
                selectedCollections = active.collections,
            )

        private companion object {
            const val STOP_TIMEOUT_MS = 5000L
        }
    }

private fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item
