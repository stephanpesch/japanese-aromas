package app.aromas.ui.map

import app.aromas.core.model.PlaceCollection

/**
 * Marker colour per collection, used for the water/sound/scenery lists. Aromas
 * keep their per-category colours (see [CategoryColors]); the AROMA value here is
 * only a fallback.
 */
object CollectionColors {
    fun hex(collection: PlaceCollection): String =
        when (collection) {
            PlaceCollection.AROMA -> "#d81b8c"
            PlaceCollection.WATER -> "#1e88e5"
            PlaceCollection.SOUND -> "#8e24aa"
            PlaceCollection.SCENERY -> "#2e7d32"
        }
}
