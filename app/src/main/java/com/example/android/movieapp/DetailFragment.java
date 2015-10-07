package com.example.android.movieapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.movieapp.database.MovieContract;
import com.example.android.movieapp.movieData.MyMovie;
import com.example.android.movieapp.reviewsAndTrailersData.Review;
import com.example.android.movieapp.reviewsAndTrailersData.ReviewAdapter;
import com.example.android.movieapp.reviewsAndTrailersData.Trailer;
import com.squareup.picasso.Picasso;

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
 * Created by Toty on 9/21/2015.
 */
public class DetailFragment extends Fragment {
    private ArrayAdapter<String> mTrailerAdapter;
    private ReviewAdapter mReviewAdapter;

    private MyMovie mCurrentMovie;
    private ArrayList<Trailer> mCurrentMovieTrailerData;
    private ArrayList<Review> mCurrentMovieReviewData;

    private View mRootView;
    private ShareActionProvider mShareActionProvider;

    //This flag will be used to update favorites when the button is clicked
    private static boolean mFavoriteButtonClicked = false;
    private boolean mFavoriteButtonStatus;
    private boolean mLoadingTrailersFinished = false;
    private boolean mLoadingReviewsFinished = false;

    private String mFavButtomMsg;       //This will be used for the toast of favorite button


    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_detail, container, false);
        initTrailerReviewViews();

        mRootView.findViewById(R.id.detail_overview_title).setBackgroundColor(getResources().getColor(R.color.red));

        Bundle arguments = getArguments();
        if (arguments != null) {

            mCurrentMovie = arguments.getParcelable(MovieFragment.MOVIE);
            String movieId = mCurrentMovie.getMovieDetails().get(getString(R.string.movie_id));

            // Check if the movie is a favorite so that to change the fav button color
            mFavoriteButtonStatus = Utility.isMovieFavorite(getActivity(), movieId);
            updateFavButton(mRootView, mFavoriteButtonStatus);

            new FetchTrailersTask(getActivity()).execute(movieId);
            new FetchReviewsTask(getActivity()).execute(movieId);
            setDetailFragmentViews(mRootView, mCurrentMovie.getMovieDetails());
            mRootView.findViewById(R.id.detail_main_parent).setVisibility(View.VISIBLE);
        }


        final ImageButton favoriteButton = (ImageButton) mRootView.findViewById(R.id.detail_button_favorite);
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mFavoriteButtonClicked = true;
                mFavoriteButtonStatus = !mFavoriteButtonStatus;
                updateFavButton(mRootView, mFavoriteButtonStatus);

                Toast toast = Toast.makeText(getActivity(), mFavButtomMsg, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 100);
                toast.show();

                new FetchMovieDatabase(getActivity()).execute(
                        mCurrentMovie.getMovieDetails(),
                        mCurrentMovieTrailerData,
                        mCurrentMovieReviewData);
            }
        });

        return mRootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.detailfragment, menu);

        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider and hold onto it to set/change the share intent.
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mCurrentMovieTrailerData != null) {
            mShareActionProvider.setShareIntent(createShareTrailerIntent());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Utility.updateTitlesTheme(getActivity(), mRootView, MainActivity.getCurrentTheme());
    }

    private Intent createShareTrailerIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                mCurrentMovieTrailerData.get(0).getLink());
        return shareIntent;
    }

    private Intent createShareNameIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                mCurrentMovie.getMovieDetails().get(getString(R.string.movie_title)));
        return shareIntent;
    }

    public void onSortChanged(String sortOder) {
    }



    // This will be used when the user presses a movie to show the detail view
    // It will fetch the trailers and reviews data, then set the corresponding view
    // with this new data
    public class FetchTrailersTask extends AsyncTask<String, Void, ArrayList<Trailer>> {
        private final String LOG_TAG = FetchTrailersTask.class.getName();
        private final Context mContext;

        public FetchTrailersTask(Context context) {
            mContext = context;
        }

        private Uri buildTrailerUri(String movieId) {
            //Construct the uri for trailer using a URI builder
            final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/";
            final String KEY_PARAM = "api_key";
            final String REQUIRED_TYPE = "videos";

            Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendPath(movieId + "/")
                    .appendEncodedPath(REQUIRED_TYPE)
                    .appendQueryParameter(KEY_PARAM, Utility.MOVIEAPI_KEY)
                    .build();

            return builtUri;
        }

        private String getJsonStr(Uri uri) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            //This string will hold the json response as a string
            String jsonStr = "";

            try {
                URL url = new URL(uri.toString());
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

                jsonStr += buffer.toString();
                //Log.v(LOG_TAG, "Movie string: " + movieJsonStr);

            } catch (IOException e) {
                //Log.e(LOG_TAG, "Error", e);
                //If the code didn't successfully get weather data, no need to parse it
                jsonStr = null;
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
            return jsonStr;
        }

        private ArrayList<Trailer> getTrailersDataFromJson(String trailerJsonStr)
                throws JSONException {

            if (trailerJsonStr == null)
                return null;

            final String MDB_RESULTS = mContext.getString(R.string.trailer_results);
            final String MDB_NAME = mContext.getString(R.string.trailer_name);
            final String MDB_KEY = mContext.getString(R.string.trailer_key);
            final String TRAILER_LINK = mContext.getString(R.string.trailer_link);

            JSONObject trailerJson = new JSONObject(trailerJsonStr);
            JSONArray trailerArray = trailerJson.getJSONArray(MDB_RESULTS);

            int totalNumOfTrailers = trailerArray.length();
            ArrayList<Trailer> trailerList = new ArrayList<>();
            String YOUTUBE_BASE_URL = "https://www.youtube.com/";

            String TRAILER_ACTION = "watch";
            String TRAILER_PARAM = "v";

            for (int i = 0; i < totalNumOfTrailers; i++) {
                // Get the JSON object representing the movie data
                JSONObject movieData = trailerArray.getJSONObject(i);

                String name = movieData.getString((MDB_NAME));
                String key = movieData.getString(MDB_KEY);

                Uri trailerUri;
                trailerUri = Uri.parse(YOUTUBE_BASE_URL).buildUpon()
                        .appendEncodedPath(TRAILER_ACTION)
                        .appendQueryParameter(TRAILER_PARAM, key)
                        .build();

                trailerList.add(new Trailer(name, key, trailerUri.toString()));
            }

            return trailerList;
        }


        @Override
        protected ArrayList<Trailer> doInBackground(String... params) {
            ArrayList<Trailer> trailerList;
            String movieId = params[0];

            Uri trailerUri = buildTrailerUri(movieId);
            String trailerJsonStr = getJsonStr(trailerUri);

            try {
                trailerList = getTrailersDataFromJson(trailerJsonStr);
                return trailerList;
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Trailer> trailerList) {
            if (trailerList.size() != 0 && mTrailerAdapter != null) {
                mTrailerAdapter.clear();
                mCurrentMovieTrailerData = trailerList;

                // Set the trailer adapter
                for (Trailer trailer : mCurrentMovieTrailerData) {
                    mTrailerAdapter.add(trailer.getName());
                }
            }

            else
                showNoTrailersText();

            if (mShareActionProvider != null) {
                if (mCurrentMovieTrailerData != null)
                    mShareActionProvider.setShareIntent(createShareTrailerIntent());
                else
                    mShareActionProvider.setShareIntent(createShareNameIntent());
            }

            mLoadingTrailersFinished = true;
        }
    }

    /**
     * Created by Toty on 9/28/2015.
     */
    public class FetchReviewsTask extends AsyncTask<String, Void, ArrayList<Review>> {
        private final String LOG_TAG = FetchReviewsTask.class.getName();
        private final Context mContext;

        public FetchReviewsTask(Context context) {
            mContext = context;
        }

        private Uri buildRevIewUri(String movieId) {
            final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/";
            final String KEY_PARAM = "api_key";
            final String REQUIRED_TYPE = "reviews";

            Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                    .appendPath(movieId + "/")
                    .appendEncodedPath(REQUIRED_TYPE)
                    .appendQueryParameter(KEY_PARAM, Utility.MOVIEAPI_KEY)
                    .build();

            return builtUri;
        }

        private String getJsonStr(Uri uri) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            //This string will hold the json response as a string
            String jsonStr = "";

            try {
                URL url = new URL(uri.toString());
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

                jsonStr += buffer.toString();
                //Log.v(LOG_TAG, "Movie string: " + movieJsonStr);

            } catch (IOException e) {
                //Log.e(LOG_TAG, "Error", e);
                //If the code didn't successfully get weather data, no need to parse it
                jsonStr = null;
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
            return jsonStr;
        }

        private ArrayList<Review> getReviewsDataFromJson(String reviewJsonStr)
                throws JSONException {

            if (reviewJsonStr == null)
                return null;

            final String MDB_RESULTS = mContext.getString(R.string.review_results);
            final String MDB_AUTHOR = mContext.getString(R.string.review_author);
            final String MDB_CONTENT = mContext.getString(R.string.review_content);

            JSONObject reviewJson = new JSONObject(reviewJsonStr);
            JSONArray reviewArray = reviewJson.getJSONArray(MDB_RESULTS);

            int totalNumOfReviews = reviewArray.length();
            ArrayList<Review> reviewList = new ArrayList<>();

            for (int i = 0; i < totalNumOfReviews; i++) {
                // Get the JSON object representing the movie data
                JSONObject reviewData = reviewArray.getJSONObject(i);
                reviewList.add(new Review(reviewData.getString(MDB_AUTHOR), reviewData.getString(MDB_CONTENT)));
            }

            return reviewList;
        }

        @Override
        protected ArrayList<Review> doInBackground(String... params) {
            ArrayList<Review> reviewList;
            String movieId = params[0];

            Uri reviewUri = buildRevIewUri(movieId);
            String reviewJsonStr = getJsonStr(reviewUri);

            try {
                reviewList = getReviewsDataFromJson(reviewJsonStr);
                return reviewList;
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Review> reviewList) {
            if (reviewList.size() != 0 && mReviewAdapter != null) {
                mReviewAdapter.clear();
                mCurrentMovieReviewData = reviewList;

                for (Review review : reviewList) {
                    mReviewAdapter.add(review);
                }

                hideNoReviewsText();
            }

            mLoadingReviewsFinished = true;
        }
    }


    /**
     * Created by Toty on 9/25/2015.
     * <p>
     * This class is used when we want to retrieve the movie data from the database
     * This happens when the user chooses the sortOrder as favorites
     */
    // This will be used when the favorite button is pressed to store the current movie data
    // in the database
    public class FetchMovieDatabase extends AsyncTask<Object, Void, Void> {
        private final Context mContext;

        public FetchMovieDatabase(Context context) {
            mContext = context;
        }

        @Override
        protected Void doInBackground(Object... params) {
            try {
                while (true) {
                    if (mLoadingTrailersFinished && mLoadingReviewsFinished)
                        break;

                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long id = updateFavoriteTable((HashMap) params[0]);
            updateTrailerTable((ArrayList<Trailer>) params[1], id);
            updateReviewTable((ArrayList<Review>) params[2], id);

            return null;
        }

        // These methods will be used to add data to corresponding table if it doesn't already exist
        // Or delete it if it exists when the favorite button is pressed
        // We collect all realted trailers or reviews and bulk insert them
        private long updateFavoriteTable(HashMap<String, String> favoriteMap) {
            String movieId = favoriteMap.get(mContext.getString(R.string.movie_id));
            long insertedId = -1;

            Uri uri = MovieContract.FavoriteEntry.CONTENT_URI;
            String selection = MovieContract.FavoriteEntry.COLUMN_MOVIE_ID + " = ?";
            String[] selectionArgs = {movieId};

            Cursor cursor = mContext.getContentResolver().query(
                    uri,
                    new String[]{MovieContract.FavoriteEntry._ID},
                    selection,
                    selectionArgs,
                    null);

            if (cursor.moveToFirst()) {
                mContext.getContentResolver().delete(uri, selection, selectionArgs);
                return insertedId;
            } else {
                ContentValues favoriteValues = new ContentValues();

                String title = favoriteMap.get(mContext.getString((R.string.movie_title)));
                String genres = favoriteMap.get(mContext.getString(R.string.movie_genres));
                String poster = favoriteMap.get(mContext.getString((R.string.movie_poster_url)));
                String background = favoriteMap.get(mContext.getString((R.string.movie_background_url)));
                String releaseDate = favoriteMap.get(mContext.getString((R.string.movie_release_date)));
                String voteAvg = favoriteMap.get(mContext.getString((R.string.movie_vote_average)));
                String overview = favoriteMap.get(mContext.getString((R.string.movie_overview)));

                favoriteValues.put(MovieContract.FavoriteEntry.COLUMN_MOVIE_ID, movieId);
                favoriteValues.put(MovieContract.FavoriteEntry.COLUMN_ORIGINAL_TITLE, title);
                favoriteValues.put(MovieContract.FavoriteEntry.COLUMN_GENRES, genres);
                favoriteValues.put(MovieContract.FavoriteEntry.COLUMN_MOVIE_POSTER, poster);
                favoriteValues.put(MovieContract.FavoriteEntry.COLUMN_BACKDROP_IMG, background);
                favoriteValues.put(MovieContract.FavoriteEntry.COLUMN_RELEASE_DATE, releaseDate);
                favoriteValues.put(MovieContract.FavoriteEntry.COLUMN_VOTE_AVG, voteAvg);
                favoriteValues.put(MovieContract.FavoriteEntry.COLUMN_OVERVIEW, overview);

                // Finally, insert the data
                Uri insertedUri = mContext.getContentResolver().insert(
                        uri,
                        favoriteValues
                );

                insertedId = ContentUris.parseId(insertedUri);
            }

            return insertedId;
        }

        private void updateTrailerTable(ArrayList<Trailer> trailerList, long favoriteId) {
            if (trailerList == null || trailerList.size() == 0)
                return;

            Uri trailerUri = MovieContract.TrailerEntry.CONTENT_URI;
            String selection = MovieContract.TrailerEntry.COLUMN_FAVORITE_RECORD_ID + " = ?";
            String[] selectionArgs = {Long.toString(favoriteId)};

            Cursor trailerCursor = mContext.getContentResolver().query(
                    trailerUri,
                    new String[]{MovieContract.TrailerEntry._ID},
                    selection,
                    selectionArgs,
                    null);

            if (trailerCursor.moveToFirst()) {
                // That means the movie was a favorite before, now it's not so delete its data
                mContext.getContentResolver().delete(trailerUri, selection, selectionArgs);
            } else {
                Vector<ContentValues> cVVector = new Vector<ContentValues>(trailerList.size());

                for (int i = 0; i < trailerList.size(); i++) {
                    ContentValues trailerValues = new ContentValues();
                    String trailerName = trailerList.get(i).getName();
                    String trailerKey = trailerList.get(i).getKey();

                    trailerValues.put(MovieContract.TrailerEntry.COLUMN_TRAILER_NAME, trailerName);
                    trailerValues.put(MovieContract.TrailerEntry.COLUMN_TRAILER_KEY, trailerKey);
                    trailerValues.put(MovieContract.TrailerEntry.COLUMN_FAVORITE_RECORD_ID, favoriteId);

                    cVVector.add(trailerValues);
                }

                // Finally, bulk insert the data
                if (cVVector.size() > 0) {
                    ContentValues[] cVArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cVArray);
                    int insertionsCount = mContext.getContentResolver().bulkInsert(
                            trailerUri,
                            cVArray
                    );
                }
            }

            trailerCursor.close();
        }

        private void updateReviewTable(ArrayList<Review> reviewList, long favoriteId) {
            Uri reviewUri = MovieContract.ReviewEntry.CONTENT_URI;
            String selection = MovieContract.ReviewEntry.COLUMN_FAVORITE_RECORD_ID + " = ?";
            String[] selectionArgs = {Long.toString(favoriteId)};

            Cursor reviewCursor = mContext.getContentResolver().query(
                    reviewUri,
                    new String[]{MovieContract.ReviewEntry._ID},
                    selection,
                    selectionArgs,
                    null);

            if (reviewCursor.moveToFirst()) {
                mContext.getContentResolver().delete(reviewUri, selection, selectionArgs);
            } else {
                Vector<ContentValues> cVVector = new Vector<>();


                for (int i = 0; i < cVVector.size(); i++) {
                    ContentValues reviewValues = new ContentValues();
                    String reviewAuthor = reviewList.get(i).getAuthor();
                    String reviewContent = reviewList.get(i).getContent();

                    reviewValues.put(MovieContract.ReviewEntry.COLUMN_AUTHOR_NAME, reviewAuthor);
                    reviewValues.put(MovieContract.ReviewEntry.COLUMN_REVIEW_CONTENT, reviewContent);
                    reviewValues.put(MovieContract.ReviewEntry.COLUMN_FAVORITE_RECORD_ID, favoriteId);

                    cVVector.add(reviewValues);
                }

                // Finally, bulk insert the data
                if (cVVector.size() > 0) {
                    ContentValues[] cVArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cVArray);
                    int insertionsCount = mContext.getContentResolver().bulkInsert(
                            reviewUri,
                            cVArray
                    );
                }
            }

            reviewCursor.close();
        }
    }


    // This will set the views with the returned data eaither from database or the internet
    private void setDetailFragmentViews(View rootView, HashMap<String, String> movieData) {
        String bgUrl = movieData.get(getString(R.string.movie_background_url));
        String posterUrl = movieData.get(getString(R.string.movie_poster_url));

        //Setting views in first layout that has a TextView and an ImageView
        ImageView bgImageView = ((ImageView) rootView.findViewById(R.id.detail_background));
        Picasso.with(getActivity()).load(bgUrl).into(bgImageView);

        ((RatingBar) rootView.findViewById(R.id.detail_movie_ratingBar))
                .setRating(Float.valueOf(movieData.get(getString(R.string.movie_vote_average))) / 2);

        ((TextView) rootView.findViewById(R.id.detail_main_title))
                .setText(movieData.get(getString(R.string.movie_title)));

        //Setting views in middle layout that has 3 TextViews and an ImageView
        ImageView posterImageView = ((ImageView) rootView.findViewById(R.id.detail_poster));
        Picasso.with(getActivity()).load(posterUrl).into(posterImageView);

        ((TextView) rootView.findViewById(R.id.detail_genres_input))
                .setText(movieData.get(getString(R.string.movie_genres)));

        ((TextView) rootView.findViewById(R.id.detail_release_date_input))
                .setText(movieData.get(getString(R.string.movie_release_date)));

        ((TextView) rootView.findViewById(R.id.detail_rating_input))
                .setText(movieData.get(getString(R.string.movie_vote_average)));

        //Setting the overview in the last layout
        ((TextView) rootView.findViewById(R.id.detail_overview_input))
                .setText(movieData.get(getString(R.string.movie_overview)));
    }

    // This initializes the trailer and reviews listviews and assigns onClick listners to them
    private void initTrailerReviewViews() {
        mTrailerAdapter = new ArrayAdapter<String>(
                getActivity(), // The current context (this activity)
                R.layout.list_item_trailer, // The name of the layout ID.
                R.id.list_item_trailer_textview, // The ID of the textview to populate.
                new ArrayList<String>());

        ListView trailerListView = (ListView) mRootView.findViewById(R.id.listview_trailers);
        trailerListView.setAdapter(mTrailerAdapter);
        trailerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position,
                                    long id) {
                String link = mCurrentMovieTrailerData.get(position).getLink();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                startActivity(intent);
            }
        });

        mReviewAdapter = new ReviewAdapter(getActivity(),
                R.layout.list_item_review, new ArrayList<Review>());

        ListView reviewListView = (ListView) mRootView.findViewById(R.id.listview_reviews);
        reviewListView.setAdapter(mReviewAdapter);
        reviewListView.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });
        //ListView.LayoutParams params = (ListView.LayoutParams) reviewListView.getLayoutParams();
        //params.height = (int) Utility.pixelsToDp(getActivity(), 150);
    }

    // This method is responsible for changing the color of the button when being pressed
    // depending whether the movie is in the database or not
    private void updateFavButton(View v, boolean buttonStatus) {
        ImageButton favButton = (ImageButton) v.findViewById(R.id.detail_button_favorite);
        if (buttonStatus == true) {
            favButton.setImageResource(R.drawable.favourites_on);
            mFavButtomMsg = "Added to favorites";
        }
        else {
            favButton.setImageResource(R.drawable.favourites_off);
            mFavButtomMsg = "Removed from favorites";
        }

    }

    // Those functions will be used in the main fragment to help update
    // favorites if the favorite button was clicked
    public static boolean isFavButtonClicked() {
        return mFavoriteButtonClicked;
    }
    public static void resetFavButtonClick() {
        mFavoriteButtonClicked = false;
    }

    // If trailers or reviews contains data, we should hide the noTrailers or noReviews texts
    private void showNoTrailersText() {
        mRootView.findViewById(R.id.detail_noTrailers_text).setVisibility(View.VISIBLE);
    }
    private void hideNoReviewsText() {
        mRootView.findViewById(R.id.detail_noReviews_text).setVisibility(View.INVISIBLE);
    }

}
