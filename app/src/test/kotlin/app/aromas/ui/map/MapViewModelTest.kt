package app.aromas.ui.map

import app.aromas.core.data.PlaceRepository
import app.aromas.core.logic.Season
import app.aromas.core.model.PlaceCollection
import app.aromas.core.model.UserLocation
import app.aromas.core.place
import app.aromas.location.LocationProvider
import app.aromas.ui.testutil.MainDispatcherExtension
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

private object FakeLocationProvider : LocationProvider {
    override suspend fun currentLocation(): UserLocation? = null
}

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherExtension::class)
class MapViewModelTest {
    private val flower = place(number = 1, categories = listOf("Blumen & Blüten"), months = listOf(6, 7, 8))
    private val coast = place(number = 2, categories = listOf("Meer & Küste"), months = listOf(3, 4, 5))
    private val tea = place(number = 3, categories = listOf("Tee"), months = emptyList(), yearRound = true)

    private fun viewModel() = MapViewModel(PlaceRepository(listOf(flower, coast, tea)), FakeLocationProvider)

    @Test
    fun `categories are the distinct dataset categories in order`() {
        assertEquals(listOf("Blumen & Blüten", "Meer & Küste", "Tee"), viewModel().categories.value)
    }

    @Test
    fun `categories are scoped to the selected collection`() =
        runTest {
            val garden = place(number = 9, collection = PlaceCollection.SCENERY, categories = listOf("Garten"))
            val vm = MapViewModel(PlaceRepository(listOf(flower, garden)), FakeLocationProvider)
            vm.categories.test {
                assertEquals(listOf("Blumen & Blüten", "Garten"), awaitItem()) // no collection filter: all
                vm.toggleCollection(PlaceCollection.SCENERY)
                assertEquals(listOf("Garten"), awaitItem()) // scoped to scenery
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `no filter shows every place`() =
        runTest {
            viewModel().filtered.test {
                assertEquals(listOf(1, 2, 3), awaitItem().map { it.number })
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `narrowing to another collection drops out-of-scope selected categories`() {
        val garden = place(number = 9, collection = PlaceCollection.SCENERY, categories = listOf("Garten"))
        val vm = MapViewModel(PlaceRepository(listOf(flower, garden)), FakeLocationProvider)
        vm.toggleCategory("Blumen & Blüten")
        vm.toggleCollection(PlaceCollection.SCENERY)
        assertEquals(emptySet<String>(), vm.filter.value.categories)
        assertEquals(setOf(PlaceCollection.SCENERY), vm.filter.value.collections)
    }

    @Test
    fun `collection filter keeps only the selected collection`() =
        runTest {
            val scene =
                place(number = 9, collection = PlaceCollection.SCENERY, categories = emptyList(), months = emptyList())
            val vm = MapViewModel(PlaceRepository(listOf(flower, scene)), FakeLocationProvider)
            assertEquals(listOf(PlaceCollection.AROMA, PlaceCollection.SCENERY), vm.collections)
            vm.filtered.test {
                assertEquals(listOf(1, 9), awaitItem().map { it.number })
                vm.toggleCollection(PlaceCollection.SCENERY)
                assertEquals(listOf(9), awaitItem().map { it.number })
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `category filter keeps only matching categories`() =
        runTest {
            val vm = viewModel()
            vm.filtered.test {
                assertEquals(listOf(1, 2, 3), awaitItem().map { it.number })
                vm.toggleCategory("Meer & Küste")
                assertEquals(listOf(2), awaitItem().map { it.number })
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `season filter matches months and always keeps year-round`() =
        runTest {
            val vm = viewModel()
            vm.filtered.test {
                assertEquals(listOf(1, 2, 3), awaitItem().map { it.number })
                vm.toggleSeason(Season.SPRING)
                // coast is a spring aroma; tea is year-round; flower (summer) drops out.
                assertEquals(listOf(2, 3), awaitItem().map { it.number })
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `clear removes all active filters`() =
        runTest {
            val vm = viewModel()
            vm.filtered.test {
                assertEquals(listOf(1, 2, 3), awaitItem().map { it.number })
                vm.toggleCategory("Tee")
                assertEquals(listOf(3), awaitItem().map { it.number })
                vm.clear()
                assertEquals(listOf(1, 2, 3), awaitItem().map { it.number })
                cancelAndConsumeRemainingEvents()
            }
        }
}
