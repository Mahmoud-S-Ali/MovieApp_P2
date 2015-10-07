package com.example.android.movieapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.example.android.movieapp.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

public class TestUtilities extends AndroidTestCase {

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    /*
        Creating values for Trailer Table for testing
     */
    static ContentValues createTrailerValues(long movieRowId) {
        ContentValues trailerValues = new ContentValues();
        trailerValues.put(MovieContract.TrailerEntry.COLUMN_FAVORITE_RECORD_ID, movieRowId);
        trailerValues.put(MovieContract.TrailerEntry.COLUMN_TRAILER_KEY, "tmeOjFno6Do");

        return trailerValues;
    }

    /*
        Creating values for Review Table for testing
     */
    static ContentValues createReviewValues(long movieRowId) {
        ContentValues reviewValues = new ContentValues();
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_FAVORITE_RECORD_ID, movieRowId);
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_AUTHOR_NAME, "Phileas Fogg");
        reviewValues.put(MovieContract.ReviewEntry.COLUMN_REVIEW_CONTENT, "Fabulous action movie.");

        return reviewValues;
    }

    /*
     */
    static ContentValues createFavoriteMovieValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(MovieContract.FavoriteEntry.COLUMN_MOVIE_ID, "76341");
        testValues.put(MovieContract.FavoriteEntry.COLUMN_ORIGINAL_TITLE, "Mad Max: Fury Road");
        testValues.put(MovieContract.FavoriteEntry.COLUMN_MOVIE_POSTER, "/kqjL17yufvn9OVLyXYpvtyrFfak.jpg");
        testValues.put(MovieContract.FavoriteEntry.COLUMN_BACKDROP_IMG, "/tbhdm8UJAb4ViCTsulYFL3lxMCd.jpg");
        testValues.put(MovieContract.FavoriteEntry.COLUMN_RELEASE_DATE, "2015-05-15");
        testValues.put(MovieContract.FavoriteEntry.COLUMN_VOTE_AVG, 7.6);
        testValues.put(MovieContract.FavoriteEntry.COLUMN_OVERVIEW, "An apocalyptic story.");

        return testValues;
    }

    static long insertFavoriteMovieValues(Context context) {
        // insert our test records into the database
        MovieDbHelper dbHelper = new MovieDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createFavoriteMovieValues();

        long movieRowId;
        movieRowId = db.insert(MovieContract.FavoriteEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert Favorite Movie Values", movieRowId != -1);

        return movieRowId;
    }

    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
