package app.aromas.ui.map

import app.aromas.core.model.Place
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

/** Turns the places into a GeoJSON source for the map's circle layer. */
object PlaceFeatures {
    const val PROP_ID = "id"
    const val PROP_COLOR = "color"

    fun collection(places: List<Place>): FeatureCollection {
        val features =
            places.map { place ->
                Feature.fromGeometry(Point.fromLngLat(place.lon, place.lat)).apply {
                    addStringProperty(PROP_ID, place.id)
                    addStringProperty(PROP_COLOR, CategoryColors.hex(place.category.orEmpty()))
                }
            }
        return FeatureCollection.fromFeatures(features)
    }
}
