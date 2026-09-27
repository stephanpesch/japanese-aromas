package app.aromas.core.logic

import app.aromas.core.aroma
import app.aromas.core.model.UserLocation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GeofenceSelectionTest {
    @Test
    fun `keeps every aroma when the dataset fits the limit`() {
        val all = (1..3).map { aroma(number = it) }
        assertEquals(all, GeofenceSelection.select(all, near = null, max = 100))
    }

    @Test
    fun `keeps the nearest aromas when over the limit`() {
        val near = aroma(number = 1, lat = 35.0, lon = 135.0)
        val mid = aroma(number = 2, lat = 36.0, lon = 136.0)
        val far = aroma(number = 3, lat = 43.0, lon = 143.0)
        val selected = GeofenceSelection.select(listOf(far, near, mid), UserLocation(35.0, 135.0), max = 2)
        assertEquals(listOf(1, 2), selected.map { it.number })
    }

    @Test
    fun `falls back to the first aromas when no location is known`() {
        val all = (1..5).map { aroma(number = it) }
        assertEquals(listOf(1, 2, 3), GeofenceSelection.select(all, near = null, max = 3).map { it.number })
    }
}
