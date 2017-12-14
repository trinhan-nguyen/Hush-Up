package com.example.android.hushup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.GeofencingEvent
import android.media.AudioManager
import android.app.NotificationManager
import com.google.android.gms.location.Geofence
import android.graphics.BitmapFactory
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.support.v4.app.NotificationCompat


/**
 * Created by ngtrnhan1205 on 12/8/17.
 */
class GeofenceBroadcastReceiver: BroadcastReceiver() {
    companion object {
        val TAG = GeofenceBroadcastReceiver::class.simpleName
        val channelId = "hushupnoti"
        fun sendNotification(context: Context, transitionType: Int) {
            // transitionType == 0 means unregister all
            // Create an explicit content Intent that starts the main Activity.
            val notificationIntent = Intent(context, MainActivity::class.java)

            val stackBuilder = TaskStackBuilder.create(context)
            stackBuilder.addParentStack(MainActivity::class.java)
            stackBuilder.addNextIntent(notificationIntent)

            val notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

            // Get a notification builder
            val builder: NotificationCompat.Builder = NotificationCompat.Builder(context, channelId)

            // Check the transition type to display the relevant icon image
            if (transitionType == Geofence.GEOFENCE_TRANSITION_ENTER) {
                builder.setSmallIcon(R.drawable.ic_volume_off_white_24dp)
                        .setLargeIcon(BitmapFactory.decodeResource(context.resources,
                                R.drawable.ic_volume_off_white_24dp))
                        .setContentTitle(context.getString(R.string.silent_mode_activated))
            } else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT || transitionType == 0) {
                builder.setSmallIcon(R.drawable.ic_volume_up_white_24dp)
                        .setLargeIcon(BitmapFactory.decodeResource(context.resources,
                                R.drawable.ic_volume_up_white_24dp))
                        .setContentTitle(context.getString(R.string.back_to_normal))
            }

            // Continue building the notification
            builder.setContentText(context.getString(R.string.touch_to_relaunch))
            builder.setContentIntent(notificationPendingIntent)

            // Dismiss notification once the user touches it.
            builder.setAutoCancel(true)

            // Get an instance of the Notification manager
            val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Issue the notification
            mNotificationManager.notify(0, builder.build())
        }

        fun setRingerMode(context: Context, mode: Int) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // Check for DND permissions for API 24+
            if (android.os.Build.VERSION.SDK_INT < 24
                    || (android.os.Build.VERSION.SDK_INT >= 24 && notificationManager.isNotificationPolicyAccessGranted)) {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.ringerMode = mode
            }
        }
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        // Catch intent from transition (Pending result)
        val geofencingEvent = GeofencingEvent.fromIntent(intent) as GeofencingEvent
        if (geofencingEvent.hasError()) {
            Log.e(TAG, String.format("Error code : %d", geofencingEvent.getErrorCode()))
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.getGeofenceTransition()

        when (geofenceTransition) {
            Geofence.GEOFENCE_TRANSITION_ENTER -> setRingerMode(context!!, AudioManager.RINGER_MODE_SILENT)
            Geofence.GEOFENCE_TRANSITION_EXIT -> setRingerMode(context!!, AudioManager.RINGER_MODE_NORMAL)
            else -> {
                Log.e(TAG, String.format("Unknown transition : %d", geofenceTransition))
                return
            }
        }
        // Send the notification
        sendNotification(context, geofenceTransition)
    }
}