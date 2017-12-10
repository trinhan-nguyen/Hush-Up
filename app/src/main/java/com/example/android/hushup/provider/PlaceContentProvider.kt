package com.example.android.hushup.provider

import android.content.*
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.google.android.gms.location.places.Place

/**
 * Created by ngtrnhan1205 on 12/7/17.
 */
open class PlaceContentProvider(): ContentProvider() {
    companion object {
        // Code to know which type of calls
        val PLACES = 100
        val PLACE_WITH_ID = 101
    }

    private val sUriMatcher = buildUriMatcher()
    private lateinit var mPlaceDbHelper: PlaceDbHelper

    // Define a static buildUriMatcher method that associates URI's with their int match
    open fun buildUriMatcher(): UriMatcher {
        // Initialize a UriMatcher
        val uriMatcher = UriMatcher(UriMatcher.NO_MATCH);
        // Add URI matches
        uriMatcher.addURI(PlaceContract.AUTHORITY, PlaceContract.PATH_PLACES, PLACES)
        uriMatcher.addURI(PlaceContract.AUTHORITY, PlaceContract.PATH_PLACES + "/*", PLACE_WITH_ID)
        return uriMatcher
    }

    override fun insert(uri: Uri?, values: ContentValues?): Uri {
        val db = mPlaceDbHelper.getWritableDatabase();

        // Write URI matching code to identify the match for the places directory
        val match = sUriMatcher.match(uri);
        lateinit var returnUri: Uri; // URI to be returned
        when (match) {
            PLACES -> {
                // Insert new values into the database
                var id = db.insert(PlaceContract.PlaceEntry.TABLE_NAME, null, values)
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(PlaceContract.PlaceEntry.CONTENT_URI, id)
                } else {
                    throw android.database.SQLException ("Failed to insert row into " + uri);
                }
            }
            // Default case throws an UnsupportedOperationException
            else -> throw UnsupportedOperationException("Unknown uri: " + uri)
        }

        // Notify the resolver if the uri has been changed, and return the newly inserted URI
        context!!.contentResolver.notifyChange(uri, null)

        // Return constructed uri (this points to the newly inserted row of data)
        return returnUri;
    }

    override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor {

        // Get access to underlying database (read-only for query)
        val db = mPlaceDbHelper.readableDatabase

        // Write URI match code and set a variable to return a Cursor
        val match = sUriMatcher.match(uri)
        val retCursor: Cursor

        when (match) {
        // Query for the places directory
            PLACES -> retCursor = db.query(PlaceContract.PlaceEntry.TABLE_NAME,
                    projection, selection, selectionArgs,null, null, sortOrder)
        // Default exception
            else -> throw UnsupportedOperationException("Unknown uri: " + uri)
        }

        // Set a notification URI on the Cursor and return that Cursor
        retCursor.setNotificationUri(context!!.contentResolver, uri)

        // Return the desired Cursor
        return retCursor
    }

    override fun onCreate(): Boolean {
        val context = getContext();
        mPlaceDbHelper = PlaceDbHelper(context);
        return true;
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        // Get access to underlying database
        val db = mPlaceDbHelper.writableDatabase
        val match = sUriMatcher.match(uri)
        // Keep track of the number of updated places
        val placesUpdated: Int

        when (match) {
            PLACE_WITH_ID -> {
                // Get the place ID from the URI path
                val id = uri.pathSegments[1]
                // Use selections/selectionArgs to filter for this ID
                placesUpdated = db.update(PlaceContract.PlaceEntry.TABLE_NAME,
                        values, "_id=?", arrayOf(id))
            }
        // Default exception
            else -> throw UnsupportedOperationException("Unknown uri: " + uri)
        }

        // Notify the resolver of a change and return the number of items updated
        if (placesUpdated != 0) {
            // A place (or more) was updated, set notification
            context!!.contentResolver.notifyChange(uri, null)
        }
        // Return the number of places deleted
        return placesUpdated
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        // Get access to the database and write URI matching code to recognize a single item
        val db = mPlaceDbHelper.writableDatabase
        val match = sUriMatcher.match(uri)
        // Keep track of the number of deleted places
        var placesDeleted: Int // starts as 0
        when (match) {
        // Handle the single item case, recognized by the ID included in the URI path
            PLACE_WITH_ID -> {
                // Get the place ID from the URI path
                val id = uri.pathSegments[1];
                // Use selections/selectionArgs to filter for this placeID
                placesDeleted = db.delete(PlaceContract.PlaceEntry.TABLE_NAME, "placeID=?", arrayOf(id))
            }
            else -> throw UnsupportedOperationException("Unknown uri: " + uri)
        }
        // Notify the resolver of a change and return the number of items deleted
        if (placesDeleted != 0) {
            // A place (or more) was deleted, set notification
            context!!.contentResolver.notifyChange(uri, null)
        }
        // Return the number of places deleted
        return placesDeleted
    }

    override fun getType(uri: Uri?): String {
        throw UnsupportedOperationException("Not yet implemented")
    }
}