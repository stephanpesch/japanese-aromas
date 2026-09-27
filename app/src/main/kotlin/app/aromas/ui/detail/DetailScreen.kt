package app.aromas.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import app.aromas.R
import app.aromas.core.logic.description
import app.aromas.core.logic.season
import app.aromas.core.logic.secondaryTitle
import app.aromas.core.logic.source
import app.aromas.core.logic.title
import app.aromas.core.model.Language
import app.aromas.core.model.Place
import app.aromas.ui.map.CategoryColors
import app.aromas.ui.map.OPENFREEMAP_STYLE_URL
import app.aromas.ui.map.rememberMapViewWithLifecycle
import coil.compose.AsyncImage
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point
import java.util.concurrent.atomic.AtomicBoolean

private const val DETAIL_SOURCE_ID = "place-detail"
private const val DETAIL_LAYER_ID = "place-detail-circle"
private const val DETAIL_ZOOM = 11.0
private const val MARKER_RADIUS = 8f
private const val MARKER_STROKE = 2f
private const val HERO_HEIGHT_DP = 220
private const val MAP_HEIGHT_DP = 200

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    place: Place,
    language: Language,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(place.title(language)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
        ) {
            place.image?.let { image ->
                AsyncImage(
                    model = "file:///android_asset/$image",
                    contentDescription = place.title(language),
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(HERO_HEIGHT_DP.dp),
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "${place.number}. ${place.title(language)}",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = place.secondaryTitle(language),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                DetailMiniMap(
                    place = place,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(MAP_HEIGHT_DP.dp)
                            .padding(top = 16.dp),
                )

                Field(stringResource(R.string.label_location), "${place.city} · ${place.prefecture}")
                val source = place.source(language)
                if (source.isNotEmpty()) Field(stringResource(R.string.label_source), source)
                val season = place.season(language)
                if (season.isNotEmpty()) Field(stringResource(R.string.label_season), season)

                Text(
                    text = place.description(language),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp),
                )
                Text(
                    text = place.description(language.opposite()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun Field(
    label: String,
    value: String,
) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun DetailMiniMap(
    place: Place,
    modifier: Modifier = Modifier,
) {
    val mapView = rememberMapViewWithLifecycle()
    val configured = remember { AtomicBoolean(false) }
    AndroidView(factory = { mapView }, modifier = modifier) { view ->
        if (configured.compareAndSet(false, true)) {
            view.getMapAsync { map ->
                map.uiSettings.setAllGesturesEnabled(false)
                map.cameraPosition =
                    CameraPosition
                        .Builder()
                        .target(LatLng(place.lat, place.lon))
                        .zoom(DETAIL_ZOOM)
                        .build()
                map.setStyle(Style.Builder().fromUri(OPENFREEMAP_STYLE_URL)) { style ->
                    style.addSource(
                        GeoJsonSource(
                            DETAIL_SOURCE_ID,
                            Feature.fromGeometry(Point.fromLngLat(place.lon, place.lat)),
                        ),
                    )
                    style.addLayer(
                        CircleLayer(DETAIL_LAYER_ID, DETAIL_SOURCE_ID).withProperties(
                            PropertyFactory.circleColor(CategoryColors.hex(place.category.orEmpty())),
                            PropertyFactory.circleRadius(MARKER_RADIUS),
                            PropertyFactory.circleStrokeColor("#ffffff"),
                            PropertyFactory.circleStrokeWidth(MARKER_STROKE),
                        ),
                    )
                }
            }
        }
    }
}
