package app.aromas.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import app.aromas.core.model.UserLocation
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/** Supplies the current user location; null when unavailable or not permitted. */
interface LocationProvider {
    suspend fun currentLocation(): UserLocation?
}

/** Whether at least coarse location permission has been granted. */
fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED

class FusedLocationProvider
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : LocationProvider {
        private val client = LocationServices.getFusedLocationProviderClient(context)

        @SuppressLint("MissingPermission")
        override suspend fun currentLocation(): UserLocation? {
            if (!hasLocationPermission(context)) return null
            val location =
                client
                    .getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        CancellationTokenSource().token,
                    ).await() ?: client.lastLocation.await()
            return location?.let { UserLocation(it.latitude, it.longitude) }
        }
    }
