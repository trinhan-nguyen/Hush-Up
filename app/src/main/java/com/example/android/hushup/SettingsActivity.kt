package com.example.android.hushup

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.CheckBox

/**
 * Created by ngtrnhan1205 on 12/11/17.
 */
class SettingsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.settings)
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

        // Initialize ringer permissions checkbox
        val ringerPermissions = findViewById<CheckBox>(R.id.ringer_permission_checkbox)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Check for API supports such permissions and is granted
        if (android.os.Build.VERSION.SDK_INT >= 24
                && !notificationManager.isNotificationPolicyAccessGranted) {
            ringerPermissions.isChecked = false
        } else {
            ringerPermissions.isChecked = true
            ringerPermissions.isEnabled = false
        }
    }

    fun locationPermissionClicked(view: View) {
        ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                MainActivity.PERMISSION_REQUEST_FINE_LOCATION)
    }

    fun ringerPermissionClicked(view: View) {
        val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        startActivity(intent)
    }
}