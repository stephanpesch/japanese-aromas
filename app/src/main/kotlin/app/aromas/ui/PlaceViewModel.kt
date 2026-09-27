package app.aromas.ui

import androidx.lifecycle.ViewModel
import app.aromas.core.data.PlaceRepository
import app.aromas.core.model.Place
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlaceViewModel
    @Inject
    constructor(
        repository: PlaceRepository,
    ) : ViewModel() {
        val places: List<Place> = repository.all()
    }
