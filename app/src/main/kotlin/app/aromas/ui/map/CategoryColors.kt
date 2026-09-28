package app.aromas.ui.map

/** Marker colour per primary category (mirrors the web app's palette). */
object CategoryColors {
    private const val FALLBACK = "#7a7a86"
    private const val SOUND = "#8e24aa"
    private const val WATER = "#1e88e5"
    private const val SCENERY = "#2e7d32"

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
            // Sound categories share the sound collection colour.
            "Fest" to SOUND,
            "Natur & Tiere" to SOUND,
            "Wasser" to SOUND,
            "Glocken & Tempel" to SOUND,
            "Handwerk & Alltag" to SOUND,
            // Water categories share the water collection colour.
            "Quelle" to WATER,
            "Fluss & Bach" to WATER,
            "Wasserfall" to WATER,
            "Brunnen & Sonstiges" to WATER,
            // Scenery categories share the scenery collection colour.
            "Garten" to SCENERY,
            "Küste & Meer" to SCENERY,
            "Berg & Schlucht" to SCENERY,
            "See & Fluss" to SCENERY,
            "Wald & Hain" to SCENERY,
        )

    fun hex(category: String): String = byCategory[category] ?: FALLBACK
}
