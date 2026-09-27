package app.aromas.ui.map

import android.graphics.RectF
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.Box
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.aromas.R
import app.aromas.core.logic.season
import app.aromas.core.logic.title
import app.aromas.core.model.Aroma
import app.aromas.core.model.Language
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource

private const val STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"
private const val SOURCE_ID = "aromas"
private const val LAYER_ID = "aroma-circles"
private const val CIRCLE_RADIUS = 6f
private const val STROKE_WIDTH = 1.5f
private const val JAPAN_LAT = 37.5
private const val JAPAN_LON = 137.5
private const val JAPAN_ZOOM = 3.8
private const val TAP_SLOP = 24f

@Composable
fun MapScreen(aromas: List<Aroma>, language: Language, modifier: Modifier = Modifier) {
    val mapView = rememberMapViewWithLifecycle()
    var selected by remember { mutableStateOf<Aroma?>(null) }
    val byNumber = remember(aromas) { aromas.associateBy { it.number } }

    Box(modifier.fillMaxSize()) {
        AndroidView(factory = { mapView }) { view ->
            view.getMapAsync { map ->
                if (map.style == null) {
                    map.cameraPosition =
                        CameraPosition.Builder()
                            .target(LatLng(JAPAN_LAT, JAPAN_LON))
                            .zoom(JAPAN_ZOOM)
                            .build()
                    map.setStyle(Style.Builder().fromUri(STYLE_URL)) { style ->
                        style.addSource(GeoJsonSource(SOURCE_ID, AromaFeatures.collection(aromas)))
                        style.addLayer(
                            CircleLayer(LAYER_ID, SOURCE_ID).withProperties(
                                PropertyFactory.circleColor(Expression.get(AromaFeatures.PROP_COLOR)),
                                PropertyFactory.circleRadius(CIRCLE_RADIUS),
                                PropertyFactory.circleStrokeColor("#ffffff"),
                                PropertyFactory.circleStrokeWidth(STROKE_WIDTH),
                            ),
                        )
                    }
                    map.addOnMapClickListener { point ->
                        val screen = map.projection.toScreenLocation(point)
                        val box =
                            RectF(
                                screen.x - TAP_SLOP,
                                screen.y - TAP_SLOP,
                                screen.x + TAP_SLOP,
                                screen.y + TAP_SLOP,
                            )
                        val hit = map.queryRenderedFeatures(box, LAYER_ID).firstOrNull()
                        val number = hit?.getNumberProperty(AromaFeatures.PROP_NUMBER)?.toInt()
                        selected = number?.let { byNumber[it] }
                        selected != null
                    }
                }
            }
        }
        selected?.let { aroma ->
            AromaInfoCard(
                aroma = aroma,
                language = language,
                onClose = { selected = null },
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp),
            )
        }
    }
}

@Composable
private fun AromaInfoCard(
    aroma: Aroma,
    language: Language,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${aroma.number}. ${aroma.title(language)}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "${aroma.prefecture} · ${aroma.season(language)}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        IconButton(onClick = onClose, modifier = Modifier.align(Alignment.End)) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close))
        }
    }
}

@Composable
private fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView =
        remember {
            MapLibre.getInstance(context)
            MapView(context)
        }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, mapView) {
        mapView.onCreate(null)
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> mapView.onStart()
                    Lifecycle.Event.ON_RESUME -> mapView.onResume()
                    Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                    Lifecycle.Event.ON_STOP -> mapView.onStop()
                    else -> Unit
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onStop()
            mapView.onDestroy()
        }
    }
    return mapView
}
