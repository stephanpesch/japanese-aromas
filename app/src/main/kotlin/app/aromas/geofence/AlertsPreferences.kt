package app.aromas.geofence

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Remembers whether the user turned proximity alerts on (survives reboots). */
class AlertsPreferences
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) {
        private val prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

        var enabled: Boolean
            get() = prefs.getBoolean(KEY_ENABLED, false)
            set(value) = prefs.edit { putBoolean(KEY_ENABLED, value) }

        private companion object {
            const val NAME = "alerts"
            const val KEY_ENABLED = "alerts_enabled"
        }
    }
