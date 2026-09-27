package app.aromas.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import dagger.hilt.android.EntryPointAccessors

/** Fires when the user enters a place's geofence and posts a proximity alert. */
class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError() || event.geofenceTransition != Geofence.GEOFENCE_TRANSITION_ENTER) return
        val entryPoint =
            EntryPointAccessors.fromApplication(context.applicationContext, GeofenceEntryPoint::class.java)
        val repository = entryPoint.repository()
        val notifier = entryPoint.notifier()
        event.triggeringGeofences.orEmpty().forEach { geofence ->
            repository.byId(geofence.requestId)?.let(notifier::notifyNearby)
        }
    }
}
