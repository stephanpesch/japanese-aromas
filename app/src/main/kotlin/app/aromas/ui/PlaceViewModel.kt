package app.aromas.ui

import androidx.lifecycle.ViewModel
import app.aromas.core.data.PlaceRepository
import app.aromas.core.model.Place
import app.aromas.visited.VisitedStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PlaceViewModel
    @Inject
    constructor(
        repository: PlaceRepository,
        visitedStore: VisitedStore,
    ) : ViewModel() {
        val places: List<Place> = repository.all()

        /** Places the user has marked as visited, for dimming their list rows. */
        val visited: StateFlow<Set<String>> = visitedStore.visited
    }
