package app.aromas.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.RectF
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.aromas.R
import app.aromas.core.logic.season
import app.aromas.core.logic.title
import app.aromas.core.model.Language
import app.aromas.core.model.Place
import app.aromas.location.hasLocationPermission
import kotlinx.coroutines.launch
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import java.util.concurrent.atomic.AtomicBoolean

private const val SOURCE_ID = "places"
private const val LAYER_ID = "place-circles"
private const val CIRCLE_RADIUS = 6f
private const val STROKE_WIDTH = 1.5f
private const val JAPAN_LAT = 37.5
private const val JAPAN_LON = 137.5
private const val JAPAN_ZOOM = 3.8
private const val USER_ZOOM = 10.0
private const val TAP_SLOP = 24f

@Composable
fun MapScreen(
    language: Language,
    onOpenDetail: (Place) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val mapView = rememberMapViewWithLifecycle()
    val places by viewModel.filtered.collectAsStateWithLifecycle()
    val filter by viewModel.filter.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val visited by viewModel.visited.collectAsStateWithLifecycle()
    var selected by remember { mutableStateOf<Place?>(null) }
    var showWhenSheet by remember { mutableStateOf(false) }
    val byId = remember(places) { places.associateBy { it.id } }
    // The map is configured once inside an async style load; read the latest
    // filtered set and lookup through these so a filter toggled while the style
    // is still loading is not lost.
    val currentPlaces by rememberUpdatedState(places)
    val currentById by rememberUpdatedState(byId)
    val currentVisited by rememberUpdatedState(visited)
    // Guards the one-time map setup: setStyle() is async, so `map.style` stays
    // null during loading — a recomposition (e.g. language toggle) must not
    // re-run the setup and stack up click listeners.
    val configured = remember { AtomicBoolean(false) }
    var maplibreMap by remember { mutableStateOf<MapLibreMap?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val locate: () -> Unit = {
        scope.launch {
            val location = viewModel.currentLocation()
            val map = maplibreMap
            if (location != null && map != null) {
                enableLocationDot(map, context)
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(LatLng(location.lat, location.lon), USER_ZOOM),
                )
            }
        }
    }
    val locationLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            // Re-read the state: picking "Approximate" grants coarse (fine denied),
            // which still satisfies hasLocationPermission and lets locate() work.
            if (hasLocationPermission(context)) locate()
        }

    Box(modifier.fillMaxSize()) {
        AndroidView(factory = { mapView }) { view ->
            if (configured.compareAndSet(false, true)) {
                view.getMapAsync { map ->
                    maplibreMap = map
                    configureMap(map, currentPlaces, currentVisited) { selected = currentById[it] }
                }
            }
        }
        MapFilterBar(
            collections = viewModel.collections,
            categories = categories,
            filter = filter,
            language = language,
            onToggleCollection = viewModel::toggleCollection,
            onToggleCategory = viewModel::toggleCategory,
            onCycleVisited = viewModel::cycleVisited,
            onOpenWhen = { showWhenSheet = true },
            onClear = viewModel::clear,
            modifier = Modifier.align(Alignment.TopCenter),
        )
        if (showWhenSheet) {
            WhenSheet(
                filter = filter,
                onSetWhenMode = viewModel::setWhenMode,
                onToggleSeason = viewModel::toggleSeason,
                onToggleMonth = viewModel::toggleMonth,
                onDismiss = { showWhenSheet = false },
            )
        }
        selected?.let { place ->
            PlaceInfoCard(
                place = place,
                language = language,
                onClose = { selected = null },
                onOpenDetail = { onOpenDetail(place) },
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp),
            )
        }
        FloatingActionButton(
            onClick = {
                if (hasLocationPermission(context)) {
                    locate()
                } else {
                    locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            },
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
        ) {
            Icon(Icons.Filled.MyLocation, contentDescription = stringResource(R.string.locate_me))
        }
    }

    // Push the filtered set into the existing marker source (re-run when the
    // visited set changes too, so newly marked places dim immediately) and drop a
    // selection that the filter just hid.
    LaunchedEffect(places, visited) {
        maplibreMap
            ?.style
            ?.getSourceAs<GeoJsonSource>(SOURCE_ID)
            ?.setGeoJson(PlaceFeatures.collection(places, visited))
        selected?.let { if (it.id !in byId) selected = null }
    }
}

/** One-time map configuration: camera, style + circle layer, tap handler. */
private fun configureMap(
    map: MapLibreMap,
    places: List<Place>,
    visited: Set<String>,
    onPick: (String?) -> Unit,
) {
    // Keep the map fixed north-up and flat.
    map.uiSettings.setRotateGesturesEnabled(false)
    map.uiSettings.setTiltGesturesEnabled(false)
    map.cameraPosition =
        CameraPosition
            .Builder()
            .target(LatLng(JAPAN_LAT, JAPAN_LON))
            .zoom(JAPAN_ZOOM)
            .build()
    map.setStyle(Style.Builder().fromUri(OPENFREEMAP_STYLE_URL)) { style ->
        style.addSource(GeoJsonSource(SOURCE_ID, PlaceFeatures.collection(places, visited)))
        style.addLayer(
            CircleLayer(LAYER_ID, SOURCE_ID).withProperties(
                PropertyFactory.circleColor(Expression.get(PlaceFeatures.PROP_COLOR)),
                PropertyFactory.circleRadius(CIRCLE_RADIUS),
                PropertyFactory.circleOpacity(Expression.get(PlaceFeatures.PROP_OPACITY)),
                PropertyFactory.circleStrokeColor("#ffffff"),
                PropertyFactory.circleStrokeWidth(STROKE_WIDTH),
                PropertyFactory.circleStrokeOpacity(Expression.get(PlaceFeatures.PROP_OPACITY)),
            ),
        )
    }
    map.addOnMapClickListener { point ->
        val screen = map.projection.toScreenLocation(point)
        val box = RectF(screen.x - TAP_SLOP, screen.y - TAP_SLOP, screen.x + TAP_SLOP, screen.y + TAP_SLOP)
        val hit = map.queryRenderedFeatures(box, LAYER_ID).firstOrNull()
        val id = hit?.getStringProperty(PlaceFeatures.PROP_ID)
        onPick(id)
        id != null
    }
}

/** Turns on the MapLibre blue "my location" dot (once the style is ready). */
@SuppressLint("MissingPermission")
private fun enableLocationDot(
    map: MapLibreMap,
    context: Context,
) {
    val style = map.style ?: return
    val component = map.locationComponent
    if (!component.isLocationComponentActivated) {
        component.activateLocationComponent(
            LocationComponentActivationOptions.builder(context, style).build(),
        )
    }
    component.isLocationComponentEnabled = true
    component.cameraMode = CameraMode.NONE
    component.renderMode = RenderMode.NORMAL
}

@Composable
private fun PlaceInfoCard(
    place: Place,
    language: Language,
    onClose: () -> Unit,
    onOpenDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(onClick = onOpenDetail, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${place.number}. ${place.title(language)}",
                    style = MaterialTheme.typography.titleMedium,
                )
                val season = place.season(language)
                Text(
                    text = if (season.isEmpty()) place.prefecture else "${place.prefecture} · $season",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close))
            }
        }
    }
}
