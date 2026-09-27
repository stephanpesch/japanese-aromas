package app.aromas.geofence

import app.aromas.core.data.PlaceRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Lets the plain broadcast receivers pull their dependencies from Hilt. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface GeofenceEntryPoint {
    fun repository(): PlaceRepository

    fun notifier(): PlaceNotifier

    fun registrar(): GeofenceRegistrar

    fun alertsPreferences(): AlertsPreferences
}
