package com.example.android.hushup

import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.media.AudioManager
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
import android.widget.Switch
import com.example.android.hushup.provider.PlaceContract
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.location.places.PlaceBuffer
import com.google.android.gms.location.places.ui.PlacePicker
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import android.view.MenuItem

class MainActivity : AppCompatActivity(),
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
        val PERMISSION_REQUEST_FINE_LOCATION = 111
        val PLACE_PICKER_REQUEST = 999
    }

    private lateinit var mAdapter: PlaceListAdapter
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mClient: GoogleApiClient
    private lateinit var mGeofencing: Geofencing
    private var mIsEnabled: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up the recycler view
        mRecyclerView = findViewById<RecyclerView>(R.id.places_list_recycler_view)
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mAdapter = PlaceListAdapter(this, null)
        mRecyclerView.adapter = mAdapter

        // Swipe to delete
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean { return false }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                val id = viewHolder.itemView.tag.toString()
                val uri = PlaceContract.PlaceEntry.CONTENT_URI.buildUpon().appendPath(id).build()
                contentResolver.delete(uri, null, null)
                refreshData()
            }
        }).attachToRecyclerView(mRecyclerView)

        // Initialize the switch state and Handle enable/disable switch change
        mIsEnabled = getPreferences(MODE_PRIVATE)
                .getBoolean(getString(R.string.setting_enabled), false)
        val onOffSwitch = findViewById<Switch>(R.id.enable_switch);

        onOffSwitch.isChecked = mIsEnabled;

        onOffSwitch.setOnCheckedChangeListener({
            _, isChecked ->  run {
                val editor = getPreferences(Context.MODE_PRIVATE).edit()
                editor.putBoolean(getString(R.string.setting_enabled), isChecked)
                mIsEnabled = isChecked
                editor.commit()
                if (isChecked) mGeofencing.registerAllGeofences()
                else {
                    mGeofencing.unRegisterAllGeofences()
                    // transitionType == 0 is special in this case
                    GeofenceBroadcastReceiver.sendNotification(this, 0)
                    GeofenceBroadcastReceiver.setRingerMode(this, AudioManager.RINGER_MODE_NORMAL)
                }
            }
        })

        // Build Api Client
        mClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build()

        mGeofencing = Geofencing(this, mClient)
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
                mAdapter.swapPlaces(places)
                mGeofencing.updateGeofencesList(places)
                if (mIsEnabled) mGeofencing.registerAllGeofences()
            }
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
            val placeId = place.id

            // Insert the new place into database
            val contentValues = ContentValues()
            contentValues.put(PlaceContract.PlaceEntry.COLUMN_PLACE_ID, placeId)
            contentResolver.insert(PlaceContract.PlaceEntry.CONTENT_URI, contentValues)

            refreshData()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        super.onOptionsItemSelected(item)
        val match = item?.itemId
        when (match) {
            R.id.settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.help -> {
                return false
            }
            else -> return false
        }
    }
}
