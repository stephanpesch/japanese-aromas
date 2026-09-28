package app.aromas.di

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.ZoneId

class DataModuleTest {
    @Test
    fun `the availability clock runs in Japan time`() {
        // Opening hours are in JST, so "now" must be evaluated in Japan's zone,
        // not the device's local zone.
        assertEquals(ZoneId.of("Asia/Tokyo"), DataModule.provideClock().zone)
    }
}
