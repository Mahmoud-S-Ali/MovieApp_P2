package com.example.android.movieapp.database;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by Toty on 9/20/2015.
 */
public class TestUriMatcher extends AndroidTestCase{
    private static final String MOVIE_ID_QUERY = "/99861";
    private static final long TEST_FAVORITE_RECORD_ID = 5L;

    // content://com.example.android.movieapp/favorite"
    private static final Uri TEST_FAVORITE_DIR = MovieContract.FavoriteEntry.CONTENT_URI;

    // content://com.example.android.movieapp/favorite?movie_id=%2F99861"
    private static final Uri TEST_FAVORITE_WITH_MOVIE_ID_DIR =
            MovieContract.FavoriteEntry.buildFavoriteMovieUriWithMovieId(MOVIE_ID_QUERY);

    // content://com.example.android.movieapp/trailer"
    private static final Uri TEST_TRAILER_DIR =
            MovieContract.TrailerEntry.CONTENT_URI;

    // content://com.example.android.movieapp/review"
    private static final Uri TEST_REVIEW_DIR =
            MovieContract.ReviewEntry.CONTENT_URI;

    /*
        This function tests that your UriMatcher returns the correct integer value
        for each of the Uri types that our ContentProvider can handle.
     */
    public void testUriMatcher() {
        UriMatcher testMatcher = MovieProvider.buildUriMatcher();

        assertEquals("Error: The FAVORITE URI was matched incorrectly.",
                testMatcher.match(TEST_FAVORITE_DIR), MovieProvider.FAVORITE);
        assertEquals("Error: The FAVORITE WITH ID URI was matched incorrectly.",
                testMatcher.match(TEST_FAVORITE_WITH_MOVIE_ID_DIR), MovieProvider.FAVORITE_WITH_MOVIE_ID);
        assertEquals("Error: The TRAILER URI was matched incorrectly.",
                testMatcher.match(TEST_TRAILER_DIR), MovieProvider.TRAILER);
        assertEquals("Error: The REVIEW URI was matched incorrectly.",
                testMatcher.match(TEST_REVIEW_DIR), MovieProvider.REVIEW);
    }
}
