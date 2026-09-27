package app.aromas.geofence

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import app.aromas.core.model.Place
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/** Registers and removes the OS geofences that back the proximity alerts. */
class GeofenceRegistrar
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val client = LocationServices.getGeofencingClient(context)

        /** Geofencing needs fine + "allow all the time" (background) location. */
        fun hasBackgroundLocationPermission(): Boolean =
            isGranted(Manifest.permission.ACCESS_FINE_LOCATION) &&
                isGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)

        @SuppressLint("MissingPermission")
        suspend fun register(places: List<Place>): Boolean {
            if (places.isEmpty() || !hasBackgroundLocationPermission()) return false
            val request =
                GeofencingRequest
                    .Builder()
                    .setInitialTrigger(0) // don't fire for places the user already sits inside
                    .addGeofences(places.map { it.toGeofence() })
                    .build()
            return runCatching { client.addGeofences(request, pendingIntent).await() }
                .onFailure { Log.w(TAG, "Failed to register geofences", it) }
                .isSuccess
        }

        suspend fun unregister() {
            runCatching { client.removeGeofences(pendingIntent).await() }
                .onFailure { Log.w(TAG, "Failed to remove geofences", it) }
        }

        private fun Place.toGeofence(): Geofence =
            Geofence
                .Builder()
                .setRequestId(id)
                .setCircularRegion(lat, lon, RADIUS_METERS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build()

        private val pendingIntent: PendingIntent by lazy {
            val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
            PendingIntent.getBroadcast(
                context,
                0,
                intent,
                // Geofencing requires a mutable PendingIntent (it fills in the trigger extras).
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
            )
        }

        private fun isGranted(permission: String): Boolean =
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

        private companion object {
            const val RADIUS_METERS = 2000f
            const val TAG = "GeofenceRegistrar"
        }
    }
