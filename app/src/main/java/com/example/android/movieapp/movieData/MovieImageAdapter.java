package com.example.android.movieapp.movieData;

import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.android.movieapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Toty on 8/16/2015.
 */
public class MovieImageAdapter extends BaseAdapter {
    private Context mContext;
    ArrayList<MyMovie> mMovieList;
    private LayoutInflater mInflater;

    public MovieImageAdapter(Context c, GridView gView) {
        mContext = c;
        mMovieList = new ArrayList<>();
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mMovieList.size();
    }

    @Override
    public Object getItem(int position) {
        return mMovieList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public ArrayList<MyMovie> getAllItems() {
        return mMovieList;
    }

    public void clearAll() {
        mMovieList.clear();
        this.notifyDataSetChanged();
    }

    public void addAll(ArrayList<MyMovie> data) {
        mMovieList.addAll(data);
        this.notifyDataSetChanged();
    }

    // create a new ImageView for each item referenced by the Adapter
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        View v = view;
        ImageView thumbnail;
        TextView name;
        RatingBar rating;

        if (v == null) {
            v = mInflater.inflate(R.layout.grid_item, parent, false);
            v.setTag(R.id.grid_thumbnail, v.findViewById(R.id.grid_thumbnail));
            v.setTag(R.id.grid_item_text_title, v.findViewById(R.id.grid_item_text_title));
            v.setTag(R.id.grid_item_ratingBar, v.findViewById(R.id.grid_item_ratingBar));
        }

        thumbnail = (ImageView) v.getTag(R.id.grid_thumbnail);
        name = (TextView) v.getTag(R.id.grid_item_text_title);
        rating = (RatingBar) v.getTag(R.id.grid_item_ratingBar);

        HashMap<String, String> movieDetails = mMovieList.get(position).getMovieDetails();

        // Assigning the movie poster to the thumbnail
        String posterUrl = movieDetails.get(mContext.getString(R.string.movie_poster_url));
        if (posterUrl != null)
            Picasso.with(mContext).load(posterUrl).into(thumbnail);
        else
            Picasso.with(mContext).load(R.drawable.no_photo_available).into(thumbnail);

        // Assigning moving text from left to right and going back
        name.setText(movieDetails.get(mContext.getString(R.string.movie_title)));
        name.setMovementMethod(new ScrollingMovementMethod());

        // Assigning rating bar to the average number returned
        rating.setRating(Float.valueOf(movieDetails.get(mContext.getString(R.string.movie_vote_average))) / 2);

        return v;
    }
}
