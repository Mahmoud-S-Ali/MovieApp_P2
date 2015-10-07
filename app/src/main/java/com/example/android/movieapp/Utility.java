package com.example.android.movieapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.example.android.movieapp.database.MovieContract;

/**
 * Created by Toty on 9/25/2015.
 */
public class Utility {
    public static final String MOVIEAPI_KEY = "PLEASE INSERT YOUR MOVIE API KEY HERE";

    public static String getPreferredSort(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sort_key),
                context.getString(R.string.pref_sort_default));
    }

    public static String getPreferredTheme(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_theme_key),
                context.getString(R.string.pref_theme_default));
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // Check if a specific movie is in favorites or not
    public static boolean isMovieFavorite(Context context, String movieId) {
        String selection = MovieContract.FavoriteEntry.COLUMN_MOVIE_ID + " = ?";
        String[] selectionArgs = {movieId};

        Cursor cursor = context.getContentResolver().query(MovieContract.FavoriteEntry.CONTENT_URI,
                new String[]{MovieContract.FavoriteEntry._ID},
                selection,
                selectionArgs,
                null);

        if (cursor.moveToFirst())
            return true;

        return false;
    }

    public static float pixelsToDp(Context context, int pixels) {
        float dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixels, context.getResources().getDisplayMetrics());
        return dp;
    }

    public static void setActionBarTitle(Context context, ActionBar actionBar, String title) {
        actionBar.setTitle(title);
    }

    public static void updateActionBarTheme(Context context, ActionBar actionBar, String theme) {

        int actionBarColor = R.color.actionbar_color;
        switch (theme) {
            case "red":       actionBarColor = R.color.red;     break;
            case "green":     actionBarColor = R.color.green;   break;
            case "blue":      actionBarColor = R.color.blue;    break;
        }

        actionBar.setBackgroundDrawable(new ColorDrawable(
                context.getResources().getColor(actionBarColor)));
    }

    public static void updateTitlesTheme(Context context, View view, String theme) {

        TextView overviewText = (TextView) view.findViewById(R.id.detail_overview_title);
        TextView trailersText = (TextView) view.findViewById(R.id.detail_trailers_title);
        TextView reviewsText = (TextView)  view.findViewById(R.id.detail_reviews_title);

        int titlesColor = R.color.titles_bg_color;

        switch (theme) {
            case "red":       titlesColor = R.color.transparentRed;     break;
            case "green":     titlesColor = R.color.transparentGreen;   break;
            case "blue":      titlesColor = R.color.transparentBlue;    break;
        }

        overviewText.setBackgroundColor(context.getResources().getColor(titlesColor));
        overviewText.setBackgroundColor(context.getResources().getColor(titlesColor));
        trailersText.setBackgroundColor(context.getResources().getColor(titlesColor));
        reviewsText.setBackgroundColor(context.getResources().getColor(titlesColor));
    }

    public static String getSortTitle(Context context, String sortOrder) {
        String title;

        if (sortOrder.equals(context.getString(R.string.pref_sort_popular)))
            title = "Most Popular";
        else if (sortOrder.equals(context.getString(R.string.pref_sort_topRated)))
            title = "Top Rated";
        else if (sortOrder.equals(context.getString(R.string.pref_sort_nowPlaying)))
            title = "Now Playing";
        else if (sortOrder.equals(context.getString(R.string.pref_sort_upcoming)))
            title = "Upcoming";
        else
            title = "Favorites";

        return title;
    }
}
