package app.aromas.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.aromas.core.data.PlaceRepository
import app.aromas.core.logic.PlaceFilter
import app.aromas.core.logic.Season
import app.aromas.core.model.Place
import app.aromas.core.model.PlaceCollection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** The active map filter: selected collections, categories (OR) and seasons. */
data class MapFilter(
    val collections: Set<PlaceCollection> = emptySet(),
    val categories: Set<String> = emptySet(),
    val seasons: Set<Season> = emptySet(),
)

@HiltViewModel
class MapViewModel
    @Inject
    constructor(
        repository: PlaceRepository,
    ) : ViewModel() {
        private val all = repository.all()

        /** All collections present in the dataset, in first-seen order (for the chips). */
        val collections: List<PlaceCollection> = all.map { it.collection }.distinct()

        /** All aroma categories present in the dataset, in first-seen order (for the chips). */
        val categories: List<String> = repository.categories()

        private val filterState = MutableStateFlow(MapFilter())
        val filter: StateFlow<MapFilter> = filterState.asStateFlow()

        val filtered: StateFlow<List<Place>> =
            filterState
                .map { active -> apply(active) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), all)

        fun toggleCollection(collection: PlaceCollection) {
            filterState.update { it.copy(collections = it.collections.toggle(collection)) }
        }

        fun toggleCategory(category: String) {
            filterState.update { it.copy(categories = it.categories.toggle(category)) }
        }

        fun toggleSeason(season: Season) {
            filterState.update { it.copy(seasons = it.seasons.toggle(season)) }
        }

        fun clear() {
            filterState.value = MapFilter()
        }

        private fun apply(active: MapFilter): List<Place> =
            PlaceFilter.filter(
                all = all,
                selectedMonths = active.seasons.flatMap { it.months }.toSet(),
                includeYearRound = true,
                selectedCategories = active.categories,
                selectedCollections = active.collections,
            )

        private companion object {
            const val STOP_TIMEOUT_MS = 5000L
        }
    }

private fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item
