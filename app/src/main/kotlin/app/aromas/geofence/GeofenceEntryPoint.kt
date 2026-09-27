package app.aromas.geofence

import app.aromas.core.data.AromaRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Lets the plain broadcast receivers pull their dependencies from Hilt. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface GeofenceEntryPoint {
    fun repository(): AromaRepository

    fun notifier(): AromaNotifier

    fun registrar(): GeofenceRegistrar

    fun alertsPreferences(): AlertsPreferences
}
