package app.aromas.core.data

import app.aromas.core.model.Aroma

/** Read-only in-memory access to the bundled 100 aromas. */
class AromaRepository(private val aromas: List<Aroma>) {
    fun all(): List<Aroma> = aromas

    fun byNumber(nummer: Int): Aroma? = aromas.firstOrNull { it.nummer == nummer }

    /** Distinct categories across all aromas, in first-seen order. */
    fun categories(): List<String> = aromas.flatMap { it.kategorien }.distinct()
}
