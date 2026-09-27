package app.aromas.ui.nearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.aromas.core.data.AromaRepository
import app.aromas.core.logic.DistanceCalculator
import app.aromas.core.model.Aroma
import app.aromas.core.model.UserLocation
import app.aromas.location.LocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** An aroma together with its distance from the user, in kilometres. */
data class NearbyItem(
    val aroma: Aroma,
    val distanceKm: Double,
)

@HiltViewModel
class NearbyViewModel
    @Inject
    constructor(
        repository: AromaRepository,
        private val locationProvider: LocationProvider,
    ) : ViewModel() {
        private val aromas = repository.all()
        private val locationState = MutableStateFlow<UserLocation?>(null)

        val location: StateFlow<UserLocation?> = locationState.asStateFlow()

        val items: StateFlow<List<NearbyItem>> =
            locationState
                .map { location -> location?.let { sortedByDistance(it) } ?: emptyList() }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

        fun refresh() {
            viewModelScope.launch { locationState.value = locationProvider.currentLocation() }
        }

        private fun sortedByDistance(location: UserLocation): List<NearbyItem> =
            aromas
                .map { NearbyItem(it, DistanceCalculator.distanceKm(it, location.lat, location.lon)) }
                .sortedBy { it.distanceKm }

        private companion object {
            const val STOP_TIMEOUT_MS = 5000L
        }
    }
