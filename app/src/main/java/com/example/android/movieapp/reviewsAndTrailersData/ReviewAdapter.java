package com.example.android.movieapp.reviewsAndTrailersData;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.android.movieapp.R;

import java.util.ArrayList;

/**
 * Created by Toty on 9/28/2015.
 */
public class ReviewAdapter extends ArrayAdapter<Review> {
    private final Context context;
    private final ArrayList<Review> data;
    private final int layoutResourceId;

    public ReviewAdapter(Context context, int layoutResourceId, ArrayList<Review> data) {
        super(context, layoutResourceId, data);
        this.context = context;
        this.data = data;
        this.layoutResourceId = layoutResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder = null;

        if(v == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            v = inflater.inflate(layoutResourceId, parent, false);

            holder = new ViewHolder();
            holder.author_textView = (TextView)v.findViewById(R.id.list_item_reviewAuthor_textview);
            holder.content_textView = (TextView)v.findViewById(R.id.list_item_reviewContent_textview);

            v.setTag(holder);
        }
        else
        {
            holder = (ViewHolder)v.getTag();
        }

        Review review = data.get(position);

        holder.author_textView.setText(review.getAuthor());
        holder.content_textView.setText(review.getContent());

        return v;
    }

    static class ViewHolder
    {
        TextView author_textView;
        TextView content_textView;
    }
}
