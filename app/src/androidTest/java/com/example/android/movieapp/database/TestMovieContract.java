package com.example.android.movieapp.database;

import android.net.Uri;
import android.test.AndroidTestCase;

/**
 * Created by Toty on 9/20/2015.
 */
public class TestMovieContract extends AndroidTestCase {
    private static final String FAVORITE_MOVIE_ID_QUERY = "/99861";

    public void testBuildFavoriteMovie() {
        Uri movieUri = MovieContract.FavoriteEntry.buildFavoriteMovieUriWithMovieId(FAVORITE_MOVIE_ID_QUERY);
        assertNotNull("Error: Null Uri returned.  You must fill-in buildFavoriteMovieUriWithMovieId in " +
                        "MovieContract.",
                movieUri);
        assertEquals("Error: Favorite Movie Uri doesn't match our expected result",
                movieUri.toString(),
                "content://com.example.android.movieapp/favorite/%2F99861");
    }
}
