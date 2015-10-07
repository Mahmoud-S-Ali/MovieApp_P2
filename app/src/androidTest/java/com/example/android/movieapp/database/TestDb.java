/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.movieapp.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    /*
        This only tests that the Favorite table has the correct columns
     */
    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(MovieContract.FavoriteEntry.TABLE_NAME);
        tableNameHashSet.add(MovieContract.TrailerEntry.TABLE_NAME);
        tableNameHashSet.add(MovieContract.ReviewEntry.TABLE_NAME);

        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new MovieDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that the database doesn't contain any of the tables
        assertTrue("Error: Your database was created without the tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + MovieContract.FavoriteEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> favoriteColumnHashSet = new HashSet<String>();
        favoriteColumnHashSet.add(MovieContract.FavoriteEntry._ID);
        favoriteColumnHashSet.add(MovieContract.FavoriteEntry.COLUMN_ORIGINAL_TITLE);
        favoriteColumnHashSet.add(MovieContract.FavoriteEntry.COLUMN_MOVIE_POSTER);
        favoriteColumnHashSet.add(MovieContract.FavoriteEntry.COLUMN_BACKDROP_IMG);
        favoriteColumnHashSet.add(MovieContract.FavoriteEntry.COLUMN_VOTE_AVG);
        favoriteColumnHashSet.add(MovieContract.FavoriteEntry.COLUMN_RELEASE_DATE);
        favoriteColumnHashSet.add(MovieContract.FavoriteEntry.COLUMN_OVERVIEW);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            favoriteColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that the database doesn't contain all of the required movie
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required movie entry columns",
                favoriteColumnHashSet.isEmpty());
        db.close();
    }

    /*
        Here is where we will build code to test that we can insert and query the
        movie database.
    */
    public void testFavoriteTable() {
        insertFavoriteMovie();
    }

    public void testTrailerTable() {
        // First insert the favorite movie, and then use the movieRowId to insert
        // the trailer.
        long favoriteMovieId = insertFavoriteMovie();

        // First step: Get reference to writable database
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create ContentValues of what you want to insert
        ContentValues values  = TestUtilities.createTrailerValues(favoriteMovieId);

        // Insert ContentValues into database and get a row ID back
        long trailerId = db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, values);

        // Query the database and receive a Cursor back
        Cursor cursor = db.query(MovieContract.TrailerEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        // Move the cursor to a valid database row
        assertTrue("Error: No data has been inserted into trailer table", cursor.moveToFirst());

        // Validate data in resulting Cursor with the original ContentValues
        TestUtilities.validateCursor("Invalid data in trailer cursor", cursor, values);

        // Finally, close the cursor and database
        db.close();
        cursor.close();
    }

    public void testReviewTable() {
        // First insert the favorite movie, and then use the movieRowId to insert
        // the review.
        long favoriteMovieId = insertFavoriteMovie();

        // First step: Get reference to writable database
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create ContentValues of what you want to insert
        ContentValues values  = TestUtilities.createReviewValues(favoriteMovieId);

        // Insert ContentValues into database and get a row ID back
        long reviewRowId = db.insert(MovieContract.ReviewEntry.TABLE_NAME, null, values);

        // Query the database and receive a Cursor back
        Cursor cursor = db.query(MovieContract.ReviewEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        // Move the cursor to a valid database row
        assertTrue("Error: No data has been inserted into review table", cursor.moveToFirst());

        // Validate data in resulting Cursor with the original ContentValues
        TestUtilities.validateCursor("Invalid data in review cursor", cursor, values);

        // Finally, close the cursor and database
        db.close();
        cursor.close();
    }


    public long insertFavoriteMovie() {
        // Get reference to writable database
        MovieDbHelper dbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create ContentValues of what we want to insert
        ContentValues values = TestUtilities.createFavoriteMovieValues();

        // Insert ContentValues into database and get a row ID back
        long rawId = db.insert(MovieContract.FavoriteEntry.TABLE_NAME, null, values);

        // Query the database and receive a Cursor back
        Cursor cursor = db.query(MovieContract.FavoriteEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null);

        // Move the cursor to a valid database row
        assertTrue("Error: data wasn't inserted in favorite table", cursor.moveToFirst());

        // Validate data in resulting Cursor with the original ContentValues
        TestUtilities.validateCursor("Invalid data in favorite table cursor", cursor, values);

        // Make sure that there is only one record
        assertFalse("Error: There should be only one record", cursor.moveToNext());

        // Finally, close the cursor and database
        db.close();
        cursor.close();

        return rawId;
    }
}
