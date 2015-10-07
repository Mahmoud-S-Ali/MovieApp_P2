package com.example.android.movieapp.movieData;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.android.movieapp.R;
import com.example.android.movieapp.Utility;
import com.example.android.movieapp.database.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by Toty on 9/25/2015.
 */
public class FetchMovieTask extends AsyncTask<String, Void, ArrayList<MyMovie>> {
    private final String LOG_TAG = FetchMovieTask.class.getName();
    private Context mContext;
    private MovieImageAdapter mMovieAdapter;
    private HashMap<String, String> mGenresMap;

    public FetchMovieTask(Context context, MovieImageAdapter movieAdapter) {
        mContext = context;
        mMovieAdapter = movieAdapter;

        mGenresMap = new HashMap<>();
        mGenresMap.put("28", "Action");
        mGenresMap.put("12", "Adventure");
        mGenresMap.put("16", "Animation");
        mGenresMap.put("35", "Comedy");
        mGenresMap.put("80", "Crime");
        mGenresMap.put("99", "Documentary");
        mGenresMap.put("18", "Drama");
        mGenresMap.put("10751", "Family");
        mGenresMap.put("10769", "Foreign");
        mGenresMap.put("36", "History");
        mGenresMap.put("27", "Horror");
        mGenresMap.put("10402", "Music");
        mGenresMap.put("14", "Fantasy");
        mGenresMap.put("9648", "Mystery");
        mGenresMap.put("10749", "Romance");
        mGenresMap.put("878", "Science Fiction");
        mGenresMap.put("10770", "TV Movie");
        mGenresMap.put("53", "Thriller");
        mGenresMap.put("10752", "War");
        mGenresMap.put("37", "Western");
    }

    @Override
    protected ArrayList<MyMovie> doInBackground(String... params) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        //This string will hold the json response as a string
        String movieJsonStr = "";

        String sortOrder = params[0];
        //Subtitute the string "key" with your real key
        String key = Utility.MOVIEAPI_KEY;
        String pageNum = params[1];

        // Check if the sortOrder is "favorite", In that case we fetch the data from the database
        if (mContext.getString(R.string.favorite_table).equals(sortOrder)) {
            Vector<ContentValues> cVVector = getFavoriteMoviesFromDB(null);
            return convertContentValuesToMyMovieList(cVVector);
        }

        try {
            Uri builtUri = buildMovieUri(sortOrder, pageNum);

            URL url = new URL(builtUri.toString());
            //Make sure that the url is built properly
            //Log.v(LOG_TAG, " Built URL: " + url.toString());

            // Create the request to themoviedatabase, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            //Read the input stream into string
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                //nothing to do
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                return null;
            }

            movieJsonStr += buffer.toString();
            //Log.v(LOG_TAG, "Movie string: " + movieJsonStr);

        } catch (IOException e) {
            //Log.e(LOG_TAG, "Error", e);
            movieJsonStr = null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            return getMoviesDataFromJson(movieJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    private Uri buildMovieUri(String sortOrder, String pageNum) {

        Uri uri;
        final String MOVIE_BASE_URL;
        final String ENCODING;

        final String PAGES_PARAM = "page";
        final String KEY_PARAM = "api_key";
        final String SORT_BY = "sort_by";

        if (sortOrder.equals(mContext.getString(R.string.pref_sort_popular))) {
                //|| sortOrder.equals(mContext.getString(R.string.pref_sort_topRated))) {

            MOVIE_BASE_URL = "http://api.themoviedb.org/3/discover/";
            ENCODING = "movie";

            uri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendEncodedPath(ENCODING)
                    .appendQueryParameter(SORT_BY, sortOrder)
                    .appendQueryParameter(PAGES_PARAM, (pageNum))
                    .appendQueryParameter(KEY_PARAM, Utility.MOVIEAPI_KEY)
                    .build();

        } else {

            MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/";
            ENCODING = sortOrder;

            uri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendEncodedPath(ENCODING)
                    .appendQueryParameter(PAGES_PARAM, (pageNum))
                    .appendQueryParameter(KEY_PARAM, Utility.MOVIEAPI_KEY)
                    .build();
        }

        return uri;
    }

    private ArrayList<MyMovie> getMoviesDataFromJson(String movieJsonStr)
            throws JSONException {

        if (movieJsonStr == null)
            return null;

        final String MDB_RESULTS     = mContext.getString(R.string.movie_results);
        final String MDB_MOVIEID     = mContext.getString(R.string.movie_id);
        final String MDB_TITLE       = mContext.getString(R.string.movie_title);
        final String MDB_GENRES      = mContext.getString(R.string.movie_genres);
        final String MDB_RELEASEDATE = mContext.getString(R.string.movie_release_date);
        final String MDB_VOTEAVG     = mContext.getString(R.string.movie_vote_average);
        final String MDB_OVERVIEW    = mContext.getString(R.string.movie_overview);
        final String MDB_BG          = mContext.getString(R.string.movie_background_url);
        final String MDB_POSTER      = mContext.getString(R.string.movie_poster_url);
        final String POSTER_SIZE     = mContext.getString(R.string.poster_size);
        final String BG_SIZE         = mContext.getString(R.string.background_size);

        JSONObject movieJson = new JSONObject(movieJsonStr);
        JSONArray movieArray = movieJson.getJSONArray(MDB_RESULTS);

        int totalNumOfMovies = movieArray.length();
        ArrayList<MyMovie> movieList = new ArrayList<>();
        String IMAGE_BASE_URL = "http://image.tmdb.org/t/p/";

        for (int i = 0; i < totalNumOfMovies; i++) {
            // Get the JSON object representing the movie data
            HashMap<String, String> movieDataMap = new HashMap<>();
            JSONObject movieData = movieArray.getJSONObject(i);

            JSONArray genresJSONArr = movieData.getJSONArray("genre_ids");

            movieDataMap.put(MDB_MOVIEID, movieData.getString(MDB_MOVIEID));
            movieDataMap.put(MDB_TITLE, movieData.getString(MDB_TITLE));
            movieDataMap.put(MDB_GENRES, getGenres(genresJSONArr));
            movieDataMap.put(MDB_RELEASEDATE, movieData.getString(MDB_RELEASEDATE));
            movieDataMap.put(MDB_VOTEAVG, movieData.getString(MDB_VOTEAVG));
            movieDataMap.put(MDB_OVERVIEW, movieData.getString(MDB_OVERVIEW));

            String posterUniqueUrl     = movieData.getString(MDB_POSTER);
            String backgroundUniqueUrl = movieData.getString(MDB_BG);
            Uri moviePosterUri;
            Uri movieBackgroundUri;

            //Make sure that the movie has an available poster
            if (posterUniqueUrl != "null") {
                moviePosterUri = Uri.parse(IMAGE_BASE_URL).buildUpon()
                        .appendEncodedPath(POSTER_SIZE)
                        .appendEncodedPath(posterUniqueUrl)
                        .build();

                movieDataMap.put(MDB_POSTER, moviePosterUri.toString());
            }

            if (backgroundUniqueUrl != "null") {
                movieBackgroundUri = Uri.parse(IMAGE_BASE_URL).buildUpon()
                        .appendEncodedPath(BG_SIZE)
                        .appendEncodedPath(backgroundUniqueUrl)
                        .build();

                movieDataMap.put(MDB_BG, movieBackgroundUri.toString());
            }

            movieList.add(new MyMovie(movieDataMap));
        }

        return movieList;
    }

    private String getGenres(JSONArray genresJSONARR)
            throws JSONException {

        String genre = "";

        for (int i = 0; i < genresJSONARR.length(); i++) {
            genre += mGenresMap.get(String.valueOf(genresJSONARR.get(i)));

            if (i != genresJSONARR.length() - 1)
                genre += "/";
        }

        return genre;
    }

    @Override
    protected void onPostExecute(ArrayList<MyMovie> result) {
        if (result != null) {
            mMovieAdapter.addAll(result);
        }
    }

    private Vector<ContentValues> getFavoriteMoviesFromDB(String sortOrder) {
        // Students: Uncomment the next lines to display what what you stored in the bulkInsert
        Cursor cur = mContext.getContentResolver().query(MovieContract.FavoriteEntry.CONTENT_URI,
                null, null, null, sortOrder);

        Vector<ContentValues> cVVector = new Vector<ContentValues>(cur.getCount());
        if ( cur.moveToFirst() ) {
            do {
                ContentValues cv = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cur, cv);
                cVVector.add(cv);
            } while (cur.moveToNext());

            return cVVector;
        }

        return null;
    }
    private ArrayList<MyMovie> convertContentValuesToMyMovieList(Vector<ContentValues> valuesVector) {
        if (valuesVector == null)
            return null;

        ArrayList<MyMovie> favoriteMovies = new ArrayList<>();

        for (int i = 0; i < valuesVector.size(); i++) {
            HashMap<String, String> movieMap = new HashMap<>();
            ContentValues value = valuesVector.elementAt(i);
            movieMap.put(mContext.getString(R.string.movie_id),
                    (String) value.get(MovieContract.FavoriteEntry.COLUMN_MOVIE_ID));

            movieMap.put(mContext.getString(R.string.movie_title),
                    (String) value.get(MovieContract.FavoriteEntry.COLUMN_ORIGINAL_TITLE));

            movieMap.put(mContext.getString(R.string.movie_genres),
                    (String) value.get(MovieContract.FavoriteEntry.COLUMN_GENRES));

            movieMap.put(mContext.getString(R.string.movie_poster_url),
                    (String) value.get(MovieContract.FavoriteEntry.COLUMN_MOVIE_POSTER));

            movieMap.put(mContext.getString(R.string.movie_background_url),
                    (String) value.get(MovieContract.FavoriteEntry.COLUMN_BACKDROP_IMG));

            movieMap.put(mContext.getString(R.string.movie_release_date),
                    (String) value.get(MovieContract.FavoriteEntry.COLUMN_RELEASE_DATE));

            movieMap.put(mContext.getString(R.string.movie_vote_average),
                    (String) value.get(MovieContract.FavoriteEntry.COLUMN_VOTE_AVG));

            movieMap.put(mContext.getString(R.string.movie_overview),
                    (String) value.get(MovieContract.FavoriteEntry.COLUMN_OVERVIEW));

            MyMovie movie = new MyMovie(movieMap);
            favoriteMovies.add(movie);
        }

        return favoriteMovies;
    }
}
