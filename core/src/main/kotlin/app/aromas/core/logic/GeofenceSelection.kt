package app.aromas.core.logic

import app.aromas.core.model.Aroma
import app.aromas.core.model.UserLocation

/**
 * Chooses which aromas to register as geofences. Android allows at most 100
 * active geofences per app, so when the dataset is larger we keep the ones
 * nearest the user (or the first [max] when no location is known yet).
 */
object GeofenceSelection {
    const val MAX_GEOFENCES = 100

    fun select(
        all: List<Aroma>,
        near: UserLocation?,
        max: Int = MAX_GEOFENCES,
    ): List<Aroma> {
        if (all.size <= max) return all
        return if (near != null) {
            all
                .sortedBy { DistanceCalculator.distanceKm(it, near.lat, near.lon) }
                .take(max)
        } else {
            all.take(max)
        }
    }
}
