package app.aromas.core.data

import app.aromas.core.model.Aroma

/** Read-only in-memory access to the bundled 100 aromas. */
class AromaRepository(
    private val aromas: List<Aroma>,
) {
    fun all(): List<Aroma> = aromas

    fun byNumber(number: Int): Aroma? = aromas.firstOrNull { it.number == number }

    /** Distinct categories across all aromas, in first-seen order. */
    fun categories(): List<String> = aromas.flatMap { it.categories }.distinct()
}
