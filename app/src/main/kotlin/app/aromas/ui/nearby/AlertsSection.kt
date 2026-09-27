package app.aromas.ui.nearby

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.aromas.R

/**
 * Enable/disable proximity alerts. Enabling walks the permission flow that
 * geofencing needs — fine location, then background ("allow all the time")
 * location, then notifications — and then registers the geofences. A denial or
 * a failed registration surfaces a hint instead of silently doing nothing.
 */
@Composable
fun AlertsSection(
    modifier: Modifier = Modifier,
    viewModel: AlertsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val enabled by viewModel.enabled.collectAsStateWithLifecycle()
    val failed by viewModel.failed.collectAsStateWithLifecycle()

    val notificationLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            // Geofences still register without notifications; the notifier checks at fire time.
            viewModel.enable()
        }
    val backgroundLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                notificationStep(
                    context,
                    notificationLauncher::launch,
                    viewModel::enable,
                )
            } else {
                viewModel.reportPermissionDenied()
            }
        }
    val fineLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                backgroundStep(
                    context,
                    backgroundLauncher::launch,
                    notificationLauncher::launch,
                    viewModel::enable,
                )
            } else {
                viewModel.reportPermissionDenied()
            }
        }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = stringResource(if (enabled) R.string.alerts_on else R.string.alerts_off),
            style = MaterialTheme.typography.bodyMedium,
        )
        if (enabled) {
            OutlinedButton(
                onClick = { viewModel.disable() },
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(stringResource(R.string.alerts_disable))
            }
        } else {
            Button(
                onClick = {
                    startEnableFlow(
                        context,
                        fineLauncher::launch,
                        backgroundLauncher::launch,
                        notificationLauncher::launch,
                        viewModel::enable,
                    )
                },
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text(stringResource(R.string.alerts_enable))
            }
            if (failed) {
                Text(
                    text = stringResource(R.string.alerts_permission_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

private fun startEnableFlow(
    context: Context,
    launchFine: (String) -> Unit,
    launchBackground: (String) -> Unit,
    launchNotifications: (String) -> Unit,
    enable: () -> Unit,
) {
    if (!isGranted(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
        launchFine(Manifest.permission.ACCESS_FINE_LOCATION)
    } else {
        backgroundStep(context, launchBackground, launchNotifications, enable)
    }
}

private fun backgroundStep(
    context: Context,
    launchBackground: (String) -> Unit,
    launchNotifications: (String) -> Unit,
    enable: () -> Unit,
) {
    if (!isGranted(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
        launchBackground(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else {
        notificationStep(context, launchNotifications, enable)
    }
}

private fun notificationStep(
    context: Context,
    launchNotifications: (String) -> Unit,
    enable: () -> Unit,
) {
    if (!isGranted(context, Manifest.permission.POST_NOTIFICATIONS)) {
        launchNotifications(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        enable()
    }
}

private fun isGranted(
    context: Context,
    permission: String,
): Boolean = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
