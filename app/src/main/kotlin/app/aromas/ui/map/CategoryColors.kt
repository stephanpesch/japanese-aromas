package app.aromas.ui.map

/** Marker colour per primary category (mirrors the web app's palette). */
object CategoryColors {
    private const val FALLBACK = "#7a7a86"

    private val byCategory =
        mapOf(
            "Blumen & Blüten" to "#d81b8c",
            "Speisen & Genuss" to "#e67e22",
            "Bäume, Wald & Grün" to "#2e8b57",
            "Räucherwerk & Duftstoffe" to "#7b5cd6",
            "Meer & Küste" to "#2277cc",
            "Sake, Essig & Braukunst" to "#b7791f",
            "Thermalquellen & Schwefel" to "#d64545",
            "Tee" to "#159a8a",
            "Handwerk & Sonstiges" to FALLBACK,
        )

    fun hex(category: String): String = byCategory[category] ?: FALLBACK
}
