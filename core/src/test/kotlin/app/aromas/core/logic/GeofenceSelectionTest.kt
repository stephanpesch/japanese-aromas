package app.aromas.core.logic

import app.aromas.core.model.UserLocation
import app.aromas.core.place
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GeofenceSelectionTest {
    @Test
    fun `keeps every place when the dataset fits the limit`() {
        val all = (1..3).map { place(number = it) }
        assertEquals(all, GeofenceSelection.select(all, near = null, max = 100))
    }

    @Test
    fun `keeps the nearest aromas when over the limit`() {
        val near = place(number = 1, lat = 35.0, lon = 135.0)
        val mid = place(number = 2, lat = 36.0, lon = 136.0)
        val far = place(number = 3, lat = 43.0, lon = 143.0)
        val selected = GeofenceSelection.select(listOf(far, near, mid), UserLocation(35.0, 135.0), max = 2)
        assertEquals(listOf(1, 2), selected.map { it.number })
    }

    @Test
    fun `falls back to the first aromas when no location is known`() {
        val all = (1..5).map { place(number = it) }
        assertEquals(listOf(1, 2, 3), GeofenceSelection.select(all, near = null, max = 3).map { it.number })
    }
}
