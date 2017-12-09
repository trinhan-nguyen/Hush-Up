package com.example.android.hushup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Created by ngtrnhan1205 on 12/8/17.
 */
class GeofenceBroadcastReceiver: BroadcastReceiver() {
    companion object {
        val TAG = GeofenceBroadcastReceiver::class.simpleName
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG, "onReceive called")
    }
}