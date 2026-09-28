package app.aromas.ui.nearby

import app.aromas.core.data.PlaceRepository
import app.aromas.core.model.Place
import app.aromas.core.model.UserLocation
import app.aromas.core.place
import app.aromas.location.LocationProvider
import app.aromas.ui.map.FilterStore
import app.aromas.ui.testutil.MainDispatcherExtension
import app.aromas.visited.VisitedStore
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Clock

private class FakeLocationProvider(
    private val location: UserLocation?,
) : LocationProvider {
    override suspend fun currentLocation(): UserLocation? = location
}

private class FakeVisitedStore : VisitedStore {
    override val visited: StateFlow<Set<String>> = MutableStateFlow(emptySet<String>()).asStateFlow()

    override fun toggle(id: String) = Unit
}

private fun filterStore(places: List<Place>) =
    FilterStore(PlaceRepository(places), FakeVisitedStore(), Clock.systemUTC())

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherExtension::class)
class NearbyViewModelTest {
    @Test
    fun `refresh loads location and sorts aromas by distance`() =
        runTest {
            val near = place(number = 1, lat = 35.0, lon = 135.0)
            val far = place(number = 2, lat = 43.0, lon = 143.0)
            val viewModel =
                NearbyViewModel(
                    filterStore(listOf(far, near)),
                    FakeLocationProvider(UserLocation(35.0, 135.0)),
                )

            viewModel.items.test {
                assertEquals(emptyList<NearbyItem>(), awaitItem())
                viewModel.refresh()
                val items = awaitItem()
                assertEquals(listOf(1, 2), items.map { it.place.number })
                assertTrue(items[0].distanceKm < items[1].distanceKm)
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `no location yields an empty list`() =
        runTest {
            val viewModel = NearbyViewModel(filterStore(listOf(place())), FakeLocationProvider(null))
            viewModel.items.test {
                assertEquals(emptyList<NearbyItem>(), awaitItem())
                viewModel.refresh()
                expectNoEvents()
                cancelAndConsumeRemainingEvents()
            }
        }
}
