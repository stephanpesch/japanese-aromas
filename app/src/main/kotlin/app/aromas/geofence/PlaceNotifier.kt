package app.aromas.geofence

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.aromas.MainActivity
import app.aromas.R
import app.aromas.core.logic.title
import app.aromas.core.model.Place
import app.aromas.settings.AppLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Posts "you are near a place" notifications, on its own channel. */
class PlaceNotifier
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val manager = NotificationManagerCompat.from(context)

        fun ensureChannel() {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.alerts_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply { description = context.getString(R.string.alerts_channel_description) }
            manager.createNotificationChannel(channel)
        }

        fun notifyNearby(place: Place) {
            if (!manager.areNotificationsEnabled()) return
            ensureChannel()
            val notification =
                NotificationCompat
                    .Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(context.getString(R.string.alert_title))
                    .setContentText(context.getString(R.string.alert_text, place.title(AppLanguage.current(context))))
                    .setContentIntent(openAppIntent())
                    .setAutoCancel(true)
                    .build()
            manager.notify(place.id.hashCode(), notification)
        }

        private fun openAppIntent(): PendingIntent {
            val intent =
                Intent(context, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        private companion object {
            const val CHANNEL_ID = "aroma_proximity"
        }
    }
