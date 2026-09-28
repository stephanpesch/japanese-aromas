package app.aromas.ui.nearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.aromas.core.logic.DistanceCalculator
import app.aromas.core.model.Place
import app.aromas.core.model.UserLocation
import app.aromas.location.LocationProvider
import app.aromas.ui.map.FilterStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** A place together with its distance from the user, in kilometres. */
data class NearbyItem(
    val place: Place,
    val distanceKm: Double,
)

@HiltViewModel
class NearbyViewModel
    @Inject
    constructor(
        private val store: FilterStore,
        private val locationProvider: LocationProvider,
    ) : ViewModel() {
        private val locationState = MutableStateFlow<UserLocation?>(null)

        val location: StateFlow<UserLocation?> = locationState.asStateFlow()

        // The nearby list shares the map filter, so it shows the same (filtered)
        // places, just sorted by distance from the user.
        val items: StateFlow<List<NearbyItem>> =
            combine(store.filter, store.visited, locationState) { active, visited, location ->
                location?.let { sortedByDistance(store.apply(active, visited), it) } ?: emptyList()
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

        fun refresh() {
            viewModelScope.launch { locationState.value = locationProvider.currentLocation() }
        }

        private fun sortedByDistance(
            places: List<Place>,
            location: UserLocation,
        ): List<NearbyItem> =
            places
                .map { NearbyItem(it, DistanceCalculator.distanceKm(it, location.lat, location.lon)) }
                .sortedBy { it.distanceKm }

        private companion object {
            const val STOP_TIMEOUT_MS = 5000L
        }
    }
