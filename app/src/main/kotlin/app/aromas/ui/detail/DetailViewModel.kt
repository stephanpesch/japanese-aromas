package app.aromas.ui.detail

import androidx.lifecycle.ViewModel
import app.aromas.visited.VisitedStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class DetailViewModel
    @Inject
    constructor(
        private val store: VisitedStore,
    ) : ViewModel() {
        val visited: StateFlow<Set<String>> = store.visited

        fun toggleVisited(id: String) = store.toggle(id)
    }
