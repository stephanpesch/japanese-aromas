package app.aromas.ui.map

import app.aromas.core.aroma
import app.aromas.core.data.AromaRepository
import app.aromas.core.logic.Season
import app.aromas.ui.testutil.MainDispatcherExtension
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MainDispatcherExtension::class)
class MapViewModelTest {
    private val flower = aroma(number = 1, categories = listOf("Blumen & Blüten"), months = listOf(6, 7, 8))
    private val coast = aroma(number = 2, categories = listOf("Meer & Küste"), months = listOf(3, 4, 5))
    private val tea = aroma(number = 3, categories = listOf("Tee"), months = emptyList(), yearRound = true)

    private fun viewModel() = MapViewModel(AromaRepository(listOf(flower, coast, tea)))

    @Test
    fun `categories are the distinct dataset categories in order`() {
        assertEquals(listOf("Blumen & Blüten", "Meer & Küste", "Tee"), viewModel().categories)
    }

    @Test
    fun `no filter shows every aroma`() =
        runTest {
            viewModel().filtered.test {
                assertEquals(listOf(1, 2, 3), awaitItem().map { it.number })
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
