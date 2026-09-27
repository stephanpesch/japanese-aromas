package app.aromas.ui.map

import app.aromas.core.model.Language

/**
 * Display labels for the aroma categories. The dataset stores category names in
 * German only, so the German label is the raw key; the Japanese labels are
 * translations for the JA user interface.
 */
object CategoryLabels {
    private val japanese =
        mapOf(
            "Blumen & Blüten" to "花・花木",
            "Speisen & Genuss" to "食べ物",
            "Bäume, Wald & Grün" to "森・緑",
            "Räucherwerk & Duftstoffe" to "お香・香料",
            "Meer & Küste" to "海・海岸",
            "Sake, Essig & Braukunst" to "酒・醸造",
            "Thermalquellen & Schwefel" to "温泉・硫黄",
            "Tee" to "茶",
            "Handwerk & Sonstiges" to "工芸・その他",
        )

    /** The German key is the label for German; JA falls back to the key if unmapped. */
    fun localized(
        category: String,
        language: Language,
    ): String =
        if (language == Language.JAPANESE) {
            japanese[category] ?: category
        } else {
            category
        }
}
