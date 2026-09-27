package app.aromas.ui.nearby

import app.aromas.core.aroma
import app.aromas.core.data.AromaRepository
import app.aromas.core.model.UserLocation
import app.aromas.location.LocationProvider
import app.aromas.ui.testutil.MainDispatcherExtension
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

private class FakeLocationProvider(private val location: UserLocation?) : LocationProvider {
    override suspend fun currentLocation(): UserLocation? = location
}

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherExtension::class)
class NearbyViewModelTest {
    @Test
    fun `refresh loads location and sorts aromas by distance`() =
        runTest {
            val near = aroma(number = 1, lat = 35.0, lon = 135.0)
            val far = aroma(number = 2, lat = 43.0, lon = 143.0)
            val viewModel =
                NearbyViewModel(
                    AromaRepository(listOf(far, near)),
                    FakeLocationProvider(UserLocation(35.0, 135.0)),
                )

            viewModel.items.test {
                assertEquals(emptyList<NearbyItem>(), awaitItem())
                viewModel.refresh()
                val items = awaitItem()
                assertEquals(listOf(1, 2), items.map { it.aroma.number })
                assertTrue(items[0].distanceKm < items[1].distanceKm)
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `no location yields an empty list`() =
        runTest {
            val viewModel = NearbyViewModel(AromaRepository(listOf(aroma())), FakeLocationProvider(null))
            viewModel.items.test {
                assertEquals(emptyList<NearbyItem>(), awaitItem())
                viewModel.refresh()
                expectNoEvents()
                cancelAndConsumeRemainingEvents()
            }
        }
}
