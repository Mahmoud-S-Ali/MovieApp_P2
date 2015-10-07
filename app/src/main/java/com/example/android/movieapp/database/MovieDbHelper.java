package com.example.android.movieapp.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.android.movieapp.database.MovieContract.FavoriteEntry;
import com.example.android.movieapp.database.MovieContract.ReviewEntry;
import com.example.android.movieapp.database.MovieContract.TrailerEntry;

/**
 * Created by Toty on 9/18/2015.
 */
public class MovieDbHelper extends SQLiteOpenHelper{
    //This value should be incremented with each change in the database schema
    private static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "movie.db";

    public MovieDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String tag = this.getClass().getName();
        // Create a table to hold locations.  A location consists of the string supplied in the
        // location setting, the city name, and the latitude and longitude
        final String SQL_CREATE_FAVORITE_TABLE = "CREATE TABLE " + FavoriteEntry.TABLE_NAME + " (" +
                FavoriteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                FavoriteEntry.COLUMN_MOVIE_ID + " TEXT UNIQUE, " +
                FavoriteEntry.COLUMN_ORIGINAL_TITLE + " TEXT NOT NULL, " +

                FavoriteEntry.COLUMN_MOVIE_POSTER + " TEXT NOT NULL, " +
                FavoriteEntry.COLUMN_BACKDROP_IMG + " TEXT NOT NULL, " +

                FavoriteEntry.COLUMN_GENRES + " TEXT NOT NULL, " +
                FavoriteEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                FavoriteEntry.COLUMN_VOTE_AVG + " REAL NOT NULL, " +
                FavoriteEntry.COLUMN_OVERVIEW + " TEXT NOT NULL" +
                ");";

        //Printing the favorite sql statement
        Log.v(tag, SQL_CREATE_FAVORITE_TABLE);

        final String SQL_CREATE_TRAILER_TABLE = "CREATE TABLE " + TrailerEntry.TABLE_NAME + " (" +
                TrailerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                TrailerEntry.COLUMN_FAVORITE_RECORD_ID + " INTEGER NOT NULL, " +

                TrailerEntry.COLUMN_TRAILER_NAME + "TEXT NOT NULL, " +

                //This will contain the key for the movie trailer on youtube
                TrailerEntry.COLUMN_TRAILER_KEY + " TEXT NOT NULL, " +

                // Set up the movie key column as a foreign key to  favorite table.
                " FOREIGN KEY (" + TrailerEntry.COLUMN_FAVORITE_RECORD_ID + ") REFERENCES " +
                FavoriteEntry.TABLE_NAME + " (" + FavoriteEntry._ID + ")" +
                ");";

        Log.v(tag, SQL_CREATE_TRAILER_TABLE);

        final String SQL_CREATE_REVIEW_TABLE = "CREATE TABLE " + ReviewEntry.TABLE_NAME + " (" +
                ReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                ReviewEntry.COLUMN_FAVORITE_RECORD_ID + " INTEGER NOT NULL, " +

                ReviewEntry.COLUMN_AUTHOR_NAME + " TEXT NOT NULL, " +

                // The content of the author's review
                ReviewEntry.COLUMN_REVIEW_CONTENT + " TEXT NOT NULL, " +

                // Set up the movie key column as a foreign key to  favorite table.
                " FOREIGN KEY (" + ReviewEntry.COLUMN_FAVORITE_RECORD_ID + ") REFERENCES " +
                FavoriteEntry.TABLE_NAME + " (" + FavoriteEntry._ID + ")" +
                " );";

        Log.v(tag, SQL_CREATE_REVIEW_TABLE);

        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITE_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_TRAILER_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_REVIEW_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        //Since the data in the database will be entered by the user, we don't want
        //to wipe the data when updating the database
        //We shall alter the table instead
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoriteEntry.TABLE_NAME);
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TrailerEntry.TABLE_NAME);
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ReviewEntry.TABLE_NAME);
        //onCreate(sqLiteDatabase);
        sqLiteDatabase.execSQL("ALTER TABLE " + FavoriteEntry.TABLE_NAME + " ADD COLUMN " +
        " COLUMN_NAME");
    }
}
