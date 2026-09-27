package app.aromas.core.logic

import app.aromas.core.aroma
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DistanceCalculatorTest {
    @Test
    fun `same point is zero`() {
        assertEquals(0.0, DistanceCalculator.distanceKm(35.0, 135.0, 35.0, 135.0), 1e-6)
    }

    @Test
    fun `Tokyo to Osaka is about 400 km`() {
        val km = DistanceCalculator.distanceKm(35.681, 139.767, 34.693, 135.502)
        assertTrue(km in 380.0..420.0, "expected ~400 km but was $km")
    }

    @Test
    fun `aroma overload matches raw coordinates`() {
        val a = aroma(lat = 34.296, lon = 132.320)
        assertEquals(
            DistanceCalculator.distanceKm(34.296, 132.320, 35.0, 135.0),
            DistanceCalculator.distanceKm(a, 35.0, 135.0),
            1e-9,
        )
    }
}
