package app.aromas.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.aromas.core.logic.GeofenceSelection
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Re-registers the geofences after a reboot, since the OS drops them on boot. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val entryPoint =
            EntryPointAccessors.fromApplication(context.applicationContext, GeofenceEntryPoint::class.java)
        val registrar = entryPoint.registrar()
        if (!entryPoint.alertsPreferences().enabled || !registrar.hasBackgroundLocationPermission()) return

        // No location fix yet at boot, so keep the first MAX aromas.
        val aromas = GeofenceSelection.select(entryPoint.repository().all(), near = null)
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                registrar.register(aromas)
            } finally {
                pending.finish()
            }
        }
    }
}
