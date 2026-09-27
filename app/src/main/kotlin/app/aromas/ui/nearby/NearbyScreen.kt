package app.aromas.ui.nearby

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.aromas.R
import app.aromas.core.logic.secondaryTitle
import app.aromas.core.logic.title
import app.aromas.core.model.Language
import app.aromas.core.model.Place
import app.aromas.location.hasLocationPermission

private const val METERS_PER_KM = 1000
private const val KM_THRESHOLD = 1.0

@Composable
fun NearbyScreen(
    language: Language,
    onOpenDetail: (Place) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NearbyViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val items by viewModel.items.collectAsStateWithLifecycle()
    val location by viewModel.location.collectAsStateWithLifecycle()
    var permissionGranted by remember { mutableStateOf(hasLocationPermission(context)) }

    // Fine may be denied while coarse is granted (the "approximate" choice), so
    // re-read the actual permission state rather than trusting the single result.
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            permissionGranted = hasLocationPermission(context)
            if (permissionGranted) viewModel.refresh()
        }

    LaunchedEffect(permissionGranted) {
        if (permissionGranted) viewModel.refresh()
    }

    when {
        !permissionGranted ->
            LocationPrompt(
                onGrant = { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                modifier = modifier,
            )

        location == null ->
            CenteredMessage(stringResource(R.string.location_loading), modifier)

        else ->
            LazyColumn(modifier = modifier.fillMaxSize()) {
                item {
                    AlertsSection()
                    HorizontalDivider()
                }
                items(items, key = { it.place.id }) { item ->
                    NearbyRow(item, language, onClick = { onOpenDetail(item.place) })
                    HorizontalDivider()
                }
            }
    }
}

@Composable
private fun LocationPrompt(
    onGrant: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.location_needed),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(onClick = onGrant, modifier = Modifier.padding(top = 16.dp)) {
            Text(stringResource(R.string.grant_location))
        }
    }
}

@Composable
private fun CenteredMessage(
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = text, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun NearbyRow(
    item: NearbyItem,
    language: Language,
    onClick: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = "${item.place.number}. ${item.place.title(language)}",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(text = item.place.secondaryTitle(language), style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "${item.place.prefecture} · ${formatDistance(item.distanceKm)}",
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

private fun formatDistance(km: Double): String =
    if (km <
        KM_THRESHOLD
    ) {
        "%.0f m".format(km * METERS_PER_KM)
    } else {
        "%.1f km".format(km)
    }
