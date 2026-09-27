package app.aromas.ui.map

import android.content.ComponentCallbacks2
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapView

/** The keyless OpenFreeMap vector style. */
const val OPENFREEMAP_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"

/** Creates a MapView and forwards the host lifecycle (incl. low-memory) to it. */
@Composable
fun rememberMapViewWithLifecycle(): MapView {
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
        // onLowMemory() comes from ComponentCallbacks2, not the lifecycle.
        val memoryCallbacks =
            object : ComponentCallbacks2 {
                override fun onConfigurationChanged(newConfig: Configuration) = Unit

                @Deprecated("Kept for the ComponentCallbacks2 contract")
                override fun onLowMemory() = mapView.onLowMemory()

                override fun onTrimMemory(level: Int) = mapView.onLowMemory()
            }
        val appContext = context.applicationContext
        appContext.registerComponentCallbacks(memoryCallbacks)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            appContext.unregisterComponentCallbacks(memoryCallbacks)
            mapView.onStop()
            mapView.onDestroy()
        }
    }
    return mapView
}
