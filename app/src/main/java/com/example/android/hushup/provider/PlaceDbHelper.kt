package com.example.android.hushup.provider

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by ngtrnhan1205 on 12/7/17.
 */
class PlaceDbHelper(context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        val DATABASE_NAME = "hushup.db"
        val DATABASE_VERSION = 1;
    }

    override fun onCreate(db: SQLiteDatabase) {

        // Create a table to hold the places data
        val SQL_CREATE_PLACES_TABLE = "CREATE TABLE " + PlaceContract.PlaceEntry.TABLE_NAME + " (" +
                PlaceContract.PlaceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                PlaceContract.PlaceEntry.COLUMN_PLACE_ID + " TEXT NOT NULL, " +
                "UNIQUE (" + PlaceContract.PlaceEntry.COLUMN_PLACE_ID + ") ON CONFLICT REPLACE" +
                "); ";

        db.execSQL(SQL_CREATE_PLACES_TABLE);
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + PlaceContract.PlaceEntry.TABLE_NAME);
        onCreate(db);
    }
}