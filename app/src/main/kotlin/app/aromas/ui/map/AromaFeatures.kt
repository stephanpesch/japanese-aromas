package app.aromas.ui.map

import app.aromas.core.model.Aroma
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point

/** Turns the aromas into a GeoJSON source for the map's circle layer. */
object AromaFeatures {
    const val PROP_NUMBER = "number"
    const val PROP_COLOR = "color"

    fun collection(aromas: List<Aroma>): FeatureCollection {
        val features =
            aromas.map { aroma ->
                Feature.fromGeometry(Point.fromLngLat(aroma.lon, aroma.lat)).apply {
                    addNumberProperty(PROP_NUMBER, aroma.number)
                    addStringProperty(PROP_COLOR, CategoryColors.hex(aroma.category))
                }
            }
        return FeatureCollection.fromFeatures(features)
    }
}
