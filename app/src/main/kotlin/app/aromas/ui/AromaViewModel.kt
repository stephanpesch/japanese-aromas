package app.aromas.ui

import androidx.lifecycle.ViewModel
import app.aromas.core.data.AromaRepository
import app.aromas.core.model.Aroma
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AromaViewModel
    @Inject
    constructor(
        repository: AromaRepository,
    ) : ViewModel() {
        val aromas: List<Aroma> = repository.all()
    }
