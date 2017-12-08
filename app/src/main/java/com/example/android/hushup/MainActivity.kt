package com.example.android.hushup

import android.app.Activity
import android.app.LoaderManager
import android.content.ContentValues
import android.content.Intent
import android.content.Loader
import android.content.pm.PackageManager
import android.database.Cursor
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Places
import android.widget.CheckBox
import com.example.android.hushup.provider.PlaceContract
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.PlaceBuffer
import com.google.android.gms.location.places.ui.PlacePicker


class MainActivity : AppCompatActivity(),
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    companion object {
        // Constants
        val TAG: String = MainActivity::class.java.simpleName
        val PERMISSION_REQUEST_FINE_LOCATION = 111
        val PLACE_PICKER_REQUEST = 999
    }

    // Member variables
    private lateinit var mAdapter: PlaceListAdapter
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mClient: GoogleApiClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up the recycler view
        mRecyclerView = findViewById<RecyclerView>(R.id.places_list_recycler_view)
        mRecyclerView!!.layoutManager = LinearLayoutManager(this)
        mAdapter = PlaceListAdapter(this, null)
        mRecyclerView!!.adapter = mAdapter

        mClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build()
    }

    override fun onConnected(bundle: Bundle?) {
        Log.i(TAG, "successfully connected!")
        refreshData()
    }

    override fun onConnectionSuspended(i: Int) {
        Log.i(TAG, "suspended")
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.i(TAG, "Failed!")
    }

    fun refreshData() {
        val uri = PlaceContract.PlaceEntry.CONTENT_URI
        val data = contentResolver.query(
                uri, null,null, null, null)
        if (data == null || data.count == 0) return
        val placesId = ArrayList<String>()
        while (data.moveToNext()) {
            placesId.add(data.getString(data.getColumnIndex(PlaceContract.PlaceEntry.COLUMN_PLACE_ID)))
        }
        val placeResult: PendingResult<PlaceBuffer> = Places.GeoDataApi.
                getPlaceById(mClient, *placesId.toTypedArray())

        // Using lambda to omit the interface and the override method onResult
        placeResult.setResultCallback {
            places -> mAdapter.swapPlaces(places)
        }
    }

    fun addNewLocation(view: View) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.need_location_permission_message), Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, getString(R.string.location_permissions_granted_message), Toast.LENGTH_LONG).show();

        val builder = PlacePicker.IntentBuilder()
        val intent = builder.build(this)
        startActivityForResult(intent, PLACE_PICKER_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            val place = PlacePicker.getPlace(this, data)
            if (place == null) {
                Log.i(TAG,"No place selected!")
                return;
            }

            // Extract the place's information
            val name = place.name.toString()
            val placeAddress = place.address.toString()
            val placeId = place.id

            // Insert the new place into database
            val contentValues = ContentValues()
            contentValues.put(PlaceContract.PlaceEntry.COLUMN_PLACE_ID, placeId)
            contentResolver.insert(PlaceContract.PlaceEntry.CONTENT_URI, contentValues)

            refreshData()
        }
    }

    override fun onResume() {
        super.onResume()

        // Initialize location permissions checkbox
        val locationPermissions = findViewById<CheckBox>(R.id.location_permission_checkbox)
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            locationPermissions.isChecked = false
        } else {
            locationPermissions.isChecked = true
            locationPermissions.isEnabled = false
        }
    }

    fun locationPermissionClicked(view: View) {
        ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_FINE_LOCATION)
    }
}
