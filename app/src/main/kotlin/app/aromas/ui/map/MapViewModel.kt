package app.aromas.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.aromas.core.data.AromaRepository
import app.aromas.core.logic.AromaFilter
import app.aromas.core.logic.Season
import app.aromas.core.model.Aroma
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/** The active map filter: selected categories (OR) and seasons (union of months). */
data class MapFilter(
    val categories: Set<String> = emptySet(),
    val seasons: Set<Season> = emptySet(),
)

@HiltViewModel
class MapViewModel
    @Inject
    constructor(
        repository: AromaRepository,
    ) : ViewModel() {
        private val all = repository.all()

        /** All categories present in the dataset, in first-seen order (for the chips). */
        val categories: List<String> = repository.categories()

        private val filterState = MutableStateFlow(MapFilter())
        val filter: StateFlow<MapFilter> = filterState.asStateFlow()

        val filtered: StateFlow<List<Aroma>> =
            filterState
                .map { active -> apply(active) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), all)

        fun toggleCategory(category: String) {
            filterState.update { it.copy(categories = it.categories.toggle(category)) }
        }

        fun toggleSeason(season: Season) {
            filterState.update { it.copy(seasons = it.seasons.toggle(season)) }
        }

        fun clear() {
            filterState.value = MapFilter()
        }

        private fun apply(active: MapFilter): List<Aroma> =
            AromaFilter.filter(
                all = all,
                selectedMonths = active.seasons.flatMap { it.months }.toSet(),
                includeYearRound = true,
                selectedCategories = active.categories,
            )

        private companion object {
            const val STOP_TIMEOUT_MS = 5000L
        }
    }

private fun <T> Set<T>.toggle(item: T): Set<T> = if (item in this) this - item else this + item
