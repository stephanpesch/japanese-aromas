package app.aromas.ui.map

import app.aromas.core.model.Place
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

/** Turns the places into a GeoJSON source for the map's circle layer. */
object PlaceFeatures {
    const val PROP_ID = "id"
    const val PROP_COLOR = "color"
    const val PROP_OPACITY = "opacity"
    const val VISITED_OPACITY = 0.3f
    const val NORMAL_OPACITY = 1.0f

    // One colour per collection, so the four lists are distinguishable at a glance.
    // Aroma categories live in the filter chips, not the marker colour.
    // Visited places are dimmed via the opacity property.
    fun collection(
        places: List<Place>,
        visited: Set<String> = emptySet(),
    ): FeatureCollection {
        val features =
            places.map { place ->
                Feature.fromGeometry(Point.fromLngLat(place.lon, place.lat)).apply {
                    addStringProperty(PROP_ID, place.id)
                    addStringProperty(PROP_COLOR, CollectionColors.hex(place.collection))
                    addNumberProperty(PROP_OPACITY, if (place.id in visited) VISITED_OPACITY else NORMAL_OPACITY)
                }
            }
        return FeatureCollection.fromFeatures(features)
    }
}
