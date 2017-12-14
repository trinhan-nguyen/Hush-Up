package com.example.android.hushup

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.net.ConnectivityManager
import android.content.SharedPreferences;
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.example.android.hushup.provider.PlaceContract
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.PlaceBuffer
import com.google.android.gms.location.places.Places

/**
 * Created by ngtrnhan1205 on 12/13/17.
 */
class MyJobService: JobService(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private var mIsEnabled = false
    private lateinit var mGeofencing: Geofencing
    private lateinit var mClient: GoogleApiClient

    override fun onConnected(bundle: Bundle?) {
        Log.i("Scheduler", "successfully connected!")
        refreshData()
    }

    override fun onConnectionSuspended(i: Int) {
        Log.i("Scheduler", "suspended")
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.i("Scheduler", "failed to connect!")
    }

    private fun refreshData() {
        val uri = PlaceContract.PlaceEntry.CONTENT_URI
        val data = contentResolver.query(
                uri, null,null, null, null)
        if (data == null || data.count == 0) {
            mGeofencing.emptyGeofenceList()
            return
        }
        val placesId = ArrayList<String>()
        while (data.moveToNext()) {
            placesId.add(data.getString(data.getColumnIndex(PlaceContract.PlaceEntry.COLUMN_PLACE_ID)))
        }
        val placeResult: PendingResult<PlaceBuffer> = Places.GeoDataApi.
                getPlaceById(mClient, *placesId.toTypedArray())

        // Using lambda to omit the interface and the override method onResult
        placeResult.setResultCallback {
            places -> run {
                mGeofencing.updateGeofencesList(places)
                if (mIsEnabled) mGeofencing.registerAllGeofences()
            }
        }
    }

    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return (networkInfo != null && networkInfo.isConnected)
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        mIsEnabled = getSharedPreferences("com.example.android.hushup", Context.MODE_PRIVATE)
                .getBoolean(getString(R.string.setting_enabled), false)
        if (isNetworkConnected() && mIsEnabled){
            // Build Api Client
            mClient = GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .build()

            mGeofencing = Geofencing(this, mClient)
            mGeofencing.registerAllGeofences()
        }
        return false
    }
}