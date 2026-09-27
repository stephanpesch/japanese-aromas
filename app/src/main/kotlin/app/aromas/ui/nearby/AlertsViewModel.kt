package app.aromas.ui.nearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.aromas.core.data.PlaceRepository
import app.aromas.core.logic.GeofenceSelection
import app.aromas.geofence.AlertsPreferences
import app.aromas.geofence.GeofenceRegistrar
import app.aromas.geofence.PlaceNotifier
import app.aromas.location.LocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Turns proximity alerts (background geofences) on and off. */
@HiltViewModel
class AlertsViewModel
    @Inject
    constructor(
        private val repository: PlaceRepository,
        private val registrar: GeofenceRegistrar,
        private val preferences: AlertsPreferences,
        private val locationProvider: LocationProvider,
        private val notifier: PlaceNotifier,
    ) : ViewModel() {
        private val enabledState = MutableStateFlow(preferences.enabled)
        val enabled: StateFlow<Boolean> = enabledState.asStateFlow()

        private val failedState = MutableStateFlow(false)

        /** True after an enable attempt that could not register the geofences. */
        val failed: StateFlow<Boolean> = failedState.asStateFlow()

        /** Register geofences around the nearest aromas; call once permissions are granted. */
        fun enable() {
            viewModelScope.launch {
                failedState.value = false
                notifier.ensureChannel()
                val location = locationProvider.currentLocation()
                val selection = GeofenceSelection.select(repository.all(), location)
                if (registrar.register(selection)) {
                    preferences.enabled = true
                    enabledState.value = true
                } else {
                    failedState.value = true
                }
            }
        }

        /** Called when the permission flow ends without the permissions geofencing needs. */
        fun reportPermissionDenied() {
            failedState.value = true
        }

        fun disable() {
            viewModelScope.launch {
                registrar.unregister()
                preferences.enabled = false
                enabledState.value = false
                failedState.value = false
            }
        }
    }
