package app.aromas.ui.nearby

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import app.aromas.core.logic.season
import app.aromas.core.logic.title
import app.aromas.core.model.Language
import app.aromas.location.hasLocationPermission

private const val METERS_PER_KM = 1000
private const val KM_THRESHOLD = 1.0

@Composable
fun NearbyScreen(
    language: Language,
    modifier: Modifier = Modifier,
    viewModel: NearbyViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val items by viewModel.items.collectAsStateWithLifecycle()
    val location by viewModel.location.collectAsStateWithLifecycle()

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) viewModel.refresh()
        }

    LaunchedEffect(Unit) {
        if (hasLocationPermission(context)) viewModel.refresh()
    }

    if (location == null) {
        LocationPrompt(
            onGrant = {
                if (hasLocationPermission(context)) {
                    viewModel.refresh()
                } else {
                    launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            },
            modifier = modifier,
        )
    } else {
        LazyColumn(modifier = modifier.fillMaxSize()) {
            items(items, key = { it.aroma.number }) { item ->
                NearbyRow(item, language)
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun LocationPrompt(onGrant: () -> Unit, modifier: Modifier = Modifier) {
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
private fun NearbyRow(item: NearbyItem, language: Language) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = "${item.aroma.number}. ${item.aroma.title(language)}",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(text = item.aroma.secondaryTitle(language), style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "${item.aroma.prefecture} · ${formatDistance(item.distanceKm)}",
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

private fun formatDistance(km: Double): String =
    if (km < KM_THRESHOLD) "%.0f m".format(km * METERS_PER_KM) else "%.1f km".format(km)
