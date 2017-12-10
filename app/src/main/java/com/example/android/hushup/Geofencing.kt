package com.example.android.hushup

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.PlaceBuffer
import com.google.android.gms.location.GeofencingRequest
import android.content.Intent
import android.util.Log
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices

/**
 * Created by ngtrnhan1205 on 12/8/17.
 */
class Geofencing(private val mContext: Context, private val mClient: GoogleApiClient) {
    companion object {
        val TAG: String = Geofencing::class.java.simpleName
        val GEOFENCE_RADIUS: Float = 30F // 30 meters
        val GEOFENCE_TIMEOUT: Long = 24 * 60 * 60 * 1000 //24 hours
    }
    private var mGeofencePendingIntent: PendingIntent? = null
    private lateinit var mGeofenceList: ArrayList<Geofence>
    private var mGeofencingClient = LocationServices.getGeofencingClient(mContext)

    fun registerAllGeofences() {
        // Check that the API client is connected and that the list has Geofences in it
        if (!mClient.isConnected || mGeofenceList.size == 0) { return }
        try {
            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                    .addOnCompleteListener { Log.i(TAG, "Geofencing added") }
        } catch (securityException: SecurityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.e(TAG, securityException.message)
        }

    }

    fun unRegisterAllGeofences() {
        if (!mClient.isConnected) return
        try {
            mGeofencingClient.removeGeofences(getGeofencePendingIntent())
                    .addOnCompleteListener { Log.i(TAG, "Geofencing removed") }
        } catch (securityException: SecurityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            Log.e(TAG, securityException.message)
        }

    }

    fun updateGeofencesList(places: PlaceBuffer) {
        mGeofenceList = ArrayList<Geofence>()
        for (place: Place in places) {
            // Read from db cursor
            // Use the same ID for places and geofences to ensure uniqueness
            val placeID = place.id
            val placeLat = place.latLng.latitude
            val placeLng = place.latLng.longitude
            // Build geofence object
            val geofence = Geofence.Builder()
                    .setRequestId(placeID)
                    .setExpirationDuration(GEOFENCE_TIMEOUT)
                    .setCircularRegion(placeLat, placeLng, GEOFENCE_RADIUS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build()
            mGeofenceList.add(geofence)
        }
    }

    private fun getGeofencingRequest(): GeofencingRequest {
        val builder = GeofencingRequest.Builder()
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
        builder.addGeofences(mGeofenceList)
        return builder.build()
    }

    private fun getGeofencePendingIntent(): PendingIntent? {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) { return mGeofencePendingIntent }
        val intent = Intent(mContext, GeofenceBroadcastReceiver::class.java)
        mGeofencePendingIntent = PendingIntent
                .getBroadcast(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        return mGeofencePendingIntent
    }
}