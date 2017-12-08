package com.example.android.hushup.provider

import android.net.Uri
import android.provider.BaseColumns

/**
 * Created by ngtrnhan1205 on 12/7/17.
 */
class PlaceContract {
    companion object {
        // The authority, which is how your code knows which Content Provider to access
        val AUTHORITY = "com.example.android.hushup";

        // The base content URI = "content://" + <authority>
        val BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY) as Uri;

        // Define the possible paths for accessing data in this contract
        // This is the path for the "places" directory
        val PATH_PLACES = "places";
    }

    open class IdBaseColumns  {
        val _ID = "_id"
    }

    open class PlaceEntry: BaseColumns {
        companion object: IdBaseColumns() {
            // TaskEntry content URI = base content URI + path
            val CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLACES).build();
            val TABLE_NAME = "places";
            val COLUMN_PLACE_ID = "placeID";
        }
    }
}