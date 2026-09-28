package app.aromas.visited

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Remembers which places the user has marked as visited (by place id). */
interface VisitedStore {
    val visited: StateFlow<Set<String>>

    /** Marks the id visited, or clears it if it already was. */
    fun toggle(id: String)
}

/** [VisitedStore] backed by [android.content.SharedPreferences]. */
@Singleton
class PrefsVisitedStore
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : VisitedStore {
        private val prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)
        private val state = MutableStateFlow(prefs.getStringSet(KEY, emptySet()).orEmpty().toSet())

        override val visited: StateFlow<Set<String>> = state.asStateFlow()

        override fun toggle(id: String) {
            val next =
                state.value
                    .toMutableSet()
                    .apply { if (!add(id)) remove(id) }
                    .toSet()
            state.value = next
            prefs.edit { putStringSet(KEY, next) }
        }

        private companion object {
            const val NAME = "visited"
            const val KEY = "visited_ids"
        }
    }
