package com.example.android.movieapp;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.example.android.movieapp.database.MovieContract;
import com.example.android.movieapp.movieData.FetchMovieTask;
import com.example.android.movieapp.movieData.MovieImageAdapter;
import com.example.android.movieapp.movieData.MyMovie;

import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class MovieFragment extends Fragment {

    private static final String SI_MOVIE_KEY = "SI_MOVIE_KEY";    //saved instance movie key
    private static final String SI_POS_KEY   = "SI_POS_KEY";
    private static final String SI_SORT_KEY  = "SI_SORT_KEY";
    public static  final String MOVIE        = "MOVIE";

    private static final String NO_CONNECTION = "No connection found";
    private static final String NO_FAVORITES = "No favorites found";

    private static final int START_PAGE = 1;

    private int mPosition = GridView.INVALID_POSITION;
    private GridView mGridView;
    private Button mRetryButton;
    private TextView mText;
    private Context mContext;
    private InfiniteScrollListener mScrollListener;

    private MovieImageAdapter mMovieAdapter;
    private ArrayList<MyMovie> mListOfMovies;

    private String mPrevSortType;       //Check if the sort type has changed so as to update
    private String mCurrentSortType;

    public MovieFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SI_SORT_KEY)) {
                mPrevSortType = savedInstanceState.getString(SI_SORT_KEY);
            }

            if (savedInstanceState.containsKey(SI_MOVIE_KEY)) {
                mListOfMovies = savedInstanceState.getParcelableArrayList(SI_MOVIE_KEY);
            }

            if (savedInstanceState.containsKey(SI_POS_KEY)) {
                mPosition = savedInstanceState.getInt(SI_POS_KEY);
            }
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mMovieAdapter.getCount() != 0) {
            mListOfMovies = mMovieAdapter.getAllItems();
        }

        if (mListOfMovies != null) {
            outState.putParcelableArrayList(SI_MOVIE_KEY,
                    (ArrayList<? extends Parcelable>) mListOfMovies);
        }

        if (mPosition != GridView.INVALID_POSITION) {
            outState.putInt(SI_POS_KEY, mPosition);
        }

        outState.putString(SI_SORT_KEY, mCurrentSortType);
    }

    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //inflater.inflate(R.menu.movie_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mGridView = (GridView)view.findViewById(R.id.main_gridView);
        mRetryButton = (Button) view.findViewById(R.id.main_retry_button);
        mText = (TextView) view.findViewById(R.id.main_empty_text);
        mContext = getActivity();

        mMovieAdapter = new MovieImageAdapter(mContext, mGridView);
        mGridView.setAdapter(mMovieAdapter);

        setGridViewColumnsNum();

        //Setting a listener for the grid view when clicking on any item
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View v, int position,
                                    long id) {

                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                MyMovie movie = (MyMovie) mMovieAdapter.getItem(position);
                if (movie != null) {
                    ((Callback) mContext).onItemSelected(movie);
                }

                mPosition = position;
            }
        });

        /*mGridView.setOnScrollListener(new InfiniteScrollListener(5) {
            @Override
            public void loadMore(int page, int totalItemsCount) {
                if (isSortChanged())
                    this.reset();

                if (!mCurrentSortType.equals(getString(R.string.favorite_table)))
                    addNewMovies(page);
            }
        });*/

        // Setting a listener for the retry button when there is no internet connection
        mRetryButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (Utility.isNetworkAvailable(mContext)) {
                    updateMainFragment();
                }
            }
        });

        return view;
    }

    public interface Callback {
        /**
         * MovieFragmentCallback for when a movie has been selected.
         */
        public void onItemSelected(MyMovie movie);
    }

    void onSortChanged( ) {
        mPrevSortType = mCurrentSortType;
        mCurrentSortType = Utility.getPreferredSort(mContext);
        updateMainFragment();

        mGridView.setOnScrollListener(new InfiniteScrollListener(5) {
            @Override
            public void loadMore(int page, int totalItemsCount) {
                if (!mCurrentSortType.equals(getString(R.string.favorite_table)))
                    addNewMovies(page);
            }
        });
    }

    //This will update the gridView with the new data
    private void updateMainFragment() {

        mCurrentSortType = Utility.getPreferredSort(mContext);

        if (mCurrentSortType.equals(getString(R.string.favorite_table))) {
            if (isFavoritesEmpty()) {
                visualizeFavorites(true);
                return;
            }

            else if (isSortChanged() || DetailFragment.isFavButtonClicked()) {
                mMovieAdapter.clearAll();
                addNewMovies(START_PAGE);
                DetailFragment.resetFavButtonClick();
            }
            else
                addExistedMovies();
        }

        else if (Utility.isNetworkAvailable(mContext)) {
            if (isSortChanged()) {
                mMovieAdapter.clearAll();
                addNewMovies(START_PAGE);
            }
            else
                addExistedMovies();
        }

        else {
            if (isSortChanged()) {
                visualizeConnection(false);
                mCurrentSortType = "dummy";
                return;
            }

            else
                addExistedMovies();
        }

        visualizeConnection(true);
    }

    private void addExistedMovies() {
        mMovieAdapter.addAll(mListOfMovies);
        if (mPosition != GridView.INVALID_POSITION) {
            mGridView.setSelection(mPosition);
        }
    }
    private void addNewMovies(int page) {
        new FetchMovieTask(mContext, mMovieAdapter).execute(mCurrentSortType, String.valueOf(page));
    }
    private boolean isFavoritesEmpty() {
        Cursor cursor = mContext.getContentResolver().query(
                MovieContract.FavoriteEntry.CONTENT_URI,
                new String[] {MovieContract.FavoriteEntry._ID},
                null,
                null,
                null);

        return cursor.getCount() == 0;
    }
    private boolean isSortChanged() {
        if (mCurrentSortType != null)
            return !mCurrentSortType.equals(mPrevSortType);

        return false;
    }

    //This method is used to show or hide the main page views depedning on connection or favorites
    private void visualizeFavorites(boolean favoritesEmpty) {
        if (!favoritesEmpty) {
            mText.setVisibility(View.INVISIBLE);
            mGridView.setVisibility(View.VISIBLE);
        }
        else {
            mGridView.setVisibility(View.INVISIBLE);
            mText.setText(NO_FAVORITES);
            mText.setVisibility(View.VISIBLE);
        }
    }
    private void visualizeConnection(boolean isConnected) {
        if (isConnected) {
            mText.setVisibility(View.INVISIBLE);
            mRetryButton.setVisibility(View.INVISIBLE);
            mGridView.setVisibility(View.VISIBLE);
        }
        else {
            mGridView.setVisibility(View.INVISIBLE);
            mText.setText(NO_CONNECTION);
            mText.setVisibility(View.VISIBLE);
            mRetryButton.setVisibility(View.VISIBLE);
        }
    }

    private void setGridViewColumnsNum() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int f = metrics.densityDpi;
        int width = metrics.widthPixels;
        int dpWidth = width * 160 / f;

        int columnsNum;
        boolean landScape = mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        if (dpWidth <= 640) {
            if(landScape)
                columnsNum = 4;
            else
                columnsNum = 2;
        }

        else if (dpWidth <= 640){
            if(landScape)
                columnsNum = 3;
            else
                columnsNum = 2;
        }

        else
            columnsNum = 3;

        mGridView.setNumColumns(columnsNum);
    }
}