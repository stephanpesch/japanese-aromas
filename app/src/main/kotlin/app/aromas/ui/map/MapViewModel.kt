package app.aromas.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.aromas.core.logic.Season
import app.aromas.core.model.Place
import app.aromas.core.model.PlaceCollection
import app.aromas.core.model.UserLocation
import app.aromas.location.LocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MapViewModel
    @Inject
    constructor(
        private val store: FilterStore,
        private val locationProvider: LocationProvider,
    ) : ViewModel() {
        /** The user's current location, for the "locate me" button; null if unavailable. */
        suspend fun currentLocation(): UserLocation? = locationProvider.currentLocation()

        val collections: List<PlaceCollection> = store.collections
        val filter: StateFlow<MapFilter> = store.filter
        val visited: StateFlow<Set<String>> = store.visited

        /** Category chips scoped to the selected collections (all collections' if none). */
        val categories: StateFlow<List<String>> =
            store.filter
                .map { store.categoriesFor(it.collections) }
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                    store.categoriesFor(emptySet()),
                )

        val filtered: StateFlow<List<Place>> =
            combine(store.filter, store.visited) { active, visited -> store.apply(active, visited) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), store.all)

        fun toggleCollection(collection: PlaceCollection) = store.toggleCollection(collection)

        fun toggleCategory(category: String) = store.toggleCategory(category)

        fun toggleSeason(season: Season) = store.toggleSeason(season)

        fun toggleMonth(month: Int) = store.toggleMonth(month)

        fun setWhenMode(mode: WhenMode) = store.setWhenMode(mode)

        fun cycleVisited() = store.cycleVisited()

        fun clear() = store.clear()

        private companion object {
            const val STOP_TIMEOUT_MS = 5000L
        }
    }
