package com.example.android.movieapp.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Toty on 9/18/2015.
 * Defines 3 tables (Favorites, Trailers, Riviews) and their column names for the Movie database.
 */
public class MovieContract {

    // The content authority is the name of the entire content provider
    // We choose it to be the package name of the app which is guranteed to be unique on device
    public static final String CONTENT_AUTHORITY = "com.example.android.movieapp";

    // Apps will use this base uri to contact the content provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths
    public static final String PATH_FAVORITE = "favorite";
    public static final String PATH_TRAILER = "trailer";
    public static final String PATH_REVIEW = "review";

    public static final class FavoriteEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAVORITE;

        // Table name
        public static final String TABLE_NAME = "favorite";

        //Columns with Original Title of the movie
        //Movie id as returned from the api, will be used to get rivews and trailers
        public static final String COLUMN_MOVIE_ID = "movie_id";

        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_GENRES = "genres";
        public static final String COLUMN_MOVIE_POSTER = "movie_poster";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_VOTE_AVG = "vote_avg";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_BACKDROP_IMG = "backdrop_img";

        public static Uri buildFavoriteMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildFavoriteMovieUriWithMovieId(String movieId) {
            return CONTENT_URI.buildUpon().appendPath(movieId).build();
        }

        public static String getMovieIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    /* Inner class that defines the table contents of the weather table */
    public static final class TrailerEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRAILER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILER;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRAILER;

        // Table name
        public static final String TABLE_NAME = "trailer";

        //This key will be used as a foreign key and will join the movie id
        public static final String COLUMN_FAVORITE_RECORD_ID = "movie_row_id";

        public static final String COLUMN_TRAILER_NAME = "name";

        // This holds the trailer key that will be passed to the youtube path
        public static final String COLUMN_TRAILER_KEY = "trailer_key";

        public static Uri buildTrailerUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildTrailerUriWithMovieId(String movieId) {
            return CONTENT_URI.buildUpon().appendQueryParameter(COLUMN_FAVORITE_RECORD_ID, movieId).build();
        }
    }

    public static final class ReviewEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEW).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_REVIEW;

        // Table name
        public static final String TABLE_NAME = "review";

        //This key will be used as a foreign key and will join the movie record id
        public static final String COLUMN_FAVORITE_RECORD_ID = "movie_row_id";

        public static final String COLUMN_AUTHOR_NAME = "author_name";

        //This will hold the content of the author's opinion
        public static final String COLUMN_REVIEW_CONTENT = "content";

        public static Uri buildReviewUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildReviewUriWithMovieId(String movieId) {
            return CONTENT_URI.buildUpon().appendQueryParameter(COLUMN_FAVORITE_RECORD_ID, movieId).build();
        }
    }
}
