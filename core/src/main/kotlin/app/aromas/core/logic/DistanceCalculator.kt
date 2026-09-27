package app.aromas.core.logic

import app.aromas.core.model.Place
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** Great-circle distance in kilometres (Haversine). */
object DistanceCalculator {
    private const val EARTH_RADIUS_KM = 6371.0

    fun distanceKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a =
            sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return 2 * EARTH_RADIUS_KM * asin(sqrt(a))
    }

    fun distanceKm(
        place: Place,
        lat: Double,
        lon: Double,
    ): Double = distanceKm(place.lat, place.lon, lat, lon)
}
